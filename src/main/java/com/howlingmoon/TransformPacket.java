// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TransformPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TransformPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "transform"));

    public static final StreamCodec<FriendlyByteBuf, TransformPacket> STREAM_CODEC = StreamCodec
            .unit(new TransformPacket());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TransformPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player))
                return;
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
            if (!cap.isWerewolf())
                return;

            // Bloqueo en Luna Llena
            if (cap.isTransformed()) {
                long dayTime = player.level().getDayTime() % 24000;
                boolean isNight = dayTime >= 13000 && dayTime <= 23000;
                boolean isFullMoon = player.level().getMoonPhase() == 0;
                if (isNight && isFullMoon) {
                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal("§cThe moon will not let you go..."));
                    return;
                }
            }

            // Cambiar estado
            boolean nowTransformed = !cap.isTransformed();
            cap.setTransformed(nowTransformed);

            // --- EFECTO VISUAL PREMIUM (Crescendo de Sombras) ---
            ServerLevel level = player.serverLevel();
            double px = player.getX();
            double py = player.getY() + 1.0; // Centro del cuerpo
            double pz = player.getZ();

            if (nowTransformed) {
                // 1. Explosión central de tinta (Oculta el cuerpo rápidamente)
                level.sendParticles(ParticleTypes.SQUID_INK, px, py, pz, 60, 0.4, 0.8, 0.4, 0.1);

                // 2. Anillo de humo grande que se expande hacia los lados
                level.sendParticles(ParticleTypes.LARGE_SMOKE, px, py, pz, 40, 0.6, 0.2, 0.6, 0.08);

                // 3. Cenizas flotantes (Toque sobrenatural/místico oscuro)
                level.sendParticles(ParticleTypes.ASH, px, py + 0.5, pz, 50, 0.5, 1.0, 0.5, 0.02);

                // 4. Sonido desgarrador: Huesos rompiéndose (Zombie) + Viento grave (Phantom)
                level.playSound(null, px, py, pz, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.PLAYERS, 1.2F,
                        0.7F);
                level.playSound(null, px, py, pz, SoundEvents.PHANTOM_SWOOP, SoundSource.PLAYERS, 1.5F, 0.5F);
            } else {
                // Destransformación: La oscuridad se disipa hacia arriba
                // Humo normal y partículas de alma apagándose (Campfire signal smoke sube alto)
                level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, px, py - 0.5, pz, 30, 0.4, 0.2, 0.4, 0.05);
                level.sendParticles(ParticleTypes.WHITE_ASH, px, py, pz, 40, 0.5, 0.8, 0.5, 0.01);

                // Sonido de alivio/escape de gas
                level.playSound(null, px, py, pz, SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 1.0F, 0.9F);
                level.playSound(null, px, py, pz, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0F, 0.6F);
            }
            // ----------------------------------------------------

            // Aplicar modificadores físicos
            if (nowTransformed)
                WerewolfAttributeHandler.applyAllModifiers(player, cap);
            else
                WerewolfAttributeHandler.removeAllModifiers(player);

            // Sincronizar con el cliente
            PacketDistributor.sendToPlayer(player, SyncWerewolfPacket.fromCap(cap));
        });
    }
}