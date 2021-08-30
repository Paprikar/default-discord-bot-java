package dev.paprikar.defaultdiscordbot.core;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;

import java.io.Serializable;

public interface DiscordProvider extends Serializable {

    Long getId();

    void setId(Long id);

    DiscordCategory getCategory();

    void setCategory(DiscordCategory category);
}
