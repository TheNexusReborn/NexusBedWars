package com.thenexusreborn.bedwars;

import com.stardevllc.starlib.function.ClassFilterPredicate;

public class GeneratorFilter extends ClassFilterPredicate<BedwarsGenerator> {
    public GeneratorFilter(String[] args) {
        if (args == null || args.length < 1) {
            addToFilter(BedwarsGenerator.class);
        } else {
            String[] split = args[0].split(",");
            for (String s : split) {
                switch (s.toLowerCase()) {
                    case "emerald", "e" -> addToFilter(EmeraldGenerator.class);
                    case "diamond", "d" -> addToFilter(DiamondGenerator.class);
                    case "forge", "f" -> addToFilter(IslandForge.class);
                }
            }
        }
    }
}