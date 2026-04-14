package com.thenexusreborn.bedwars;

import com.stardevllc.minecraft.registry.PluginRegistry;
import com.stardevllc.stargenerators.StarGenerators;
import com.stardevllc.starlib.collections.listmap.ArrayListMap;
import com.stardevllc.starlib.collections.listmap.ListMap;
import com.stardevllc.starlib.objects.key.Key;
import com.stardevllc.starlib.objects.key.Keys;
import com.stardevllc.starlib.registry.IRegistry;
import com.stardevllc.starlib.repository.*;
import com.thenexusreborn.api.util.NetworkType;
import com.thenexusreborn.nexuscore.NexusCore;
import com.thenexusreborn.nexuscore.api.NexusSpigotPlugin;
import com.thenexusreborn.nexuscore.api.events.NexusServerSetupEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class NexusBedWarsPlugin extends NexusSpigotPlugin implements Listener {
    
    private final Map<Integer, BWVirtualServer> servers = new HashMap<>();
    
    private final Map<Key, Game> games = new HashMap<>();
    private final ListMap<Key, TeamInstance> teamInstances = new ArrayListMap<>();
    
    public static final IRegistry<BedwarsGenerator> GENERATORS = PluginRegistry.builder(BedwarsGenerator.class)
            .withKey(Keys.of("bedwars:generators"))
            .withName("Bedwars Generators")
            .withParent(StarGenerators.ITEM_GENERATORS)
            .build();
    
    private final IRepository<UUID, BWPlayer> players = HashRepository.newBuilder(UUID.class, BWPlayer.class)
            .withKey(Keys.of("bw:players"))
            .withName("BW Players")
            .withLoader(BWPlayer::new)
            .build();
    
    private NexusCore nexusCore;
    private NexusHubHook nexusHubHook;
    
    private int numberOfServers = 1;
    
    @Override
    public void onLoad() {
        Plugin nexusCorePlugin = Bukkit.getPluginManager().getPlugin("NexusCore");
        if (nexusCorePlugin == null) {
            getLogger().severe("NexusCore not found, disabling " + getDescription().getName());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        nexusCore = (NexusCore) nexusCorePlugin;
        nexusCore.addNexusPlugin(this);
        getLogger().info("Loaded NexusCore");
    }
    
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
        
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (BedwarsGenerator generator : GENERATORS) {
                generator.updateHologram();
            }
        }, 1L, 1L);
        
        getServer().getPluginManager().registerEvents(this, this);
        
        Plugin nexusHub = Bukkit.getPluginManager().getPlugin("NexusHub");
        if (nexusHub != null) {
            this.nexusHubHook = new NexusHubHook(this, nexusHub);
        }
    }
    
    public NexusHubHook getNexusHubHook() {
        return nexusHubHook;
    }
    
    public IRepository<UUID, BWPlayer> getPlayers() {
        return players;
    }
    
    @EventHandler
    public void onServerSetup(NexusServerSetupEvent e) {
        if (e.getNetworkType() == NetworkType.MULTI) {
            e.setServer(new BWInstanceServer(this, "BW")); //TODO Name from somewhere
            return;
        }
        
        if (getNexusHubHook() == null) {
            e.setServer(new BWInstanceServer(this, "BW"));
        } else {
            BWVirtualServer sg1 = new BWVirtualServer(this, "BW1");
            e.setPrimaryVirtualServer(sg1);
            e.addVirtualServer(sg1);
            getServers().put(1, sg1);
            for (int i = 1; i < numberOfServers; i++) {
                BWVirtualServer server = new BWVirtualServer(this, "BW" + (i + 1));
                e.addVirtualServer(server);
                getServers().put(i + 1, server);
            }
        }
    }
    
    public Map<Integer, BWVirtualServer> getServers() {
        return servers;
    }
    
    public Map<Key, Game> getGames() {
        return games;
    }
    
    public ListMap<Key, TeamInstance> getTeamInstances() {
        return teamInstances;
    }
    
    public NexusCore getNexusCore() {
        return nexusCore;
    }
}