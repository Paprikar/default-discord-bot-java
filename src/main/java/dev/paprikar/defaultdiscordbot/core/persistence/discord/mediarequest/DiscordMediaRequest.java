package dev.paprikar.defaultdiscordbot.core.persistence.discord.mediarequest;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * An entity containing information about the media request.
 */
@Entity
@Table(name = "discord_media_request", indexes = {
        @Index(name = "discord_media_request_creation_timestamp_idx", columnList = "creation_timestamp")
})
public class DiscordMediaRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "discord_media_request_id_generator")
    @SequenceGenerator(name = "discord_media_request_id_generator",
            sequenceName = "discord_media_request_id_seq",
            allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id",
            foreignKey = @ForeignKey(name = "category_id_fkey"),
            nullable = false)
    private DiscordCategory category;

    @Column(nullable = false)
    private String content;

    @Column(name = "creation_timestamp", nullable = false)
    private Timestamp creationTimestamp;

    /**
     * Constructs the entity.
     */
    public DiscordMediaRequest() {
    }

    /**
     * Constructs the entity.
     *
     * @param category the category
     * @param content the content
     */
    public DiscordMediaRequest(DiscordCategory category, String content) {
        this.category = category;
        this.content = content;
        this.creationTimestamp = Timestamp.from(Instant.now());
    }

    /**
     * @return the id of the media request
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id of the media request
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the category to which this media request is attached
     */
    public DiscordCategory getCategory() {
        return category;
    }

    /**
     * @param category the category to which this media request is attached
     */
    public void setCategory(DiscordCategory category) {
        this.category = category;
    }

    /**
     * @return the content of the media request
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content of the media request
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the time of creation of the media request
     */
    public Timestamp getCreationTimestamp() {
        return creationTimestamp;
    }

    /**
     * @param creationTimestamp the time of creation of the media request
     */
    public void setCreationTimestamp(Timestamp creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    @Override
    public String toString() {
        return "DiscordMediaRequest{" +
                "id=" + id +
                ", category=" + category +
                ", content='" + content + '\'' +
                ", creationTimestamp=" + creationTimestamp +
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

        DiscordMediaRequest that = (DiscordMediaRequest) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
