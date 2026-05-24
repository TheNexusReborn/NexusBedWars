package com.thenexusreborn.bedwars.game;

import com.stardevllc.starlib.objects.key.Key;
import com.stardevllc.starlib.objects.key.Keyable;

/**
 * This represents a full game that stores and controls progression and things
 */
public class Game implements Keyable {
    
    private Key key;
    
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