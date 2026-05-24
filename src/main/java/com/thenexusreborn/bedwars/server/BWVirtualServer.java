package com.thenexusreborn.bedwars.server;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starlib.objects.key.Key;
import com.stardevllc.starlib.objects.key.Keys;
import com.stardevllc.starlib.registry.HashRegistry;
import com.stardevllc.starlib.registry.IRegistry;
import com.thenexusreborn.api.player.NexusPlayer;
import com.thenexusreborn.api.player.Rank;
import com.thenexusreborn.api.server.InstanceServer;
import com.thenexusreborn.api.server.VirtualServer;
import com.thenexusreborn.bedwars.BWPlayer;
import com.thenexusreborn.bedwars.NexusBedWarsPlugin;
import com.thenexusreborn.bedwars.arena.BedwarsArena;
import com.thenexusreborn.bedwars.game.Game;
import com.thenexusreborn.bedwars.generator.BedwarsGenerator;
import com.thenexusreborn.bedwars.map.BedwarsMap;
import com.thenexusreborn.bedwars.team.*;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;

public class BWVirtualServer extends VirtualServer {
    /* TODO
        We will have to decide how Nexus handles the network structure
        This can be how it is currently in SG where they game lobby is in the actual server and players vote for things for that specific game
        OR 
        We can have it similar to Hypixel where each game has their own dedicated lobbies and then when the players play
            The server is set to a specific map and they are teleported to a spot above the map for the pre-game and then teleported
            into their spots when the game starts
        For the experimental phase purposes, we will only be using a single map so this doesn't matter for now
     */
    
    
    private NexusBedWarsPlugin plugin;
    
    private Game game;
    private TeamMode teamMode = TeamMode.SOLO;
    
    private World world;
    
    private BedwarsArena arena;
    
    @Deprecated
    private final IRegistry<BedwarsGenerator> generators;
    
    @Deprecated
    private final Map<Key, TeamIsland> islands = new HashMap<>();
    @Deprecated
    private final Map<Key, TeamInstance> teams = new HashMap<>();
    
    private final Map<UUID, BWPlayer> bedwarsPlayers = new HashMap<>();
    
    public BWVirtualServer(NexusBedWarsPlugin plugin, InstanceServer parent, String name) {
        super(parent, name, "bedwars", 32);
        this.plugin = plugin;
        generators = HashRegistry.newBuilder(BedwarsGenerator.class)
                .appendKeyToObjectToParent()
                .withParent(NexusBedWarsPlugin.GENERATORS)
                .withKey(Keys.of(name))
                .build();
        this.arena = new BedwarsArena(plugin, this, null, "default");
    }
    
    public BWVirtualServer(NexusBedWarsPlugin plugin, String name) {
        this(plugin, null, name);
    }
    
    public IRegistry<BedwarsGenerator> getGenerators() {
        return generators;
    }
    
    public BedwarsArena getArena() {
        return arena;
    }
    
    public void setArena(BedwarsArena arena) {
        this.arena = arena;
    }
    
    @Deprecated
    public BedwarsArena createArena(BedwarsMap map, String name) {
        this.arena = new BedwarsArena(plugin, this, map, map.getName() + " Arena");
        return this.arena;
    }
    
    public TeamMode getTeamMode() {
        return teamMode;
    }
    
    public void setTeamMode(TeamMode mode) {
        this.teamMode = mode;
    }
    
    @Override
    public void join(NexusPlayer player) {
        player.setServer(this);
        
        BWPlayer bwPlayer = plugin.getPlayers().get(player.getUniqueId());
        bwPlayer.setJoinTime(System.currentTimeMillis());
        
        GameTeam team = bwPlayer.getTeam();
        if (team != null) {
            TeamInstance teamInstance = this.teams.computeIfAbsent(team.getKey(), k -> new TeamInstance(this, team));
            teamInstance.addPlayer(bwPlayer.getUniqueId());
            bwPlayer.setTeamInstance(teamInstance);
        }
        
        if (player.getRank().ordinal() <= Rank.MEDIA.ordinal()) {
            if (!player.isNicked()) {
                plugin.getNexusCore().getStaffChannel().sendMessage(new ChatContext(player.getTrueDisplayName() + " &7&l-> &6" + name.get()));
            }
        }
        
        this.players.add(player.getUniqueId());
        this.bedwarsPlayers.put(player.getUniqueId(), bwPlayer);
        
        var world = Bukkit.getWorld("world");
        var block = world.getBlockAt(0, 60, 0);
        block.setType(Material.BEDROCK);
        bwPlayer.getServerPlayer().teleport(new Location(world, 0, 61, 0));
    }
    
    public sealed interface AddToTeamResult {
        record NoBWPlayerData(Player player, BWVirtualServer server, GameTeam team) implements AddToTeamResult {}
        record AlreadyInTeam(Player player, BWPlayer bwPlayer, BWVirtualServer server, TeamInstance team) implements AddToTeamResult {}
        record Success(Player player, BWPlayer bwPlayer, BWVirtualServer server, TeamInstance team, GameTeam previousTeam) implements AddToTeamResult {}
    }
    
    public AddToTeamResult addToTeam(Player player, GameTeam team) {
        BWPlayer bwPlayer = this.bedwarsPlayers.get(player.getUniqueId());
        if (bwPlayer == null) {
            return new AddToTeamResult.NoBWPlayerData(player, this, team);
        }
        
        TeamInstance teamInstance = getTeamInstance(team);
        TeamIsland teamIsland = getTeamIsland(team);
        
        if (teamInstance.getPlayers().contains(player.getUniqueId())) {
            return new AddToTeamResult.AlreadyInTeam(player, bwPlayer, this, teamInstance);
        }
        
        GameTeam previousTeam = bwPlayer.getTeam();
        if (previousTeam != null) {
            TeamInstance prevInstance = getTeamInstance(previousTeam);
            prevInstance.removePlayer(player);
        }
        
        teamInstance.addPlayer(player);
        bwPlayer.setTeam(team);
        return new AddToTeamResult.Success(player, bwPlayer, this, teamInstance, previousTeam);
    }
    
    public sealed interface RemoveFromTeamResult {
        record NoBWPlayerData(Player player, BWVirtualServer server, GameTeam team) implements RemoveFromTeamResult {}
        record NotInTeam(Player player, BWPlayer bwPlayer, BWVirtualServer server, TeamInstance team) implements RemoveFromTeamResult {}
        record Success(Player player, BWPlayer bwPlayer, BWVirtualServer server, TeamInstance team) implements RemoveFromTeamResult {}
    }
    
    public RemoveFromTeamResult removeFromTeam(Player player, GameTeam team) {
        BWPlayer bwPlayer = this.bedwarsPlayers.get(player.getUniqueId());
        if (bwPlayer == null) {
            return new RemoveFromTeamResult.NoBWPlayerData(player, this, team);
        }
        
        TeamInstance teamInstance = getTeamInstance(team);
        
        if (!teamInstance.getPlayers().contains(player.getUniqueId())) {
            return new RemoveFromTeamResult.NotInTeam(player, bwPlayer, this, teamInstance);
        }
        
        teamInstance.removePlayer(player);
        bwPlayer.setTeam(null);
        return new RemoveFromTeamResult.Success(player, bwPlayer, this, teamInstance);
    }
    
    public TeamInstance getTeamInstance(GameTeam gameTeam) {
        if (this.teams.containsKey(gameTeam.getKey())) {
            return this.teams.get(gameTeam.getKey());
        }
        
        TeamInstance teamInstance = new TeamInstance(this, gameTeam);
        this.teams.put(gameTeam.getKey(), teamInstance);
        
        ChatRoom teamRoom = new TeamChatroom(plugin, this, gameTeam);
        teamInstance.setChatRoom(teamRoom);
        StarChat.getInstance().getRoomRegistry().register(teamRoom.getName(), teamRoom);
        return teamInstance;
    }
    
    public TeamInstance getTeamInstance(UUID uniqueId) {
        for (TeamInstance teamInstance : this.teams.values()) {
            if (teamInstance.getPlayers().contains(uniqueId)) {
                return teamInstance;
            }
        }
        
        return null;
    }
    
    public TeamInstance getTeamInstance(Player player) {
        return getTeamInstance(player.getUniqueId());
    }
    
    public TeamIsland getTeamIsland(GameTeam gameTeam) {
        if (this.islands.containsKey(gameTeam.getKey())) {
            return this.islands.get(gameTeam.getKey());
        }
        
        TeamIsland teamIsland = new TeamIsland(gameTeam);
        this.islands.put(gameTeam.getKey(), teamIsland);
        return teamIsland;
    }
    
    @Override
    public void quit(NexusPlayer player) {
        BWPlayer bwPlayer = plugin.getPlayers().get(player.getUniqueId());
        bwPlayer.setJoinTime(0);
        this.players.remove(player.getUniqueId());
    }
    
    @Override
    public void onStart() {
        
    }
    
    @Override
    public void onStop() {
        
    }
    
    public BWPlayer getBedwarsPlayer(UUID uuid) {
        return this.bedwarsPlayers.get(uuid);
    }
    
    public Game getGame() {
        return game;
    }
    
    public void setGame(Game game) {
        this.game = game;
    }
    
    public Map<Key, TeamInstance> getTeams() {
        return teams;
    }
}
