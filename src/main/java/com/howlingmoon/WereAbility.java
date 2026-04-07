// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.util.StringRepresentable;

public enum WereAbility implements StringRepresentable {
    HOWL("howl", "Howl", "A terrifying roar that weakens nearby enemies and forces them to flee in panic.", 1, 600),
    LEAP("leap", "Leap", "Dash forward with great force. Grants temporary fall damage immunity.", 1, 100),
    BITE("bite", "Bite", "A vicious bite that deals high damage, poisons the target, and heals you slightly.", 2, 80),
    CLIMB("climb", "Climb", "Extend your claws to scale walls. Sneak to slide down, release to hang in place.", 1, 0),
    FEAR("fear", "Fear", "Strike terror into the hearts of enemies in a large radius, crippling their movement.", 2,
            400),
    BERSERK("berserk", "Berserk",
            "Enter a state of primal rage. Massively increases damage and regeneration, but drains hunger.", 3, 1200),
    MAIM("maim", "Maim", "A crippling strike that heavily slows and weakens a single target.", 2, 150),
    SHRED("shred", "Shred", "Unleash a flurry of rapid claw attacks in a cone in front of you.", 2, 60),
    RAM("ram", "Ram", "Charge forward. Colliding with enemies during the dash knocks them away violently.", 2, 120),
    LIFT("lift", "Lift", "Grab a target in front of you and violently throw them into the air.", 3, 200),
    NIGHT_VISION("night_vision", "Night Vision", "Toggleable. See clearly in the absolute dark.", 1, 0),
    SCENT_TRACKING("scent_tracking", "Scent Tracking",
            "Activate your senses for 30s. Reveals the glowing trails of living creatures through walls.", 2, 600);

    private final String name;
    private final String displayName;
    private final String description;
    private final int cost;
    private final int baseCooldown; // In ticks

    WereAbility(String name, String displayName, String description, int cost, int baseCooldown) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.cost = cost;
        this.baseCooldown = baseCooldown;
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

    public int getCost() {
        return cost;
    }

    public int getBaseCooldown() {
        return baseCooldown;
    }
}