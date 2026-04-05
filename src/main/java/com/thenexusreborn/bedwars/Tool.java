package com.thenexusreborn.bedwars;

import com.stardevllc.minecraft.itembuilder.ItemBuilder;
import com.stardevllc.minecraft.smaterial.SMaterial;
import com.stardevllc.starcore.ItemBuilders;
import com.stardevllc.starlib.objects.builder.IBuilder;
import com.stardevllc.starlib.objects.key.*;
import com.stardevllc.starlib.registry.*;
import com.stardevllc.starlib.table.HashTable;
import com.stardevllc.starlib.table.Table;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Tool implements Item {
    
    public static final IRegistry<Tool> REGISTRY = HashRegistry.builder(Tool.class)
            .withId(Keys.of("tools"))
            .withName("Tools")
            .allowFreezing()
            .build();
    
    private static final Registerer<Tool> REGISTERER = Registerer.create(REGISTRY);
    
    public static final RegistryObject<Tool> SWORD = REGISTERER.register("sword", Tool.builder()
            .sharedFlags(ItemFlag.HIDE_UNBREAKABLE)
            .baseItem(ItemBuilders.of(SMaterial.WOODEN_SWORD).unbreakable(true))
            .addUpgrade(ItemBuilders.of(SMaterial.STONE_SWORD).unbreakable(true))
            .addUpgrade(ItemBuilders.of(SMaterial.IRON_SWORD).unbreakable(true))
            .addUpgrade(ItemBuilders.of(SMaterial.DIAMOND_SWORD).unbreakable(true))
            .build());
    
    public static final RegistryObject<Tool> SHEARS = REGISTERER.register("shears", Tool.builder().baseItem(ItemBuilders.of(SMaterial.SHEARS).unbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE)).build());
    
    public static final RegistryObject<Tool> PICKAXES = REGISTERER.register("pickaxe", Tool.builder()
            .sharedFlags(ItemFlag.HIDE_UNBREAKABLE)
            .baseItem(ItemBuilders.of(SMaterial.WOODEN_PICKAXE).addEnchant(Enchantment.DIG_SPEED, 1).unbreakable(true))
            .addUpgrade(ItemBuilders.of(SMaterial.IRON_PICKAXE).addEnchant(Enchantment.DIG_SPEED, 1).unbreakable(true))
            .addUpgrade(ItemBuilders.of(SMaterial.GOLDEN_PICKAXE).addEnchant(Enchantment.DIG_SPEED, 1).unbreakable(true))
            .addUpgrade(ItemBuilders.of(SMaterial.DIAMOND_PICKAXE).addEnchant(Enchantment.DIG_SPEED, 1).unbreakable(true))
            .build());
    
    public static final RegistryObject<Tool> AXES = REGISTERER.register("axe", Tool.builder()
            .sharedFlags(ItemFlag.HIDE_UNBREAKABLE)
            .baseItem(ItemBuilders.of(SMaterial.WOODEN_AXE).addEnchant(Enchantment.DIG_SPEED, 1).unbreakable(true))
            .addUpgrade(ItemBuilders.of(SMaterial.STONE_AXE).addEnchant(Enchantment.DIG_SPEED, 1).unbreakable(true))
            .addUpgrade(ItemBuilders.of(SMaterial.IRON_AXE).addEnchant(Enchantment.DIG_SPEED, 1).unbreakable(true))
            .addUpgrade(ItemBuilders.of(SMaterial.DIAMOND_AXE).addEnchant(Enchantment.DIG_SPEED, 1).unbreakable(true))
            .build());
    
    static {
        REGISTRY.freeze();
    }
    
    public static void checkTools(Player player) {
        for (Tool tool : REGISTRY) {
            if (!tool.hasTool(player)) {
                tool.removeTool(player);
            }
        }
    }
    
    private static class PlayerTool {
        private ItemStack currentItem;
        private int index = -1;
    }
    
    private static final Table<UUID, Key, PlayerTool> PLAYER_TOOLS = new HashTable<>();
    
    private Key key;
    
    private final ItemBuilder<?, ?> baseItem;
    private final LinkedList<ItemBuilder<?, ?>> upgrades;
    
    public Tool(ItemBuilder<?, ?> baseItem, LinkedList<ItemBuilder<?, ?>> upgrades) {
        this.baseItem = baseItem;
        this.upgrades = new LinkedList<>(upgrades);
    }
    
    public ItemBuilder<?, ?> getBaseItem() {
        return baseItem;
    }
    
    public LinkedList<ItemBuilder<?, ?>> getUpgrades() {
        return upgrades;
    }
    
    public static Builder builder() {
        return new Builder();
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
    public ItemStack getItemStack(Player player) {
        if (player == null) {
            return null;
        }
        
        if (!hasTool(player)) {
            upgrade(player);
        }
        return PLAYER_TOOLS.get(player, getKey()).currentItem;
    }
    
    @Override
    public ItemStack getItemStack(GamePlayer player) {
        return getItemStack(Bukkit.getPlayer(player.getUniqueId()));
    }
    
    public boolean removeTool(Player player) {
        if (player == null || getKey() == null) {
            return false;
        }
        
        PlayerTool removed = PLAYER_TOOLS.remove(player.getUniqueId(), getKey());
        return removed != null;
    }
    
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasTool(Player player) {
        PlayerTool playerTool = PLAYER_TOOLS.get(player.getUniqueId(), getKey());
        
        if (playerTool == null) {
            return false;
        }
        
        if (playerTool.currentItem == null) {
            playerTool.index = -1;
            return false;
        }
        
        if (!player.getInventory().contains(playerTool.currentItem) && !(player.getItemOnCursor() != null && playerTool.currentItem.equals(player.getItemOnCursor()))) {
            playerTool.currentItem = null;
            playerTool.index = -1;
            return false;
        }
        
        return true;
    }
    
    public ItemStack queryNext(Player player) {
        if (player == null) {
            return null;
        }
        
        PlayerTool playerTool = PLAYER_TOOLS.computeIfAbsent(player.getUniqueId(), getKey(), (u, tk) -> new PlayerTool());
        if (playerTool.currentItem == null) {
            return getBaseItem().build();
        } else {
            if (playerTool.index + 1 >= getUpgrades().size()) {
                return null;
            }
            
            player.getInventory().remove(playerTool.currentItem);
            return getUpgrades().get(playerTool.index + 1).build();
        }
    }
    
    public boolean canUpgrade(Player player) {
        if (player == null) {
            return false;
        }
        
        PlayerTool playerTool = PLAYER_TOOLS.computeIfAbsent(player.getUniqueId(), getKey(), (u, tk) -> new PlayerTool());
        if (playerTool.currentItem == null) {
            return true;
        } else {
            if (getUpgrades().isEmpty()) {
                return false;
            }
            
            return playerTool.index + 1 < getUpgrades().size();
        }
    }
    
    public boolean canDowngrade(Player player) {
        if (player == null) {
            return false;
        }
        
        PlayerTool playerTool = PLAYER_TOOLS.computeIfAbsent(player.getUniqueId(), getKey(), (u, tk) -> new PlayerTool());
        if (playerTool.currentItem == null) {
            //This just means that this is a new thing of tool, so it does nothing as it is before the base
            return false;
        } else {
            //This means that the player had the base item already, and cannot go any further
            if (getUpgrades().isEmpty()) {
                return false;
            }
            
            //This means that the current index is 0 or more, meaning there are more upgrades below and/or the base item
            return playerTool.index >= 0;
        }
    }
    
    public boolean downgrade(Player player) {
        if (player == null) {
            return false;
        }
        
        PlayerTool playerTool = PLAYER_TOOLS.computeIfAbsent(player.getUniqueId(), getKey(), (u, tk) -> new PlayerTool());
        if (playerTool.currentItem == null) {
            //This just means that this is a new thing of tool, so it does nothing as it is before the base
            return false;
        } else {
            //This means that the player had the base item already, and cannot go any further
            if (getUpgrades().isEmpty()) {
                return false;
            }
            
            //This means that the current index is 1 or more, so we just handle the non-base items, the base items (index=0) will be handled in the branch
            if (playerTool.index > 0) {
                player.getInventory().remove(playerTool.currentItem);
                playerTool.currentItem = getUpgrades().get(playerTool.index - 1).build();
                player.getInventory().addItem(playerTool.currentItem);
                playerTool.index--;
                return true;
            } else if (playerTool.index == 0) {
                player.getInventory().remove(playerTool.currentItem);
                playerTool.currentItem = getBaseItem().build();
                player.getInventory().addItem(playerTool.currentItem);
                playerTool.index = -1;
                return true;
            }
        }
        
        return false;
    }
    
    public boolean upgrade(Player player) {
        if (player == null) {
            return false;
        }
        
        PlayerTool playerTool = PLAYER_TOOLS.computeIfAbsent(player.getUniqueId(), getKey(), (u, tk) -> new PlayerTool());
        if (playerTool.currentItem == null) {
            ItemStack itemStack = getBaseItem().build();
            player.getInventory().addItem(itemStack);
            playerTool.currentItem = itemStack;
        } else {
            if (playerTool.index + 1 >= getUpgrades().size()) {
                return false;
            }
            
            player.getInventory().remove(playerTool.currentItem);
            ItemStack itemStack = getUpgrades().get(playerTool.index + 1).build();
            player.getInventory().addItem(itemStack);
            playerTool.currentItem = itemStack;
            playerTool.index++;
        }
        return true;
    }
    
    public static class Builder implements IBuilder<Tool, Builder> {
        private ItemFlag[] sharedFlags;
        private ItemBuilder<?, ?> baseItem;
        private final LinkedList<ItemBuilder<?, ?>> upgrades = new LinkedList<>();
        
        public Builder() {
        }
        
        public Builder(Builder builder) {
            this.baseItem = builder.baseItem;
            this.upgrades.addAll(builder.upgrades);
            this.sharedFlags = builder.sharedFlags;
        }
        
        public Builder sharedFlags(ItemFlag... flags) {
            sharedFlags = flags;
            return self();
        }
        
        public Builder baseItem(ItemBuilder<?, ?> baseItem) {
            this.baseItem = baseItem;
            if (sharedFlags != null) {
                this.baseItem.addItemFlags(sharedFlags);
            }
            return self();
        }
        
        public Builder baseItem(SMaterial material) {
            return baseItem(ItemBuilders.of(material));
        }
        
        public Builder addUpgrade(ItemBuilder<?, ?> upgrade) {
            this.upgrades.add(upgrade);
            if (sharedFlags != null) {
                upgrade.addItemFlags(sharedFlags);
            }
            return self();
        }
        
        public Builder addUpgrade(SMaterial material) {
            return addUpgrade(ItemBuilders.of(material));
        }
        
        @Override
        public Tool build() {
            return new Tool(baseItem, upgrades);
        }
        
        @Override
        public Builder clone() {
            return new Builder(this);
        }
    }
}