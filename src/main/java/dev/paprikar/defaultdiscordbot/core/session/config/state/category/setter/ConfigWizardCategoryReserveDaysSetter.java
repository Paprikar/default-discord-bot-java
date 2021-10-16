package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockService;
import dev.paprikar.defaultdiscordbot.core.media.sending.SendingService;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import dev.paprikar.defaultdiscordbot.core.session.config.ConfigWizardSetterResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

@Component
public class ConfigWizardCategoryReserveDaysSetter implements ConfigWizardCategorySetter {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryReserveDaysSetter.class);

    private final DiscordCategoryService categoryService;

    private final SendingService sendingService;

    private final ReadWriteLockService readWriteLockService;

    @Autowired
    public ConfigWizardCategoryReserveDaysSetter(DiscordCategoryService categoryService,
                                                 SendingService sendingService,
                                                 ReadWriteLockService readWriteLockService) {
        this.categoryService = categoryService;
        this.sendingService = sendingService;
        this.readWriteLockService = readWriteLockService;
    }

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value, @Nonnull DiscordCategory category) {
        int reserveDays;
        try {
            reserveDays = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Value has an invalid format")
                    .build()
            );
        }

        if (reserveDays < 1) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("ReserveDays value can only be positive")
                    .build()
            );
        }

        ReadWriteLock lock = readWriteLockService.get(
                ReadWriteLockScope.GUILD_CONFIGURATION, category.getGuild().getId());
        if (lock == null) {
            // todo error response
            return null;
        }

        Lock writeLock = lock.writeLock();
        writeLock.lock();

        category.setReserveDays(reserveDays);
        categoryService.save(category);

        sendingService.updateSender(category);

        writeLock.unlock();

        logger.debug("The category={id={}} reserveDays is set to '{}'", category.getId(), value);

        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("ReserveDays value has been set to `" + value + "`")
                .build()
        );
    }
}
