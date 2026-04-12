// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class WerewolfAttachment {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, HowlingMoon.MODID);

    public static final Supplier<AttachmentType<WerewolfCapability>> WEREWOLF_DATA = ATTACHMENT_TYPES
            .register("werewolf_data", () -> AttachmentType.builder(WerewolfCapability::new)
                    .serialize(
                            new net.neoforged.neoforge.attachment.IAttachmentSerializer<CompoundTag, WerewolfCapability>() {

                                @Override
                                public WerewolfCapability read(
                                        net.neoforged.neoforge.attachment.IAttachmentHolder holder, CompoundTag tag,
                                        net.minecraft.core.HolderLookup.Provider provider) {
                                    WerewolfCapability cap = new WerewolfCapability();
                                    cap.setWerewolf(tag.getBoolean("isWerewolf"));
                                    cap.setTransformed(tag.getBoolean("isTransformed"));
                                    cap.setInfected(tag.getBoolean("isInfected")); // PERSISTENCIA DE INFECCIÓN
                                    cap.setMoonForced(tag.getBoolean("moonForced"));
                                    cap.setLevel(tag.getInt("level"));
                                    cap.setExperience(tag.getInt("experience"));
                                    cap.setUsedAttributePoints(tag.getInt("usedAttributePoints"));
                                    cap.setUsedAbilityPoints(tag.getInt("usedAbilityPoints"));

                                    if (tag.contains("selectedAbility")) {
                                        try {
                                            cap.setSelectedAbility(
                                                    WereAbility.valueOf(tag.getString("selectedAbility")));
                                        } catch (Exception ignored) {
                                        }
                                    }

                                    java.util.Set<WereAbility> unlocked = java.util.EnumSet.noneOf(WereAbility.class);
                                    CompoundTag unlockedTag = tag.getCompound("unlockedAbilities");
                                    for (String key : unlockedTag.getAllKeys()) {
                                        try {
                                            unlocked.add(WereAbility.valueOf(key));
                                        } catch (Exception ignored) {
                                        }
                                    }
                                    cap.setUnlockedAbilities(unlocked);

                                    Map<String, Integer> tree = new HashMap<>();
                                    CompoundTag treeTag = tag.getCompound("attributeTree");
                                    for (String key : treeTag.getAllKeys())
                                        tree.put(key, treeTag.getInt(key));
                                    cap.setAttributeTree(tree);

                                    java.util.Set<Integer> completedTrials = new java.util.HashSet<>();
                                    int[] trialsArray = tag.getIntArray("completedTrials");
                                    for (int t : trialsArray)
                                        completedTrials.add(t);
                                    cap.setCompletedTrials(completedTrials);

                                    if (tag.contains("inclination")) {
                                        try {
                                            cap.setInclination(WereInclination.valueOf(tag.getString("inclination")));
                                        } catch (Exception ignored) {
                                        }
                                    }

                                    return cap;
                                }

                                @Override
                                public CompoundTag write(WerewolfCapability cap,
                                        net.minecraft.core.HolderLookup.Provider provider) {
                                    CompoundTag tag = new CompoundTag();
                                    tag.putBoolean("isWerewolf", cap.isWerewolf());
                                    tag.putBoolean("isTransformed", cap.isTransformed());
                                    tag.putBoolean("isInfected", cap.isInfected()); // GUARDADO DE INFECCIÓN
                                    tag.putBoolean("moonForced", cap.isMoonForced());
                                    tag.putInt("level", cap.getLevel());
                                    tag.putInt("experience", cap.getExperience());
                                    tag.putInt("usedAttributePoints", cap.getUsedAttributePoints());
                                    tag.putInt("usedAbilityPoints", cap.getUsedAbilityPoints());

                                    if (cap.getSelectedAbility() != null)
                                        tag.putString("selectedAbility", cap.getSelectedAbility().name());
                                    CompoundTag unlockedTag = new CompoundTag();
                                    for (WereAbility ability : cap.getUnlockedAbilities())
                                        unlockedTag.putBoolean(ability.name(), true);
                                    tag.put("unlockedAbilities", unlockedTag);

                                    CompoundTag treeTag = new CompoundTag();
                                    for (Map.Entry<String, Integer> entry : cap.getAttributeTree().entrySet())
                                        treeTag.putInt(entry.getKey(), entry.getValue());
                                    tag.put("attributeTree", treeTag);

                                    tag.putIntArray("completedTrials",
                                            cap.getCompletedTrials().stream().mapToInt(Integer::intValue).toArray());
                                    if (cap.getInclination() != null)
                                        tag.putString("inclination", cap.getInclination().name());

                                    return tag;
                                }
                            })
                    .build());
}