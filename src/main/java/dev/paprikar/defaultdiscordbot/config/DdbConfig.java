package dev.paprikar.defaultdiscordbot.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Component
@ConfigurationProperties("ddb")
@Validated
public class DdbConfig {

    @NotBlank
    private String token;

    private String commandPrefix = "!";

    private int botChannelId;

    @NestedConfigurationProperty
    private Map<String, PicsCategory> picsCategories;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    public int getBotChannelId() {
        return botChannelId;
    }

    public void setBotChannelId(int botChannelId) {
        this.botChannelId = botChannelId;
    }

    public Map<String, PicsCategory> getPicsCategories() {
        return picsCategories;
    }

    public void setPicsCategories(Map<String, PicsCategory> picsCategories) {
        this.picsCategories = picsCategories;
    }

    @Override
    public String toString() {
        return "DdbConfig{" +
                "token='" + token + '\'' +
                ", commandPrefix='" + commandPrefix + '\'' +
                ", botChannelId=" + botChannelId +
                ", picsCategories=" + picsCategories +
                '}';
    }
}
