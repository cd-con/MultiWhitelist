package org.github.cdcon.multiwhitelist;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.github.cdcon.multiwhitelist.commands.BotShieldCommand;
import org.github.cdcon.multiwhitelist.commands.PluginReloadCommand;
import org.github.cdcon.multiwhitelist.commands.PluginStatsCommand;
import org.github.cdcon.multiwhitelist.commands.WhitelistCommand;
import org.github.cdcon.multiwhitelist.structs.GroupStruct;
import org.github.cdcon.multiwhitelist.structs.IPStruct;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public final class MultiWhitelist extends JavaPlugin implements Listener {

    public static MultiWhitelist instance;
    public FileConfiguration config;
    private boolean isWhitelistEnabled = true;
    public boolean isAntiBotEnabled = true;
    private List<String> BotNamesPattern = new ArrayList<>();

    private List<IPStruct> PlayerIPStruct = new ArrayList<>();

    public Map<String, GroupStruct> presentGroups = new HashMap<>();

    // Messages

    private static String Prefix = "[MW]";
    private static String UniversalWhitelistMessage = "You don't belongs to any group in the whitelist.";
    private static String ShieldActivatedMessage = "Bot shield activated!";
    private static String ShieldDeactivatedMessage = "Bot shield deactivated!";
    private static String GroupEnabledMessage = "Group was enabled! Now players from that group can join server.";
    private static String GroupDisabledMessage = "Group was disabled! Players from that group no longer can join server.";
    private static String PluginReloadedMessage = "Plugin reloaded.";
    private static String ConfigValueUpdated = "Value updated.";
    private static String BotStopListMessage = "You were banned due to bot actions. Contact with server administrator if this was wrong.";

    // Counters
    private long PlayersJoinedCounter = 0;
    private long PlayersWhitelistBlockedCounter = 0;
    private long botsBlockedCounter = 0;

    @Override
    public void onEnable() {
        getLogger().info("Starting MultiWhitelist!");
        instance = this;

        if (!this.getDataFolder().exists()) {
            this.saveDefaultConfig();
            this.getConfig().options().copyDefaults(true);
        }

        File dPath = new File(getDataFolder().getPath() + "/groups/");
        if (dPath.exists()) {
            try {
                saveResource("groups/myGroup.txt", true);
            } catch (Exception e) {
                getLogger().warning("Error occured while trying to create default files. Skipping...");
            }
        }
        reloadConfigFromFile();
        getLogger().info("And here is my stats:\nPlayers ever joined: " + PlayersJoinedCounter +
                "\nPlayers ever blocked by whitelist: " + PlayersWhitelistBlockedCounter +
                "\nBots blocked: " + botsBlockedCounter +
                "\nAnd ALL this was brought to your server by MultiWhitelist and cd-con");

        // Registering events
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("mwstats").setExecutor(new PluginStatsCommand());
        getCommand("botshield").setExecutor(new BotShieldCommand());
        getCommand("mwreload").setExecutor(new PluginReloadCommand());
        getCommand("whitelistgroup").setExecutor(new WhitelistCommand());
    }


    @Override
    public void onDisable() {
        if (config != null) {
            saveConfig();
            return;
        }
        getLogger().warning("Error occurred while attempt to save config file. Not critical, but stats from this session will be removed");
    }

    @EventHandler
    public void onPlayer(PlayerLoginEvent event) {
        if (isAntiBotEnabled) {
            String playerNickname = event.getPlayer().getName();

            for (String botName : BotNamesPattern) {
                if (playerNickname.toLowerCase().contains(botName.toLowerCase())) {
                    botsBlockedCounter++;
                    config.set("stats.players-blocked-by-bot-filter", botsBlockedCounter);
                    event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
                            Component.text(BotStopListMessage));
                    return;
                }
            }

        }
        if (isWhitelistEnabled) {
            getLogger().info("Processing player " + event.getPlayer().getName());
            for (Map.Entry<String, GroupStruct> entry : presentGroups.entrySet()) {
                for (String name : entry.getValue().WhitelistedPlayers) {
                    if (name.equals(event.getPlayer().getName())) {
                        if (entry.getValue().Enabled) {
                            PlayersJoinedCounter++;
                            config.set("stats.players-joined-ever", PlayersJoinedCounter);
                            event.allow();
                        } else {
                            PlayersWhitelistBlockedCounter++;
                            config.set("stats.players-blocked-by-whitelist", PlayersWhitelistBlockedCounter);
                            event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
                                    Component.text(ChatColor.translateAlternateColorCodes('&',
                                            entry.getValue().WhitelistMessage)));

                        }
                        return;
                    }
                }

            }
            PlayersWhitelistBlockedCounter++;
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST,
                    Component.text(ChatColor.translateAlternateColorCodes('&',
                            UniversalWhitelistMessage)));
            return;
        }
        event.getPlayer().getAddress();
        PlayersJoinedCounter++;
        config.set("stats.players-joined-ever", PlayersJoinedCounter);
    }
    public void reloadConfigFromFile() {
        if (config != null) {
            reloadConfig();
        }
        config = getConfig();
        PlayersJoinedCounter = config.getLong("stats.players-joined-ever", 0);
        PlayersWhitelistBlockedCounter = config.getLong("stats.players-blocked-by-whitelist", 0);
        botsBlockedCounter = config.getLong("stats.players-blocked-by-bot-filter", 0);
        isWhitelistEnabled = config.getBoolean("enabled", false);

        // Anti-bot
        isAntiBotEnabled = config.getBoolean("anit-bot.enabled", false);
        BotNamesPattern = (List<String>) config.getList("anit-bot.bot-name-pattern");

        // Messages
        Prefix = config.getString("prefix");
        UniversalWhitelistMessage = config.getString("locale.universal-whitelist-message");
        ShieldActivatedMessage = config.getString("locale.shield-state-activated");
        ShieldDeactivatedMessage = config.getString("locale.shield-state-deactivated");
        GroupEnabledMessage = config.getString("locale.group-whitelist-enabled");
        GroupDisabledMessage = config.getString("locale.group-whitelist-disabled");
        PluginReloadedMessage = config.getString("locale.mw-reloaded");
        BotStopListMessage = config.getString("locale.message-for-bot");

        Set<String> groupSection = config.getConfigurationSection("groups").getKeys(false);
        // Missing of that thing was breaking all plugin logic for 2 days
        presentGroups.clear();
        for (String groupName : groupSection) {
            getLogger().info("Detected group: " + groupName);
            GroupStruct groupInfo = new GroupStruct();
            groupInfo.Enabled = config.getBoolean("groups." + groupName + ".enabled");
            groupInfo.WhitelistMessage = config.getString("groups." + groupName + ".whitelist-message");
            try {
                groupInfo.WhitelistedPlayers = readFromInputStream(Files.newInputStream(Paths.get(getDataFolder() + Objects.requireNonNull(config.getString("groups." + groupName + ".whitelist-file")))));
            } catch (Exception e) {
                getLogger().info(groupName + " player list is not valid!");
                getLogger().warning("Trace: " + e);
                groupInfo.Enabled = false;
                groupInfo.WhitelistedPlayers = new ArrayList<String>();
            }
            getLogger().info("Group enabled: " + groupInfo.Enabled);
            getLogger().info("Group whitelist message: " + groupInfo.WhitelistMessage);
            getLogger().info("------------------------------------------------------");
            presentGroups.put(groupName, groupInfo);
        }

        // For debug purpose
        getLogger().info("Alright, and here we are!");
        getLogger().info("IsWhitelistEnabled=" + isWhitelistEnabled);
        getLogger().info("BotProtectionEnabled=" + isAntiBotEnabled);
        getLogger().info("presentGroups.size=" + presentGroups.size());
    }

    private List<String> readFromInputStream(InputStream inputStream) throws IOException {
        List<String> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(removeNonTypeCharacters(line));
            }
        }
        return result;
    }

    public String removeNonTypeCharacters(String str) {
        // С днём регулярок :D
        str = str.replaceAll(
                "/([A-Z])\\w+/g", "");
        return str;
    }

    public void send2SenderTemplatedMessage(CommandSender s, String template) {
        s.sendMessage(Component.text(ChatColor.translateAlternateColorCodes('&',Prefix + " " + template.replaceAll("%ssa%", ShieldActivatedMessage).replaceAll("%ssd%", ShieldDeactivatedMessage).replaceAll("%gwe%", GroupEnabledMessage).replaceAll("%gwd%", GroupDisabledMessage).replaceAll("%mwr%", PluginReloadedMessage).replaceAll("%mwu%", ConfigValueUpdated))));
    }


    public void sendStats(CommandSender s) {
        // TODO Добавить локализацию
        s.sendMessage("And here is my stats:\nPlayers ever joined: " + PlayersJoinedCounter +
                "\nPlayers ever blocked by whitelist: " + PlayersWhitelistBlockedCounter +
                "\nBots blocked: " + botsBlockedCounter +
                "\nAnd ALL this was brought to your server by MultiWhitelist and cd-con :)");
    }
}
