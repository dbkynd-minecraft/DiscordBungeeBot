package com.dbkynd.DBBungeeBot.bot;

import com.dbkynd.DBBungeeBot.Main;
import com.dbkynd.DBBungeeBot.bot.command.AddMeCommand;
import com.dbkynd.DBBungeeBot.bot.util.CommandParser;
import com.dbkynd.DBBungeeBot.sql.MySQLConnection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.logging.Level;

public class ServerBot {

    private Main plugin;

    private JDA jda;

    private CommandParser parser;

    private HashMap<String, Command> commands = new HashMap<>();

    public ServerBot(Main plugin) {
        this.plugin = plugin;
    }

    public void runBot() {
        parser = new CommandParser();
        plugin.log(Level.INFO, "Initializing Discord Bot...");
        String token = plugin.getBotToken();
        plugin.log(Level.INFO, "Connecting bot to Discord...");
        try {
            jda = new JDABuilder(token)
                    .setAutoReconnect(true)
                    .setDisabledCacheFlags(EnumSet.of(CacheFlag.VOICE_STATE))
                    .addEventListeners(new BotListener(this))
                    .build()
                    .awaitReady();

            String link = "https://discordapp.com/oauth2/authorize?&client_id=" + jda.getSelfUser().getId();
            link = link + "&scope=bot";
            plugin.log(Level.INFO, "Connection successful!");
            plugin.log(Level.INFO, "You can add this bot to Discord using this link: ");
            plugin.log(Level.INFO, link);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.log(Level.SEVERE, "There was an issue connecting to Discord. Bot shutting down!");
        }

        commands.put(plugin.getAddMeCommand(), new AddMeCommand(this));
    }

    public void handleCommand(CommandParser.CommandContainer cmd) {
        if (commands.containsKey(cmd.invoke)) {
            boolean safe = commands.get(cmd.invoke).called(cmd.args, cmd.event);

            if (safe) {
                commands.get(cmd.invoke).action(cmd.args, cmd.event);
                commands.get(cmd.invoke).executed(true, cmd.event);
            } else {
                commands.get(cmd.invoke).executed(false, cmd.event);
            }
        }
    }

    public MySQLConnection getSQL() {
        return plugin.getSQL();
    }

    public String getTableName() {
        return plugin.getTableName();
    }

    public void log(Level level, String msg) {
        plugin.log(level, msg);
    }

    public CommandParser getCommandParser() {
        return parser;
    }

    public JDA getJDA() {
        return jda;
    }

    public void shutDown() {
        jda.shutdownNow();
    }

    public Main getPlugin() {
        return plugin;
    }

}
