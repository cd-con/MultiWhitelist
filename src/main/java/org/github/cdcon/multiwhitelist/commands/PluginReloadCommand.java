package org.github.cdcon.multiwhitelist.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.github.cdcon.multiwhitelist.MultiWhitelist;
import org.jetbrains.annotations.NotNull;

public class PluginReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals("mwreload")){
            MultiWhitelist.instance.reloadConfigFromFile();
            MultiWhitelist.instance.send2SenderTemplatedMessage(sender, "%mwr%");
        }
        return true;
    }
}
