package com.thenexusreborn.bedwars;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.stardevllc.minecraft.Position;
import com.stardevllc.minecraft.StarColors;
import com.stardevllc.minecraft.command.StarCommand;
import com.stardevllc.minecraft.command.SubCommand;
import com.stardevllc.minecraft.registry.*;
import com.stardevllc.starlib.helper.StringHelper;
import com.stardevllc.starlib.objects.key.Key;
import com.stardevllc.starlib.objects.key.Keys;
import com.stardevllc.starlib.registry.*;
import com.thenexusreborn.api.NexusReborn;
import com.thenexusreborn.api.player.NexusPlayer;
import com.thenexusreborn.api.server.NexusServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

@SuppressWarnings("DuplicatedCode")
public class BedwarsCommand extends StarCommand<NexusBedWarsPlugin> {
    
    private static CuboidSelection getWorldEditSelection(Player player) {
        Selection selection = WorldEditPlugin.getPlugin(WorldEditPlugin.class).getSelection(player);
        if (selection == null) {
            return null;
        }
        
        if (!(selection instanceof CuboidSelection cuboidRegion)) {
            return null;
        }
        
        return cuboidRegion;
    }
    
    private static Position[] getPointsFromSelection(CuboidSelection selection) {
        Location min = selection.getMinimumPoint();
        Location max = selection.getMaximumPoint();
        
        Position minimum = new Position(min.getBlockX(), min.getBlockY(), min.getBlockZ());
        Position maximum = new Position(max.getBlockX(), max.getBlockY(), max.getBlockZ());
        return new Position[]{minimum, maximum};
    }
    
    public BedwarsCommand(NexusBedWarsPlugin plugin) {
        super(plugin, "bedwars", "Main Bedwars Command", "nexusbedwars.command", "bw");
        this.invalidSubCommandMessage = getColors().colorLegacy("&cInvalid Subcommand");
        this.noPermissionMessage = getColors().colorLegacy("&cYou do not have permission to use that command");
        this.subCommands.add(new TeamsCmd());
        this.subCommands.add(new ToolsCmd());
        this.subCommands.add(new GeneratorsCmd());
    }
    
    private class GeneratorsCmd extends SubCommand<NexusBedWarsPlugin> {
        // The commands will work with most BedwarsGenerators, I just don't have that in place yet
        // But these registries will exist to help separate them from the game based generators
        // The startall and stopall commands will only work on generators created by the command
        // The upgrade command will work on all (when the full systems are in place)
        
        private final IRegistry<BedwarsGenerator> REGISTRY = PluginRegistry.builder(BedwarsGenerator.class)
                .withKey(Keys.of("bedwarscmd_gens"))
                .withName("Bedwars Command Generators")
                .withParent(NexusBedWarsPlugin.GENERATORS)
                .build();
        
        private final IRegistry<IslandForge> FORGE_REGISTRY = PluginRegistry.builder(IslandForge.class)
                .withKey(Keys.of("bedwarscmd_forges"))
                .withName("Bedwars Cmd Forges")
                .withParent(REGISTRY)
                .build();
        
        private final Registerer<IslandForge> FORGE_REGISTERER = PluginRegisterer.create(FORGE_REGISTRY, BedwarsCommand.this.plugin);
        
        private final IRegistry<DiamondGenerator> DIAMOND_REGISTRY = PluginRegistry.builder(DiamondGenerator.class)
                .withKey(Keys.of("bedwarscmd_diamond"))
                .withName("Bedwars Cmd Diamond Gens")
                .withParent(REGISTRY)
                .build();
        
        private final Registerer<DiamondGenerator> DIAMOND_REGISTERER = PluginRegisterer.create(DIAMOND_REGISTRY, BedwarsCommand.this.plugin);
        
        private final IRegistry<EmeraldGenerator> EMERALD_REGISTRY = PluginRegistry.builder(EmeraldGenerator.class)
                .withKey(Keys.of("bedwarscmd_emerald"))
                .withName("Bedwars Cmd Emerald Gens")
                .withParent(REGISTRY)
                .build();
        
        private final Registerer<EmeraldGenerator> EMERALD_REGISTERER = PluginRegisterer.create(EMERALD_REGISTRY, BedwarsCommand.this.plugin);
        
        public GeneratorsCmd() {
            super(BedwarsCommand.this.plugin, BedwarsCommand.this, 0, "generators", "Manage the generators", "nexusbedwars.command.generators", "gens");
            
            this.subCommands.add(new StartCmd());
            this.subCommands.add(new StopCmd());
            this.subCommands.add(new UpgradeCmd());
            //upgradeall command
            this.subCommands.add(new StartAllCmd());
            this.subCommands.add(new StopAllCmd());
            this.subCommands.add(new CreateCmd());
        }
        
        private final class CreateCmd extends SubCommand<NexusBedWarsPlugin> {
            public CreateCmd() {
                super(GeneratorsCmd.this.plugin, GeneratorsCmd.this, 1, "create", "Create a generator", "nexusbedwars.command.generators.create");
                this.subCommands.add(new CreateDiamond());
                this.subCommands.add(new CreateEmerald());
                this.subCommands.add(new CreateForge());
            }
            
            private abstract sealed class SingleResource extends SubCommand<NexusBedWarsPlugin> {
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
                        
                        generator.init(player.getWorld());
                        
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
            
            private final class CreateForge extends SubCommand<NexusBedWarsPlugin> {
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
                        
                        Position boundsMin, boundsMax;
                        
                        CuboidSelection selection = getWorldEditSelection(player);
                        if (selection == null) {
                            boundsMin = position;
                            boundsMax = position;
                        } else {
                            Position[] points = getPointsFromSelection(selection);
                            boundsMin = points[0];
                            boundsMax = points[1];
                        }
                        
                        IslandForge forge = new IslandForge(name, boundsMin, boundsMax, position);
                        forge.init(player.getWorld());
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
        
        Completer<NexusBedWarsPlugin> generatorCompleter = (plugin, sender, label, args, flagResults) -> {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                for (Key key : NexusBedWarsPlugin.GENERATORS.keySet()) {
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
        
        private class StopAllCmd extends SubCommand<NexusBedWarsPlugin> {
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
        
        private class StartAllCmd extends SubCommand<NexusBedWarsPlugin> {
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
        
        private class StartCmd extends SubCommand<NexusBedWarsPlugin> {
            public StartCmd() {
                super(BedwarsCommand.this.plugin, GeneratorsCmd.this, 1, "start", "Starts a generator", "nexusbedwars.command.generators.start");
                
                this.executor = (plugin, sender, label, args, flagResults) -> {
                    if (!(args.length > 0)) {
                        getColors().coloredLegacy(sender, "&cYou must provide a generator id");
                        return true;
                    }
                    
                    Player player = (Player) sender;
                    
                    PluginKey key = PluginKey.of(plugin, args[0]);
                    BedwarsGenerator generator = NexusBedWarsPlugin.GENERATORS.get(key);
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
        
        private class StopCmd extends SubCommand<NexusBedWarsPlugin> {
            public StopCmd() {
                super(BedwarsCommand.this.plugin, GeneratorsCmd.this, 1, "stop", "Stops a generator", "nexusbedwars.command.generators.stop");
                
                this.executor = (plugin, sender, label, args, flagResults) -> {
                    if (!(args.length > 0)) {
                        getColors().coloredLegacy(sender, "&cYou must provide a generator id");
                        return true;
                    }
                    
                    PluginKey key = PluginKey.of(plugin, args[0]);
                    BedwarsGenerator generator = NexusBedWarsPlugin.GENERATORS.get(key);
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
        
        private class UpgradeCmd extends SubCommand<NexusBedWarsPlugin> {
            public UpgradeCmd() {
                super(BedwarsCommand.this.plugin, GeneratorsCmd.this, 1, "upgrade", "Upgrades a generator", "nexusbedwars.command.generators.upgrade");
                
                this.executor = (plugin, sender, label, args, flagResults) -> {
                    if (!(args.length > 0)) {
                        getColors().coloredLegacy(sender, "&cYou must provide a generator id");
                        return true;
                    }
                    
                    PluginKey key = PluginKey.of(plugin, args[0]);
                    BedwarsGenerator generator = NexusBedWarsPlugin.GENERATORS.get(key);
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
    
    private class ToolsCmd extends SubCommand<NexusBedWarsPlugin> {
        public ToolsCmd() {
            super(BedwarsCommand.this.plugin, BedwarsCommand.this, 0, "tools", "Manage the tools", "nexusbedwars.command.tools");
            this.subCommands.add(new GetCmd());
            this.subCommands.add(new UpgradeCmd());
            this.subCommands.add(new DowngradeCmd());
        }
        
        public static Completer<NexusBedWarsPlugin> completer = (p, sender, label, args, flagResults) -> {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                for (Tool tool : Tool.REGISTRY) {
                    completions.add(tool.getKey().toString().toLowerCase());
                }
                
                completions.removeIf(c -> !c.startsWith(args[0]));
            }
            
            return completions;
        };
        
        private class GetCmd extends SubCommand<NexusBedWarsPlugin> {
            public GetCmd() {
                super(BedwarsCommand.this.plugin, ToolsCmd.this, 1, "get", "Get a tool", "nexusbedwars.command.tools.get");
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
        
        private class UpgradeCmd extends SubCommand<NexusBedWarsPlugin> {
            public UpgradeCmd() {
                super(BedwarsCommand.this.plugin, ToolsCmd.this, 1, "upgrade", "Upgrade a tool", "nexusbedwars.command.tools.upgrade");
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
        
        private class DowngradeCmd extends SubCommand<NexusBedWarsPlugin> {
            public DowngradeCmd() {
                super(BedwarsCommand.this.plugin, ToolsCmd.this, 1, "downgrade", "Downgrade a tool", "nexusbedwars.command.tools.downgrade");
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
    
    private class TeamsCmd extends SubCommand<NexusBedWarsPlugin> {
        public TeamsCmd() {
            super(BedwarsCommand.this.plugin, BedwarsCommand.this, 0, "teams", "Manage the teams", "nexusbedwars.command.teams");
            
            for (GameTeam gameTeam : GameTeam.REGISTRY) {
                this.subCommands.add(new TeamCmd(gameTeam));
            }
        }
        
        private class TeamCmd extends SubCommand<NexusBedWarsPlugin> {
            public TeamCmd(GameTeam team) {
                super(TeamsCmd.this.plugin, TeamsCmd.this, 1, team.getName().toLowerCase(), "Manage the team " + team.getName().toLowerCase(), "nexusbedwars.command.teams." + team.getName().toLowerCase());
                this.subCommands.add(new PlayersCmd(team));
                this.subCommands.add(new UpgradesCmd(team));
                this.subCommands.add(new ForgeCmd(team));
                this.subCommands.add(new RegionCmd(team));
            }
            
            private class PlayersCmd extends SubCommand<NexusBedWarsPlugin> {
                public PlayersCmd(GameTeam team) {
                    super(TeamCmd.this.plugin, TeamCmd.this, 2, "players", "Manage the players of the team" + team.getName().toLowerCase(), "nexusbedwars.command.teams." + team.getName().toLowerCase() + ".players");
                    this.subCommands.add(new AddCmd(team));
                    this.subCommands.add(new RemoveCmd(team));
                }
                
                private class AddCmd extends SubCommand<NexusBedWarsPlugin> {
                    public AddCmd(GameTeam team) {
                        super(PlayersCmd.this.plugin, PlayersCmd.this, 3, "add", "Add a player to the team " + team.getName().toLowerCase(), "nexusbedwars.command.teams." + team.getName().toLowerCase() + ".players.add");
                        this.playerOnly = true;
                        
                        this.executor = (plugin, sender, label, args, flagResults) -> {
                            Player player = (Player) sender;
                            NexusPlayer nexusPlayer = NexusReborn.getPlayerManager().getNexusPlayer(player.getUniqueId());
                            if (nexusPlayer == null) {
                                return true;
                            }
                            
                            NexusServer server = nexusPlayer.getServer();
                            if (!(server instanceof BWVirtualServer bwServer)) {
                                getColors().coloredLegacy(player, "&cYou are not on a Bed Wars Server");
                                return true;
                            }
                            
                            TeamInstance teamInstance = bwServer.getTeamInstance(team);
                            
                            if (!(args.length > 0)) {
                                getColors().coloredLegacy(sender, "&cYou must provide a player to add.");
                                return true;
                            }
                            
                            Player target = Bukkit.getPlayer(args[0]);
                            if (target == null) {
                                getColors().coloredLegacy(player, "&cYou provided an invalid player name. They must be online.");
                                return true;
                            }
                            
                            if (teamInstance.getPlayers().contains(target.getUniqueId())) {
                                getColors().coloredLegacy(player, "&c" + player.getName() + " is already a member of the team " + team.getName().toUpperCase());
                                return true;
                            }
                            
                            
                            teamInstance.addPlayer(target);
                            getColors().coloredLegacy(player, "&e" + player.getName() + " has been added to the team " + team.getChatColor() + team.getName().toUpperCase());
                            getColors().coloredLegacy(target, "&eYou have been added to the team " + team.getChatColor() + team.getName() + " &eby &b" + player.getName());
                            return true;
                        };
                        
                        this.completer = (plugin, sender, label, args, flagResults) -> {
                            if (args.length != 1) {
                                return List.of();
                            }
                            
                            Player player = (Player) sender;
                            NexusPlayer nexusPlayer = NexusReborn.getPlayerManager().getNexusPlayer(player.getUniqueId());
                            if (nexusPlayer == null) {
                                return List.of();
                            }
                            
                            if (!(nexusPlayer.getServer() instanceof BWVirtualServer server)) {
                                return List.of();
                            }
                            
                            TeamInstance teamInstance = server.getTeamInstance(team);
                            
                            List<String> completions = new ArrayList<>();
                            for (UUID uuid : server.getPlayers()) {
                                Player p = Bukkit.getPlayer(uuid);
                                if (p == null) {
                                    continue;
                                }
                                
                                if (teamInstance.getPlayers().contains(uuid)) {
                                    continue;
                                }
                                
                                completions.add(p.getName());
                            }
                            
                            completions.removeIf(c -> !c.toLowerCase().startsWith(args[0].toLowerCase()));
                            return completions;
                        };
                    }
                }
                
                private class RemoveCmd extends SubCommand<NexusBedWarsPlugin> {
                    public RemoveCmd(GameTeam team) {
                        super(PlayersCmd.this.plugin, PlayersCmd.this, 3, "remove", "Removes a player from the team " + team.getName(), "nexusbedwars.command.teams." + team.getName().toLowerCase() + ".players.remove");
                        this.playerOnly = true;
                        
                        this.executor = (plugin, sender, label, args, flagResults) -> {
                            Player player = (Player) sender;
                            NexusPlayer nexusPlayer = NexusReborn.getPlayerManager().getNexusPlayer(player.getUniqueId());
                            if (nexusPlayer == null) {
                                return true;
                            }
                            
                            NexusServer server = nexusPlayer.getServer();
                            if (!(server instanceof BWVirtualServer bwServer)) {
                                getColors().coloredLegacy(player, "&cYou are not on a Bed Wars Server");
                                return true;
                            }
                            
                            TeamInstance teamInstance = bwServer.getTeamInstance(team);
                            
                            if (!(args.length > 0)) {
                                getColors().coloredLegacy(sender, "&cYou must provide a player to add.");
                                return true;
                            }
                            
                            Player target = Bukkit.getPlayer(args[0]);
                            if (target == null) {
                                getColors().coloredLegacy(player, "&cYou provided an invalid player name. They must be online.");
                                return true;
                            }
                            
                            if (!teamInstance.getPlayers().contains(target.getUniqueId())) {
                                getColors().coloredLegacy(player, "&c" + player.getName() + " is not a member of the team " + team.getName().toUpperCase());
                                return true;
                            }
                            
                            teamInstance.removePlayer(target);
                            getColors().coloredLegacy(player, "&e" + player.getName() + " has been removed from the team " + team.getChatColor() + team.getName().toUpperCase());
                            getColors().coloredLegacy(target, "&eYou have been removed from the team " + team.getChatColor() + team.getName() + " &eby &b" + player.getName());
                            return true;
                        };
                        
                        this.completer = (plugin, sender, label, args, flagResults) -> {
                            if (args.length != 1) {
                                return List.of();
                            }
                            
                            Player player = (Player) sender;
                            NexusPlayer nexusPlayer = NexusReborn.getPlayerManager().getNexusPlayer(player.getUniqueId());
                            if (nexusPlayer == null) {
                                return List.of();
                            }
                            
                            if (!(nexusPlayer.getServer() instanceof BWVirtualServer server)) {
                                return List.of();
                            }
                            
                            TeamInstance teamInstance = server.getTeamInstance(team);
                            
                            List<String> completions = new ArrayList<>();
                            for (UUID uuid : teamInstance.getPlayers()) {
                                Player p = Bukkit.getPlayer(uuid);
                                if (p == null) {
                                    continue;
                                }
                                
                                completions.add(p.getName());
                            }
                            
                            completions.removeIf(c -> !c.toLowerCase().startsWith(args[0].toLowerCase()));
                            return completions;
                        };
                    }
                }
            }
            
            private class UpgradesCmd extends SubCommand<NexusBedWarsPlugin> {
                public UpgradesCmd(GameTeam team) {
                    super(TeamCmd.this.plugin, TeamCmd.this, 2, "upgrades", "Manages the upgrades for the team " + team.getName(), "nexusbedwars.command.teams." + team.getName().toLowerCase() + ".upgrades");
                    
                    for (TeamUpgrade teamUpgrade : TeamUpgrade.REGISTRY) {
                        this.subCommands.add(new UpgradeTypeCmd(team, teamUpgrade));
                    }
                }
                
                private class UpgradeTypeCmd extends SubCommand<NexusBedWarsPlugin> {
                    public UpgradeTypeCmd(GameTeam team, TeamUpgrade upgrade) {
                        super(UpgradesCmd.this.plugin, UpgradesCmd.this, 3, upgrade.getName().toLowerCase().replace(" ", "_"), "Manages the upgrade " + upgrade.getName() + " for the team " + team.getName(), "nexusbedwars.command.teams." + team.getName().toLowerCase() + ".upgrades." + upgrade.getName().toLowerCase());
                        this.subCommands.add(new UpgradeCmd(team, upgrade));
                        this.subCommands.add(new SetLevelCmd(team, upgrade));
                    }
                    
                    private class UpgradeCmd extends SubCommand<NexusBedWarsPlugin> {
                        public UpgradeCmd(GameTeam team, TeamUpgrade upgrade) {
                            super(UpgradeTypeCmd.this.plugin, UpgradeTypeCmd.this, 4, "upgrade", "Upgrades the team " + team.getName() + " with the upgrade " + upgrade.getName(), "nexusbedwars.command.teams." + team.getName().toLowerCase() + ".upgrades." + upgrade.getName().toLowerCase() + ".upgrade");
                            
                            this.executor = (plugin, sender, label, args, flagResults) -> {
                                Player player = (Player) sender;
                                NexusPlayer nexusPlayer = NexusReborn.getPlayerManager().getNexusPlayer(player.getUniqueId());
                                if (nexusPlayer == null) {
                                    return true;
                                }
                                
                                NexusServer server = nexusPlayer.getServer();
                                if (!(server instanceof BWVirtualServer bwServer)) {
                                    getColors().coloredLegacy(player, "&cYou are not on a Bed Wars Server");
                                    return true;
                                }
                                
                                TeamInstance teamInstance = bwServer.getTeamInstance(team);
                                if (!teamInstance.canUpgrade(upgrade)) {
                                    getColors().coloredLegacy(sender, "&cThe team " + team.getName() + " does not have any more " + upgrade.getName() + " upgrades.");
                                    return true;
                                }
                                
                                if (!teamInstance.upgrade(upgrade)) {
                                    getColors().coloredLegacy(sender, "&cFailed to apply the upgrade " + upgrade.getName() + " to the team " + team.getName());
                                    return true;
                                }
                                
                                getColors().coloredLegacy(sender, "&eUpgraded the team " + team.getName() + "'s " + upgrade.getName() + " to level " + teamInstance.getCurrentUpgradeLevel(upgrade).getName());
                                return true;
                            };
                        }
                    }
                    
                    private class SetLevelCmd extends SubCommand<NexusBedWarsPlugin> {
                        public SetLevelCmd(GameTeam team, TeamUpgrade upgrade) {
                            super(UpgradeTypeCmd.this.plugin, UpgradeTypeCmd.this, 4, "setlevel", "Sets the upgrade level of the team " + team.getName() + " with the upgrade " + upgrade.getName(), "nexusbedwars.command.teams." + team.getName().toLowerCase() + ".upgrades." + upgrade.getName().toLowerCase() + ".setlevel");
                            
                            this.executor = (plugin, sender, label, args, flagResults) -> {
                                Player player = (Player) sender;
                                NexusPlayer nexusPlayer = NexusReborn.getPlayerManager().getNexusPlayer(player.getUniqueId());
                                if (nexusPlayer == null) {
                                    return true;
                                }
                                
                                NexusServer server = nexusPlayer.getServer();
                                if (!(server instanceof BWVirtualServer bwServer)) {
                                    getColors().coloredLegacy(player, "&cYou are not on a Bed Wars Server");
                                    return true;
                                }
                                
                                TeamInstance teamInstance = bwServer.getTeamInstance(team);
                                
                                int level;
                                try {
                                    level = Integer.parseInt(args[0]);
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    getColors().coloredLegacy(sender, "&cYou must provide a level number");
                                    return true;
                                } catch (NumberFormatException e) {
                                    getColors().coloredLegacy(sender, "&cInvalid number value: " + args[0]);
                                    return true;
                                }
                                
                                TeamUpgrade.Level upgradeLevel = upgrade.getLevel(level);
                                if (upgradeLevel == null) {
                                    getColors().coloredLegacy(sender, "&cInvalid level: " + level);
                                    return true;
                                }
                                
                                if (!teamInstance.setLevel(upgrade, upgradeLevel)) {
                                    getColors().coloredLegacy(sender, "&cFailed to apply the upgrade " + upgrade.getName() + " to the team " + team.getName());
                                    return true;
                                }
                                
                                getColors().coloredLegacy(sender, "&eUpgraded the team " + team.getName() + "'s " + upgrade.getName() + " to " + teamInstance.getCurrentUpgradeLevel(upgrade).getName());
                                return true;
                            };
                        }
                    }
                }
            }
            
            private class ForgeCmd extends SubCommand<NexusBedWarsPlugin> {
                public ForgeCmd(GameTeam team) {
                    super(TeamCmd.this.plugin, TeamCmd.this, 2, "forge", "Manage the forge of the team " + team.getName().toLowerCase(), "nexusbedwars.command.teams." + team.getName().toLowerCase() + ".forge");
                    this.subCommands.add(new InitCmd(team));
                    this.subCommands.add(new SetTierCmd(team));
                }
                
                private class InitCmd extends SubCommand<NexusBedWarsPlugin> {
                    public InitCmd(GameTeam team) {
                        super(ForgeCmd.this.plugin, ForgeCmd.this, 3, "init", "Initialize the team's forge", "nexusbedwars.command.teams." + team.getName().toLowerCase() + ".forge.init");
                        
                        this.executor = (plugin, sender, label, args, flagResults) -> {
                            Player player = (Player) sender;
                            NexusPlayer nexusPlayer = NexusReborn.getPlayerManager().getNexusPlayer(player.getUniqueId());
                            if (nexusPlayer == null) {
                                return true;
                            }
                            
                            NexusServer server = nexusPlayer.getServer();
                            if (!(server instanceof BWVirtualServer bwServer)) {
                                getColors().coloredLegacy(player, "&cYou are not on a Bed Wars Server");
                                return true;
                            }
                            
                            TeamInstance teamInstance = bwServer.getTeamInstance(team);
                            if (teamInstance.getForge() != null) {
                                getColors().coloredLegacy(player, "&cThat team already has their forge initialized.");
                                return true;
                            }
                            
                            Location location = player.getLocation();
                            Position position = new Position(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                            
                            Position boundsMin, boundsMax;
                            
                            CuboidSelection selection = getWorldEditSelection(player);
                            if (selection == null) {
                                boundsMin = position;
                                boundsMax = position;
                            } else {
                                Position[] points = getPointsFromSelection(selection);
                                boundsMin = points[0];
                                boundsMax = points[1];
                            }
                            
                            IslandForge forge = new IslandForge(server.getName().toLowerCase() + "_" + team.getName().toLowerCase() + "_forge", boundsMin, boundsMax, position);
                            PluginKey key = PluginKey.of(plugin, forge.getName());
                            forge.setKey(key);
                            teamInstance.setForge(forge);
                            NexusBedWarsPlugin.GENERATORS.register(key, forge);
                            forge.init(player.getWorld());
                            getColors().coloredLegacy(sender, "&eYou initialized the island forge for the team " + team.getChatColor() + team.getName().toUpperCase());
                            return true;
                        };
                    }
                }
                
                private class SetTierCmd extends SubCommand<NexusBedWarsPlugin> {
                    public SetTierCmd(GameTeam team) {
                        super(ForgeCmd.this.plugin, ForgeCmd.this, 3, "settier", "Sets the tier of the team's forge", "nexusbedwars.command.teams." + team.getName().toLowerCase() + ".forge.settier");
                        
                        this.executor = (plugin, sender, label, args, flagResults) -> {
                            Player player = (Player) sender;
                            NexusPlayer nexusPlayer = NexusReborn.getPlayerManager().getNexusPlayer(player.getUniqueId());
                            if (nexusPlayer == null) {
                                return true;
                            }
                            
                            NexusServer server = nexusPlayer.getServer();
                            if (!(server instanceof BWVirtualServer bwServer)) {
                                getColors().coloredLegacy(player, "&cYou are not on a Bed Wars Server");
                                return true;
                            }
                            
                            TeamInstance teamInstance = bwServer.getTeamInstance(team);
                            IslandForge forge = teamInstance.getForge();
                            if (forge == null) {
                                getColors().coloredLegacy(player, "&cThat team does not have their forge initialized.");
                                return true;
                            }
                            
                            if (!(args.length > 0)) {
                                getColors().coloredLegacy(player, "&cYou must provide a tier name");
                                return true;
                            }
                            
                            IslandForge.Tier tier;
                            try {
                                tier = IslandForge.Tier.valueOf(args[0].toUpperCase());
                            } catch (Exception e) {
                                getColors().coloredLegacy(player, "&cInvalid Tier Name: " + args[0]);
                                return true;
                            }
                            
                            forge.setTier(tier);
                            getColors().coloredLegacy(sender, "&eYou set " + team.getChatColor() + team.getName().toUpperCase() + "&e's forge to &e" + tier.name() + "&e.");
                            return true;
                        };
                        
                        //TODO Tab Completion for the tier
                    }
                }
            }
            
            private class RegionCmd extends SubCommand<NexusBedWarsPlugin> {
                public RegionCmd(GameTeam team) {
                    super(TeamsCmd.this.plugin, TeamsCmd.this, 2, "region", "Manages the region of the team " + team.getName(), "nexusbedwars.command.teams." + team.getName().toLowerCase() + ".region");
                    this.subCommands.add(new SetCmd(team));
                }
                
                private class SetCmd extends SubCommand<NexusBedWarsPlugin> {
                    public SetCmd(GameTeam team) {
                        super(RegionCmd.this.plugin, RegionCmd.this, 3, "set", "Sets the region to your current WorldEdit selection", "nexusbedwars.command.teams." + team.getName().toLowerCase() + ".region.set");
                        
                        this.executor = (plugin, sender, label, args, flagResults) -> {
                            Player player = (Player) sender;
                            NexusPlayer nexusPlayer = NexusReborn.getPlayerManager().getNexusPlayer(player.getUniqueId());
                            if (nexusPlayer == null) {
                                return true;
                            }
                            
                            NexusServer server = nexusPlayer.getServer();
                            if (!(server instanceof BWVirtualServer bwServer)) {
                                getColors().coloredLegacy(player, "&cYou are not on a Bed Wars Server");
                                return true;
                            }
                            
                            TeamInstance teamInstance = bwServer.getTeamInstance(team);
                            CuboidSelection selection = getWorldEditSelection(player);
                            if (selection == null) {
                                getColors().coloredLegacy(sender, "&cYou do not have a valid WorldEdit Cuboid Selection.");
                                return true;
                            }
                            
                            CuboidRegion region = new CuboidRegion(selection.getNativeMinimumPoint(), selection.getNativeMaximumPoint());
                            teamInstance.setRegion(region);
                            getColors().coloredLegacy(player, "&eYou set the " + team.getChatColor() + team.getName() + "&e's region to your current WorldEdit selection");
                            return true;
                        };
                    }
                }
            }
        }
    }
}
