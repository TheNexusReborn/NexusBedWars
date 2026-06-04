package com.thenexusreborn.bedwars.arena;

import com.stardevllc.minecraft.Cuboid;
import com.stardevllc.minecraft.Position;
import com.stardevllc.starlib.objects.key.Key;
import com.stardevllc.starlib.objects.key.Keys;
import com.stardevllc.starlib.registry.HashRegistry;
import com.stardevllc.starlib.registry.IRegistry;
import com.thenexusreborn.bedwars.NexusBedWarsPlugin;
import com.thenexusreborn.bedwars.generator.*;
import com.thenexusreborn.bedwars.item.Resource;
import com.thenexusreborn.bedwars.map.BedwarsMap;
import com.thenexusreborn.bedwars.map.MapTeam;
import com.thenexusreborn.bedwars.server.BWVirtualServer;
import com.thenexusreborn.bedwars.team.*;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

/**
 * This will store information much like the server for different things like teams, generators, island information
 */
public class BedwarsArena extends GameArena<NexusBedWarsPlugin, BWVirtualServer, BedwarsMap> {
    
    private Cuboid region;
    
    private final Map<GameTeam, TeamIsland> islands = new HashMap<>();
    private final Map<GameTeam, TeamInstance> teams = new HashMap<>();
    
    private final IRegistry<BedwarsGenerator> generators;
    
    private final IRegistry<IslandForge> forges;
    private final IRegistry<DiamondGenerator> diamondGenerators;
    private final IRegistry<EmeraldGenerator> emeraldGenerators;
    
    public BedwarsArena(NexusBedWarsPlugin plugin, BWVirtualServer server, BedwarsMap map, String name) {
        super(plugin, server, map);
        
        this.generators = HashRegistry.newBuilder(BedwarsGenerator.class)
                .appendKeyToObjectToParent()
                .withParent(server.getGenerators())
                .withKey(Keys.of(name))
                .build();
        
        this.forges = HashRegistry.newBuilder(IslandForge.class)
                .appendKeyToObjectToParent()
                .withParent(this.generators)
                .withKey(Keys.of("forges"))
                .build();
        
        this.diamondGenerators = HashRegistry.newBuilder(DiamondGenerator.class)
                .appendKeyToObjectToParent()
                .withParent(this.generators)
                .withKey(Keys.of("diamond"))
                .build();
        
        this.emeraldGenerators = HashRegistry.newBuilder(EmeraldGenerator.class)
                .appendKeyToObjectToParent()
                .withParent(this.generators)
                .withKey(Keys.of("emerald"))
                .build();
    }
    
    @Override
    public boolean init(World world) {
        this.world = world;
        
        this.region = new Cuboid(map.getArenaMinimum(), map.getArenaMaximum());
        
        if (map != null) {
            this.map.getTeams().forEach((color, teamInfo) -> {
                GameTeam gameTeam = GameTeam.get(color);
                TeamIsland teamIsland = new TeamIsland(teamInfo);
                if (teamIsland.init(world)) {
                    islands.put(gameTeam, teamIsland);
                }
                
                TeamInstance teamInstance = new TeamInstance(server, gameTeam);
                teamInstance.setIsland(teamIsland);
                teams.put(gameTeam, teamInstance);
            });
            
            int dgenIndex = 0;
            for (Position dgen : this.map.getDiamondGenerators()) {
                DiamondGenerator diamondGenerator = new DiamondGenerator(dgenIndex, dgen);
                diamondGenerators.register(Keys.of(dgenIndex), diamondGenerator);
                dgenIndex++;
            }
            
            int egenIndex = 0;
            for (Position dgen : this.map.getEmeraldGenerators()) {
                EmeraldGenerator emeraldGenerator = new EmeraldGenerator(egenIndex, dgen);
                emeraldGenerators.register(Keys.of(egenIndex), emeraldGenerator);
                egenIndex++;
            }
        }
        
        this.islands.forEach((team, island) -> forges.register(team.getTeamColor().name().toLowerCase(), island.getForge()));
        
        this.generators.forEach(gen -> gen.init(world));
        
        this.teams.forEach((team, instance) -> instance.setChatRoom(new TeamChatroom(plugin, server, team, this)));
        
        super.registerListeners();
        return true;
    }
    
    @Override
    public boolean deinit() {
        this.world = null;
        
        Iterator<Map.Entry<Key, IslandForge>> forgesIterator = this.forges.entrySet().iterator();
        while (forgesIterator.hasNext()) {
            Map.Entry<Key, IslandForge> entry = forgesIterator.next();
            Key key = entry.getKey();
            IslandForge forge = entry.getValue();
            
            forge.stop();
            forgesIterator.remove();
        }
        
        Iterator<Map.Entry<Key, DiamondGenerator>> diamondIterator = this.diamondGenerators.entrySet().iterator();
        while (diamondIterator.hasNext()) {
            Map.Entry<Key, DiamondGenerator> entry = diamondIterator.next();
            Key key = entry.getKey();
            DiamondGenerator generator = entry.getValue();
            
            generator.stop();
            diamondIterator.remove();
        }
        
        Iterator<Map.Entry<Key, EmeraldGenerator>> emeraldIterator = this.emeraldGenerators.entrySet().iterator();
        while (emeraldIterator.hasNext()) {
            Map.Entry<Key, EmeraldGenerator> entry = emeraldIterator.next();
            Key key = entry.getKey();
            EmeraldGenerator generator = entry.getValue();
            
            generator.stop();
            emeraldIterator.remove();
        }
        
        this.teams.forEach((team, instance) -> {
            for (UUID uuid : instance.getPlayers()) {
                instance.removePlayer(uuid);
            }
        });
        this.teams.clear();
        
        this.islands.forEach((team, island) -> {
            island.getItemShopEntity().remove();
            island.getUpgradeShopEntity().remove();
        });
        this.islands.clear();
        
        super.unregisterListeners();
        return this.forges.isEmpty() && this.diamondGenerators.isEmpty() && this.emeraldGenerators.isEmpty() && this.teams.isEmpty() && this.islands.isEmpty();
    }
    
    public TeamInstance getTeamInstance(GameTeam gameTeam) {
        return teams.get(gameTeam);
    }
    
    public TeamIsland getTeamIsland(GameTeam gameTeam) {
        return islands.get(gameTeam);
    }
    
    @Override
    public void setMap(BedwarsMap map) {
        if (isInitialized()) {
            map.setArenaMinimum(this.region.getMinimum());
            map.setArenaMaximum(this.region.getMaximum());
            
            Set<GameTeam> teams = new HashSet<>(this.teams.keySet());
            teams.addAll(this.islands.keySet());
            
            for (GameTeam team : teams) {
                MapTeam mapTeam = new MapTeam(team.getTeamColor());
                TeamIsland island = this.islands.get(team);
                if (island != null) {
                    mapTeam.setSpawnpoint(island.getTeamInfo().getSpawnpoint());
                    mapTeam.setForgeSpawn(island.getTeamInfo().getForgeSpawn());
                    mapTeam.setForgeMin(island.getTeamInfo().getForgeMin());
                    mapTeam.setForgeMax(island.getTeamInfo().getForgeMax());
                    mapTeam.setIslandMin(island.getTeamInfo().getIslandMin());
                    mapTeam.setIslandMax(island.getTeamInfo().getIslandMax());
                    mapTeam.setItemShopPosition(island.getTeamInfo().getItemShopPosition());
                    mapTeam.setUpgradeShopPosition(island.getTeamInfo().getUpgradeShopPosition());
                }
                
                map.addTeam(mapTeam);
            }
            
            for (DiamondGenerator diamondGenerator : this.diamondGenerators) {
                map.addDiamondGenerator(diamondGenerator.getSpawnPosition(Resource.DIAMOND.get()));
            }
            
            for (EmeraldGenerator emeraldGenerator : this.emeraldGenerators) {
                map.addEmeraldGenerator(emeraldGenerator.getSpawnPosition(Resource.EMERALD.get()));
            }
        }
    }
    
    @Override
    public boolean contains(Location location) {
        if (this.region != null) {
            return this.region.contains(location);
        }
        
        return false;
    }
    
    public void addIsland(TeamIsland island) {
        GameTeam team = GameTeam.get(island.getTeamInfo().getTeamColor());
        this.islands.put(team, island);
    }
}