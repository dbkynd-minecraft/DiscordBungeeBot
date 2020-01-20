package com.dbkynd.DiscordBungeeBot.sql;

import com.dbkynd.DiscordBungeeBot.DiscordBungeeBot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;

public class MySQLConnection {

    private com.dbkynd.DiscordBungeeBot.DiscordBungeeBot plugin;

    private Connection connection;

    private String host;
    private String port;
    private String user;
    private String password;
    private String database;

    public MySQLConnection(DiscordBungeeBot plugin, String sqlhost, String sqlport, String sqluser, String sqlpassword, String sqldatabase) {
        this.plugin = plugin;
        this.host = sqlhost;
        this.port = sqlport;
        this.user = sqluser;
        this.password = sqlpassword;
        this.database = sqldatabase;
    }

    public Connection getConnection() {
        return connection;
    }

    public void connect() {
        if (host == null || host.equalsIgnoreCase("")) {
            plugin.log(Level.SEVERE, "You have not specified a host in the Main config!");
        } else if (user == null || user.equalsIgnoreCase("")) {
            plugin.log(Level.SEVERE, "You have not specified a user in the Main config!");
        } else if (password == null || password.equalsIgnoreCase("")) {
            plugin.log(Level.SEVERE, "You have not specified a password in the Main config!");
        } else if (database == null || database.equalsIgnoreCase("")) {
            plugin.log(Level.SEVERE, "You have not specified a database in the Main config!");
        } else {
            login();
        }
    }

    public void disconnect() {
        try {
            if (getConnection() != null) {
                connection.close();
            } else {
                plugin.log(Level.SEVERE, "There was an issue with MySQL: Main is not currently connected to a database.");
            }
        } catch (Exception e) {
            plugin.log(Level.SEVERE, "There was an issue with MySQL: " + e.getMessage());
        }
        connection = null;
    }

    public void reconnect() {
        disconnect();
        connect();
    }

    public void login() {
        try {
            if (getConnection() != null) {
                connection.close();
            }
        } catch (Exception e) {
            // Do Nothing
        }
        connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
        } catch (Exception e) {
            plugin.log(Level.SEVERE, "There was an issue with MySQL: " + e.getMessage());
        }
    }

    public ResultSet query(String query) {
        if (query == null) {
            return null;
        }
        connect();
        ResultSet results = null;
        try {
            Statement statement = getConnection().createStatement();
            results = statement.executeQuery(query);
        } catch (Exception e) {
            plugin.log(Level.SEVERE, "There has been an error:" + e.getMessage());
            plugin.log(Level.SEVERE, "Failed Query in MySQL using the following query input:");
            plugin.log(Level.SEVERE, query);
        }
        return results;
    }

    public void update(String input) {
        if (input == null) {
            return;
        }
        connect();
        try {
            Statement statement = getConnection().createStatement();
            statement.executeUpdate(input);
            statement.close();
        } catch (Exception e) {
            plugin.log(Level.SEVERE, "There has been an error:" + e.getMessage());
            plugin.log(Level.SEVERE, "Failed to update MySQL using the following update input:");
            plugin.log(Level.SEVERE, input);
        }
    }

    public boolean tableExists(String tablename) {
        if (tablename == null) {
            return false;
        }
        try {
            if (getConnection() == null) {
                return false;
            }
            if (getConnection().getMetaData() == null) {
                return false;
            }
            ResultSet results = getConnection().getMetaData().getTables(null, null, tablename, null);
            if (results.next()) {
                return true;
            }
        } catch (Exception localException) {
            // Do Nothing
        }
        return false;
    }

    public boolean itemExists(String column, String data, String table) {
        if (data != null) {
            data = "'" + data + "'";
        }
        try {
            ResultSet results = query("SELECT * FROM " + table + " WHERE " + column + "=" + data);
            while (results.next()) {
                if (results.getString(column) != null) {
                    return true;
                }
            }
        } catch (Exception localException) {
            // Do Nothing
        }
        return false;
    }


    public void set(String selected, Object object, String column, String equality, String data, String table) {
        if (object != null) {
            object = "'" + object + "'";
        }
        if (data != null) {
            data = "'" + data + "'";
        }
        update("UPDATE " + table + " SET " + selected + "=" + object + " WHERE " + column + equality + data + ";");
    }

    public Object get(String selected, String column, String equality, String data, String table) {
        if (data != null) {
            data = "'" + data + "'";
        }
        try {
            ResultSet rs = query("SELECT * FROM " + table + " WHERE " + column + equality + data);
            if (rs.next()) {
                return rs.getObject(selected);
            }
        } catch (Exception localException) {
            // Do Nothing
        }
        return null;
    }

}
