package com.thenexusreborn.bedwars.arena;

import com.thenexusreborn.api.server.NexusServer;
import com.thenexusreborn.gamemaps.model.GameMap;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class is for storing and using information from a GameMap as to not modify the GameMap class itself
 */
public abstract class GameArena<P extends JavaPlugin, S extends NexusServer, M extends GameMap> implements Listener {
    
    protected final P plugin;
    protected final S server;
    protected M map;
    protected World world;
    protected String name;
    
    public GameArena(P plugin, S server, M map) {
        this.plugin = plugin;
        this.server = server;
        this.map = map;
        if (map != null) {
            this.name = map.getName();
        }
    }
    
    public GameArena(P plugin, S server) {
        this(plugin, server, null);
    }
    
    public P getPlugin() {
        return plugin;
    }
    
    public abstract boolean contains(Location location);
    public abstract boolean init(World world);
    public abstract boolean deinit();
    
    public boolean isInitialized() {
        return world != null;
    }
    
    protected void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    protected void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }
    
    public void teleportToCenter(Entity entity) {
        
    }
    
    public void copyFrom(GameArena<P, S, M> arena) {
        this.map = arena.map;
        this.world = arena.world;
    }
    
    public World getWorld() {
        return this.world;
    }
    
    public M getMap() {
        return map;
    }
    
    public void setMap(M map) {
        this.map = map;
    }
    
    public S getServer() {
        return server;
    }
    
    public boolean contains(Entity entity) {
        return contains(entity.getLocation());
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}