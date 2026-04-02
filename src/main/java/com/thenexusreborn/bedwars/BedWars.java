package com.thenexusreborn.bedwars;

import com.thenexusreborn.nexuscore.api.NexusSpigotPlugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class BedWars extends NexusSpigotPlugin {
    
    public void onEnable() {
        BedwarsCommand bedwarsCommand = new BedwarsCommand(this);
        PluginCommand cmd = getCommand("bedwars");
        cmd.setExecutor(bedwarsCommand);
        cmd.setTabCompleter(bedwarsCommand);
        
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                Tool.checkTools(player);
            }
        }, 1L, 20L);
    }
}