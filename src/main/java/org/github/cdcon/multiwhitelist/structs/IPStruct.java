package org.github.cdcon.multiwhitelist.structs;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.entity.Player;

import java.util.List;

public class IPStruct {
    private String IP = "0.0.0.0";
    private List<Player> players;

    public IPStruct(String ipAddress) {
        if (ipAddress == null) {
            throw new NullArgumentException("ipAddress");
        }
        IP = ipAddress;
    }

    public String getIP() {
        return IP;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player p) {
        if (p == null) {
            throw new NullArgumentException("player");
        }
        players.add(p);
    }

    public void removePlayer(Player p){
        if (p == null) {
            throw new NullArgumentException("player");
        }
        players.remove(p);
    }

    public Player popPlayer(){
        return players.get(players.size() - 1);
    }
}
