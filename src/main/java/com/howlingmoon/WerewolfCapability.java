// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import java.util.HashMap;
import java.util.Map;

public class WerewolfCapability {

    private boolean isWerewolf = false;
    private boolean isTransformed = false;
    private boolean moonForced = false;
    private boolean isLeaping = false;

    private int level = 1;
    private int experience = 0;
    private int usedAttributePoints = 0;
    private int usedAbilityPoints = 0;

    private java.util.Set<WereAbility> unlockedAbilities = java.util.EnumSet.noneOf(WereAbility.class);
    private WereAbility selectedAbility = null;
    private WereInclination inclination = WereInclination.NEUTRAL;
    private java.util.Set<Integer> completedTrials = new java.util.HashSet<>();

    private Map<String, Integer> attributeTree = new HashMap<>();
    private Map<WereAbility, Integer> cooldowns = new HashMap<>();

    public boolean isWerewolf() {
        return isWerewolf;
    }

    public void setWerewolf(boolean werewolf) {
        isWerewolf = werewolf;
    }

    public boolean isTransformed() {
        return isTransformed;
    }

    public void setTransformed(boolean transformed) {
        isTransformed = transformed;
    }

    public boolean isMoonForced() {
        return moonForced;
    }

    public void setMoonForced(boolean moonForced) {
        this.moonForced = moonForced;
    }

    public boolean isLeaping() {
        return isLeaping;
    }

    public void setLeaping(boolean leaping) {
        this.isLeaping = leaping;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void forceSetLevel(int newLevel) {
        this.level = Math.max(1, Math.min(newLevel, LEVEL_CAP));
        this.experience = 0;
        for (int i = 5; i <= this.level; i += 5) {
            completeTrial(i);
        }
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getUsedAttributePoints() {
        return usedAttributePoints;
    }

    public void setUsedAttributePoints(int points) {
        this.usedAttributePoints = points;
    }

    public int getUsedAbilityPoints() {
        return usedAbilityPoints;
    }

    public void setUsedAbilityPoints(int points) {
        this.usedAbilityPoints = points;
    }

    public java.util.Set<WereAbility> getUnlockedAbilities() {
        return unlockedAbilities;
    }

    public void setUnlockedAbilities(java.util.Set<WereAbility> abilities) {
        this.unlockedAbilities = abilities;
    }

    public WereAbility getSelectedAbility() {
        return selectedAbility;
    }

    public void setSelectedAbility(WereAbility ability) {
        this.selectedAbility = ability;
    }

    public WereInclination getInclination() {
        return inclination;
    }

    public void setInclination(WereInclination inclination) {
        this.inclination = inclination;
    }

    public java.util.Set<Integer> getCompletedTrials() {
        return completedTrials;
    }

    public void setCompletedTrials(java.util.Set<Integer> trials) {
        this.completedTrials = trials;
    }

    public boolean hasCompletedTrialFor(int level) {
        return completedTrials.contains(level);
    }

    public void completeTrial(int level) {
        completedTrials.add(level);
    }

    private static final int LEVEL_CAP = 20;

    public int expNeededForNextLevel() {
        return 50 + (level * 30);
    }

    public void addExperience(int amount) {
        if (!isTransformed)
            return;
        experience += amount;
        while (experience >= expNeededForNextLevel() && level < LEVEL_CAP) {
            if (level % 5 == 0 && !hasCompletedTrialFor(level)) {
                experience = Math.min(experience, expNeededForNextLevel() - 1);
                break;
            }
            experience -= expNeededForNextLevel();
            level++;
        }
    }

    public int getAvailableAttributePoints() {
        return level - usedAttributePoints;
    }

    public int getAvailableAbilityPoints() {
        return level - usedAbilityPoints;
    }

    public boolean canUnlockAbility(WereAbility ability) {
        return !unlockedAbilities.contains(ability) && getAvailableAbilityPoints() >= ability.getCost();
    }

    public void unlockAbility(WereAbility ability) {
        if (!canUnlockAbility(ability))
            return;
        unlockedAbilities.add(ability);
        usedAbilityPoints += ability.getCost();
        if (selectedAbility == null)
            selectedAbility = ability;
    }

    public int getAttributeLevel(WereAttribute attribute) {
        return attributeTree.getOrDefault(attribute.getKey(), 0);
    }

    public boolean canUpgradeAttribute(WereAttribute attribute) {
        return getAvailableAttributePoints() > 0 && getAttributeLevel(attribute) < attribute.getMaxLevel();
    }

    public void upgradeAttribute(WereAttribute attribute) {
        if (!canUpgradeAttribute(attribute))
            return;
        int current = getAttributeLevel(attribute);
        attributeTree.put(attribute.getKey(), current + 1);
        usedAttributePoints++;
    }

    public Map<String, Integer> getAttributeTree() {
        return attributeTree;
    }

    public void setAttributeTree(Map<String, Integer> tree) {
        this.attributeTree = tree;
    }

    public void setCooldown(WereAbility ability, int ticks) {
        if (ticks <= 0)
            cooldowns.remove(ability);
        else
            cooldowns.put(ability, ticks);
    }

    public int getCooldown(WereAbility ability) {
        return cooldowns.getOrDefault(ability, 0);
    }

    public void tickCooldowns() {
        if (cooldowns.isEmpty())
            return;
        cooldowns.entrySet().removeIf(entry -> {
            int timeLeft = entry.getValue() - 1;
            entry.setValue(timeLeft);
            return timeLeft <= 0;
        });
    }

    // --- RESET TOTAL PARA CURACIÓN ---
    public void reset() {
        this.isWerewolf = false;
        this.isTransformed = false;
        this.moonForced = false;
        this.isLeaping = false;
        this.level = 1;
        this.experience = 0;
        this.usedAttributePoints = 0;
        this.usedAbilityPoints = 0;
        this.unlockedAbilities.clear();
        this.selectedAbility = null;
        this.inclination = WereInclination.NEUTRAL; // RESET DE SENDA
        this.attributeTree.clear();
        this.cooldowns.clear();
        this.completedTrials.clear();
    }
}