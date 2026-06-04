package com.thenexusreborn.bedwars;

import com.stardevllc.minecraft.smaterial.ArmorSet;
import com.thenexusreborn.api.NexusReborn;
import com.thenexusreborn.api.player.NexusPlayer;
import com.thenexusreborn.bedwars.game.GamePlayer;
import com.thenexusreborn.bedwars.team.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A class to manage things about each player that is not a per game thing
 */
public class BWPlayer {
    private final UUID uniqueId;
    private final NexusPlayer nexusPlayer;
    
    private GamePlayer gamePlayer;
    private GameTeam team;
    private TeamInstance teamInstance;
    private ArmorSet armor;
    
    private long joinTime;
    
    public BWPlayer(NexusPlayer nexusPlayer) {
        this.uniqueId = nexusPlayer.getUniqueId();
        this.nexusPlayer = nexusPlayer;
    }
    
    public BWPlayer(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.nexusPlayer = NexusReborn.getPlayerManager().getNexusPlayer(uniqueId);
    }
    
    public TeamInstance getTeamInstance() {
        return teamInstance;
    }
    
    public void setTeamInstance(TeamInstance teamInstance) {
        this.teamInstance = teamInstance;
    }
    
    public TeamIsland getIsland() {
        if (teamInstance == null) {
            return null;
        }
        return teamInstance.getIsland();
    }
    
    public NexusPlayer getNexusPlayer() {
        return nexusPlayer;
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public Player getServerPlayer() {
        return Bukkit.getPlayer(this.uniqueId);
    }
    
    public long getJoinTime() {
        return joinTime;
    }
    
    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }
    
    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }
    
    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }
    
    public GameTeam getTeam() {
        return team;
    }
    
    public void setTeam(GameTeam team) {
        this.team = team;
    }
    
    public ArmorSet getArmor() {
        return armor;
    }
    
    public void setArmor(ArmorSet armor) {
        this.armor = armor;
    }
}