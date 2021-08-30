package dev.paprikar.defaultdiscordbot.config;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;


@Validated
public class PicsCategory {

    @NestedConfigurationProperty
    private SendModule sendModule;

    @NestedConfigurationProperty
    private SuggestionModule suggestionModule;

    public SendModule getSendModule() {
        return sendModule;
    }

    public void setSendModule(SendModule sendModule) {
        this.sendModule = sendModule;
    }

    public SuggestionModule getSuggestionModule() {
        return suggestionModule;
    }

    public void setSuggestionModule(SuggestionModule suggestionModule) {
        this.suggestionModule = suggestionModule;
    }

    @Override
    public String toString() {
        return "PicsCategory{" +
                "sendModule=" + sendModule +
                ", suggestionModule=" + suggestionModule +
                '}';
    }
}
