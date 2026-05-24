package com.thenexusreborn.bedwars.map;

import com.stardevllc.minecraft.Position;
import com.thenexusreborn.bedwars.team.GameTeam.TeamColor;

public class MapTeam {
    private final TeamColor teamColor;
    private Position spawnpoint;
    private Position itemShopPosition;
    private Position upgradeShopPosition;
    
    private Position forgeMin, forgeMax, forgeSpawn;
    private Position islandMin, islandMax;
    
    public MapTeam(TeamColor teamColor) {
        this.teamColor = teamColor;
    }
    
    public TeamColor getTeamColor() {
        return teamColor;
    }
    
    public void setSpawnpoint(Position spawnpoint) {
        this.spawnpoint = spawnpoint;
    }
    
    public void setItemShopPosition(Position itemShopPosition) {
        this.itemShopPosition = itemShopPosition;
    }
    
    public void setUpgradeShopPosition(Position upgradeShopPosition) {
        this.upgradeShopPosition = upgradeShopPosition;
    }
    
    public void setForgeMin(Position forgeMin) {
        this.forgeMin = forgeMin;
    }
    
    public void setForgeMax(Position forgeMax) {
        this.forgeMax = forgeMax;
    }
    
    public void setForgeSpawn(Position forgeSpawn) {
        this.forgeSpawn = forgeSpawn;
    }
    
    public void setIslandMin(Position islandMin) {
        this.islandMin = islandMin;
    }
    
    public void setIslandMax(Position islandMax) {
        this.islandMax = islandMax;
    }
    
    public Position getSpawnpoint() {
        return spawnpoint;
    }
    
    public Position getItemShopPosition() {
        return itemShopPosition;
    }
    
    public Position getUpgradeShopPosition() {
        return upgradeShopPosition;
    }
    
    public Position getForgeMin() {
        return forgeMin;
    }
    
    public Position getForgeMax() {
        return forgeMax;
    }
    
    public Position getForgeSpawn() {
        return forgeSpawn;
    }
    
    public Position getIslandMin() {
        return islandMin;
    }
    
    public Position getIslandMax() {
        return islandMax;
    }
}