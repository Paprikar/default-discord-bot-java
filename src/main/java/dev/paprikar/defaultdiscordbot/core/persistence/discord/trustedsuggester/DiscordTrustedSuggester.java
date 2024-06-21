package dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester;

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
 * An entity containing information about the trusted suggester.
 */
@Entity
@Table(name = "discord_trusted_suggester")
public class DiscordTrustedSuggester {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "discord_trusted_suggester_id_generator")
    @SequenceGenerator(name = "discord_trusted_suggester_id_generator",
            sequenceName = "discord_trusted_suggester_id_seq",
            allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id",
            foreignKey = @ForeignKey(name = "category_id_fkey"),
            nullable = false)
    private DiscordCategory category;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Constructs the entity.
     */
    public DiscordTrustedSuggester() {
    }

    /**
     * @return the id of the suggester
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id of the suggester
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the category to which this suggester is attached
     */
    public DiscordCategory getCategory() {
        return category;
    }

    /**
     * @param category the category to which this suggester is attached
     */
    public void setCategory(DiscordCategory category) {
        this.category = category;
    }

    /**
     * @return the discord id of trusted suggesting user
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId the discord id of trusted suggesting user
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "DiscordTrustedSuggester{" +
                "id=" + id +
                ", category=" + category +
                ", userId=" + userId +
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

        DiscordTrustedSuggester that = (DiscordTrustedSuggester) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
