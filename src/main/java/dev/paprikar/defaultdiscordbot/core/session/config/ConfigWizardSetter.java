package dev.paprikar.defaultdiscordbot.core.session.config;

/**
 * The interface for configuration value setters.
 */
public interface ConfigWizardSetter {

    /**
     * @return the name of the configuration variable to set
     */
    String getVariableName();
}
