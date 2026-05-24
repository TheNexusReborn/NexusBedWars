package com.thenexusreborn.bedwars.map;

import com.stardevllc.minecraft.Position;
import com.thenexusreborn.api.sql.annotations.column.ColumnIgnored;
import com.thenexusreborn.bedwars.team.GameTeam.TeamColor;
import com.thenexusreborn.gamemaps.model.GameMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class BedwarsMap extends GameMap {
    
    @ColumnIgnored
    private final Map<TeamColor, MapTeam> teams = new EnumMap<>(TeamColor.class);
    @ColumnIgnored
    private final Set<Position> diamondGenerators = new HashSet<>();
    @ColumnIgnored
    private final Set<Position> emeraldGenerators = new HashSet<>();
    
    public BedwarsMap(String url, String name) {
        super(url, name);
    }
    
    public BedwarsMap(String name) {
        super(name);
    }
    
    public BedwarsMap() {}
    
    public void setTeam(TeamColor color, MapTeam team) {
        this.teams.put(color, team);
    }
    
    public void addTeam(MapTeam team) {
        setTeam(team.getTeamColor(), team);
    }
    
    public Map<TeamColor, MapTeam> getTeams() {
        return teams;
    }
    
    public void addDiamondGenerator(Position diamondGenerator) {
        this.diamondGenerators.add(diamondGenerator);
    }
    
    public Set<Position> getDiamondGenerators() {
        return diamondGenerators;
    }
    
    public void addEmeraldGenerator(Position emeraldGenerator) {
        this.emeraldGenerators.add(emeraldGenerator);
    }
    
    public Set<Position> getEmeraldGenerators() {
        return emeraldGenerators;
    }
    
    public static BedwarsMap loadFromYaml(FileConfiguration config) {
        BedwarsMap gameMap = GameMap.loadFromYaml(new BedwarsMap(), config);
        ConfigurationSection teamsSection = config.getConfigurationSection("teams");
        if (teamsSection != null) {
            for (String color : teamsSection.getKeys(false)) {
                MapTeam team = new MapTeam(TeamColor.valueOf(color.toUpperCase()));
                team.setSpawnpoint((Position) teamsSection.get(color + ".spawnpoint"));
                team.setItemShopPosition((Position) teamsSection.get(color + ".shop.item"));
                team.setUpgradeShopPosition((Position) teamsSection.get(color + ".shop.upgrade"));
                team.setForgeMin((Position) teamsSection.get(color + ".forge.min"));
                team.setForgeMax((Position) teamsSection.get(color + ".forge.max"));
                team.setForgeSpawn((Position) teamsSection.get(color + ".forge.spawn"));
                team.setIslandMin((Position) teamsSection.get(color + ".island.min"));
                team.setIslandMax((Position) teamsSection.get(color + ".island.max"));
                gameMap.setTeam(team.getTeamColor(), team);
            }
        }
        
        ConfigurationSection diamondSection = config.getConfigurationSection("generators.diamond");
        if (diamondSection != null) {
            for (String key : diamondSection.getKeys(false)) {
                gameMap.addDiamondGenerator((Position) diamondSection.get(key));
            }
        }
        
        ConfigurationSection emeraldSection = config.getConfigurationSection("generators.emerald");
        if (emeraldSection != null) {
            for (String key : emeraldSection.getKeys(false)) {
                gameMap.addEmeraldGenerator((Position) emeraldSection.get(key));
            }
        }
        
        return gameMap;
    }
    
    @Override
    public void saveToYaml(FileConfiguration config) {
        super.saveToYaml(config);
        teams.forEach((color, team) -> {
            config.set("teams." + color.name().toLowerCase() + ".spawnpoint", team.getSpawnpoint());
            config.set("teams." + color.name().toLowerCase() + ".shop.item", team.getItemShopPosition());
            config.set("teams." + color.name().toLowerCase() + ".shop.upgrade", team.getUpgradeShopPosition());
            config.set("teams." + color.name().toLowerCase() + ".forge.min", team.getForgeMin());
            config.set("teams." + color.name().toLowerCase() + ".forge.max", team.getForgeMax());
            config.set("teams." + color.name().toLowerCase() + ".forge.spawn", team.getForgeSpawn());
            config.set("teams." + color.name().toLowerCase() + ".island.min", team.getIslandMin());
            config.set("teams." + color.name().toLowerCase() + ".island.min", team.getIslandMax());
        });
        
        int diamondIndex = 0;
        for (Position dgen : this.diamondGenerators) {
            config.set("generators.diamond." + diamondIndex++, dgen);
        }
        
        int emeraldIndex = 0;
        for (Position egen : this.emeraldGenerators) {
            config.set("generators.emerald." + emeraldIndex++, egen);
        }
    }
    
    @Override
    public void copyFrom(GameMap other) {
        if (!(other instanceof BedwarsMap otherMap)) {
            return;
        }
        
        this.teams.putAll(otherMap.teams);
        this.diamondGenerators.addAll(otherMap.diamondGenerators);
        this.emeraldGenerators.addAll(otherMap.emeraldGenerators);
        
        super.copyFrom(other);
    }
}