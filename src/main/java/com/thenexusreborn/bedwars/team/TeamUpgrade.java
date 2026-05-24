package com.thenexusreborn.bedwars.team;

import com.stardevllc.minecraft.Cuboid;
import com.stardevllc.starlib.objects.builder.AbstractBuilder;
import com.stardevllc.starlib.objects.key.*;
import com.stardevllc.starlib.registry.*;
import com.thenexusreborn.bedwars.generator.IslandForge;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents an upgrade for a whole team that is purchased with diamonds <br>
 * Traps will be a separate class/handler and not a part of this one
 */
public class TeamUpgrade implements Keyable {
    public static final IRegistry<TeamUpgrade> REGISTRY = HashRegistry.newBuilder(TeamUpgrade.class)
            .withKey(Keys.of("team_upgrades"))
            .withName("Team Upgrades")
            .allowFreezing()
            .build();
    
    private static final Registerer<TeamUpgrade> REGISTERER = new Registerer<>(REGISTRY);
    
    private static abstract class EnchantmentConsumer implements Consumer<Player> {
        
        private final Enchantment enchantment;
        private final int level;
        
        public EnchantmentConsumer(Enchantment enchantment, int level) {
            this.enchantment = enchantment;
            this.level = level;
        }
        
        protected ItemStack applyEnchant(ItemStack itemStack) {
            if (itemStack == null) {
                return null;
            }
            
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.addEnchant(enchantment, level, false);
                itemStack.setItemMeta(itemMeta);
            }
            return itemStack;
        }
    }
    
    private static final class ProtectionConsumer extends EnchantmentConsumer {
        public ProtectionConsumer(int level) {
            super(Enchantment.PROTECTION_ENVIRONMENTAL, level);
        }
        
        public void accept(Player p) {
            PlayerInventory inv = p.getInventory();
            inv.setHelmet(applyEnchant(inv.getHelmet()));
            inv.setChestplate(applyEnchant(inv.getChestplate()));
            inv.setLeggings(applyEnchant(inv.getLeggings()));
            inv.setBoots(applyEnchant(inv.getBoots()));
        }
    }
    
    private static class FeatherFallingConsumer extends EnchantmentConsumer {
        public FeatherFallingConsumer(int level) {
            super(Enchantment.PROTECTION_FALL, level);
        }
        
        @Override
        public void accept(Player player) {
            player.getInventory().setBoots(applyEnchant(player.getInventory().getBoots()));
        }
    }
    
    public static final RegistryObject<TeamUpgrade> SHARPNESS = REGISTERER.register("sharpness", TeamUpgrade.builder()
            .name("Sharpened Swords")
            .addLevel(Level.builder()
                    .name("Sharpness I")
                    .number(1)
                    .setCost(4, TeamMode.SOLO, TeamMode.DUOS)
                    .setCost(8, TeamMode.TRIOS, TeamMode.QUADS)
                    .playerConsumer(p -> {
                        for (int i = 0; i < p.getInventory().getSize(); i++) {
                            ItemStack itemStack = p.getInventory().getItem(i);
                            if (itemStack == null) {
                                continue;
                            }
                            
                            if (itemStack.getType().name().contains("_SWORD")) {
                                ItemMeta itemMeta = itemStack.getItemMeta();
                                itemMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
                                itemStack.setItemMeta(itemMeta);
                                p.getInventory().setItem(i, itemStack);
                            }
                        }
                    }))
            .build());
    
    public static final RegistryObject<TeamUpgrade> PROTECTION = REGISTERER.register("protection", TeamUpgrade.builder()
            .name("Protection")
            .addLevel(Level.builder().name("Protection I").number(1)
                    .setCost(2, TeamMode.SOLO, TeamMode.DUOS)
                    .setCost(5, TeamMode.TRIOS, TeamMode.QUADS)
                    .playerConsumer(new ProtectionConsumer(1)))
            .addLevel(Level.builder().name("Protection II").number(2)
                    .setCost(4, TeamMode.SOLO, TeamMode.DUOS)
                    .setCost(10, TeamMode.TRIOS, TeamMode.QUADS)
                    .playerConsumer(new ProtectionConsumer(2)))
            .addLevel(Level.builder().name("Protection III").number(3)
                    .setCost(8, TeamMode.SOLO, TeamMode.DUOS)
                    .setCost(20, TeamMode.TRIOS, TeamMode.QUADS)
                    .playerConsumer(new ProtectionConsumer(3)))
            .addLevel(Level.builder().name("Protection IV").number(4)
                    .setCost(16, TeamMode.SOLO, TeamMode.DUOS)
                    .setCost(30, TeamMode.TRIOS, TeamMode.QUADS)
                    .playerConsumer(new ProtectionConsumer(4)))
            .build());
    
    public static final RegistryObject<TeamUpgrade> FORGE = REGISTERER.register("forge", TeamUpgrade.builder()
            .name("Forge")
            .addLevel(Level.builder().name("Iron Forge").number(1)
                    .setCost(2, TeamMode.SOLO, TeamMode.DUOS)
                    .setCost(4, TeamMode.TRIOS, TeamMode.QUADS)
                    .teamConsumer(t -> t.getForge().setTier(IslandForge.Tier.IRON)))
            .addLevel(Level.builder().name("Golden Forge").number(2)
                    .setCost(4, TeamMode.SOLO, TeamMode.DUOS)
                    .setCost(8, TeamMode.TRIOS, TeamMode.QUADS)
                    .teamConsumer(t -> t.getForge().setTier(IslandForge.Tier.GOLD)))
            .addLevel(Level.builder().name("Emerald Forge").number(3)
                    .setCost(6, TeamMode.SOLO, TeamMode.DUOS)
                    .setCost(12, TeamMode.TRIOS, TeamMode.QUADS)
                    .teamConsumer(t -> t.getForge().setTier(IslandForge.Tier.EMERALD)))
            .addLevel(Level.builder().name("Molten Forge").number(4)
                    .setCost(8, TeamMode.SOLO, TeamMode.DUOS)
                    .setCost(16, TeamMode.TRIOS, TeamMode.QUADS)
                    .teamConsumer(t -> t.getForge().setTier(IslandForge.Tier.MOLTEN)))
            .build());
    
    public static final RegistryObject<TeamUpgrade> HASTE = REGISTERER.register("haste", TeamUpgrade.builder()
            .name("Maniac Miner")
            .addLevel(Level.builder().name("Haste I").number(1)
                    .setCost(2, TeamMode.SOLO, TeamMode.DUOS)
                    .setCost(4, TeamMode.TRIOS, TeamMode.QUADS)
                    .playerConsumer(p -> p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 0, true, false))))
            .addLevel(Level.builder().name("Haste II").number(2)
                    .setCost(4, TeamMode.SOLO, TeamMode.DUOS)
                    .setCost(6, TeamMode.TRIOS, TeamMode.QUADS)
                    .playerConsumer(p -> p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 1, true, false))))
            .build());
    
    public static final RegistryObject<TeamUpgrade> HEAL_POOL = REGISTERER.register("heal_pool", TeamUpgrade.builder()
            .name("Heal Pool")
            .addLevel(Level.builder().name("Regeneration I").number(1)
                    .setCost(1, TeamMode.SOLO, TeamMode.DUOS)
                    .setCost(2, TeamMode.TRIOS, TeamMode.QUADS)
                    .repeating().teamConsumer(t -> {
                for (UUID uuid : t.getPlayers()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        continue;
                    }
                    
                    Cuboid region = t.getRegion();
                    if (region.contains(player.getLocation())) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 0, false, false));
                    } else {
                        player.removePotionEffect(PotionEffectType.REGENERATION);
                    }
                }
            }))
            .build());
    
    public static final RegistryObject<TeamUpgrade> FEATHER_FALLING = REGISTERER.register("feather_falling", TeamUpgrade.builder()
            .name("Cushioned Boots")
            .addLevel(Level.builder().name("Feather Falling I").number(1)
                    .setCost(2, TeamMode.SOLO, TeamMode.DUOS)
                    .setCost(4, TeamMode.TRIOS, TeamMode.QUADS)
                    .playerConsumer(new FeatherFallingConsumer(1)))
            .addLevel(Level.builder().name("Feather Falling II").number(2)
                    .setCost(2, TeamMode.SOLO, TeamMode.DUOS)
                    .setCost(6, TeamMode.TRIOS, TeamMode.QUADS)
                    .playerConsumer(new FeatherFallingConsumer(2)))
            .build());
    
    static {
        REGISTRY.freeze();
    }
    
    private Key key;
    private String name;
    
    private final SortedMap<Integer, Level> levels = new TreeMap<>();
    
    public TeamUpgrade(String name) {
        this.name = name;
    }
    
    public TeamUpgrade(String name, SortedMap<Integer, Level> levels) {
        this.name = name;
        this.levels.putAll(levels);
    }
    
    public SortedMap<Integer, Level> getLevels() {
        return new TreeMap<>(levels);
    }
    
    public void addLevel(Level level) {
        if (!levels.containsKey(level.getNumber())) {
            levels.put(level.getNumber(), level);
        }
    }
    
    public void addLevel(Level.Builder levelBuilder) {
        addLevel(levelBuilder.build());
    }
    
    public void setLevel(Level level) {
        levels.put(level.getNumber(), level);
    }
    
    public void setLevel(Level.Builder levelBuilder) {
        setLevel(levelBuilder.build());
    }
    
    public Level getLevel(int number) {
        return levels.get(number);
    }
    
    public Level getNextLevel(int current) {
        if (this.levels.containsKey(current + 1)) {
            return levels.get(current + 1);
        }
        
        return null;
    }
    
    public String getName() {
        return name;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder extends AbstractBuilder<TeamUpgrade, Builder> {
        
        private String name;
        private final SortedMap<Integer, Level> levels = new TreeMap<>();
        
        public Builder() {
        }
        
        public Builder(Builder builder) {
            super(builder);
            this.name = builder.name;
            this.levels.putAll(builder.levels);
        }
        
        public Builder name(String name) {
            this.name = name;
            return self();
        }
        
        public Builder addLevel(Level level) {
            this.levels.put(level.getNumber(), level);
            return self();
        }
        
        public Builder addLevel(Level.Builder levelBuilder) {
            return addLevel(levelBuilder.build());
        }
        
        @Override
        public TeamUpgrade build() {
            return new TeamUpgrade(name, levels);
        }
        
        @Override
        public Builder clone() {
            return new Builder(this);
        }
    }
    
    /**
     * Represents a level of a team upgrade. Each Upgrade must have 1 level minimum to be valid
     */
    public static class Level {
        private String name;
        private int number;
        private boolean repeating;
        private Map<TeamMode, Integer> costs = new EnumMap<>(TeamMode.class); //rename to cost
        private Consumer<Player> playerConsumer;
        private Consumer<TeamInstance> teamConsumer;
        
        public Level(String name, int number, boolean repeating, Map<TeamMode, Integer> costs, Consumer<Player> playerConsumer, Consumer<TeamInstance> teamConsumer) {
            this.name = name;
            this.number = number;
            this.repeating = repeating;
            this.costs.putAll(costs);
            this.playerConsumer = playerConsumer;
            this.teamConsumer = teamConsumer;
        }
        
        public int getNumber() {
            return number;
        }
        
        public boolean isRepeating() {
            return repeating;
        }
        
        public Map<TeamMode, Integer> getCosts() {
            return costs;
        }
        
        public String getName() {
            return name;
        }
        
        public Consumer<Player> getPlayerConsumer() {
            return playerConsumer;
        }
        
        public Consumer<TeamInstance> getTeamConsumer() {
            return teamConsumer;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder extends AbstractBuilder<Level, Builder> {
            private String name;
            private int number;
            private boolean repeating;
            private Map<TeamMode, Integer> costs = new EnumMap<>(TeamMode.class); //rename to cost
            private Consumer<Player> playerConsumer;
            private Consumer<TeamInstance> teamConsumer;
            
            public Builder() {
            }
            
            public Builder(Builder builder) {
                super(builder);
                this.name = builder.name;
                this.number = builder.number;
                this.repeating = builder.repeating;
                this.costs.putAll(builder.costs);
                this.playerConsumer = builder.playerConsumer;
                this.teamConsumer = builder.teamConsumer;
            }
            
            public Builder name(String name) {
                this.name = name;
                return self();
            }
            
            public Builder number(int number) {
                this.number = number;
                return self();
            }
            
            public Builder setCost(TeamMode mode, int cost) {
                this.costs.put(mode, cost);
                return self();
            }
            
            public Builder setCost(int cost, TeamMode mode, TeamMode... teamModes) {
                this.costs.put(mode, cost);
                if (teamModes != null) {
                    for (TeamMode teamMode : teamModes) {
                        this.costs.put(teamMode, cost);
                    }
                }
                
                return self();
            }
            
            public Builder repeating() {
                this.repeating = true;
                return self();
            }
            
            public Builder single() {
                this.repeating = false;
                return self();
            }
            
            public Builder playerConsumer(Consumer<Player> consumer) {
                this.playerConsumer = consumer;
                return self();
            }
            
            public Builder teamConsumer(Consumer<TeamInstance> teamConsumer) {
                this.teamConsumer = teamConsumer;
                return self();
            }
            
            @Override
            public Level build() {
                return new Level(name, number, this.repeating, costs, playerConsumer, teamConsumer);
            }
            
            @Override
            public Builder clone() {
                return new Builder(this);
            }
        }
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