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

    @Override
    public String toString() {
        return "DdbConfig{" +
                "token='" + token + '\'' +
                ", defaults=" + defaults +
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
    }
}
