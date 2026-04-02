package com.thenexusreborn.bedwars;

import com.stardevllc.smaterial.SMaterial;
import com.stardevllc.staritems.ItemBuilders;
import com.stardevllc.starlib.objects.key.Key;
import com.stardevllc.starlib.objects.key.Keys;
import com.stardevllc.starlib.registry.*;
import com.stardevllc.starlib.table.HashTable;
import com.stardevllc.starlib.table.Table;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public final class Tools {
    private Tools() {
    }
    
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
    
    private static class PlayerTool {
        private ItemStack currentItem;
        private int index = -1;
    }
    
    private static final Table<UUID, Key, PlayerTool> PLAYER_TOOLS = new HashTable<>();
    
    public static boolean removeTool(Player player, Tool tool) {
        if (tool == null) {
            return false;
        }
        
        return removeTool(player, tool.getKey());
    }
    
    public static boolean removeTool(Player player, Key toolKey) {
        if (player == null || toolKey == null) {
            return false;
        }
        
        PlayerTool removed = PLAYER_TOOLS.remove(player.getUniqueId(), toolKey);
        return removed != null;
    }
    
    public static boolean hasTool(Player player, Tool tool) {
        PlayerTool playerTool = PLAYER_TOOLS.get(player.getUniqueId(), tool.getKey());
        
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
    
    public static void checkTools(Player player) {
        for (Tool tool : REGISTRY) {
            if (!hasTool(player, tool)) {
                removeTool(player, tool);
            }
        }
    }
    
    public static ItemStack queryNext(Player player, Tool tool) {
        if (player == null || tool == null) {
            return null;
        }
        
        PlayerTool playerTool = PLAYER_TOOLS.computeIfAbsent(player.getUniqueId(), tool.getKey(), (u, tk) -> new PlayerTool());
        if (playerTool.currentItem == null) {
            return tool.getBaseItem().build();
        } else {
            if (playerTool.index + 1 >= tool.getUpgrades().size()) {
                return null;
            }
            
            player.getInventory().remove(playerTool.currentItem);
            return tool.getUpgrades().get(playerTool.index + 1).build();
        }
    }
    
    public static boolean canUpgrade(Player player, Tool tool) {
        if (player == null || tool == null) {
            return false;
        }
        
        PlayerTool playerTool = PLAYER_TOOLS.computeIfAbsent(player.getUniqueId(), tool.getKey(), (u, tk) -> new PlayerTool());
        if (playerTool.currentItem == null) {
            return true;
        } else {
            if (tool.getUpgrades().isEmpty()) {
                return false;
            }
            
            return playerTool.index + 1 < tool.getUpgrades().size();
        }
    }
    
    public static boolean canDowngrade(Player player, Tool tool) {
        if (player == null || tool == null) {
            return false;
        }
        
        PlayerTool playerTool = PLAYER_TOOLS.computeIfAbsent(player.getUniqueId(), tool.getKey(), (u, tk) -> new PlayerTool());
        if (playerTool.currentItem == null) {
            //This just means that this is a new thing of tool, so it does nothing as it is before the base
            return false;
        } else {
            //This means that the player had the base item already, and cannot go any further
            if (tool.getUpgrades().isEmpty()) {
                return false;
            }
            
            //This means that the current index is 0 or more, meaning there are more upgrades below and/or the base item
            return playerTool.index >= 0;
        }
    }
    
    public static boolean downgrade(Player player, Tool tool) {
        if (player == null || tool == null) {
            return false;
        }
        
        PlayerTool playerTool = PLAYER_TOOLS.computeIfAbsent(player.getUniqueId(), tool.getKey(), (u, tk) -> new PlayerTool());
        if (playerTool.currentItem == null) {
            //This just means that this is a new thing of tool, so it does nothing as it is before the base
            return false;
        } else {
            //This means that the player had the base item already, and cannot go any further
            if (tool.getUpgrades().isEmpty()) {
                return false;
            }
            
            //This means that the current index is 1 or more, so we just handle the non-base items, the base items (index=0) will be handled in the branch
            if (playerTool.index > 0) {
                player.getInventory().remove(playerTool.currentItem);
                playerTool.currentItem = tool.getUpgrades().get(playerTool.index - 1).build();
                player.getInventory().addItem(playerTool.currentItem);
                playerTool.index--;
                return true;
            } else if (playerTool.index == 0) {
                player.getInventory().remove(playerTool.currentItem);
                playerTool.currentItem = tool.getBaseItem().build();
                player.getInventory().addItem(playerTool.currentItem);
                playerTool.index = -1;
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean downgrade(Player player, Key toolKey) {
        if (toolKey == null) {
            return false;
        }
        
        return downgrade(player, REGISTRY.get(toolKey));
    }
    
    public static boolean upgrade(Player player, Tool tool) {
        if (player == null || tool == null) {
            return false;
        }
        
        PlayerTool playerTool = PLAYER_TOOLS.computeIfAbsent(player.getUniqueId(), tool.getKey(), (u, tk) -> new PlayerTool());
        if (playerTool.currentItem == null) {
            ItemStack itemStack = tool.getBaseItem().build();
            player.getInventory().addItem(itemStack);
            playerTool.currentItem = itemStack;
        } else {
            if (playerTool.index + 1 >= tool.getUpgrades().size()) {
                return false;
            }
            
            player.getInventory().remove(playerTool.currentItem);
            ItemStack itemStack = tool.getUpgrades().get(playerTool.index + 1).build();
            player.getInventory().addItem(itemStack);
            playerTool.currentItem = itemStack;
            playerTool.index++;
        }
        return true;
    }
    
    public static boolean upgrade(Player player, Key toolKey) {
        if (toolKey == null) {
            return false;
        }
        
        return upgrade(player, REGISTRY.get(toolKey));
    }
}