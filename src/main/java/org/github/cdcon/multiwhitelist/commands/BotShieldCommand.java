package org.github.cdcon.multiwhitelist.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.github.cdcon.multiwhitelist.MultiWhitelist;
import org.jetbrains.annotations.NotNull;

public class BotShieldCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals("botshield")){
            boolean ShieldState = MultiWhitelist.instance.isAntiBotEnabled;
            MultiWhitelist.instance.isAntiBotEnabled = !ShieldState;
            MultiWhitelist.instance.config.set("bot-attack-protection", !ShieldState);

            if (MultiWhitelist.instance.isAntiBotEnabled){
                MultiWhitelist.instance.send2SenderTemplatedMessage(sender,"%ssa%");
            }else{
                MultiWhitelist.instance.send2SenderTemplatedMessage(sender, "%ssd%");
            }
        }
        return true;
    }
}
