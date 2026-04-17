package com.thenexusreborn.bedwars;

import com.thenexusreborn.gamemaps.model.GameMap;

public class BedwarsMap extends GameMap {
    
    /*
        Teams: 
            Player Spawn Position
            Forge Bounds and Item Spawn Position
            Color
            Item Shop Location
            Upgrade Shop Location
            Island Bounds
            Bed Location
            Bed Facing
        Emerald Gen Locations
        Diamond Gen Locations
     */
    
    public BedwarsMap(String fileName, String name) {
        super(fileName, name);
    }
}