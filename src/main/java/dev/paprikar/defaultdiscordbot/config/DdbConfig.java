package dev.paprikar.defaultdiscordbot.config;


import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;


/**
 * Component for storing the application configuration.
 */
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

    /**
     * @return the token of the discord bot
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token of the discord bot
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return the default values
     */
    public DdbDefaults getDefaults() {
        return defaults;
    }

    /**
     * @param defaults the default values
     */
    public void setDefaults(DdbDefaults defaults) {
        this.defaults = defaults;
    }

    /**
     * @return the event pool size of the discord bot
     */
    public Integer getDiscordEventPoolSize() {
        return discordEventPoolSize;
    }

    /**
     * @param discordEventPoolSize the event pool size of the discord bot
     */
    public void setDiscordEventPoolSize(Integer discordEventPoolSize) {
        this.discordEventPoolSize = discordEventPoolSize;
    }

    /**
     * @return the maximum reconnection delay of the discord bot in seconds
     */
    public Integer getDiscordMaxReconnectDelay() {
        return discordMaxReconnectDelay;
    }

    /**
     * @param maxDiscordReconnectDelay the maximum reconnection delay of the discord bot in seconds
     */
    public void setDiscordMaxReconnectDelay(Integer maxDiscordReconnectDelay) {
        this.discordMaxReconnectDelay = maxDiscordReconnectDelay;
    }

    /**
     * @return the maximum reconnection delay of the vk bot in seconds
     */
    public Integer getVkMaxReconnectDelay() {
        return vkMaxReconnectDelay;
    }

    /**
     * @param maxVkReconnectDelay the maximum reconnection delay of the vk bot in seconds
     */
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

        if (defaults == null) {
            defaults = new DdbDefaults();
        } else {
            config.defaults.validate(errors);
        }

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

    private static class PropertyFieldName {

        public static final String DISCORD_EVENT_POOL_SIZE = "discord-event-pool-size";

        public static final String DISCORD_MAX_RECONNECT_DELAY = "discord-max-reconnect-delay";

        public static final String VK_MAX_RECONNECT_DELAY = "vk-max-reconnect-delay";
    }
}
