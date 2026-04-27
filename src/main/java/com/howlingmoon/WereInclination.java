// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.util.StringRepresentable;

public enum WereInclination implements StringRepresentable {
    NEUTRAL("neutral", "Neutral", "Balanced progression with steady XP gain."),
    SKILLFUL("skillful", "Skillful (The Wind Spirit)",
            "Mastery of agility and abilities.\n- Zen Reset: 15% chance to ignore ability cooldowns.\n- Flow State: Using abilities grants Jump Boost.\n- Ethereal Reach: Increased melee range.\n- 25% faster base cooldowns and slightly faster movement."),
    MASTERY("mastery", "Mastery (The Lunar Colossus)",
            "Unstoppable physical force.\n- Siege Claws: Mine stone and ores barehanded.\n- Sweep Attack: Melee hits deal area damage.\n- Immovable: Massive knockback resistance.\n- Aegis Plate: Takes 30% less projectile damage.\n- Bonus scaling to Strength and Protection."),
    PREDATOR("predator", "Predator (The Blood Hunter)",
            "Lethal speed, but driven by eternal hunger.\n- Thrill of the Hunt: Kills grant Speed II and Strength.\n- Blood Feed: Kills restore hunger and health.\n- Apex Speed: Permanent 15% movement speed bonus.\n- Lunar Frenzy: Enters a furious frenzy during full moon nights.\n- Double XP gain, but is constantly much hungrier.");

    private final String name;
    private final String displayName;
    private final String description;

    WereInclination(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}