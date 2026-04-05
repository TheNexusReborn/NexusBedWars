package com.thenexusreborn.bedwars;

import com.stardevllc.minecraft.Position;
import com.stardevllc.minecraft.StarColors;
import com.stardevllc.minecraft.command.StarCommand;
import com.stardevllc.minecraft.command.SubCommand;
import com.stardevllc.minecraft.v1_8.LeatherArmorBuilder;
import com.stardevllc.minecraft.registry.*;
import com.stardevllc.minecraft.smaterial.ArmorSlot;
import com.stardevllc.stargenerators.StarGenerators;
import com.stardevllc.starcore.ItemBuilders;
import com.stardevllc.starlib.helper.StringHelper;
import com.stardevllc.starlib.objects.key.Key;
import com.stardevllc.starlib.objects.key.Keys;
import com.stardevllc.starlib.registry.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class BedwarsCommand extends StarCommand<BedWars> {
    public BedwarsCommand(BedWars plugin) {
        super(plugin, "bedwars", "Main Bedwars Command", "nexusbedwars.command", "bw");
        this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
        this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
        this.subCommands.add(new TeamsCmd());
        this.subCommands.add(new ToolsCmd());
        this.subCommands.add(new GeneratorsCmd());
    }
    
    private class GeneratorsCmd extends SubCommand<BedWars> {
        // The commands will work with most BedwarsGenerators, I just don't have that in place yet
        // But these registries will exist to help separate them from the game based generators
        // The startall and stopall commands will only work on generators created by the command
        // The upgrade command will work on all (when the full systems are in place)
        
        private final IRegistry<BedwarsGenerator> REGISTRY = PluginRegistry.builder(BedwarsGenerator.class)
                .withId(Keys.of("bedwarscmd_gens"))
                .withName("Bedwars Command Generators")
                .withParent(StarGenerators.ITEM_GENERATORS)
                .build();
        
        private final IRegistry<IslandForge> FORGE_REGISTRY = PluginRegistry.builder(IslandForge.class)
                .withId(Keys.of("bedwarscmd_forges"))
                .withName("Bedwars Cmd Forges")
                .withParent(REGISTRY)
                .build();
        
        private final Registerer<IslandForge> FORGE_REGISTERER = PluginRegisterer.create(FORGE_REGISTRY, BedwarsCommand.this.plugin);
        
        private final IRegistry<DiamondGenerator> DIAMOND_REGISTRY = PluginRegistry.builder(DiamondGenerator.class)
                .withId(Keys.of("bedwarscmd_diamond"))
                .withName("Bedwars Cmd Diamond Gens")
                .withParent(REGISTRY)
                .build();
        
        private final Registerer<DiamondGenerator> DIAMOND_REGISTERER = PluginRegisterer.create(DIAMOND_REGISTRY, BedwarsCommand.this.plugin);
        
        private final IRegistry<EmeraldGenerator> EMERALD_REGISTRY = PluginRegistry.builder(EmeraldGenerator.class)
                .withId(Keys.of("bedwarscmd_emerald"))
                .withName("Bedwars Cmd Emerald Gens")
                .withParent(REGISTRY)
                .build();
        
        private final Registerer<EmeraldGenerator> EMERALD_REGISTERER = PluginRegisterer.create(EMERALD_REGISTRY, BedwarsCommand.this.plugin);
        
        public GeneratorsCmd() {
            super(BedwarsCommand.this.plugin, BedwarsCommand.this, 0, "generators", "Manage the generators", "nexusbedwars.command.generators", "gens");
            
            this.subCommands.add(new StartCmd());
            this.subCommands.add(new StopCmd());
            this.subCommands.add(new UpgradeCmd());
            this.subCommands.add(new StartAllCmd());
            this.subCommands.add(new StopAllCmd());
            this.subCommands.add(new CreateCmd());
            //create <type> <args based on type> - The types will be discrete sub commands as they are preset in the plugin
        }
        
        private final class CreateCmd extends SubCommand<BedWars> {
            public CreateCmd() {
                super(GeneratorsCmd.this.plugin, GeneratorsCmd.this, 1, "create", "Create a generator", "nexusbedwars.command.generators.create");
                this.subCommands.add(new CreateDiamond());
                this.subCommands.add(new CreateEmerald());
                this.subCommands.add(new CreateForge());
            }
            
            private abstract sealed class SingleResource extends SubCommand<BedWars> {
                public SingleResource(String name, IRegistry<? extends BedwarsGenerator> registry) {
                    super(CreateCmd.this.plugin, CreateCmd.this, 2, name, "Create a " + StringHelper.titlize(name) + " generator", "nexusbedwars.command.generators.create." + name);
                    
                    this.executor = (plugin, sender, label, args, flagResults) -> {
                        Player player = (Player) sender;
                        Position position = new Position(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
                        
                        if (!(args.length > 0)) {
                            getColors().coloredLegacy(sender, "&cYou must provide a number");
                            return true;
                        }
                        
                        int number;
                        try {
                            number = Integer.parseInt(args[0]);
                        } catch (NumberFormatException e) {
                            getColors().coloredLegacy(sender, "&cYou provided an invalid number");
                            return true;
                        }
                        
                        PluginKey key = PluginKey.of(plugin, "cmd_" + name + "_" + number);
                        if (registry.containsKey(key)) {
                            getColors().coloredLegacy(sender, "&cA generator already exists of the type &e" + name + " &cand the number &e" + number);
                            return true;
                        }
                        
                        BedwarsGenerator generator = createGenerator(position, number);
                        if (generator == null) {
                            getColors().coloredLegacy(sender, "&cFailed to create the generator. Report as a bug");
                            return true;
                        }
                        
                        if (!registerGenerator(key, generator)) {
                            getColors().coloredLegacy(sender, "&cFailed to register the generator. Report as a bug");
                            return true;
                        }
                        
                        getColors().coloredLegacy(sender, "&eCreated the &d" + name + " &egenerator &b" + key.getKey());
                        return true;
                    };
                }
                
                abstract BedwarsGenerator createGenerator(Position position, int number);
                
                abstract boolean registerGenerator(PluginKey key, BedwarsGenerator generator);
            }
            
            private final class CreateDiamond extends SingleResource {
                public CreateDiamond() {
                    super("diamond", DIAMOND_REGISTRY);
                }
                
                @Override
                DiamondGenerator createGenerator(Position position, int number) {
                    return new DiamondGenerator(number, position);
                }
                
                @Override
                boolean registerGenerator(PluginKey key, BedwarsGenerator generator) {
                    if (!(generator instanceof DiamondGenerator diamondGenerator)) {
                        return false;
                    }
                    
                    return DIAMOND_REGISTERER.register(key, diamondGenerator).isPresent();
                }
            }
            
            private final class CreateEmerald extends SingleResource {
                public CreateEmerald() {
                    super("emerald", EMERALD_REGISTRY);
                }
                
                @Override
                EmeraldGenerator createGenerator(Position position, int number) {
                    return new EmeraldGenerator(number, position);
                }
                
                @Override
                boolean registerGenerator(PluginKey key, BedwarsGenerator generator) {
                    if (!(generator instanceof EmeraldGenerator emeraldGenerator)) {
                        return false;
                    }
                    
                    return EMERALD_REGISTERER.register(key, emeraldGenerator).isPresent();
                }
            }
            
            private final class CreateForge extends SubCommand<BedWars> {
                public CreateForge() {
                    super(CreateCmd.this.plugin, CreateCmd.this, 2, "forge", "Create a forge generator", "nexusbedwars.command.generators.create.forge");
                    
                    this.executor = (plugin, sender, label, args, flagResults) -> {
                        Player player = (Player) sender;
                        Position position = new Position(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
                        
                        if (!(args.length > 0)) {
                            getColors().coloredLegacy(sender, "&cYou must provide a name");
                            return true;
                        }
                        
                        String name = args[0];
                        
                        PluginKey key = PluginKey.of(plugin, "cmd_forge_" + name);
                        if (FORGE_REGISTRY.containsKey(key)) {
                            getColors().coloredLegacy(sender, "&cA generator already exists of the type &eforge &cand the name &e" + name);
                            return true;
                        }
                        
                        //TODO have world edit selection interaction as well. The null provided will set it to the position
                        
                        IslandForge forge = new IslandForge(name, null, null, position);
                        RegistryObject<IslandForge> genObject = FORGE_REGISTERER.register(key, forge);
                        
                        if (!genObject.isPresent()) {
                            getColors().coloredLegacy(sender, "&cFailed to register the generator. Report as a bug");
                            return true;
                        }
                        
                        getColors().coloredLegacy(sender, "&eCreated the &dforge &b" + key.getKey());
                        return true;
                    };
                }
            }
        } 
        
        Completer<BedWars> generatorCompleter = (plugin, sender, label, args, flagResults) -> {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                for (Key key : REGISTRY.keySet()) {
                    if (key instanceof PluginKey pluginKey) {
                        completions.add(pluginKey.getKey());
                    } else {
                        completions.add(key.toString());
                    }
                }
                
                completions.removeIf(c -> !c.startsWith(args[0]));
            }
            
            return completions;
        };
        
        private class StopAllCmd extends SubCommand<BedWars> {
            public StopAllCmd() {
                super(BedwarsCommand.this.plugin, GeneratorsCmd.this, 1, "stopall", "Stops all generators", "nexusbedwars.command.generators.stopall");
                
                this.executor = (plugin, sender, label, args, flagResults) -> {
                    int successfulGenerators = 0;
                    for (BedwarsGenerator generator : REGISTRY) {
                        if (!generator.isInitialized()) {
                            continue;
                        }
                        
                        if (!generator.isRunning()) {
                            continue;
                        }
                        
                        generator.stop();
                        successfulGenerators++;
                    }
                    
                    getColors().coloredLegacy(sender, "&eStopped &b" + successfulGenerators + " &egenerators.");
                    return true;
                };
            }
        }
        
        private class StartAllCmd extends SubCommand<BedWars> {
            public StartAllCmd() {
                super(BedwarsCommand.this.plugin, GeneratorsCmd.this, 1, "startall", "Starts all generators", "nexusbedwars.command.generators.startall");
                
                this.executor = (plugin, sender, label, args, flagResults) -> {
                    Player player = (Player) sender;
                    
                    int successfulGenerators = 0;
                    for (BedwarsGenerator generator : REGISTRY) {
                        if (!generator.isInitialized()) {
                            generator.init(player.getWorld());
                        }
                        
                        if (generator.isRunning()) {
                            continue;
                        }
                        
                        generator.start();
                        successfulGenerators++;
                    }
                    
                    getColors().coloredLegacy(sender, "&eStarted &b" + successfulGenerators + " &egenerators.");
                    return true;
                };
            }
        }
        
        private class StartCmd extends SubCommand<BedWars> {
            public StartCmd() {
                super(BedwarsCommand.this.plugin, GeneratorsCmd.this, 1, "start", "Starts a generator", "nexusbedwars.command.generators.start");
                
                this.executor = (plugin, sender, label, args, flagResults) -> {
                    if (!(args.length > 0)) {
                        getColors().coloredLegacy(sender, "&cYou must provide a generator id");
                        return true;
                    }
                    
                    Player player = (Player) sender;
                    
                    PluginKey key = PluginKey.of(plugin, args[0]);
                    BedwarsGenerator generator = REGISTRY.get(key);
                    if (generator == null) {
                        getColors().coloredLegacy(sender, "&cInvalid generator id &e" + args[0] + "&c.");
                        return true;
                    }
                    
                    if (!generator.isInitialized()) {
                        generator.init(player.getWorld());
                    }
                    
                    if (generator.isRunning()) {
                        getColors().coloredLegacy(sender, "&cThe generator &e" + generator.getKey() + " &cis already running.");
                        return true;
                    }
                    
                    generator.start();
                    getColors().coloredLegacy(sender, "&eStarted the generator &b" + generator.getKey());
                    return true;
                };
                
                this.completer = generatorCompleter;
            }
        }
        
        private class StopCmd extends SubCommand<BedWars> {
            public StopCmd() {
                super(BedwarsCommand.this.plugin, GeneratorsCmd.this, 1, "stop", "Stops a generator", "nexusbedwars.command.generators.stop");
                
                this.executor = (plugin, sender, label, args, flagResults) -> {
                    if (!(args.length > 0)) {
                        getColors().coloredLegacy(sender, "&cYou must provide a generator id");
                        return true;
                    }
                    
                    PluginKey key = PluginKey.of(plugin, args[0]);
                    BedwarsGenerator generator = REGISTRY.get(key);
                    if (generator == null) {
                        getColors().coloredLegacy(sender, "&cInvalid generator id &e" + args[0] + "&c.");
                        return true;
                    }
                    
                    if (!generator.isInitialized()) {
                        getColors().coloredLegacy(sender, "&cThe generator &e" + generator.getKey() + " &cis not initialized");
                        return true;
                    }
                    
                    if (!generator.isRunning()) {
                        getColors().coloredLegacy(sender, "&cThe generator &e" + generator.getKey() + " &cis not running");
                        return true;
                    }
                    
                    generator.stop();
                    getColors().coloredLegacy(sender, "&eStopped the generator &b" + generator.getKey());
                    return true;
                };
                
                this.completer = generatorCompleter;
            }
        }
        
        private class UpgradeCmd extends SubCommand<BedWars> {
            public UpgradeCmd() {
                super(BedwarsCommand.this.plugin, GeneratorsCmd.this, 1, "upgrade", "Upgrades a generator", "nexusbedwars.command.generators.upgrade");
                
                this.executor = (plugin, sender, label, args, flagResults) -> {
                    if (!(args.length > 0)) {
                        getColors().coloredLegacy(sender, "&cYou must provide a generator id");
                        return true;
                    }
                    
                    PluginKey key = PluginKey.of(plugin, args[0]);
                    BedwarsGenerator generator = REGISTRY.get(key);
                    if (generator == null) {
                        getColors().coloredLegacy(sender, "&cInvalid generator id &e" + args[0] + "&c.");
                        return true;
                    }
                    
                    if (!generator.isInitialized()) {
                        getColors().coloredLegacy(sender, "&cThe generator &e" + generator.getKey() + " &cis not initialized");
                        return true;
                    }
                    
                    if (!generator.isRunning()) {
                        getColors().coloredLegacy(sender, "&cThe generator &e" + generator.getKey() + " &cis not running");
                        return true;
                    }
                    
                    if (!generator.upgrade()) {
                        getColors().coloredLegacy(sender, "&cThe generator &e" + generator.getKey() + " &ccould not be upgraded");
                        return true;
                    }
                    
                    getColors().coloredLegacy(sender, "&eThe generator &b" + generator.getKey() + " &ewas upgraded to &b" + generator.getTierName());
                    return true;
                };
                
                this.completer = generatorCompleter;
            }
        }
    }
    
    private class ToolsCmd extends SubCommand<BedWars> {
        public ToolsCmd() {
            super(BedwarsCommand.this.plugin, BedwarsCommand.this, 0, "tools", "Manage the tools", "nexusbedwars.command.tools");
//            this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
//            this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
            
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
//                this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
//                this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
                
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
//                this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
//                this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
                
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
//                this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
//                this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
                
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
//            this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
//            this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
        }
        
        private class TestCmd extends SubCommand<BedWars> {
            public TestCmd(BedWars plugin) {
                super(BedwarsCommand.this.plugin, TeamsCmd.this, 1, "test", "Test team settings", "nexusbedwars.command.teams.test");
                this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
                this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
                this.executor = (p, sender, label, args, flagResults) -> {
                    GameTeam gameTeam = GameTeam.get(args[0]);
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
                        for (GameTeam gameTeam : GameTeam.REGISTRY) {
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
