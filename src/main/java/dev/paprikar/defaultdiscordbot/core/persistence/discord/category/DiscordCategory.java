package dev.paprikar.defaultdiscordbot.core.persistence.discord.category;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.guild.DiscordGuild;

import javax.persistence.*;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * An entity containing information about the category.
 */
@Entity
@Table(name = "discord_category", indexes = {
        @Index(name = "discord_category_guild_id_idx", columnList = "guild_id")
})
public class DiscordCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "discord_category_id_generator")
    @SequenceGenerator(name = "discord_category_id_generator",
            sequenceName = "discord_category_id_seq",
            allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "guild_id",
            foreignKey = @ForeignKey(name = "guild_id_fkey"),
            nullable = false)
    private DiscordGuild guild;

    @Column(length = 32, nullable = false)
    private String name;

    @Column(name = "sending_channel_id")
    private Long sendingChannelId;

    @Column(name = "approval_channel_id")
    private Long approvalChannelId;

    @Column(name = "start_time")
    private Time startTime;

    @Column(name = "end_time")
    private Time endTime;

    @Column(name = "reserve_days")
    private Integer reserveDays;

    @Column(name = "positive_approval_emoji", nullable = false)
    private Character positiveApprovalEmoji = 0x2705; // ✅

    @Column(name = "negative_approval_emoji", nullable = false)
    private Character negativeApprovalEmoji = 0x274E; // ❎

    @Column(nullable = false)
    private Boolean enabled = false;

    @Column(name = "last_send_timestamp")
    private Timestamp lastSendTimestamp;

    @Column(name = "bulk_submit", nullable = false)
    private Boolean bulkSubmit = false;

    /**
     * Constructs the entity.
     */
    public DiscordCategory() {
    }

    /**
     * @return the id of the category
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *         the id of the category
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the guild to which this category is attached
     */
    public DiscordGuild getGuild() {
        return guild;
    }

    /**
     * @param guild
     *         the guild to which this category is attached
     */
    public void setGuild(DiscordGuild guild) {
        this.guild = guild;
    }

    /**
     * @return the name of the category
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *         the name of the category
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the sending channel id of the category
     */
    public Long getSendingChannelId() {
        return sendingChannelId;
    }

    /**
     * @param sendingChannelId
     *         the sending channel id of the category
     */
    public void setSendingChannelId(Long sendingChannelId) {
        this.sendingChannelId = sendingChannelId;
    }

    /**
     * @return the approval channel id of the category
     */
    public Long getApprovalChannelId() {
        return approvalChannelId;
    }

    /**
     * @param approvalChannelId
     *         the approval channel id of the category
     */
    public void setApprovalChannelId(Long approvalChannelId) {
        this.approvalChannelId = approvalChannelId;
    }

    /**
     * @return the start time for sending of the category
     */
    public Time getStartTime() {
        return startTime;
    }

    /**
     * @param startTime
     *         the start time for sending of the category
     */
    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the end time for sending of the category
     */
    public Time getEndTime() {
        return endTime;
    }

    /**
     * @param endTime
     *         the end time for sending of the category
     */
    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the number of reserve days for sending of the category
     */
    public Integer getReserveDays() {
        return reserveDays;
    }

    /**
     * @param reserveDays
     *         the number of reserve days for sending of the category
     */
    public void setReserveDays(Integer reserveDays) {
        this.reserveDays = reserveDays;
    }

    /**
     * @return the positive approval emoji of the category
     */
    public Character getPositiveApprovalEmoji() {
        return positiveApprovalEmoji;
    }

    /**
     * @param positiveApprovalEmoji
     *         the positive approval emoji of the category
     */
    public void setPositiveApprovalEmoji(Character positiveApprovalEmoji) {
        this.positiveApprovalEmoji = positiveApprovalEmoji;
    }

    /**
     * @return the negative approval emoji of the category
     */
    public Character getNegativeApprovalEmoji() {
        return negativeApprovalEmoji;
    }

    /**
     * @param negativeApprovalEmoji
     *         the negative approval emoji of the category
     */
    public void setNegativeApprovalEmoji(Character negativeApprovalEmoji) {
        this.negativeApprovalEmoji = negativeApprovalEmoji;
    }

    /**
     * @return {@code true} if the category is enabled, otherwise {@code false}
     */
    public Boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled
     *         {@code true} if the category should be enabled, otherwise {@code false}
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the last suggestion sending time of the category
     */
    public Timestamp getLastSendTimestamp() {
        return lastSendTimestamp;
    }

    /**
     * @param lastSendTimestamp
     *         the last suggestion sending time of the category
     */
    public void setLastSendTimestamp(Timestamp lastSendTimestamp) {
        this.lastSendTimestamp = lastSendTimestamp;
    }


    /**
     * @return {@code true} if the bulk submit is enabled for this category, otherwise {@code false}
     */
    public Boolean isBulkSubmit() {
        return bulkSubmit;
    }

    /**
     * @param bulkSubmit
     *         {@code true} if the bulk submit is enabled for this category, otherwise {@code false}
     */
    public void setBulkSubmit(Boolean bulkSubmit) {
        this.bulkSubmit = bulkSubmit;
    }

    @Override
    public String toString() {
        return "DiscordCategory{" +
                "id=" + id +
                ", guild=" + guild +
                ", name='" + name + '\'' +
                ", sendingChannelId=" + sendingChannelId +
                ", approvalChannelId=" + approvalChannelId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", reserveDays=" + reserveDays +
                ", positiveApprovalEmoji=" + positiveApprovalEmoji +
                ", negativeApprovalEmoji=" + negativeApprovalEmoji +
                ", enabled=" + enabled +
                ", lastSendTimestamp=" + lastSendTimestamp +
                ", bulkSubmit=" + bulkSubmit +
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

        DiscordCategory that = (DiscordCategory) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
