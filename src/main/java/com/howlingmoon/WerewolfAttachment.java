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

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HowlingMoon.MODID);

    public static final Supplier<AttachmentType<WerewolfCapability>> WEREWOLF_DATA =
            ATTACHMENT_TYPES.register("werewolf_data", () ->
                    AttachmentType.builder(WerewolfCapability::new)
                            .serialize(new net.neoforged.neoforge.attachment.IAttachmentSerializer<CompoundTag, WerewolfCapability>() {

                                @Override
                                public WerewolfCapability read(net.neoforged.neoforge.attachment.IAttachmentHolder holder, CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
                                    WerewolfCapability cap = new WerewolfCapability();
                                    cap.setWerewolf(tag.getBoolean("isWerewolf"));
                                    cap.setTransformed(tag.getBoolean("isTransformed"));
                                    cap.setMoonForced(tag.getBoolean("moonForced"));
                                    cap.setLevel(tag.getInt("level"));
                                    cap.setExperience(tag.getInt("experience"));
                                    cap.setUsedAttributePoints(tag.getInt("usedAttributePoints"));

                                    Map<String, Integer> tree = new HashMap<>();
                                    CompoundTag treeTag = tag.getCompound("attributeTree");
                                    for (String key : treeTag.getAllKeys()) {
                                        tree.put(key, treeTag.getInt(key));
                                    }
                                    cap.setAttributeTree(tree);

                                    return cap;
                                }

                                @Override
                                public CompoundTag write(WerewolfCapability cap, net.minecraft.core.HolderLookup.Provider provider) {
                                    CompoundTag tag = new CompoundTag();
                                    tag.putBoolean("isWerewolf", cap.isWerewolf());
                                    tag.putBoolean("isTransformed", cap.isTransformed());
                                    tag.putBoolean("moonForced", cap.isMoonForced());
                                    tag.putInt("level", cap.getLevel());
                                    tag.putInt("experience", cap.getExperience());
                                    tag.putInt("usedAttributePoints", cap.getUsedAttributePoints());

                                    CompoundTag treeTag = new CompoundTag();
                                    for (Map.Entry<String, Integer> entry : cap.getAttributeTree().entrySet()) {
                                        treeTag.putInt(entry.getKey(), entry.getValue());
                                    }
                                    tag.put("attributeTree", treeTag);

                                    return tag;
                                }
                            })
                            .build());
}