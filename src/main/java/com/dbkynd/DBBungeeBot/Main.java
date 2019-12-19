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
            sql.update("CREATE TABLE " + sqltable + " (DiscordID CHAR(18), MinecraftName VARCHAR(16), PRIMARY KEY (DiscordID));");
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

    public String isRegistered(String name) {
        ArrayList<String> ids = new ArrayList<String>();
        ResultSet rs;

        if (sql.itemExists("MinecraftName", name, sqltable)) {
            rs = sql.query("SELECT * FROM " + sqltable + " HAVING MinecraftName = " + "\'" + name.toLowerCase() + "\';");
            try {
                while (rs.next()) {
                    ids.add( rs.getString("DiscordID"));
                }
                if (ids.size() > 0) return ids.get(0);
            } catch (SQLException e) {
                log(Level.SEVERE, "Error getting Discord IDs from database!");
                e.printStackTrace();
            }
        }
        return null;
    }
}
