package dev.paprikar.defaultdiscordbot.core.persistence.discord.uservkconnection;

import javax.persistence.*;

/**
 * An entity containing information about vk connections of discord users.
 */
@Entity
@Table(name = "discord_user_vk_connection", indexes = {
        @Index(name = "discord_user_vk_connection_vk_user_id_idx", columnList = "vk_user_id")
})
public class DiscordUserVkConnection {

    @Id
    @Column(name = "discord_user_id")
    private Long discordUserId;

    @Column(name = "vk_user_id")
    private Integer vkUserId;

    /**
     * Constructs the entity.
     */
    public DiscordUserVkConnection() {
    }

    /**
     * @return the discord user id
     */
    public Long getDiscordUserId() {
        return discordUserId;
    }

    /**
     * @param discordUserId
     *         the discord user id
     */
    public void setDiscordUserId(Long discordUserId) {
        this.discordUserId = discordUserId;
    }

    /**
     * @return the vk user id
     */
    public Integer getVkUserId() {
        return vkUserId;
    }

    /**
     * @param vkUserId
     *         the vk user id
     */
    public void setVkUserId(Integer vkUserId) {
        this.vkUserId = vkUserId;
    }

    @Override
    public String toString() {
        return "DiscordUserVkConnection{" +
                "discordUserId=" + discordUserId +
                ", vkUserId=" + vkUserId +
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

        DiscordUserVkConnection that = (DiscordUserVkConnection) o;

        if (!discordUserId.equals(that.discordUserId)) {
            return false;
        }
        return vkUserId.equals(that.vkUserId);
    }

    @Override
    public int hashCode() {
        int result = discordUserId.hashCode();
        result = 31 * result + vkUserId.hashCode();
        return result;
    }

    /**
     * An entity projection that stores information about discord user id.
     */
    public interface ProjectionDiscordUserId {

        /**
         * @return the discord user id
         */
        Long getDiscordUserId();
    }
}
