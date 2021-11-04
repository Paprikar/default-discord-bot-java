package dev.paprikar.defaultdiscordbot.core.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;

public interface DiscordCommand {

    void execute(@Nonnull String argsString, @Nonnull GuildMessageReceivedEvent event);
}
