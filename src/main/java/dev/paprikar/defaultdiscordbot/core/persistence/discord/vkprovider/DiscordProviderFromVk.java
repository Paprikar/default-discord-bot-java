package dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.persistence.*;

/**
 * An entity containing information about the vk provider.
 */
@Entity
@Table(name = "discord_provider_from_vk")
public class DiscordProviderFromVk {

    private static final Logger logger = LoggerFactory.getLogger(DiscordProviderFromVk.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "discord_provider_from_vk_id_generator")
    @SequenceGenerator(name = "discord_provider_from_vk_id_generator",
            sequenceName = "discord_provider_from_vk_id_seq",
            allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "discord_category_id",
            foreignKey = @ForeignKey(name = "discord_category_id_fkey"),
            nullable = false)
    private DiscordCategory category;

    @Column(length = 32)
    private String name;

    @Column
    private Integer groupId;

    @Column
    private String token;

    @Column(nullable = false)
    private Boolean enabled = false;

    // todo vip list


    /**
     * Constructs the entity.
     */
    public DiscordProviderFromVk() {
    }

    /**
     * @return the id of the vk provider
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *         the id of the vk provider
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the category to which this vk provider is attached
     */
    public DiscordCategory getCategory() {
        return category;
    }

    /**
     * @param category
     *         the category to which this vk provider is attached
     */
    public void setCategory(DiscordCategory category) {
        this.category = category;
    }

    /**
     * @return the name of the vk provider
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *         the name of the vk provider
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the group id of the vk provider
     */
    public Integer getGroupId() {
        return groupId;
    }

    /**
     * @param groupId
     *         the group id of the vk provider
     */
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    /**
     * @return the token of the vk provider
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token
     *         the token of the vk provider
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return {@code true} if the vk provider is enabled, otherwise {@code false}
     */
    public Boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled
     *         {@code true} if the vk provider should be enabled, otherwise {@code false}
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Attaches the vk provider to the specified category.
     *
     * @param category
     *         the category
     *
     * @throws IllegalStateException
     *         if the vk provider is already attached to the category
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
     * Detaches the vk provider from its category.
     *
     * @throws IllegalStateException
     *         if the vk provider is already detached from the category
     */
    public void detach() {
        if (category == null) {
            String message = "The provider not attached to the category cannot be detached from it";
            logger.error(message);
            throw new IllegalStateException(message);
        }
        category = null;
    }

    @Override
    public String toString() {
        return "DiscordProviderFromVk{" +
                "id=" + id +
                ", category=" + category +
                ", name='" + name + '\'' +
                ", groupId=" + groupId +
                ", token='" + token + '\'' +
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

        DiscordProviderFromVk that = (DiscordProviderFromVk) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
