package dev.paprikar.defaultdiscordbot.core.media.sending;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordMediaRequest;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordMediaRequestService;
import dev.paprikar.defaultdiscordbot.utils.JdaUtils.RequestErrorHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.*;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class MediaRequestSender {

    private static final Logger logger = LoggerFactory.getLogger(MediaRequestSender.class);

    private final JDA jda;

    private final DiscordCategoryService categoryService;

    private final DiscordMediaRequestService mediaRequestService;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final Object lock = new Object();

    private final RequestErrorHandler requestSendingErrorHandler;

    private volatile boolean toTerminate;

    private volatile ScheduledFuture<?> taskFuture;

    private volatile DiscordCategory category;

    private volatile LocalDateTime lastSendDateTime;

    public MediaRequestSender(@Nonnull JDA jda,
                              @Nonnull DiscordCategoryService categoryService,
                              @Nonnull DiscordMediaRequestService mediaRequestService) {
        this.jda = jda;
        this.categoryService = categoryService;
        this.mediaRequestService = mediaRequestService;

        // exception can usually occur when the connection is lost or the channel is unavailable,
        // which in both cases causes the sender to stop working externally via events.
        this.requestSendingErrorHandler = RequestErrorHandler.createBuilder()
                .setMessage("An error occurred while sending the suggestion")
                .setAction(() -> {
                    toTerminate = true;
                    // revert lastSendDateTime changes
                    lastSendDateTime = LocalDateTime.from(category.getLastSendTimestamp().toInstant());
                })
                .warnOn(ErrorResponse.UNKNOWN_CHANNEL)
                .build();
    }

    @Nullable
    public DiscordCategory getCategory() {
        synchronized (lock) {
            return category;
        }
    }

    public void start(@Nonnull DiscordCategory category) {
        synchronized (lock) {
            if (taskFuture != null) {
                String message = "Sender is already started";
                logger.error(message);
                throw new IllegalStateException(message);
            }

            Timestamp dt = category.getLastSendTimestamp();
            if (dt == null) {
                lastSendDateTime = null;
            } else {
                lastSendDateTime = dt.toLocalDateTime();
            }
            toTerminate = false;
            taskFuture = executor.schedule(new SenderTask(category, false), 0, TimeUnit.NANOSECONDS);
            this.category = category;
        }
    }

    public void stop() {
        synchronized (lock) {
            if (taskFuture != null) {
                taskFuture.cancel(false);
                if (!taskFuture.isDone()) {
                    try {
                        taskFuture.get(); // todo fix deadlock with SenderTask#run()
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error(e.toString());
                    }
                }
            }
            toTerminate = true;
            taskFuture = null;
            category = null;
        }
    }

    public void update(@Nonnull DiscordCategory category) {
        stop();
        start(category);
    }

    private class SenderTask implements Runnable {

        final DiscordCategory category;

        final boolean toSend;

        public SenderTask(DiscordCategory category, boolean toSend) {
            this.category = category;
            this.toSend = toSend;
        }

        @Override
        public void run() {
            synchronized (lock) {
                if (toSend) {
                    sendRequest();
                }
                if (toTerminate) {
                    taskFuture = null;
                    MediaRequestSender.this.category = null;
                    return;
                }
                schedule();
            }
        }

        private void sendRequest() {
            Optional<DiscordMediaRequest> requestOptional = mediaRequestService.findFirstByCategoryId(category.getId());
            if (requestOptional.isEmpty()) {
                // normally should not happen
                toTerminate = true;
                lastSendDateTime = LocalDateTime.from(category.getLastSendTimestamp().toInstant());
                logger.error("SenderTask#sendRequest(): No requests were found to be sent");
                return;
            }
            DiscordMediaRequest request = requestOptional.get();

            Long sendingChannelId = category.getSendingChannelId();
            TextChannel channel = jda.getTextChannelById(sendingChannelId);
            if (channel == null) {
                // can happen if the service has not stopped yet after the channel deletion event
                toTerminate = true;
                logger.warn("SenderTask#sendRequest(): The channel for sending requests cannot be found");
                return;
            }

            Consumer<Message> onSendSuccess = message -> {
                logger.debug("SenderTask#sendRequest(): The request with id={} was successfully sent"
                        + " to text channel with id={}", request.getId(), sendingChannelId);

                mediaRequestService.delete(request);
                // save lastSendDateTime changes
                category.setLastSendTimestamp(
                        Timestamp.from(lastSendDateTime.atZone(ZoneId.systemDefault()).toInstant()));
                MediaRequestSender.this.category = categoryService.save(category);
            };

            channel.sendMessage(request.getContent())
                    .queue(onSendSuccess, requestSendingErrorHandler);
        }

        private void schedule() {
            long queueSize = mediaRequestService.countByCategoryId(category.getId());
            if (queueSize == 0) {
                // source always calls refresh() when a new request is added to the queue,
                // so there is no need to wait for the addition event from here
                taskFuture = null;
                MediaRequestSender.this.category = null;
                return;
            }

            LocalTime startTime = category.getStartTime().toLocalTime();
            LocalTime endTime = category.getEndTime().toLocalTime();
            int reserveDays = category.getReserveDays();

            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime startDateTime = overrideTime(currentDateTime, startTime);
            LocalDateTime endDateTime = overrideTime(currentDateTime, endTime);
            boolean inTime = timeInRange(currentDateTime.toLocalTime(), startTime, endTime);

            StringBuilder debugMsgBuilder = new StringBuilder();
            debugMsgBuilder.append("SenderTask#schedule(): ")
                    .append("category={id=").append(category.getId()).append("}, ")
                    .append("currentDateTime=").append(currentDateTime);

            if (inTime) {
                // when it starts yesterday
                if (currentDateTime.isBefore(startDateTime)) {
                    startDateTime = startDateTime.minusDays(1);
                }
                // when it ends tomorrow
                if (!(startDateTime.isBefore(endDateTime))) {
                    endDateTime = endDateTime.plusDays(1);
                }
            } else { // wait for the next day
                // when it starts tomorrow
                if (currentDateTime.isAfter(startDateTime)) {
                    startDateTime = startDateTime.plusDays(1);
                }
                long cooldown = deltaMillis(currentDateTime, startDateTime);
                debugMsgBuilder
                        .append(", target=").append(startDateTime)
                        .append(", cooldown=").append(cooldown)
                        .append(". Wait for the next day");
                logger.debug(debugMsgBuilder.toString());
                taskFuture = executor.schedule(new SenderTask(category, false), cooldown, TimeUnit.MILLISECONDS);
                return;
            }

            if (lastSendDateTime == null) {
                lastSendDateTime = startDateTime;
            } else {
                if (lastSendDateTime.isBefore(startDateTime)) {
                    lastSendDateTime = startDateTime;
                }
            }
            debugMsgBuilder
                    .append(", startDateTime=").append(startDateTime)
                    .append(", endDateTime=").append(endDateTime)
                    .append(", lastSendDateTime=").append(lastSendDateTime);

            long millisPerDay = deltaMillis(startDateTime, endDateTime);
            long millisTotal = millisPerDay * reserveDays;
            long millisLeft = deltaMillis(lastSendDateTime, endDateTime);
            // in millis
            float periodTotal = (float) millisTotal / (queueSize + reserveDays);
            // at least 1 picture per day
            periodTotal = Math.min(periodTotal, millisPerDay / 2F);
            float periods = Math.max(millisLeft / periodTotal, 1F);
            // in millis
            float period = millisLeft / periods;

            debugMsgBuilder
                    .append(", queueSize=").append(queueSize)
                    .append(", millisPerDay=").append(millisPerDay)
                    .append(", millisTotal=").append(millisTotal)
                    .append(", millisLeft=").append(millisLeft)
                    .append(", periodTotal=").append(periodTotal)
                    .append(", periods=").append(periods)
                    .append(", period=").append(period);

            Duration periodDuration = Duration.ofNanos((long) (period * 1000_000));

            // send immediately if it is late
            if (currentDateTime.isAfter(lastSendDateTime.plus(periodDuration))) {
                lastSendDateTime = currentDateTime;
                debugMsgBuilder.append(". Send immediately if it is late");
                logger.debug(debugMsgBuilder.toString());
                taskFuture = executor.schedule(new SenderTask(category, true), 0, TimeUnit.NANOSECONDS);
                return;
            }

            // skipping the end point of the day
            if (currentDateTime.isAfter(endDateTime.minus(periodDuration))) {
                // wait for the next day
                LocalDateTime target = startDateTime.plusDays(1);
                long cooldown = deltaMillis(currentDateTime, target);
                debugMsgBuilder
                        .append(", target=").append(target)
                        .append(", cooldown=").append(cooldown)
                        .append(". Skipping the end point of the day");
                logger.debug(debugMsgBuilder.toString());
                taskFuture = executor.schedule(new SenderTask(category, false), cooldown, TimeUnit.MILLISECONDS);
                return;
            }

            // skipping the start point of the day
            if (lastSendDateTime.isEqual(startDateTime)) {
                LocalDateTime target = startDateTime.plus(periodDuration);
                lastSendDateTime = target;
                long cooldown = deltaMillis(currentDateTime, target);
                debugMsgBuilder
                        .append(", target=").append(target)
                        .append(", cooldown=").append(cooldown)
                        .append(". Skipping the start point of the day");
                logger.debug(debugMsgBuilder.toString());
                taskFuture = executor.schedule(new SenderTask(category, true), cooldown, TimeUnit.MILLISECONDS);
                return;
            }

            // send at the next point
            LocalDateTime target = lastSendDateTime.plus(periodDuration);
            lastSendDateTime = target;
            long cooldown = deltaMillis(currentDateTime, target);
            debugMsgBuilder
                    .append(", target=").append(target)
                    .append(", cooldown=").append(cooldown)
                    .append(". Send at the next point");
            logger.debug(debugMsgBuilder.toString());
            taskFuture = executor.schedule(new SenderTask(category, true), cooldown, TimeUnit.MILLISECONDS);
        }

        private long deltaMillis(LocalDateTime from, LocalDateTime to) {
            return to.toInstant(ZoneOffset.UTC).toEpochMilli() - from.toInstant(ZoneOffset.UTC).toEpochMilli();
        }

        private LocalDateTime overrideTime(LocalDateTime date, LocalTime time) {
            return LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(),
                    time.getHour(), time.getMinute(), time.getSecond()
            );
        }

        private boolean timeInRange(LocalTime currentTime, LocalTime startTime, LocalTime endTime) {
            if (startTime.isBefore(endTime)) {
                return (!(startTime.isAfter(currentTime)) && currentTime.isBefore(endTime));
            }
            if (startTime.isAfter(endTime)) {
                return (!(startTime.isAfter(currentTime)) || currentTime.isBefore(endTime));
            }
            return true;
        }
    }
}
