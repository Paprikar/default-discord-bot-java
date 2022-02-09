package dev.paprikar.defaultdiscordbot.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotBlank;

@Component
@ConfigurationProperties("ddb")
@Validated
public class DdbConfig implements Validator {

    @NotBlank
    private String token;

    @NestedConfigurationProperty
    private DdbDefaults defaults;

    private Integer discordEventPoolSize = 0;

    private Integer discordMaxReconnectDelay = 64;

    private Integer vkMaxReconnectDelay = 64;

    public DdbConfig() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public DdbDefaults getDefaults() {
        return defaults;
    }

    public void setDefaults(DdbDefaults defaults) {
        this.defaults = defaults;
    }

    public Integer getDiscordEventPoolSize() {
        return discordEventPoolSize;
    }

    public void setDiscordEventPoolSize(Integer discordEventPoolSize) {
        this.discordEventPoolSize = discordEventPoolSize;
    }

    public Integer getDiscordMaxReconnectDelay() {
        return discordMaxReconnectDelay;
    }

    public void setDiscordMaxReconnectDelay(Integer maxDiscordReconnectDelay) {
        this.discordMaxReconnectDelay = maxDiscordReconnectDelay;
    }

    public Integer getVkMaxReconnectDelay() {
        return vkMaxReconnectDelay;
    }

    public void setVkMaxReconnectDelay(Integer maxVkReconnectDelay) {
        this.vkMaxReconnectDelay = maxVkReconnectDelay;
    }

    @Override
    public String toString() {
        return "DdbConfig{" +
                "token='" + token + '\'' +
                ", defaults=" + defaults +
                ", discordEventPoolSize=" + discordEventPoolSize +
                ", discordMaxReconnectDelay=" + discordMaxReconnectDelay +
                ", vkMaxReconnectDelay=" + vkMaxReconnectDelay +
                '}';
    }

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return DdbConfig.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        DdbConfig config = (DdbConfig) target;

        config.defaults.validate(errors);

        validateDiscordEventPoolSize(errors);

        validateDiscordMaxReconnectDelay(errors);

        validateVkMaxReconnectDelay(errors);
    }

    private void validateDiscordEventPoolSize(Errors errors) {
        if (discordEventPoolSize < 0) {
            errors.rejectValue(PropertyFieldName.DISCORD_EVENT_POOL_SIZE, "field.invalid",
                    "The value must be greater than or equal to 0");
        }
    }

    private void validateDiscordMaxReconnectDelay(Errors errors) {
        if (discordMaxReconnectDelay < 64) {
            errors.rejectValue(PropertyFieldName.DISCORD_MAX_RECONNECT_DELAY, "field.invalid",
                    "The value must be greater than or equal to 64");
        }
    }

    private void validateVkMaxReconnectDelay(Errors errors) {
        if (vkMaxReconnectDelay < 64) {
            errors.rejectValue(PropertyFieldName.VK_MAX_RECONNECT_DELAY, "field.invalid",
                    "The value must be greater than or equal to 64");
        }
    }

    static class PropertyFieldName {

        public static final String DISCORD_EVENT_POOL_SIZE = "discord-event-pool-size";

        public static final String DISCORD_MAX_RECONNECT_DELAY = "discord-max-reconnect-delay";

        public static final String VK_MAX_RECONNECT_DELAY = "vk-max-reconnect-delay";
    }
}
