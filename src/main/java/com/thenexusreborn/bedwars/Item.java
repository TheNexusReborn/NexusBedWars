package com.thenexusreborn.bedwars;

import com.stardevllc.starlib.objects.key.Key;
import com.stardevllc.starlib.objects.key.Keyable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Item extends Keyable {
    
    ItemStack getItemStack(Player player);
    
    @Override
    default boolean supportsSettingKey() {
        return true;
    }
    
    @Override
    void setKey(Key key);
}
