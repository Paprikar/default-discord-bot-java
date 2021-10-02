package dev.paprikar.defaultdiscordbot.core.persistence.entity;

import dev.paprikar.defaultdiscordbot.utils.DefaultObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "discord_guild", uniqueConstraints = {
        @UniqueConstraint(name = "discord_guild_discord_id_unique", columnNames = "discord_id")
})
public class DiscordGuild implements Serializable {

    private static final long serialVersionUID = 9149541181598340425L;

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

    public DiscordGuild() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(Long discordId) {
        this.discordId = discordId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return DefaultObjectMapper.serializeAsString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DiscordGuild guild = (DiscordGuild) o;

        return Objects.equals(id, guild.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
