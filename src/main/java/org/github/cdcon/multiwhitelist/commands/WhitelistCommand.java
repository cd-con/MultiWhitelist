package org.github.cdcon.multiwhitelist.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.github.cdcon.multiwhitelist.structs.GroupStruct;
import org.github.cdcon.multiwhitelist.MultiWhitelist;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class WhitelistCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals("whitelistgroup") && args.length > 0) {
            if (args[1].equals("wmessage") && args.length > 2){
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                     stringBuilder.append(args[i]);
                }
                MultiWhitelist.instance.presentGroups.get(args[0]).WhitelistMessage = stringBuilder.toString();
                MultiWhitelist.instance.send2SenderTemplatedMessage(sender,"%mwu%");
                return true;
            }else if (args[1].equals("add") && args.length > 2){
                // TODO implement player saving
            }else{
                MultiWhitelist.instance.presentGroups.get(args[0]).Enabled = !MultiWhitelist.instance.presentGroups.get(args[0]).Enabled;
                if (MultiWhitelist.instance.presentGroups.get(args[0]).Enabled) {
                    MultiWhitelist.instance.send2SenderTemplatedMessage(sender, "%gwe%");
                }else{
                    MultiWhitelist.instance.send2SenderTemplatedMessage(sender, "%gwd%");
                }
            }
            // Update data for all existing groups
            for (Map.Entry<String, GroupStruct> entry:MultiWhitelist.instance.presentGroups.entrySet()) {
                MultiWhitelist.instance.config.set("groups."+entry.getKey()+".enabled", entry.getValue().Enabled);
                MultiWhitelist.instance.config.set("groups."+entry.getKey()+".whitelist-message", entry.getValue().WhitelistMessage);
            }

        }
        return true;
    }
}