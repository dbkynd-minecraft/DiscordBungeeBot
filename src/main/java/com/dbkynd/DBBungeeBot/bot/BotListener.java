package com.dbkynd.DBBungeeBot.bot;

import com.dbkynd.DBBungeeBot.Main;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.logging.Level;

public class BotListener extends ListenerAdapter {

    ServerBot bot;

    Main plugin;

    public BotListener(ServerBot bot) {
        this.bot = bot;
        plugin = bot.getPlugin();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Exit if message authored by a BOT
        if (event.getAuthor().isBot()) return;

        // Exit of not ran in text channel
        if (event.getChannelType() != ChannelType.TEXT) return;

        // Exit if the message is not prefixed correctly
        if (!event.getMessage().getContentDisplay().startsWith(plugin.getCommandPrefix())) return;

        // Exit if the message came from self
        if (event.getMessage().getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) return;

        // Handle the command
        bot.handleCommand(bot.getCommandParser().parse(event.getMessage().getContentRaw(), event, plugin.getCommandPrefix()));
    }

    @Override
    public void onReady(ReadyEvent event) {
        bot.log(Level.INFO, "Bot is ready!");
    }

}
