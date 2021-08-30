package dev.paprikar.defaultdiscordbot.core.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;

public class DiscordPingCommand implements DiscordCommand {

    @Override
    public void execute(@Nonnull String argsString, @Nonnull GuildMessageReceivedEvent event) {
        event.getChannel().sendMessage("...pong").queue();
    }
}
