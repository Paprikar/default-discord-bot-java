package dev.paprikar.defaultdiscordbot.core.persistence.entity;

import dev.paprikar.defaultdiscordbot.core.DiscordProvider;
import dev.paprikar.defaultdiscordbot.utils.DefaultObjectMapper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "discord_provider_from_discord")
public class DiscordProviderFromDiscord implements DiscordProvider {

    private static final long serialVersionUID = 8165088705252344004L;

    private final transient Logger logger = LoggerFactory.getLogger(DiscordProviderFromDiscord.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "discord_provider_from_discord_id_generator")
    @SequenceGenerator(name = "discord_provider_from_discord_id_generator",
            sequenceName = "discord_provider_from_discord_id_seq",
            allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "discord_category_id",
            foreignKey = @ForeignKey(name = "discord_category_id_fkey"),
            nullable = false)
    private DiscordCategory category;

    @Column(length = 32)
    private String name;

    @Column(name = "suggestion_channel_id")
    private Long suggestionChannelId;

    @Column(nullable = false)
    private Boolean enabled = false;

    // todo vip list

    public DiscordProviderFromDiscord() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public DiscordCategory getCategory() {
        return category;
    }

    @Override
    public void setCategory(DiscordCategory category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSuggestionChannelId() {
        return suggestionChannelId;
    }

    public void setSuggestionChannelId(Long suggestionChannelId) {
        this.suggestionChannelId = suggestionChannelId;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void attach(@Nonnull DiscordCategory category) {
        if (this.category != null) {
            String message = "The provider is already attached to the category";
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
        this.category = category;
    }

    public void detach() {
        if (category == null) {
            String message = "The provider not attached to the category cannot be detached from the category";
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
        category = null;
    }

    @Override
    public String toString() {
        return DefaultObjectMapper.serializeAsString(this);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DiscordProviderFromDiscord category = (DiscordProviderFromDiscord) o;

        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
