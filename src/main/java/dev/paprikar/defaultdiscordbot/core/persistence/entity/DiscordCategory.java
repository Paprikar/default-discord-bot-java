package dev.paprikar.defaultdiscordbot.core.persistence.entity;

import dev.paprikar.defaultdiscordbot.utils.DefaultObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "discord_category", indexes = {
        @Index(name = "discord_guild_id_idx", columnList = "discord_guild_id")
})
public class DiscordCategory implements Serializable {

    private static final long serialVersionUID = 7792186337399345374L;

    private final transient Logger logger = LoggerFactory.getLogger(DiscordCategory.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "discord_category_id_generator")
    @SequenceGenerator(name = "discord_category_id_generator",
            sequenceName = "discord_category_id_seq",
            allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "discord_guild_id",
            foreignKey = @ForeignKey(name = "discord_guild_id_fkey"),
            nullable = false)
    private DiscordGuild guild;

    @Column(length = 32)
    private String name;

    @Column(name = "sending_channel_id")
    private Long sendingChannelId;

    @Column(name = "approval_channel_id")
    private Long approvalChannelId;

    @Temporal(TemporalType.TIME)
    @Column(name = "start_time")
    private Date startTime;

    @Temporal(TemporalType.TIME)
    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "reserve_days")
    private Integer reserveDays;

    @Column(name = "positive_approval_emoji")
    private Character positiveApprovalEmoji;

    @Column(name = "negative_approval_emoji")
    private Character negativeApprovalEmoji;

    @Column(nullable = false)
    private Boolean enabled = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_send_datetime")
    private Date lastSendDatetime;

    public DiscordCategory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DiscordGuild getGuild() {
        return guild;
    }

    public void setGuild(DiscordGuild guild) {
        this.guild = guild;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSendingChannelId() {
        return sendingChannelId;
    }

    public void setSendingChannelId(Long sendingChannelId) {
        this.sendingChannelId = sendingChannelId;
    }

    public Long getApprovalChannelId() {
        return approvalChannelId;
    }

    public void setApprovalChannelId(Long approvalChannelId) {
        this.approvalChannelId = approvalChannelId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getReserveDays() {
        return reserveDays;
    }

    public void setReserveDays(Integer reserveDays) {
        this.reserveDays = reserveDays;
    }

    public Character getPositiveApprovalEmoji() {
        return positiveApprovalEmoji;
    }

    public void setPositiveApprovalEmoji(Character positiveApprovalEmoji) {
        this.positiveApprovalEmoji = positiveApprovalEmoji;
    }

    public Character getNegativeApprovalEmoji() {
        return negativeApprovalEmoji;
    }

    public void setNegativeApprovalEmoji(Character negativeApprovalEmoji) {
        this.negativeApprovalEmoji = negativeApprovalEmoji;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Date getLastSendDatetime() {
        return lastSendDatetime;
    }

    public void setLastSendDatetime(Date lastSendDatetime) {
        this.lastSendDatetime = lastSendDatetime;
    }

    public void attach(@Nonnull DiscordGuild guild) {
        if (this.guild != null) {
            String message = "The category is already attached to the guild";
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
        this.guild = guild;
    }

    public void detach() {
        if (guild == null) {
            String message = "The category not attached to the guild cannot be detached from the guild";
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
        guild = null;
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

        DiscordCategory category = (DiscordCategory) o;

        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
