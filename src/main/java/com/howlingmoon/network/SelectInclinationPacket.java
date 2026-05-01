// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.network;

import com.howlingmoon.HowlingMoon;
import com.howlingmoon.WereInclination;
import com.howlingmoon.WerewolfAttachment;
import com.howlingmoon.WerewolfCapability;
import com.howlingmoon.SyncWerewolfPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectInclinationPacket(WereInclination inclination) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SelectInclinationPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "select_inclination")
            );

    public static final StreamCodec<FriendlyByteBuf, SelectInclinationPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> buf.writeEnum(packet.inclination()),
                    buf -> new SelectInclinationPacket(buf.readEnum(WereInclination.class))
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SelectInclinationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                WerewolfCapability cap = serverPlayer.getData(WerewolfAttachment.WEREWOLF_DATA);
                // Only allow choosing if NEUTRAL
                if (cap.getInclination() == WereInclination.NEUTRAL) {
                    cap.setInclination(packet.inclination());
                    com.howlingmoon.HMAdvancements.trigger(serverPlayer, "get_path");
                    PacketDistributor.sendToPlayer(serverPlayer, SyncWerewolfPacket.fromCap(cap));
                }
            }
        });
    }
}
