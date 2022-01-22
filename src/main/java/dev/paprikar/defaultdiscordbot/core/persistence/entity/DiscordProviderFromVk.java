package dev.paprikar.defaultdiscordbot.core.persistence.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.persistence.*;

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

    public DiscordProviderFromVk() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DiscordCategory getCategory() {
        return category;
    }

    public void setCategory(DiscordCategory category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
            String message = "The provider not attached to the category cannot be detached from it";
            logger.error(message);
            throw new IllegalArgumentException(message);
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
