package com.dbkynd.DBBungeeBot.listener;

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

import com.dbkynd.DBBungeeBot.Main;

public class PostLoginListener implements Listener {

    private Main plugin;

    public PostLoginListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        String name = event.getConnection().getName();

        String discordId = plugin.isRegistered(name);

        if (discordId != null) {

            // Return if the player is a member and we are not checking roles
            if (!plugin.checkRole()) {
                plugin.log(Level.INFO, "[" + name + "] is a registered member.");
                return;
            }

            for (Guild guild : plugin.getBot().getJDA().getGuilds()) {
                // Get the guild role object
                Role role = null;
                for (Role r : guild.getRoles()) {
                    if (r.getName().equalsIgnoreCase(plugin.getRequiredRole())) {
                        role = r;
                    }
                }

                User user = plugin.getBot().getJDA().getUserById(discordId);
                if (user != null && user.getMutualGuilds().contains(guild)) {
                    Member mem = guild.getMember(user);
                    if (mem != null && role != null && mem.getRoles().contains(role)) {
                        plugin.log(Level.INFO, "[" + name + "] has the role: '" + role.getName() + "'.");
                        return;
                    }
                }
            }
        }

        event.setCancelReason(new TextComponent(ChatColor.RED + plugin.getKickMessage()));
        event.setCancelled(true);
    }
}
