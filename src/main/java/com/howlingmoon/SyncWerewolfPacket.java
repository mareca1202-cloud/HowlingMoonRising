// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SyncWerewolfPacket(
        boolean isWerewolf,
        boolean isTransformed,
        boolean isInfected, // NUEVO
        int level,
        int experience,
        int usedAttributePoints,
        int usedAbilityPoints,
        java.util.Set<WereAbility> unlockedAbilities,
        WereAbility selectedAbility,
        WereInclination inclination,
        java.util.Set<Integer> completedTrials,
        Map<String, Integer> attributeTree,
        boolean moonForced) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncWerewolfPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "sync_werewolf"));

    public static final StreamCodec<FriendlyByteBuf, SyncWerewolfPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBoolean(packet.isWerewolf());
                buf.writeBoolean(packet.isTransformed());
                buf.writeBoolean(packet.isInfected()); // NUEVO
                buf.writeInt(packet.level());
                buf.writeInt(packet.experience());
                buf.writeInt(packet.usedAttributePoints());
                buf.writeInt(packet.usedAbilityPoints());
                buf.writeInt(packet.unlockedAbilities().size());
                for (WereAbility ability : packet.unlockedAbilities())
                    buf.writeEnum(ability);
                buf.writeBoolean(packet.selectedAbility() != null);
                if (packet.selectedAbility() != null)
                    buf.writeEnum(packet.selectedAbility());
                buf.writeInt(packet.attributeTree().size());
                packet.attributeTree().forEach((k, v) -> {
                    buf.writeUtf(k);
                    buf.writeInt(v);
                });
                buf.writeEnum(packet.inclination());
                buf.writeInt(packet.completedTrials().size());
                for (int t : packet.completedTrials())
                    buf.writeInt(t);
                buf.writeBoolean(packet.moonForced());
            },
            buf -> {
                boolean isWerewolf = buf.readBoolean();
                boolean isTransformed = buf.readBoolean();
                boolean isInfected = buf.readBoolean(); // NUEVO
                int level = buf.readInt();
                int experience = buf.readInt();
                int usedAttributePoints = buf.readInt();
                int usedAbilityPoints = buf.readInt();
                int unlockedSize = buf.readInt();
                java.util.Set<WereAbility> unlocked = java.util.EnumSet.noneOf(WereAbility.class);
                for (int i = 0; i < unlockedSize; i++)
                    unlocked.add(buf.readEnum(WereAbility.class));
                WereAbility selected = buf.readBoolean() ? buf.readEnum(WereAbility.class) : null;
                int treeSize = buf.readInt();
                Map<String, Integer> tree = new HashMap<>();
                for (int i = 0; i < treeSize; i++)
                    tree.put(buf.readUtf(), buf.readInt());
                WereInclination inclination = buf.readEnum(WereInclination.class);
                int trialSize = buf.readInt();
                java.util.Set<Integer> trials = new java.util.HashSet<>();
                for (int i = 0; i < trialSize; i++)
                    trials.add(buf.readInt());
                boolean moonForced = buf.readBoolean();
                return new SyncWerewolfPacket(isWerewolf, isTransformed, isInfected, level, experience,
                        usedAttributePoints, usedAbilityPoints, unlocked, selected, inclination, trials, tree,
                        moonForced);
            });

    public static SyncWerewolfPacket fromCap(WerewolfCapability cap) {
        return new SyncWerewolfPacket(cap.isWerewolf(), cap.isTransformed(), cap.isInfected(), cap.getLevel(),
                cap.getExperience(), cap.getUsedAttributePoints(), cap.getUsedAbilityPoints(),
                cap.getUnlockedAbilities(), cap.getSelectedAbility(), cap.getInclination(), cap.getCompletedTrials(),
                cap.getAttributeTree(), cap.isMoonForced());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncWerewolfPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                WerewolfCapability cap = context.player().getData(WerewolfAttachment.WEREWOLF_DATA);
                cap.setWerewolf(packet.isWerewolf());
                cap.setTransformed(packet.isTransformed());
                cap.setInfected(packet.isInfected()); // NUEVO
                cap.setMoonForced(packet.moonForced());
                cap.setLevel(packet.level());
                cap.setExperience(packet.experience());
                cap.setUsedAttributePoints(packet.usedAttributePoints());
                cap.setUsedAbilityPoints(packet.usedAbilityPoints());
                cap.setUnlockedAbilities(packet.unlockedAbilities());
                cap.setSelectedAbility(packet.selectedAbility());
                cap.setInclination(packet.inclination());
                cap.setCompletedTrials(packet.completedTrials());
                cap.setAttributeTree(packet.attributeTree());
            }
        });
    }
}