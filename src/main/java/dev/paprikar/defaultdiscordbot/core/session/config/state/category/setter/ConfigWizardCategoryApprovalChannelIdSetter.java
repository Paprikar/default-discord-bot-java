package dev.paprikar.defaultdiscordbot.core.session.config.state.category.setter;

import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockScope;
import dev.paprikar.defaultdiscordbot.core.concurrency.lock.ReadWriteLockService;
import dev.paprikar.defaultdiscordbot.core.media.approve.ApproveService;
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
public class ConfigWizardCategoryApprovalChannelIdSetter implements ConfigWizardCategorySetter {

    private final Logger logger = LoggerFactory.getLogger(ConfigWizardCategoryApprovalChannelIdSetter.class);

    private final DiscordCategoryService categoryService;

    private final ApproveService approveService;

    private final ReadWriteLockService readWriteLockService;

    @Autowired
    public ConfigWizardCategoryApprovalChannelIdSetter(DiscordCategoryService categoryService,
                                                       ApproveService approveService,
                                                       ReadWriteLockService readWriteLockService) {
        this.categoryService = categoryService;
        this.approveService = approveService;
        this.readWriteLockService = readWriteLockService;
    }

    @Nonnull
    @Override
    public ConfigWizardSetterResponse set(@Nonnull String value, @Nonnull DiscordCategory category) {
        long id;
        try {
            id = Long.parseLong(value);
        } catch (NumberFormatException e) {
            return new ConfigWizardSetterResponse(false, new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Configuration Wizard Error")
                    .setTimestamp(Instant.now())
                    .appendDescription("Value has an invalid format")
                    .build()
            );
        }

        // todo checks

        ReadWriteLock lock = readWriteLockService.get(
                ReadWriteLockScope.GUILD_CONFIGURATION, category.getGuild().getId());
        if (lock == null) {
            // todo error response
            return null;
        }

        Lock writeLock = lock.writeLock();
        writeLock.lock();

        category.setApprovalChannelId(id);
        categoryService.save(category);

        approveService.updateCategory(category);

        writeLock.unlock();

        logger.debug("The category={id={}} approvalChannelId is set to '{}'", category.getId(), value);

        return new ConfigWizardSetterResponse(true, new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Configuration Wizard")
                .setTimestamp(Instant.now())
                .appendDescription("ApprovalChannelId value has been set to `" + value + "`")
                .build()
        );
    }
}
