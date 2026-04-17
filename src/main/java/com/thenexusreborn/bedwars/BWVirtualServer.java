package com.thenexusreborn.bedwars;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starlib.objects.key.Key;
import com.thenexusreborn.api.player.NexusPlayer;
import com.thenexusreborn.api.player.Rank;
import com.thenexusreborn.api.server.InstanceServer;
import com.thenexusreborn.api.server.VirtualServer;
import org.bukkit.entity.Player;

import java.util.*;

public class BWVirtualServer extends VirtualServer {
    
    private NexusBedWarsPlugin plugin;
    
    private Game game;
    
    private final Map<Key, TeamInstance> teams = new HashMap<>();
    private TeamMode teamMode = TeamMode.SOLO;
    
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
            TeamInstance teamInstance = this.teams.computeIfAbsent(team.getKey(), k -> new TeamInstance(team));
            teamInstance.addPlayer(bwPlayer.getUniqueId());
        }
        
        if (player.getRank().ordinal() <= Rank.MEDIA.ordinal()) {
            if (!player.isNicked()) {
                plugin.getNexusCore().getStaffChannel().sendMessage(new ChatContext(player.getTrueDisplayName() + " &7&l-> &6" + name.get()));
            }
        }
        
        this.players.add(player.getUniqueId());
    }
    
    public TeamInstance getTeamInstance(GameTeam gameTeam) {
        if (this.teams.containsKey(gameTeam.getKey())) {
            return this.teams.get(gameTeam.getKey());
        }
        
        TeamInstance teamInstance = new TeamInstance(gameTeam);
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
