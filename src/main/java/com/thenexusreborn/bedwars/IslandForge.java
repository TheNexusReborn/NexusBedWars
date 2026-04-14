package com.thenexusreborn.bedwars;

import com.stardevllc.minecraft.Position;

public class IslandForge extends BedwarsGenerator {
    
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
    
    public IslandForge(String name, Position boundsMin, Position boundsMax, Position spawnPos) {
        super(name, boundsMin != null ? boundsMin : spawnPos, boundsMax != null ? boundsMax : spawnPos);
        this.tier = Tier.BASE;
        
        addEntry(Resource.IRON.get(), spawnPos, this.tier.getIronCooldown(), this.tier.getIronMaxItems(), this.tier.getIronStackSize());
        addEntry(Resource.GOLD.get(), spawnPos, this.tier.getGoldCooldown(), this.tier.getGoldMaxItems(), this.tier.getGoldStackSize());
        addEntry(Resource.EMERALD.get(), spawnPos, this.tier.getEmeraldCooldown(), this.tier.getEmeraldMaxItems(), this.tier.getEmeraldStackSize());
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
