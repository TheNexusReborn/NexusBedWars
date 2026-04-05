package com.thenexusreborn.bedwars;

import com.stardevllc.minecraft.registry.PluginRegisterer;
import com.stardevllc.minecraft.registry.PluginRegistry;
import com.stardevllc.minecraft.smaterial.SMaterial;
import com.stardevllc.stargenerators.StarGenerators;
import com.stardevllc.stargenerators.model.ItemEntry;
import com.stardevllc.starcore.ItemBuilders;
import com.stardevllc.starlib.objects.key.Keys;
import com.stardevllc.starlib.registry.*;

public final class Resource {
    public static final IRegistry<ItemEntry> REGISTRY = PluginRegistry.builder(ItemEntry.class)
            .withParent(StarGenerators.ITEMS)
            .withId(Keys.of("resources"))
            .withName("Resources")
            .allowFreezing()
            .build();
    
    private static final Registerer<ItemEntry> REGISTERER = PluginRegisterer.create(REGISTRY, BedWars.getPlugin(BedWars.class));
    
    public static final RegistryObject<ItemEntry> IRON = REGISTERER.register("iron", new ItemEntry(ItemBuilders.of(SMaterial.IRON_INGOT), ItemEntry.Flag.PERSISTENT, ItemEntry.Flag.INVULNERABLE));
    public static final RegistryObject<ItemEntry> GOLD = REGISTERER.register("gold", new ItemEntry(ItemBuilders.of(SMaterial.GOLD_INGOT), ItemEntry.Flag.PERSISTENT, ItemEntry.Flag.INVULNERABLE));
    public static final RegistryObject<ItemEntry> DIAMOND = REGISTERER.register("diamond", new ItemEntry(ItemBuilders.of(SMaterial.DIAMOND), ItemEntry.Flag.PERSISTENT, ItemEntry.Flag.INVULNERABLE));
    public static final RegistryObject<ItemEntry> EMERALD = REGISTERER.register("emerald", new ItemEntry(ItemBuilders.of(SMaterial.EMERALD), ItemEntry.Flag.PERSISTENT, ItemEntry.Flag.INVULNERABLE));
    
    static {
        REGISTRY.freeze();
    }
}