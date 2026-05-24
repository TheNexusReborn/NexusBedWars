package com.thenexusreborn.bedwars.map;

import com.thenexusreborn.gamemaps.MapManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class YamlMapManager extends MapManager<BedwarsMap> {
    
    private File configsFolder;
    private long lastId = 1;
    
    public YamlMapManager(JavaPlugin plugin) {
        super(plugin);
        configsFolder = new File(plugin.getDataFolder(), "mapconfigs");
        if (!configsFolder.exists()) {
            configsFolder.mkdirs();
        }
    }

    public long getLastId() {
        return lastId;
    }

    public void setLastId(long lastId) {
        this.lastId = lastId;
    }

    @Override
    public void loadMaps() {
        for (File file : configsFolder.listFiles()) {
            if (file.getName().contains(".yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                BedwarsMap bwMap = BedwarsMap.loadFromYaml(config);
                lastId = Math.max(this.lastId, bwMap.getId());
                gameMaps.add(bwMap);
            }
        }
    }

    @Override
    public void saveMaps() {
        for (BedwarsMap gameMap : gameMaps) {
            saveMap(gameMap);
        }
    }
    
    public void deleteMap(BedwarsMap map) {
        File configFile = new File(configsFolder, normalizeFunction.apply(map.getName())+ ".yml");
        if (!configFile.exists()) {
            return;
        }
        
        configFile.delete();
        this.getMaps().remove(map);
    }

    @Override
    public void saveMap(BedwarsMap gameMap) {
        File configFile = new File(configsFolder, normalizeFunction.apply(gameMap.getName()) + ".yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        
        if (gameMap.getId() == 0) {
            gameMap.setId(++this.lastId);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        gameMap.saveToYaml(config);

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BedwarsMap loadMap(String name) {
        String mapName = normalizeFunction.apply(name);
        File configFile = new File(configsFolder, mapName + ".yml");
        if (!configFile.exists()) {
            return null;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        BedwarsMap bwMap = BedwarsMap.loadFromYaml(config);
        if (this.lastId < bwMap.getId()) {
            this.lastId = bwMap.getId();
        }
        return bwMap;
    }
}