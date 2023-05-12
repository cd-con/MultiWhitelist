package org.github.cdcon.multiwhitelist.structs;

import java.util.ArrayList;
import java.util.List;

public class GroupStruct {
    public boolean Enabled = true;
    public List<String> WhitelistedPlayers = new ArrayList<String>();
    public String WhitelistMessage;
}
