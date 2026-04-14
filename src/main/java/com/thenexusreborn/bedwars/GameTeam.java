package com.thenexusreborn.bedwars;

import com.stardevllc.minecraft.smaterial.SMaterial;
import com.stardevllc.starlib.objects.key.*;
import com.stardevllc.starlib.registry.*;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GameTeam implements Keyable {
    
    public enum TeamColor {
        RED(Color.RED, "&c", SMaterial.RED_WOOL, SMaterial.RED_TERRACOTTA, SMaterial.RED_STAINED_GLASS), 
        BLUE(Color.BLUE, "&9", SMaterial.BLUE_WOOL, SMaterial.BLUE_TERRACOTTA, SMaterial.BLUE_STAINED_GLASS), 
        GREEN(Color.GREEN, "&a", SMaterial.LIME_WOOL, SMaterial.LIME_TERRACOTTA, SMaterial.LIME_STAINED_GLASS), 
        YELLOW(Color.YELLOW, "&e", SMaterial.YELLOW_WOOL, SMaterial.YELLOW_TERRACOTTA, SMaterial.YELLOW_STAINED_GLASS), 
        AQUA(Color.AQUA, "&b", SMaterial.LIGHT_BLUE_WOOL, SMaterial.LIGHT_BLUE_TERRACOTTA, SMaterial.LIGHT_BLUE_STAINED_GLASS), 
        PINK(Color.FUCHSIA, "&d", SMaterial.PINK_WOOL, SMaterial.PINK_TERRACOTTA, SMaterial.PINK_STAINED_GLASS), 
        ORANGE(Color.ORANGE, "&6", SMaterial.ORANGE_WOOL, SMaterial.ORANGE_TERRACOTTA, SMaterial.ORANGE_STAINED_GLASS), 
        WHITE(Color.WHITE, "&5", SMaterial.WHITE_WOOL, SMaterial.WHITE_TERRACOTTA, SMaterial.WHITE_STAINED_GLASS), 
        SPECATATOR(Color.GRAY, "&7", null, null, null);
        
        private final Color dyeColor;
        private final String chatColor;
        private final SMaterial wool;
        private final SMaterial clay;
        private final SMaterial glass;
        
        TeamColor(Color dyeColor, String chatColor, SMaterial wool, SMaterial clay, SMaterial glass) {
            this.dyeColor = dyeColor;
            this.chatColor = chatColor;
            this.wool = wool;
            this.clay = clay;
            this.glass = glass;
        }
        
        public SMaterial getWool() {
            return wool;
        }
        
        public SMaterial getClay() {
            return clay;
        }
        
        public SMaterial getGlass() {
            return glass;
        }
        
        public Color getDyeColor() {
            return dyeColor;
        }
        
        public String getChatColor() {
            return chatColor;
        }
    }
    
    public static final IRegistry<GameTeam> REGISTRY = HashRegistry.newBuilder(GameTeam.class)
            .checkPartialInGet()
            .allowFreezing()
            .withName("Teams")
            .withKey(Keys.of("teams"))
            .build();
    
    private static final Registerer<GameTeam> REGISTERER = Registerer.create(REGISTRY);
    
    public static final RegistryObject<GameTeam> RED = REGISTERER.register("red", new GameTeam(GameTeam.TeamColor.RED));
    public static final RegistryObject<GameTeam> BLUE = REGISTERER.register("blue", new GameTeam(GameTeam.TeamColor.BLUE));
    public static final RegistryObject<GameTeam> GREEN = REGISTERER.register("green", new GameTeam(GameTeam.TeamColor.GREEN));
    public static final RegistryObject<GameTeam> YELLOW = REGISTERER.register("yellow", new GameTeam(GameTeam.TeamColor.YELLOW));
    public static final RegistryObject<GameTeam> AQUA = REGISTERER.register("aqua", new GameTeam(GameTeam.TeamColor.AQUA));
    public static final RegistryObject<GameTeam> PINK = REGISTERER.register("pink", new GameTeam(GameTeam.TeamColor.PINK));
    public static final RegistryObject<GameTeam> ORANGE = REGISTERER.register("orange", new GameTeam(GameTeam.TeamColor.ORANGE));
    public static final RegistryObject<GameTeam> WHITE = REGISTERER.register("white", new GameTeam(GameTeam.TeamColor.WHITE));
    public static final RegistryObject<GameTeam> SPECATORS = REGISTERER.register("spectators", new GameTeam(TeamColor.SPECATATOR));
    
    static {
        REGISTRY.freeze();
    }
    
    public static GameTeam get(String string) {
        return REGISTRY.get(string);
    }
    
    public static GameTeam get(GameTeam.TeamColor color) {
        for (GameTeam gameTeam : REGISTRY) {
            if (gameTeam.getTeamColor() == color) {
                return gameTeam;
            }
        }
        
        return null;
    }
    
    private Key key;
    private final TeamColor teamColor;
    private final String name;
    
    public GameTeam(TeamColor color) {
        this.teamColor = color;
        this.name = color.name();
    }
    
    public TeamColor getTeamColor() {
        return teamColor;
    }
    
    public Color getDyeColor() {
        return this.teamColor.getDyeColor();
    }
    
    public String getChatColor() {
        return this.teamColor.getChatColor();
    }
    
    public String getName() {
        return name;
    }
    
    public boolean giveWool(Player player, int amount) {
        if (getWoolMaterial() != null) {
            ItemStack itemStack = getWoolMaterial().parseItem();
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                itemStack.setAmount(16);
                player.getInventory().addItem(itemStack);
                return true;
            }
        }
        
        return false;
    }
    
    public SMaterial getWoolMaterial() {
        return teamColor.getWool();
    }
    
    public boolean giveClay(Player player, int amount) {
        if (getClayMaterial() != null) {
            ItemStack itemStack = getClayMaterial().parseItem();
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                itemStack.setAmount(16);
                player.getInventory().addItem(itemStack);
                return true;
            }
        }
        
        return false;
    }
    
    public SMaterial getClayMaterial() {
        return teamColor.getClay();
    }
    
    public boolean giveGlass(Player player, int amount) {
        if (getGlassMaterial() != null) {
            ItemStack itemStack = getGlassMaterial().parseItem();
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                itemStack.setAmount(16);
                player.getInventory().addItem(itemStack);
                return true;
            }
        }
        
        return false;
    }
    
    public SMaterial getGlassMaterial() {
        return teamColor.getGlass();
    }
    
    @Override
    public Key getKey() {
        return key;
    }
    
    @Override
    public void setKey(Key key) {
        this.key = key;
    }
    
    @Override
    public boolean supportsSettingKey() {
        return true;
    }
}