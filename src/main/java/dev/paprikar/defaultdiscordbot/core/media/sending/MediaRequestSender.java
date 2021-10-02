package dev.paprikar.defaultdiscordbot.core.media.sending;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordMediaRequest;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordMediaRequestService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.*;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.*;

public class MediaRequestSender {

    private final Logger logger = LoggerFactory.getLogger(MediaRequestSender.class);

    private final JDA jda;

    private final DiscordMediaRequestService mediaRequestService;

    private final DiscordCategoryService categoryService;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> taskFeature;

    private LocalDateTime lastSendDateTime;

    public MediaRequestSender(JDA jda,
                              DiscordMediaRequestService mediaRequestService,
                              DiscordCategoryService categoryService) {
        this.jda = jda;
        this.mediaRequestService = mediaRequestService;
        this.categoryService = categoryService;
    }

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    public void start(@Nonnull DiscordCategory category) {
        Date dt = category.getLastSendDateTime();
        if (dt == null) {
            lastSendDateTime = null;
        } else {
            lastSendDateTime = LocalDateTime.from(dt.toInstant());
        }
        executor.execute(new SenderTask(category, false));
    }

    public void stop(boolean block) {
        if (taskFeature == null) {
            return;
        }
        taskFeature.cancel(false);
        if (!taskFeature.isDone() && block) {
            try {
                taskFeature.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.toString());
                throw new RuntimeException(e);
            }
        }
        taskFeature = null;
    }

    public void refresh(@Nonnull DiscordCategory category) {
        stop(true);
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
            if (toSend) {
                sendRequest();
            }
            schedule();
        }

        private void schedule() {
            long queueSize = mediaRequestService.countByCategoryId(category.getId());
            if (queueSize == 0) {
                // provider always calls refresh() when a new request is added to the queue,
                // so there is no need to wait for the addition event from here
                taskFeature = null;
                return;
            }
            LocalTime startTime = LocalTime.from(category.getStartTime().toInstant());
            LocalTime endTime = LocalTime.from(category.getEndTime().toInstant());
            int reserveDays = category.getReserveDays();

            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime startDateTime = overrideTime(currentDateTime, startTime);
            LocalDateTime endDateTime = overrideTime(currentDateTime, endTime);
            boolean inTime = timeInRange(currentDateTime.toLocalTime(), startTime, endTime);

            StringBuilder debugMsgBuilder = new StringBuilder();
            debugMsgBuilder.append("SenderTask.schedule(): ")
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
                taskFeature = executor.schedule(new SenderTask(category, false), cooldown, TimeUnit.MILLISECONDS);
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
                executor.execute(new SenderTask(category, true));
                return;
            }

            // skipping the end point of the day
            if (currentDateTime.isAfter(endDateTime.plus(periodDuration))) {
                // wait for the next day
                LocalDateTime target = startDateTime.plusDays(1);
                long cooldown = deltaMillis(currentDateTime, target);
                debugMsgBuilder
                        .append(", target=").append(target)
                        .append(", cooldown=").append(cooldown)
                        .append(". Skipping the end point of the day");
                logger.debug(debugMsgBuilder.toString());
                taskFeature = executor.schedule(new SenderTask(category, false), cooldown, TimeUnit.MILLISECONDS);
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
                taskFeature = executor.schedule(new SenderTask(category, true), cooldown, TimeUnit.MILLISECONDS);
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
            taskFeature = executor.schedule(new SenderTask(category, true), cooldown, TimeUnit.MILLISECONDS);
        }

        private void sendRequest() {
            Optional<DiscordMediaRequest> requestOptional = mediaRequestService
                    .findFirstByCategoryId(category.getId());
            if (!requestOptional.isPresent()) {
                String message = "No requests were found to be sent";
                logger.error(message);
                throw new RuntimeException(message);
            }
            DiscordMediaRequest request = requestOptional.get();
            TextChannel channel = jda.getTextChannelById(category.getSendingChannelId());
            if (channel == null) {
                String message = "The channel for sending requests cannot be found";
                logger.error(message);
                throw new RuntimeException(message);
            }
            channel.sendMessage(request.getContent()).queue(
                    m -> {
                        // save lastSendDateTime changes
                        category.setLastSendDateTime(
                                Date.from(lastSendDateTime.atZone(ZoneId.systemDefault()).toInstant()));
                        categoryService.save(category);
                    },
                    t -> {
                        // revert changes
                        lastSendDateTime = LocalDateTime.from(category.getLastSendDateTime().toInstant());
                        String message = "Failed to send request";
                        logger.error(message);
                        throw new RuntimeException(message);
                    }
            );
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
