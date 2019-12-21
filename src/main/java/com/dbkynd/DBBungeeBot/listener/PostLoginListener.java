package com.dbkynd.DBBungeeBot.listener;

import com.dbkynd.DBBungeeBot.http.WebRequest;
import com.dbkynd.DBBungeeBot.mojang.MojangJSON;
import com.dbkynd.DBBungeeBot.sql.UserRecord;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.logging.Level;

import com.dbkynd.DBBungeeBot.permissions.LuckPermissions;
import com.dbkynd.DBBungeeBot.Main;

public class PostLoginListener implements Listener {

    Main plugin;
    LuckPermissions luck = new LuckPermissions();
    WebRequest webRequest = new WebRequest();

    public PostLoginListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        String name = event.getConnection().getName();

        // Check our own database for a Discord registered user record
        UserRecord userRecord = plugin.getRegistered(name);

        if (userRecord != null) {
            // Return if the player is a member and we are not checking roles
            if (!plugin.checkRole()) {
                plugin.log(Level.INFO, "[" + name + "] is a registered member.");
                return;
            }

            for (Guild guild : plugin.getBot().getJDA().getGuilds()) {
                // Get the guild role object
                Role role = null;
                for (Role r : guild.getRoles()) {
                    if (r.getId().equals(plugin.getRequiredRoleId())) {
                        role = r;
                    }
                }

                User user = plugin.getBot().getJDA().getUserById(userRecord.getDiscordId());
                if (user != null && user.getMutualGuilds().contains(guild)) {
                    Member mem = guild.getMember(user);
                    if (mem != null && role != null && mem.getRoles().contains(role)) {
                        plugin.log(Level.INFO, "[" + name + "] has the role: '" + role.getName() + "'.");
                        return;
                    }
                }
            }
        }

        // See if the player has bypass permissions via the lucKPerm api
        if (hasLuckPerms(name)) {
            plugin.log(Level.INFO, "[" + name + "] has bypass permissions.");
            return;
        }

        // No checks have passed
        // Kick the connecting player
        plugin.log(Level.INFO, name + " attempted to connect but is not registered.");
        event.setCancelReason(new TextComponent(ChatColor.GOLD + plugin.getKickMessage()));
        event.setCancelled(true);
    }

    private boolean hasLuckPerms(String name) {
        // Hit the Mojang API for the UUID and see if they have bypass permissions
        // as LuckPerms can't do a reliable name lookup with the user in a not quite
        // online / offline state
        MojangJSON mojangJSON = webRequest.getMojangData(name);

        if (mojangJSON != null) {
            if (luck.hasPermission(mojangJSON.getUUID(), "dbbungeebot.bypass")) {
                return true;
            }
            if (luck.hasPermission(mojangJSON.getUUID(), "dbbungeebot.*")) {
                return true;
            }
        }

        return false;
    }
}
