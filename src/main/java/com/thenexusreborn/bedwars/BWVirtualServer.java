package com.thenexusreborn.bedwars;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starlib.objects.key.Key;
import com.thenexusreborn.api.player.NexusPlayer;
import com.thenexusreborn.api.player.Rank;
import com.thenexusreborn.api.server.InstanceServer;
import com.thenexusreborn.api.server.VirtualServer;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;

public class BWVirtualServer extends VirtualServer {
    
    private NexusBedWarsPlugin plugin;
    
    private Game game;
    private TeamMode teamMode = TeamMode.SOLO;
    
    private final Map<Key, TeamInstance> teams = new HashMap<>();
    
    private final Map<UUID, BWPlayer> bedwarsPlayers = new HashMap<>();
    
    public BWVirtualServer(NexusBedWarsPlugin plugin, InstanceServer parent, String name) {
        super(parent, name, "bedwars", 32);
        this.plugin = plugin;
    }
    
    public BWVirtualServer(NexusBedWarsPlugin plugin, String name) {
        super(name, "bedwars", 32);
        this.plugin = plugin;
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
        
        ChatRoom teamRoom = new GameTeamChatroom(plugin, this, gameTeam);
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
