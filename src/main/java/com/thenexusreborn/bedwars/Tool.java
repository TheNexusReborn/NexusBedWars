package com.thenexusreborn.bedwars;

import com.stardevllc.itembuilder.common.ItemBuilder;
import com.stardevllc.smaterial.SMaterial;
import com.stardevllc.staritems.ItemBuilders;
import com.stardevllc.starlib.objects.builder.IBuilder;
import com.stardevllc.starlib.objects.key.Key;
import com.stardevllc.starlib.objects.key.Keyable;
import org.bukkit.inventory.ItemFlag;

import java.util.*;

public class Tool implements Keyable {
    
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
    public boolean supportsSettingKey() {
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