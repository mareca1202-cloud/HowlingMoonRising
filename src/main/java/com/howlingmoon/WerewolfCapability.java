// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import java.util.HashMap;
import java.util.Map;

public class WerewolfCapability {

    private boolean isWerewolf = false;
    private boolean isTransformed = false;
    private boolean moonForced = false;

    private int level = 1;
    private int experience = 0;
    private int usedAttributePoints = 0;

    private Map<String, Integer> attributeTree = new HashMap<>();

    public boolean isWerewolf() { return isWerewolf; }
    public void setWerewolf(boolean werewolf) { isWerewolf = werewolf; }

    public boolean isTransformed() { return isTransformed; }
    public void setTransformed(boolean transformed) { isTransformed = transformed; }

    public boolean isMoonForced() { return moonForced; }
    public void setMoonForced(boolean moonForced) { this.moonForced = moonForced; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public int getUsedAttributePoints() { return usedAttributePoints; }
    public void setUsedAttributePoints(int points) { this.usedAttributePoints = points; }

    private static final int LEVEL_CAP = 20;

    public int expNeededForNextLevel() {
        return 50 + (level * 30);
    }

    public void addExperience(int amount) {
        if (!isTransformed) return;
        experience += amount;
        while (experience >= expNeededForNextLevel() && level < LEVEL_CAP) {
            experience -= expNeededForNextLevel();
            level++;
        }
        if (level >= LEVEL_CAP) {
            experience = 0;
        }
    }

    public int getAvailableAttributePoints() {
        return level - usedAttributePoints;
    }

    public int getAttributeLevel(WereAttribute attribute) {
        return attributeTree.getOrDefault(attribute.getKey(), 0);
    }

    public boolean canUpgradeAttribute(WereAttribute attribute) {
        return getAvailableAttributePoints() > 0
                && getAttributeLevel(attribute) < attribute.getMaxLevel();
    }

    public void upgradeAttribute(WereAttribute attribute) {
        if (!canUpgradeAttribute(attribute)) return;
        int current = getAttributeLevel(attribute);
        attributeTree.put(attribute.getKey(), current + 1);
        usedAttributePoints++;
    }

    public Map<String, Integer> getAttributeTree() { return attributeTree; }
    public void setAttributeTree(Map<String, Integer> tree) { this.attributeTree = tree; }

    public void resetAttributePoints() {
        attributeTree.clear();
        usedAttributePoints = 0;
    }

    public void reset() {
        isWerewolf = false;
        isTransformed = false;
        moonForced = false;
        level = 1;
        experience = 0;
        usedAttributePoints = 0;
        attributeTree.clear();
    }
}