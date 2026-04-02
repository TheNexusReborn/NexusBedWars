package com.thenexusreborn.bedwars;

import com.stardevllc.StarColors;
import com.stardevllc.command.StarCommand;
import com.stardevllc.command.SubCommand;
import com.stardevllc.itembuilder.v1_8.LeatherArmorBuilder;
import com.stardevllc.smaterial.ArmorSlot;
import com.stardevllc.staritems.ItemBuilders;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class BedwarsCommand extends StarCommand<BedWars> {
    public BedwarsCommand(BedWars plugin) {
        super(plugin, "bedwars", "Main Bedwars Command", "nexusbedwars.command", "bw");
        this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
        this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
        this.subCommands.add(new TeamsCmd());
        this.subCommands.add(new ToolsCmd());
    }
    
    private class ToolsCmd extends SubCommand<BedWars> {
        public ToolsCmd() {
            super(BedwarsCommand.this.plugin, BedwarsCommand.this, 0, "tools", "Manage the tools", "nexusbedwars.command.tools");
            this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
            this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
            
            this.subCommands.add(new GetCmd());
            this.subCommands.add(new UpgradeCmd());
            this.subCommands.add(new DowngradeCmd());
        }
        
        public static Completer<BedWars> completer = (p, sender, label, args, flagResults) -> {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                for (Tool tool : Tool.REGISTRY) {
                    completions.add(tool.getKey().toString().toLowerCase());
                }
                
                completions.removeIf(c -> !c.startsWith(args[0]));
            }
            
            return completions;
        };
        
        private class GetCmd extends SubCommand<BedWars> {
            public GetCmd() {
                super(BedwarsCommand.this.plugin, ToolsCmd.this, 1, "get", "Get a tool", "nexusbedwars.command.tools.get");
                this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
                this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
                
                this.executor = (p, sender, label, args, flagResults) -> {
                    Tool tool = Tool.REGISTRY.get(args[0]);
                    if (tool == null) {
                        StarColors.coloredMessage(sender, "&cInvalid tool");
                        return true;
                    }
                    
                    Player player = (Player) sender;
                    
                    if (tool.hasTool(player)) {
                        StarColors.coloredMessage(sender, "&cYou aready have that tool, use the upgrade command");
                        return true;
                    }
                    
                    boolean result = tool.upgrade(player);
                    if (result) {
                        StarColors.coloredMessage(sender, "&aSuccessfully retreived the tool &b" + tool.getKey());
                    } else {
                        StarColors.coloredMessage(sender, "&cFailed to get the tool &e" + tool.getKey());
                    }
                    return true;
                };
                
                this.completer = ToolsCmd.completer;
            }
        }
        private class UpgradeCmd extends SubCommand<BedWars> {
            public UpgradeCmd() {
                super(BedwarsCommand.this.plugin, ToolsCmd.this, 1, "upgrade", "Upgrade a tool", "nexusbedwars.command.tools.upgrade");
                this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
                this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
                
                this.executor = (p, sender, label, args, flagResults) -> {
                    Tool tool = Tool.REGISTRY.get(args[0]);
                    if (tool == null) {
                        StarColors.coloredMessage(sender, "&cInvalid tool");
                        return true;
                    }
                    
                    Player player = (Player) sender;
                    
                    if (!tool.canUpgrade(player)) {
                        StarColors.coloredMessage(player, "&cYou cannot upgrade the tool &e" + tool.getKey() + " &canymore!");
                        return true;
                    }
                    
                    boolean result = tool.upgrade(player);
                    if (result) {
                        StarColors.coloredMessage(sender, "&aSuccessfully upgraded the tool &b" + tool.getKey());
                    } else {
                        StarColors.coloredMessage(sender, "&cFailed to upgrade the tool &e" + tool.getKey());
                    }
                    return true;
                };
                
                this.completer = ToolsCmd.completer;
            }
        }
        
        private class DowngradeCmd extends SubCommand<BedWars> {
            public DowngradeCmd() {
                super(BedwarsCommand.this.plugin, ToolsCmd.this, 1, "downgrade", "Downgrade a tool", "nexusbedwars.command.tools.downgrade");
                this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
                this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
                
                this.executor = (p, sender, label, args, flagResults) -> {
                    Tool tool = Tool.REGISTRY.get(args[0]);
                    if (tool == null) {
                        StarColors.coloredMessage(sender, "&cInvalid tool");
                        return true;
                    }
                    
                    Player player = (Player) sender;
                    
                    if (!tool.hasTool(player)) {
                        StarColors.coloredMessage(player, "&cYou do not have the tool &e" + tool.getKey() + "&c.");
                        return true;
                    }
                    
                    if (!tool.canDowngrade(player)) {
                        StarColors.coloredMessage(player, "&cYou cannot downgrade the tool &e" + tool.getKey() + " &canymore!");
                        return true;
                    }
                    
                    boolean result = tool.downgrade(player);
                    if (result) {
                        StarColors.coloredMessage(sender, "&aSuccessfully downgraded the tool &b" + tool.getKey());
                    } else {
                        StarColors.coloredMessage(sender, "&cFailed to downgrade the tool &e" + tool.getKey());
                    }
                    return true;
                };
                
                this.completer = ToolsCmd.completer;
            }
        }
    }
    
    private class TeamsCmd extends SubCommand<BedWars> {
        public TeamsCmd() {
            super(BedwarsCommand.this.plugin, BedwarsCommand.this, 0, "teams", "Manage the teams", "nexusbedwars.command.teams");
            this.subCommands.add(new TestCmd(plugin));
            this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
            this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
        }
        
        private class TestCmd extends SubCommand<BedWars> {
            public TestCmd(BedWars plugin) {
                super(BedwarsCommand.this.plugin, TeamsCmd.this, 1, "test", "Test team settings", "nexusbedwars.command.teams.test");
                this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
                this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
                this.executor = (p, sender, label, args, flagResults) -> {
                    GameTeam gameTeam = GameTeams.get(args[0]);
                    if (gameTeam == null) {
                        StarColors.coloredMessage(sender, "&cInvalid Team " + args[0]);
                        return true;
                    }
                    
                    Player player = (Player) sender;
                    
                    PlayerInventory inv = player.getInventory();
                    inv.setHelmet(new LeatherArmorBuilder(ArmorSlot.HELMET).addEnchant(Enchantment.WATER_WORKER, 1).unbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).color(gameTeam.getDyeColor()).build());
                    inv.setChestplate(new LeatherArmorBuilder(ArmorSlot.CHESTPLATE).unbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).color(gameTeam.getDyeColor()).build());
                    inv.setLeggings(new LeatherArmorBuilder(ArmorSlot.LEGGINGS).unbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).color(gameTeam.getDyeColor()).build());
                    inv.setBoots(new LeatherArmorBuilder(ArmorSlot.BOOTS).unbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).color(gameTeam.getDyeColor()).build());
                    
                    inv.setItem(0, ItemBuilders.of(gameTeam.getWoolMaterial()).amount(64).build());
                    inv.setItem(1, ItemBuilders.of(gameTeam.getGlassMaterial()).amount(64).build());
                    inv.setItem(2, ItemBuilders.of(gameTeam.getClayMaterial()).amount(64).build());
                    
                    StarColors.coloredMessage(sender, "&7You have received the items for team " + gameTeam.getChatColor() + gameTeam.getName());
                    return true;
                };
                
                this.completer = (p, sender, label, args, flagResults) -> {
                    List<String> completions = new ArrayList<>();
                    if (args.length == 1) {
                        for (GameTeam gameTeam : GameTeams.REGISTRY) {
                            completions.add(gameTeam.getName().toLowerCase());
                        }
                        
                        completions.removeIf(c -> !c.startsWith(args[0]));
                    }
                    
                    return completions;
                };
            }
        }
    }
}
