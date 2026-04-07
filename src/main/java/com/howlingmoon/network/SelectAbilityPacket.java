// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.network;

import com.howlingmoon.HowlingMoon;
import com.howlingmoon.SyncWerewolfPacket;
import com.howlingmoon.WereAbility;
import com.howlingmoon.WerewolfAttachment;
import com.howlingmoon.WerewolfCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectAbilityPacket(WereAbility ability) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SelectAbilityPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "select_ability"));

    public static final StreamCodec<FriendlyByteBuf, SelectAbilityPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeEnum(packet.ability()),
            buf -> new SelectAbilityPacket(buf.readEnum(WereAbility.class)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SelectAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
                cap.setSelectedAbility(packet.ability());
                // Sincronizar de vuelta para confirmar
                PacketDistributor.sendToPlayer(player, SyncWerewolfPacket.fromCap(cap));
            }
        });
    }
}