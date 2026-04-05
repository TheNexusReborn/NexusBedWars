package com.thenexusreborn.bedwars;

import com.stardevllc.minecraft.Position;
import com.stardevllc.stargenerators.model.ItemEntry;
import com.stardevllc.stargenerators.model.ItemGenerator;

import java.util.Collection;

public abstract class BedwarsGenerator extends ItemGenerator {
    public BedwarsGenerator(String name, Position boundsMin, Position boundsMax) {
        super(name, boundsMin, boundsMax);
    }
    
    public BedwarsGenerator(String name, Collection<ItemEntry> itemEntries, Position boundsMin, Position boundsMax) {
        super(name, itemEntries, boundsMin, boundsMax);
    }
    
    public abstract boolean upgrade();
    
    public abstract String getTierName();
}
