package com.thenexusreborn.bedwars;

import com.stardevllc.minecraft.Cuboid;
import com.stardevllc.minecraft.Position;
import com.stardevllc.stargenerators.model.listener.ItemPickupListener;
import com.stardevllc.starlib.helper.StringHelper;
import com.stardevllc.starlib.time.TimeUnit;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

public class IslandForge extends BedwarsGenerator {
    
    /**
     * Responsible for handling duping items NOT based on a teams
     */
    public static final ItemPickupListener PICKUP_LISTENER = (entity, item) -> {
        if (!(entity instanceof Player)) {
            return;
        }
        
        if (item.entry().getKey().equals(Resource.EMERALD.getKey())) {
            return;
        }
        
        Cuboid region = item.generator().getRegion();
        
        if (region == null) {
            return;
        }
        
        for (Entity cEntity : item.item().getLocation().getChunk().getEntities()) {
            if (cEntity instanceof Player player) {
                if (cEntity == entity) {
                    continue;
                }
                
                if (region.contains(player)) {
                    player.getInventory().addItem(item.entry().createItemStack(false));
                }
            }
        }
    };
    
    private record ResourceInfo(long cooldown, int maxItems, int stackSize) {}
    
    public enum Tier {
        BASE(new ResourceInfo(1000, 48, 3), new ResourceInfo(3000, 16, 1), new ResourceInfo(0, 0, 0)), 
        IRON(new ResourceInfo(900, 48, 3), new ResourceInfo(2500, 16, 1), new ResourceInfo(0, 0, 0)), 
        GOLD(new ResourceInfo(800, 48, 3), new ResourceInfo(2000, 16, 1), new ResourceInfo(0, 0, 0)), 
        EMERALD(new ResourceInfo(500, 48, 3), new ResourceInfo(1500, 16, 1), new ResourceInfo(10000, 4, 1)), 
        MOLTEN(new ResourceInfo(300, 48, 3), new ResourceInfo(1200, 16, 1), new ResourceInfo(5000, 4, 1)); 
        
        private final ResourceInfo iron, gold, emerald;
        
        Tier(ResourceInfo iron, ResourceInfo gold, ResourceInfo emerald) {
            this.iron = iron;
            this.gold = gold;
            this.emerald = emerald;
        }
        
        public long getIronCooldown() {
            return iron.cooldown;
        }
        
        public long getGoldCooldown() {
            return gold.cooldown;
        }
        
        public long getEmeraldCooldown() {
            return emerald.cooldown;
        }
        
        public int getIronMaxItems() {
            return iron.maxItems;
        }
        
        public int getGoldMaxItems() {
            return gold.maxItems;
        }
        
        public int getEmeraldMaxItems() {
            return emerald.maxItems;
        }
        
        public int getIronStackSize() {
            return iron.stackSize;
        }
        
        public int getGoldStackSize() {
            return gold.stackSize;
        }
        
        public int getEmeraldStackSize() {
            return emerald.stackSize;
        }
    }
    
    private Tier tier;
    private GameTeam team;
    
    public IslandForge(String name, Position position) {
        this(name, position, position, position);
    }
    
    public IslandForge(String name, Position boundsMin, Position boundsMax, Position spawnPos) {
        super(name, boundsMin != null ? boundsMin : spawnPos, boundsMax != null ? boundsMax : spawnPos);
        this.tier = Tier.BASE;
        
        addEntry(Resource.IRON.get(), spawnPos, this.tier.getIronCooldown(), this.tier.getIronMaxItems(), this.tier.getIronStackSize());
        addEntry(Resource.GOLD.get(), spawnPos, this.tier.getGoldCooldown(), this.tier.getGoldMaxItems(), this.tier.getGoldStackSize());
        addEntry(Resource.EMERALD.get(), spawnPos, this.tier.getEmeraldCooldown(), this.tier.getEmeraldMaxItems(), this.tier.getEmeraldStackSize());
        addPickupListener(PICKUP_LISTENER);
    }
    
    @Override
    protected Hologram createHologram() {
        if (this.hologram != null) {
            return hologram;
        }
        Position position = getSpawnPosition(Resource.IRON.getKey());
        Location location = new Location(world, position.getBlockX() + 0.5, position.getBlockY() + 3, position.getBlockZ() + 0.5);
        return this.hologram = DHAPI.createHologram("forge_" + this.getKey().toString().replace(":", "_"), location, List.of("", "", "", "", ""));
    }
    
    @Override
    protected List<String> getHologramLines() {
        DecimalFormat format = new DecimalFormat("0.0");
        List<String> lines = new LinkedList<>();
        lines.add("&bForge");
        lines.add("&eTier &c" + StringHelper.titlize(getTier().name()));
        lines.add("&7Iron &eNext Spawn in &c" + format.format(TimeUnit.MILLISECONDS.toSeconds(getNextSpawn(Resource.IRON.get()))) + " &eseconds");
        lines.add("&6Gold &eNext Spawn in &c" + format.format(TimeUnit.MILLISECONDS.toSeconds(getNextSpawn(Resource.GOLD.get()))) + " &eseconds");
        if (tier.emerald.cooldown > 0) {
            lines.add("&2Emerald &eNext Spawn in &c" + format.format(TimeUnit.MILLISECONDS.toSeconds(getNextSpawn(Resource.EMERALD.get()))) + " &eseconds");
        }
        
        return lines;
    }
    
    public void addPickupListener(ItemPickupListener listener) {
        addPickupListener(Resource.IRON.get(), listener);
        addPickupListener(Resource.GOLD.get(), listener);
    }
    
    public GameTeam getTeam() {
        return team;
    }
    
    public void setTeam(GameTeam team) {
        this.team = team;
    }
    
    @Override
    public boolean upgrade() {
        if (this.tier.ordinal() + 1 < Tier.values().length) {
            setTier(Tier.values()[this.tier.ordinal() + 1]);
            return true;
        }
        
        return false;
    }
    
    @Override
    public String getTierName() {
        return this.tier.name();
    }
    
    public void setTier(Tier newTier) {
        setEntryValues(Resource.IRON.get(), newTier.getIronCooldown(), newTier.getIronMaxItems(), newTier.getIronStackSize());
        setEntryValues(Resource.GOLD.get(), newTier.getGoldCooldown(), newTier.getGoldMaxItems(), newTier.getGoldStackSize());
        setEntryValues(Resource.EMERALD.get(), newTier.getEmeraldCooldown(), newTier.getEmeraldMaxItems(), newTier.getEmeraldStackSize());
        this.tier = newTier;
    }
    
    public Tier getTier() {
        return tier;
    }
}
