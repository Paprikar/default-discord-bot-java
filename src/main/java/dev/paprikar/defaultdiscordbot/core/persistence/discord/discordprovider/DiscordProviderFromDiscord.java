package dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * An entity containing information about the discord provider.
 */
@Entity
@Table(name = "discord_provider_from_discord")
public class DiscordProviderFromDiscord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "discord_provider_from_discord_id_generator")
    @SequenceGenerator(name = "discord_provider_from_discord_id_generator",
            sequenceName = "discord_provider_from_discord_id_seq",
            allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id",
            foreignKey = @ForeignKey(name = "category_id_fkey"),
            nullable = false)
    private DiscordCategory category;

    @Column(length = 32, nullable = false)
    private String name;

    @Column(name = "suggestion_channel_id")
    private Long suggestionChannelId;

    @Column(nullable = false)
    private Boolean enabled = false;

    /**
     * Constructs the entity.
     */
    public DiscordProviderFromDiscord() {
    }

    /**
     * @return the id of the discord provider
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id of the discord provider
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the category to which this discord provider is attached
     */
    public DiscordCategory getCategory() {
        return category;
    }

    /**
     * @param category the category to which this discord provider is attached
     */
    public void setCategory(DiscordCategory category) {
        this.category = category;
    }

    /**
     * @return the name of the discord provider
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name of the discord provider
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the suggestion channel id of the discord provider
     */
    public Long getSuggestionChannelId() {
        return suggestionChannelId;
    }

    /**
     * @param suggestionChannelId the suggestion channel id of the discord provider
     */
    public void setSuggestionChannelId(Long suggestionChannelId) {
        this.suggestionChannelId = suggestionChannelId;
    }

    /**
     * @return {@code true} if the discord provider is enabled, otherwise {@code false}
     */
    public Boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled {@code true} if the discord provider should be enabled, otherwise {@code false}
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "DiscordProviderFromDiscord{" +
                "id=" + id +
                ", category=" + category +
                ", name='" + name + '\'' +
                ", suggestionChannelId=" + suggestionChannelId +
                ", enabled=" + enabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DiscordProviderFromDiscord that = (DiscordProviderFromDiscord) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
