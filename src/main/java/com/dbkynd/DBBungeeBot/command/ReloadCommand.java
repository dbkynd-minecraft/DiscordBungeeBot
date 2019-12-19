package com.dbkynd.DBBungeeBot.command;

import com.dbkynd.DBBungeeBot.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class ReloadCommand extends Command {

    public Main plugin;

    public ReloadCommand(Main plugin) {
        super("dbreload", "dbbungeebot.reload");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        try {
            plugin.loadConfig();
            commandSender.sendMessage(new TextComponent("[" + ChatColor.GOLD + "DBBungeeBot" + ChatColor.RESET + "]: The config.yml file has been reloaded."));
        } catch (Exception e) {
            commandSender.sendMessage(new TextComponent("[" + ChatColor.GOLD + "DBBungeeBot" + ChatColor.RESET + "]: " + ChatColor.RED + "There was an error reloading the config.yml file."));
        }
    }
}
