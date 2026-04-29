package com.thenexusreborn.bedwars;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("ClassCanBeRecord")
public class GamePlayer {
    private final UUID uniqueId;
    private final Game game;
    
    private final TeamInstance team; //It's fine to store this as the GamePlayer class is not kept between games
    
    public GamePlayer(UUID uniqueId, Game game, TeamInstance team) {
        this.uniqueId = uniqueId;
        this.game = game;
        this.team = team;
    }
    
    public TeamInstance getTeam() {
        return team;
    }
    
    private void safePlayer(Consumer<Player> consumer ) {
        Player player = Bukkit.getPlayer(uniqueId);
        if (player != null) {
            consumer.accept(player);
        }
    }
    
    public Game getGame() {
        return game;
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public PlayerInventory getInventory() {
        return Bukkit.getPlayer(uniqueId).getInventory();
    }
    
    public void addPotionEffect(PotionEffect potionEffect) {
        Bukkit.getPlayer(uniqueId).addPotionEffect(potionEffect);
    }
}