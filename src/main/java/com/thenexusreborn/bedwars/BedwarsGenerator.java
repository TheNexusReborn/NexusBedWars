package com.thenexusreborn.bedwars;

import com.stardevllc.minecraft.Position;
import com.stardevllc.stargenerators.model.ItemEntry;
import com.stardevllc.stargenerators.model.ItemGenerator;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.List;

public abstract class BedwarsGenerator extends ItemGenerator {
    
    protected Hologram hologram;
    
    public BedwarsGenerator(String name, Position boundsMin, Position boundsMax) {
        super(name, boundsMin, boundsMax);
    }
    
    public BedwarsGenerator(String name, Collection<ItemEntry> itemEntries, Position boundsMin, Position boundsMax) {
        super(name, itemEntries, boundsMin, boundsMax);
    }
    
    public Hologram getHologram() {
        return hologram;
    }
    
    public void updateHologram() {
        if (this.world == null) {
            if (this.hologram != null) {
                this.hologram.delete();
                this.hologram = null;
            }
            
            return;
        }
        
        if (this.hologram == null) {
            this.hologram = createHologram();
        }
        
        List<String> lines = getHologramLines();
        for (int i = 0; i < lines.size(); i++) {
            DHAPI.setHologramLine(this.hologram, i, ChatColor.translateAlternateColorCodes('&', lines.get(i)));
        }
    }
    
    protected Hologram createHologram() {
        return this.hologram;
    }
    
    protected List<String> getHologramLines() {
        return List.of();
    }
    
    public void deleteHologram() {
        if (this.hologram != null) {
            this.hologram.delete();
            this.hologram = null;
        }
    }
    
    public abstract boolean upgrade();
    
    public abstract String getTierName();
}
