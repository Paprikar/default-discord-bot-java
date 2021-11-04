package dev.paprikar.defaultdiscordbot.core.persistence.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "discord_media_request", indexes = {
        @Index(name = "creation_timestamp_idx", columnList = "creation_timestamp")
})
public class DiscordMediaRequest implements Serializable {

    private static final long serialVersionUID = 7730732395610934894L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "discord_media_request_id_generator")
    @SequenceGenerator(name = "discord_media_request_id_generator",
            sequenceName = "discord_media_request_id_seq",
            allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "discord_category_id",
            foreignKey = @ForeignKey(name = "discord_category_id_fkey"),
            nullable = false)
    private DiscordCategory category;

    @Column(nullable = false)
    private String content;

    @Column(name = "creation_timestamp", nullable = false)
    private Timestamp creationTimestamp;

    public DiscordMediaRequest() {
    }

    public DiscordMediaRequest(DiscordCategory category, String content) {
        this.category = category;
        this.content = content;
        this.creationTimestamp = Timestamp.from(Instant.now());
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Timestamp creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }
}
