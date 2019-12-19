package com.dbkynd.DBBungeeBot;

import com.dbkynd.DBBungeeBot.command.ReloadCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dbkynd.DBBungeeBot.bot.ServerBot;
import com.dbkynd.DBBungeeBot.sql.MySQLConnection;
import com.dbkynd.DBBungeeBot.listener.PostLoginListener;

public class Main extends Plugin {

    /* CONFIG.YML VARIABLES */

    private String sqlhost;
    private String sqlport;
    private String sqluser;
    private String sqldatabase;
    private String sqlpassword;
    private String sqltable;
    private String bottoken;
    private String commandprefix;
    private String addmecommand;
    private boolean checkrole = false;
    private String requiredrole;
    private String kickmsg;

    /* END OF CONFIG.YML VARIABLES */

    private MySQLConnection sql;

    ServerBot bot;

    public static Logger log = Logger.getLogger("DBBungeeBot");

    @Override
    public void onEnable() {
        // Load config.yml
        loadConfig();

        // Register Events
        getProxy().getPluginManager().registerListener(this, new PostLoginListener(this));
        // Register Commands
        getProxy().getPluginManager().registerCommand(this, new ReloadCommand(this));

        // Get sql instance and connect
        sql = new MySQLConnection(this, sqlhost, sqlport, sqluser, sqlpassword, sqldatabase);
        sql.connect();
        // Create table if does not exist
        if (!sql.tableExists(sqltable)) {
            log(Level.INFO, "Table not found. Creating new table...");
            sql.update("CREATE TABLE " + sqltable + " (DiscordID CHAR(18), MinecraftName VARCHAR(16), UUID CHAR(36), PRIMARY KEY (DiscordID));");
            // Ensure table was created before saying so
            if (sql.tableExists(sqltable)) {
                log(Level.INFO, "Table created!");
            }
        }
        // Reconnect to the MySQL database every 20 minutes
        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                sql.reconnect();
            }
        }, 1, 20, TimeUnit.MINUTES);

        // Instantiate and run the Discord Bot
        bot = new ServerBot(this);
        bot.runBot();
    }

    public void loadConfig() {
        // Create the data folder if missing
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        // Set the file path
        File file = new File(getDataFolder(), "config.yml");

        // If the file does not exist save out our resource config.yml
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            // Attempt to load the config file
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

            // Load config.yml into class variables
            sqlhost = config.getString("Host");
            sqlport = config.getString("Port");
            sqluser = config.getString("User");
            sqlpassword = config.getString("Password");
            sqldatabase = config.getString("Database");
            sqltable = config.getString("TableName");
            bottoken = config.getString("BotToken");
            checkrole = config.getBoolean("CheckRole");
            requiredrole = config.getString("RequiredRole");
            kickmsg = config.getString("KickMessage");
            addmecommand = config.getString("AddMeCommand");
            commandprefix = config.getString("CommandPrefix");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(Level level, String msg) {
        log.log(level, msg.replaceAll("�[0-9A-FK-OR]", ""));
    }

    public String getBotToken() {
        return bottoken;
    }

    public MySQLConnection getSQL() {
        return sql;
    }

    public ServerBot getBot() {
        return bot;
    }

    public String getTableName() {
        return sqltable;
    }

    public String getCommandPrefix() {
        return commandprefix;
    }

    public String getAddMeCommand() {
        return addmecommand;
    }

    public String getKickMessage() {
        return kickmsg;
    }

    public boolean checkRole() {
        return checkrole;
    }

    public String getRequiredRole() {
        return requiredrole;
    }

    public User getDiscordUser(ProxiedPlayer p) {
        String name = p.getName().toLowerCase();
        ArrayList<String> ids = new ArrayList<String>();
        ResultSet rs = sql.query("SELECT * FROM " + sqltable + " HAVING MinecraftName = " + "\'" + name.toLowerCase() + "\';");
        try {
            while (rs.next()) {
                ids.add(rs.getString("DiscordID"));
            }
        } catch (SQLException e) {
            log(Level.SEVERE, "Error getting Discord IDs from database!");
            e.printStackTrace();
        }
        for (String id : ids) {
            User user = bot.getJDA().getUserById(id);
            if (user != null) {
                for (Guild guild : bot.getJDA().getGuilds()) {
                    if (guild.isMember(user)) {
                        return user;
                    }
                }
            }
        }
        return null;
    }

    public boolean isMember(String name, String uuid) {
        ArrayList<String> ids = new ArrayList<String>();
        ResultSet rs;
        // See if the player logging in is registered in the database
        if (sql.itemExists("MinecraftName", name, sqltable)) {
            rs = sql.query("SELECT * FROM " + sqltable + " HAVING MinecraftName = " + "\'" + name.toLowerCase() + "\';");
            // Add all the Discord IDs that match the MC IGN name into the array
            try {
                while (rs.next()) {
                    String tempid = rs.getString("DiscordID");
                    ids.add(tempid);
                }
            } catch (SQLException e) {
                log(Level.SEVERE, "Error getting Discord IDs from database!");
                e.printStackTrace();
            }
            // Loop through Discord UserIds
            for (String id : ids) {
                // Get the user from cache
                User user = bot.getJDA().getUserById(id);
                if (user != null) {
                    // If the user is a current member of any of the Guilds the client is in
                    // update the database record with their UUID
                    for (Guild guild : bot.getJDA().getGuilds()) {
                        if (guild.isMember(user)) {
                            sql.update("UPDATE " + sqltable + " SET UUID = " + "\'" + uuid + "\'" + " WHERE DiscordID = " + "\'" + id + "\';");
                            // isMember true
                            return true;
                        }
                    }
                }
            }
        }

        // The user might of changed their MinecraftName
        // check for a matching UUID and update the name if there is a match

        if (sql.itemExists("UUID", uuid, sqltable)) {
            ids = new ArrayList<String>();
            rs = sql.query("SELECT * FROM " + sqltable + " WHERE UUID = " + "\'" + uuid + "\';");
            // Add all the Discord IDs that match the MC UUID into the array
            try {
                while (rs.next()) {
                    ids.add(rs.getString("DiscordID"));
                }
            } catch (SQLException e) {
                log(Level.SEVERE, "Error getting Discord IDs from database!");
                e.printStackTrace();
            }
            // Loop through Discord UserIds
            for (String id : ids) {
                // Get the user from cache
                User user = bot.getJDA().getUserById(id);
                // If the user is a current member of any of the Guilds the client is in
                // update the database record with their MinecraftName
                if (user != null) {
                    for (Guild guild : bot.getJDA().getGuilds()) {
                        if (guild.isMember(user)) {
                            sql.update("UPDATE " + sqltable + " SET MinecraftName = " + "\'" + name.toLowerCase() + "\'" + " WHERE DiscordID = " + "\'" + id + "\';");
                            // isMember true
                            return true;
                        }
                    }
                }
            }
        }

        // isMember false
        // No matching names or UUIDs in the database
        return false;
    }
}
