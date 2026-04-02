package com.thenexusreborn.bedwars;

import com.stardevllc.starlib.objects.key.Keys;
import com.stardevllc.starlib.registry.*;

public final class GameTeams {
    private GameTeams() {}
    
    public static final IRegistry<GameTeam> REGISTRY = HashRegistry.builder(GameTeam.class)
            .checkPartialInGet()
            .allowFreezing()
            .withName("Teams")
            .withId(Keys.of("teams"))
            .build();
    
    private static final Registerer<GameTeam> REGISTERER = Registerer.create(REGISTRY);
    
    public static final RegistryObject<GameTeam> RED = REGISTERER.register("red", new GameTeam(GameTeam.TeamColor.RED));
    public static final RegistryObject<GameTeam> BLUE = REGISTERER.register("blue", new GameTeam(GameTeam.TeamColor.BLUE));
    public static final RegistryObject<GameTeam> GREEN = REGISTERER.register("green", new GameTeam(GameTeam.TeamColor.GREEN));
    public static final RegistryObject<GameTeam> YELLOW = REGISTERER.register("yellow", new GameTeam(GameTeam.TeamColor.YELLOW));
    public static final RegistryObject<GameTeam> AQUA = REGISTERER.register("aqua", new GameTeam(GameTeam.TeamColor.AQUA));
    public static final RegistryObject<GameTeam> PINK = REGISTERER.register("pink", new GameTeam(GameTeam.TeamColor.PINK));
    public static final RegistryObject<GameTeam> ORANGE = REGISTERER.register("orange", new GameTeam(GameTeam.TeamColor.ORANGE));
    public static final RegistryObject<GameTeam> PURPLE = REGISTERER.register("purple", new GameTeam(GameTeam.TeamColor.PURPLE));
    
    static {
        REGISTRY.freeze();
    }
    
    public static GameTeam get(String string) {
        return REGISTRY.get(string);
    }
    
    public static GameTeam get(GameTeam.TeamColor color) {
        for (GameTeam gameTeam : REGISTRY) {
            if (gameTeam.getTeamColor() == color) {
                return gameTeam;
            }
        }
        
        return null;
    }
}