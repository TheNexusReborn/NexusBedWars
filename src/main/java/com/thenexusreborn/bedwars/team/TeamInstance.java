package com.thenexusreborn.bedwars.team;

import com.stardevllc.minecraft.Cuboid;
import com.stardevllc.minecraft.smaterial.ArmorSlot;
import com.stardevllc.minecraft.v1_8.LeatherArmorBuilder;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starchat.rooms.DefaultPermissions;
import com.stardevllc.starlib.objects.key.Key;
import com.thenexusreborn.bedwars.generator.IslandForge;
import com.thenexusreborn.bedwars.server.BWVirtualServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Holds game-specific information about a team
 */
public class TeamInstance {
    private final BWVirtualServer server;
    private final GameTeam team;
    private final Set<UUID> players = new HashSet<>();
    private TeamIsland island;
    private Location spawnPoint;
    private ChatRoom chatRoom;
    private boolean hasBed;
    private final Map<Key, Integer> upgrades = new HashMap<>();
    private Cuboid region;
    private IslandForge forge;
    private UUID itemShopEntityId, upgradeShopEntityId;
    
    public TeamInstance(BWVirtualServer server, GameTeam team) {
        this.server = server;
        this.team = team;
    }
    
    public TeamIsland getIsland() {
        return island;
    }
    
    public void setIsland(TeamIsland island) {
        this.island = island;
    }
    
    public ItemStack[] getArmor() {
        ItemStack[] armor = new ItemStack[4];
        LeatherArmorBuilder armorBuilder = new LeatherArmorBuilder();
        
        armor[0] = armorBuilder.slot(ArmorSlot.BOOTS).color(team.getDyeColor()).build();
        armor[1] = armorBuilder.slot(ArmorSlot.LEGGINGS).color(team.getDyeColor()).build();
        armor[2] = armorBuilder.slot(ArmorSlot.CHESTPLATE).color(team.getDyeColor()).build();
        armor[3] = armorBuilder.slot(ArmorSlot.HELMET).color(team.getDyeColor()).build();
        
        return armor;
    }
    
    public BWVirtualServer getServer() {
        return server;
    }
    
    @Deprecated(forRemoval = true)
    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
    }
    
    public void upgrade(Key key) {
        upgrade(TeamUpgrade.REGISTRY.get(key));
    }
    
    public TeamUpgrade.Level getCurrentUpgradeLevel(Key upgradeKey) {
        return getCurrentUpgradeLevel(TeamUpgrade.REGISTRY.get(upgradeKey));
    }
    
    public TeamUpgrade.Level getCurrentUpgradeLevel(TeamUpgrade upgrade) {
        if (upgrade == null) {
            return null;
        }
        
        if (!this.upgrades.containsKey(upgrade.getKey())) {
            return null;
        }
        
        return upgrade.getLevel(this.upgrades.get(upgrade.getKey()));
    }
    
    public boolean canUpgrade(TeamUpgrade teamUpgrade) {
        if (teamUpgrade == null) {
            return false;
        }
        
        int currentLevel = this.upgrades.getOrDefault(teamUpgrade.getKey(), 0);
        TeamUpgrade.Level nextLevel = teamUpgrade.getNextLevel(currentLevel);
        return nextLevel != null;
    }
    
    public boolean setLevel(TeamUpgrade teamUpgrade, TeamUpgrade.Level level) {
        this.upgrades.put(teamUpgrade.getKey(), level.getNumber());
        
        if (level.getTeamConsumer() != null) {
            level.getTeamConsumer().accept(this);
        }
        
        if (level.getPlayerConsumer() != null) {
            for (UUID uuid : this.players) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) {
                    continue;
                }
                
                level.getPlayerConsumer().accept(player);
            }
        }
        
        return true;
    }
    
    public boolean upgrade(TeamUpgrade teamUpgrade) {
        if (teamUpgrade == null) {
            return false;
        }
        
        int currentLevel = this.upgrades.getOrDefault(teamUpgrade.getKey(), 0);
        TeamUpgrade.Level nextLevel = teamUpgrade.getNextLevel(currentLevel);
        if (nextLevel == null) {
            return false;
        }
        
        return setLevel(teamUpgrade, nextLevel);
    }
    
    public void setChatRoom(ChatRoom chatRoom) {
        if (this.chatRoom != null) {
            for (UUID uuid : this.players) {
                this.chatRoom.removeMember(uuid);
            }
            
            StarChat.getInstance().getRoomRegistry().remove(this.chatRoom.getName());
        }
        
        this.chatRoom = chatRoom;
        
        if (this.chatRoom != null) {
            for (UUID uuid : this.players) {
                this.chatRoom.addMember(uuid, DefaultPermissions.VIEW_MESSAGES, DefaultPermissions.SEND_MESSAGES);
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    StarChat.getInstance().setPlayerFocus(player, this.chatRoom);
                }
            }
        }
    }
    
    public void setHasBed(boolean hasBed) {
        this.hasBed = hasBed;
    }
    
    @Deprecated(forRemoval = true)
    public void setRegion(Cuboid region) {
        this.region = region;
    }
    
    @Deprecated(forRemoval = true)
    public void setForge(IslandForge forge) {
        this.forge = forge;
    }
    
    public GameTeam getTeam() {
        return team;
    }
    
    public Set<UUID> getPlayers() {
        return new HashSet<>(players);
    }
    
    public void addPlayer(Player player) {
        addPlayer(player.getUniqueId());
    }
    
    public void removePlayer(Player player) {
        removePlayer(player.getUniqueId());
    }
    
    public ChatRoom getChatRoom() {
        return chatRoom;
    }
    
    public boolean hasBed() {
        return hasBed;
    }
    
    public Map<Key, Integer> getUpgrades() {
        return upgrades;
    }
    
    public Cuboid getRegion() {
        return region;
    }
    
    @Deprecated(forRemoval = true)
    public IslandForge getForge() {
        return forge;
    }
    
    public void addPlayer(UUID uniqueId) {
        this.players.add(uniqueId);
        if (this.chatRoom != null) {
            this.chatRoom.addMember(uniqueId, DefaultPermissions.SEND_MESSAGES, DefaultPermissions.VIEW_MESSAGES);
            Player player = Bukkit.getPlayer(uniqueId);
            if (player != null) {
                StarChat.getInstance().setPlayerFocus(player, this.chatRoom);
            }
        }
    }
    
    public void removePlayer(UUID uniqueId) {
        this.players.remove(uniqueId);
        if (this.chatRoom != null) {
            this.chatRoom.removeMember(uniqueId);
            Player player = Bukkit.getPlayer(uniqueId);
            if (player != null) {
                StarChat.getInstance().setPlayerFocus(player, null);
            }
        }
    }
    
    public void applyUpgrades(Player player) {
        for (TeamUpgrade teamUpgrade : TeamUpgrade.REGISTRY) {
            int levelNumber = this.upgrades.getOrDefault(teamUpgrade.getKey(), 0);
            TeamUpgrade.Level level = teamUpgrade.getLevel(levelNumber);
            if (level == null) {
                continue;
            }
            
            if (level.getPlayerConsumer() != null) {
                level.getPlayerConsumer().accept(player);
            }
        }
    }
}