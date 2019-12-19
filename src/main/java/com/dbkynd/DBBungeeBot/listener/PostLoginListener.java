package com.dbkynd.DBBungeeBot.listener;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;
import java.util.logging.Level;

import com.dbkynd.DBBungeeBot.Main;

public class PostLoginListener implements Listener {

    private Main plugin;

    public PostLoginListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String name = player.getName();
        UUID uuid = player.getUniqueId();

        // Return if the player has bypass permissions
        if (player.hasPermission("dbbungeebot.bypass")) {
            plugin.log(Level.INFO, "[" + name + "] has bypass permissions.");
            return;
        }

        // Check that the user is a current member of the discord
        // This method also updates the database records with the UUID of the connecting player
        if (plugin.isMember(name, uuid.toString())) {

            // Return if the player is a member and we are not checking roles
            if (!plugin.checkRole()) {
                plugin.log(Level.INFO, "[" + name + "] is a member.");
                return;
            }

            // Loop through all the guild the client is a menber of
            for (Guild guild : plugin.getBot().getJDA().getGuilds()) {
                // Loop through all the user defined roles
                for (String rolename : plugin.getRequiredRole().split(",")) {

                    // Get the guild role object
                    Role role = null;
                    for (Role r : guild.getRoles()) {
                        if (r.getName().equalsIgnoreCase(rolename)) {
                            role = r;
                        }
                    }

                    // Get the user and member objects
                    User user = plugin.getDiscordUser(player);
                    Member mem = guild.getMember(user);

                    // If the user is a member of this guild
                    if (user.getMutualGuilds().contains(guild)) {
                        // If we have a member and role
                        // If the member roles matches the role iteration
                        if (mem != null && role != null && mem.getRoles().contains(role)) {
                            plugin.log(Level.INFO, "[" + name + "] has the role: '" + role.getName() + "'.");
                            return;
                        }
                    }
                }
            }
        }

        // Kick the player with the kick message
        player.disconnect(new TextComponent(ChatColor.RED + plugin.getKickMessage()));
    }
}
