package com.thenexusreborn.bedwars;

import com.stardevllc.minecraft.Position;

public class DiamondGenerator extends BedwarsGenerator {
    
    public enum Tier {
        ONE(1, 30000, 8, 1),
        TWO(2, 20000, 8, 1),
        THREE(3, 10000, 8, 1);
        
        private final int number;
        private final long cooldown;
        private final int maxItems;
        private final int stackSize;
        
        Tier(int number, long cooldown, int maxItems, int stackSize) {
            this.number = number;
            this.cooldown = cooldown;
            this.maxItems = maxItems;
            this.stackSize = stackSize;
        }
        
        public int getNumber() {
            return number;
        }
        
        public long getCooldown() {
            return cooldown;
        }
        
        public int getMaxItems() {
            return maxItems;
        }
        
        public int getStackSize() {
            return stackSize;
        }
    }
    
    private Tier tier;
    
    public DiamondGenerator(int number, Position position) {
        super("diamond" + number, position, position);
        this.tier = Tier.ONE;
        addEntry(Resource.DIAMOND.get(), position, this.tier.getCooldown(), this.tier.getMaxItems(), this.tier.getStackSize());
    }
    
    @Override
    public boolean upgrade() {
        if (this.tier.ordinal() + 1 < Tier.values().length) {
            return upgrade(Tier.values()[this.tier.ordinal() + 1]);
        }
        
        return false;
    }
    
    @Override
    public String getTierName() {
        return this.tier.name();
    }
    
    public boolean upgrade(Tier newTier) {
        if (this.tier.ordinal() >= newTier.ordinal()) {
            return false;
        }
        
        setEntryValues(Resource.DIAMOND.get(), newTier.getCooldown(), newTier.getMaxItems(), newTier.getStackSize());
        
        this.tier = newTier;
        return true;
    }
    
    public Tier getTier() {
        return tier;
    }
}
