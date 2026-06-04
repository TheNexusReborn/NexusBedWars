package com.thenexusreborn.bedwars.team;

import com.stardevllc.minecraft.*;
import com.thenexusreborn.bedwars.generator.IslandForge;
import com.thenexusreborn.bedwars.item.Resource;
import com.thenexusreborn.bedwars.map.MapTeam;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * This class maps from a MapTeam to the actual values of an island
 */
public class TeamIsland {
    
    private final MapTeam teamInfo;
    
    private World world;
    
    private Location spawnpoint;
    private LivingEntity itemShopEntity, upgradeShopEntity;
    private Cuboid region;
    private IslandForge forge;
    
    public TeamIsland(GameTeam team) {
        this(new MapTeam(team.getTeamColor()));
    }
    
    public TeamIsland(GameTeam.TeamColor color) {
        this(new MapTeam(color));
    }
    
    public TeamIsland(MapTeam teamInfo) {
        this.teamInfo = teamInfo;
    }
    
    public MapTeam getTeamInfo() {
        return teamInfo;
    }
    
    public boolean init(World world) {
        if (world == null) {
            return false;
        }
        
        this.spawnpoint = new Location(world, teamInfo.getSpawnpoint().getBlockX() + 0.5, teamInfo.getSpawnpoint().getBlockY() + 1, teamInfo.getSpawnpoint().getBlockZ() + 0.5);
        
        initItemShop(world);
        initUpgradeShop(world);
        
        this.forge = new IslandForge(teamInfo.getForgeMin(), teamInfo.getForgeMax(), teamInfo.getForgeSpawn());
        this.forge.init(world);
        
        this.region = new Cuboid(this.teamInfo.getIslandMin(), this.teamInfo.getIslandMax());
        
        this.world = world;
        return true;
    }
    
    public boolean isInitialized() {
        return world != null;
    }
    
    public Location getSpawnpoint() {
        return spawnpoint;
    }
    
    public void setSpawnpoint(Location spawnpoint) {
        this.spawnpoint = spawnpoint;
        this.teamInfo.setSpawnpoint(new Position(spawnpoint.getBlockX(), spawnpoint.getBlockY(), spawnpoint.getBlockZ()));
    }
    
    public void setSpawnpoint(Position position) {
        this.teamInfo.setSpawnpoint(position);
    }
    
    public LivingEntity getItemShopEntity() {
        return itemShopEntity;
    }
    
    public void setItemShopEntity(LivingEntity itemShopEntity) {
        this.itemShopEntity = itemShopEntity;
        Location location = itemShopEntity.getLocation();
        this.teamInfo.setItemShopPosition(new Position(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
    
    public void setItemShopPosition(Position position) {
        this.teamInfo.setItemShopPosition(position);
    }
    
    public LivingEntity getUpgradeShopEntity() {
        return upgradeShopEntity;
    }
    
    public void setUpgradeShopEntity(LivingEntity upgradeShopEntity) {
        this.upgradeShopEntity = upgradeShopEntity;
        Location location = upgradeShopEntity.getLocation();
        this.teamInfo.setItemShopPosition(new Position(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
    
    public void setUpgradeShopPosition(Position position) {
        this.teamInfo.setUpgradeShopPosition(position);
    }
    
    public Cuboid getRegion() {
        return region;
    }
    
    public void setRegion(Cuboid region) {
        this.region = region;
        this.teamInfo.setIslandMin(new Position(region.getXMin(), region.getYMin(), region.getZMin()));
        this.teamInfo.setIslandMax(new Position(region.getXMax(), region.getYMax(), region.getZMax()));
    }
    
    public IslandForge getForge() {
        return forge;
    }
    
    public void setForge(IslandForge forge) {
        this.forge = forge;
        this.teamInfo.setForgeMin(forge.getBoundsMin());
        this.teamInfo.setForgeMax(forge.getBoundsMax());
        this.teamInfo.setForgeSpawn(forge.getSpawnPosition(Resource.IRON.get()));
    }
    
    public void setForgeBounds(Position min, Position max) {
        this.teamInfo.setForgeMin(min);
        this.teamInfo.setForgeMax(max);
    }
    
    public void setForgeSpawn(Position position) {
        this.teamInfo.setForgeSpawn(position);
    }
    
    private void initItemShop(World world) {
        if (this.itemShopEntity != null) {
            this.itemShopEntity.remove();
            this.itemShopEntity = null;
        }
        
        if (world == null) {
            return;
        }
        
        this.itemShopEntity = (Villager) world.spawnEntity(
                new Location(world,
                        teamInfo.getItemShopPosition().getBlockX() + 0.5,
                        teamInfo.getItemShopPosition().getBlockY() + 1,
                        teamInfo.getItemShopPosition().getBlockZ() + 0.5),
                EntityType.VILLAGER);
        
        if (this.itemShopEntity == null) {
            return;
        }
        
        this.itemShopEntity.setCustomName(StarColors.color("&e&lITEM SHOP"));
        this.itemShopEntity.setCustomNameVisible(true);
        this.itemShopEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, Integer.MAX_VALUE, true, false));
    }
    
    private void initUpgradeShop(World world) {
        if (this.upgradeShopEntity != null) {
            this.upgradeShopEntity.remove();
            this.upgradeShopEntity = null;
        }
        
        if (world == null) {
            return;
        }
        
        this.upgradeShopEntity = (Villager) world.spawnEntity(
                new Location(world,
                        teamInfo.getUpgradeShopPosition().getBlockX() + 0.5,
                        teamInfo.getUpgradeShopPosition().getBlockY() + 1,
                        teamInfo.getUpgradeShopPosition().getBlockZ() + 0.5),
                EntityType.VILLAGER);
        
        if (this.upgradeShopEntity == null) {
            return;
        }
        
        this.upgradeShopEntity.setCustomName(StarColors.color("&b&lUPGRADE SHOP"));
        this.upgradeShopEntity.setCustomNameVisible(true);
        this.upgradeShopEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, Integer.MAX_VALUE, true, false));
    }
}