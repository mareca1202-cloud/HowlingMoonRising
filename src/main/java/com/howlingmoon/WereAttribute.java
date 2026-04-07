// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

public enum WereAttribute {

    STRENGTH("attribute.strength", 5),
    PROTECTION("attribute.protection", 5),
    SPEED("attribute.speed", 5),
    JUMP("attribute.jump", 3),
    FALL("attribute.fall", 3),
    KNOCKBACK("attribute.knockback", 3),
    KNOCKRESIST("attribute.knockresist", 3),
    HUNGER("attribute.hunger", 5),
    REGENERATION("attribute.regeneration", 5),
    CLARITY("attribute.clarity", 3),
    EXHILARATING("attribute.exhilarating", 3),
    RESISTANCE("attribute.resistance", 3);

    private final String key;
    private final int maxLevel;

    WereAttribute(String key, int maxLevel) {
        this.key = key;
        this.maxLevel = maxLevel;
    }

    public String getKey() {
        return key;
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}