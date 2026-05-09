package com.ocif.keepinventorybylevel;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue START_LEVEL;
    public static final ModConfigSpec.IntValue LEVELS_PER_SLOT;
    public static final ModConfigSpec.ConfigValue<List<String>> SLOT_ORDER;

    public static final List<String> DEFAULT_SLOT_ORDER = List.of(
            "offhand",
            "hotbar.0", "hotbar.1", "hotbar.2", "hotbar.3", "hotbar.4", "hotbar.5", "hotbar.6", "hotbar.7", "hotbar.8",
            "armor.head", "armor.chest", "armor.legs", "armor.feet",
            "inventory.0", "inventory.1", "inventory.2", "inventory.3", "inventory.4", "inventory.5",
            "inventory.6", "inventory.7", "inventory.8", "inventory.9", "inventory.10", "inventory.11",
            "inventory.12", "inventory.13", "inventory.14", "inventory.15", "inventory.16", "inventory.17",
            "inventory.18", "inventory.19", "inventory.20", "inventory.21", "inventory.22", "inventory.23",
            "inventory.24", "inventory.25", "inventory.26"
    );

    public static final ModConfigSpec.ConfigValue<String> XP_LOSS;

    static {
        BUILDER.push("KeepInventoryByLevel");

        START_LEVEL = BUILDER
                .comment("The experience level at which inventory protection begins.")
                .defineInRange("startLevel", 10, 0, Integer.MAX_VALUE);

        LEVELS_PER_SLOT = BUILDER
                .comment("How many levels are required to protect one additional slot.")
                .defineInRange("levelsPerSlot", 1, 1, Integer.MAX_VALUE);

        SLOT_ORDER = BUILDER
                .comment("The order in which slots are protected. Valid entries: offhand, hotbar.0-8, armor.head/chest/legs/feet, inventory.0-26")
                .define("slotOrder", DEFAULT_SLOT_ORDER);

        XP_LOSS = BUILDER
                .comment("""
                        Experience loss on death. Supported formats:
                          "50%"   - lose percentage of total experience points (e.g. 1000XP -> 500XP)
                          "10l"   - lose fixed number of levels (e.g. lose 10 levels)
                          "100"   - lose fixed amount of experience points
                          ""      - vanilla behavior (default drop rule)
                        Default: "50%" (lose half of total experience points)""")
                .define("xpLoss", "50%");

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
