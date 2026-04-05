package com.thenexusreborn.bedwars;

import com.stardevllc.minecraft.Position;

public class EmeraldGenerator extends BedwarsGenerator {
    
    public enum Tier {
        ONE(1, 60000, 8, 1),
        TWO(2, 45000, 8, 1),
        THREE(3, 30000, 8, 1);
        
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
    
    public EmeraldGenerator(int number, Position position) {
        super("emerald" + number, position, position);
        this.tier = Tier.ONE;
        addEntry(Resource.EMERALD.get(), position, this.tier.getCooldown(), this.tier.getMaxItems(), this.tier.getStackSize());
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
        
        setEntryValues(Resource.EMERALD.get(), newTier.getCooldown(), newTier.getMaxItems(), newTier.getStackSize());
        
        this.tier = newTier;
        return true;
    }
    
    public Tier getTier() {
        return tier;
    }
}
