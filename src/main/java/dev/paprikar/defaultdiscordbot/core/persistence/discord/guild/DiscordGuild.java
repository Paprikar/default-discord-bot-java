package dev.paprikar.defaultdiscordbot.core.persistence.discord.guild;

import javax.persistence.*;

/**
 * An entity containing information about the guild.
 */
@Entity
@Table(name = "discord_guild", uniqueConstraints = {
        @UniqueConstraint(name = "discord_guild_discord_id_unique", columnNames = "discord_id")
})
public class DiscordGuild {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "discord_guild_id_generator")
    @SequenceGenerator(name = "discord_guild_id_generator",
            sequenceName = "discord_guild_id_seq",
            allocationSize = 1)
    private Long id;

    @Column(name = "discord_id", nullable = false)
    private Long discordId;

    @Column(length = 32, nullable = false)
    private String prefix = "!";

    /**
     * Constructs the entity.
     */
    public DiscordGuild() {
    }

    /**
     * @return the id of the guild
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *         the id of the guild
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the discord id of the guild
     */
    public Long getDiscordId() {
        return discordId;
    }

    /**
     * @param discordId
     *         the discord id of the guild
     */
    public void setDiscordId(Long discordId) {
        this.discordId = discordId;
    }

    /**
     * @return the command prefix of the guild
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix
     *         the command prefix of the guild
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return "DiscordGuild{" +
                "id=" + id +
                ", discordId=" + discordId +
                ", prefix='" + prefix + '\'' +
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

        DiscordGuild that = (DiscordGuild) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
