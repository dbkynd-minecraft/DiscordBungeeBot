package com.dbkynd.DBBungeeBot.bot.command;

import com.dbkynd.DBBungeeBot.Main;
import com.dbkynd.DBBungeeBot.bot.Command;
import com.dbkynd.DBBungeeBot.bot.ServerBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.logging.Level;

public class AddMeCommand implements Command {

    private ServerBot bot;

    private final String HELP;

    Main plugin;

    public AddMeCommand(ServerBot bot) {
        this.bot = bot;
        plugin = bot.getPlugin();
        HELP = "USAGE: " + plugin.getCommandPrefix() + plugin.getAddMeCommand() + " <username>";
    }

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if (args.length == 1) {
            String name = args[0];
            if (name.length() > 16) {
                return;
            }
            bot.log(Level.INFO, event.getAuthor().getName() + " issued a Discord Bot command: " + plugin.getCommandPrefix() + plugin.getAddMeCommand() + " " + name);

            if (bot.getSQL().itemExists("DiscordID", event.getAuthor().getId(), bot.getTableName())) {
                bot.getSQL().set("MinecraftName", name.toLowerCase(), "DiscordID", "=", event.getAuthor().getId(), bot.getTableName());
                bot.getSQL().set("UUID", null, "DiscordID", "=", event.getAuthor().getId(), bot.getTableName());
            } else {
                bot.getSQL().update("INSERT INTO " + bot.getTableName() + " (DiscordID,MinecraftName) VALUES (\'" + event.getAuthor().getId() + "\',\'" + name.toLowerCase() + "\');");
            }

            event.getChannel().sendMessage("Updating Minecraft user database with username **" + args[0] + "**.").queue();
        }

    }

    @Override
    public String help() {
        return HELP;
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        return;
    }

}
