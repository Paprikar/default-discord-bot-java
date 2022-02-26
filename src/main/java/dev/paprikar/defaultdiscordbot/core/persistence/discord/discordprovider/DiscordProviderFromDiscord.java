package dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.persistence.*;

/**
 * An entity containing information about the discord provider.
 */
@Entity
@Table(name = "discord_provider_from_discord")
public class DiscordProviderFromDiscord {

    private static final Logger logger = LoggerFactory.getLogger(DiscordProviderFromDiscord.class);

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
     * @param id
     *         the id of the discord provider
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
     * @param category
     *         the category to which this discord provider is attached
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
     * @param name
     *         the name of the discord provider
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
     * @param suggestionChannelId
     *         the suggestion channel id of the discord provider
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
     * @param enabled
     *         {@code true} if the discord provider should be enabled, otherwise {@code false}
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Attaches the discord provider to the specified category.
     *
     * @param category
     *         the category
     *
     * @throws IllegalStateException
     *         if the discord provider is already attached to the category
     */
    public void attach(@Nonnull DiscordCategory category) {
        if (this.category != null) {
            String message = "The provider is already attached to the category";
            logger.error(message);
            throw new IllegalStateException(message);
        }
        this.category = category;
    }

    /**
     * Detaches the discord provider from its category.
     *
     * @throws IllegalStateException
     *         if the discord provider is already detached from the category
     */
    public void detach() {
        if (category == null) {
            String message = "The provider not attached to the category cannot be detached from the category";
            logger.error(message);
            throw new IllegalStateException(message);
        }
        category = null;
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
