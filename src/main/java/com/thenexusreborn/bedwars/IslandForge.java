package com.thenexusreborn.bedwars;

import com.stardevllc.minecraft.Position;

public class IslandForge extends BedwarsGenerator {
    
    private record ResourceInfo(long cooldown, int maxItems, int stackSize) {}
    
    public enum Level {
        BASE(new ResourceInfo(1000, 48, 3), new ResourceInfo(3000, 16, 1), new ResourceInfo(0, 0, 0)), 
        IRON(new ResourceInfo(900, 48, 3), new ResourceInfo(2500, 16, 1), new ResourceInfo(0, 0, 0)), 
        GOLD(new ResourceInfo(800, 48, 3), new ResourceInfo(2000, 16, 1), new ResourceInfo(0, 0, 0)), 
        EMERALD(new ResourceInfo(500, 48, 3), new ResourceInfo(1500, 16, 1), new ResourceInfo(10000, 4, 1)), 
        MOLTEN(new ResourceInfo(300, 48, 3), new ResourceInfo(1200, 16, 1), new ResourceInfo(5000, 4, 1)); 
        
        private final ResourceInfo iron, gold, emerald;
        
        Level(ResourceInfo iron, ResourceInfo gold, ResourceInfo emerald) {
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
    
    private Level level;
    
    public IslandForge(String name, Position boundsMin, Position boundsMax, Position spawnPos) {
        super(name, boundsMin != null ? boundsMin : spawnPos, boundsMax != null ? boundsMax : spawnPos);
        this.level = Level.BASE;
        
        addEntry(Resource.IRON.get(), spawnPos, this.level.getIronCooldown(), this.level.getIronMaxItems(), this.level.getIronStackSize());
        addEntry(Resource.GOLD.get(), spawnPos, this.level.getGoldCooldown(), this.level.getGoldMaxItems(), this.level.getGoldStackSize());
        addEntry(Resource.EMERALD.get(), spawnPos, this.level.getEmeraldCooldown(), this.level.getEmeraldMaxItems(), this.level.getEmeraldStackSize());
    }
    
    @Override
    public boolean upgrade() {
        if (this.level.ordinal() + 1 < Level.values().length) {
            return upgrade(Level.values()[this.level.ordinal() + 1]);
        }
        
        return false;
    }
    
    @Override
    public String getTierName() {
        return this.level.name();
    }
    
    public boolean upgrade(Level newLevel) {
        if (this.level.ordinal() >= newLevel.ordinal()) {
            return false;
        }
        
        setEntryValues(Resource.IRON.get(), newLevel.getIronCooldown(), newLevel.getIronMaxItems(), newLevel.getIronStackSize());
        setEntryValues(Resource.GOLD.get(), newLevel.getGoldCooldown(), newLevel.getGoldMaxItems(), newLevel.getGoldStackSize());
        setEntryValues(Resource.EMERALD.get(), newLevel.getEmeraldCooldown(), newLevel.getEmeraldMaxItems(), newLevel.getEmeraldStackSize());
        
        this.level = newLevel;
        return true;
    }
    
    public Level getLevel() {
        return level;
    }
}
