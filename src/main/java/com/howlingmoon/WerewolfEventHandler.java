// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HowlingMoon.MODID)
public class WerewolfEventHandler {

    // =====================
    //   LOGIN / RESPAWN
    // =====================

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        syncToClient(player);
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (cap.isTransformed()) {
            WerewolfAttributeHandler.applyAllModifiers(player, cap);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        syncToClient(player);
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (cap.isTransformed()) {
            WerewolfAttributeHandler.applyAllModifiers(player, cap);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            WerewolfCapability oldCap = event.getOriginal().getData(WerewolfAttachment.WEREWOLF_DATA);
            WerewolfCapability newCap = event.getEntity().getData(WerewolfAttachment.WEREWOLF_DATA);

            newCap.setWerewolf(oldCap.isWerewolf());
            newCap.setTransformed(false);
            newCap.setMoonForced(false);
            newCap.setLevel(oldCap.getLevel());
            newCap.setExperience(oldCap.getExperience());
            newCap.setUsedAttributePoints(oldCap.getUsedAttributePoints());
            newCap.setAttributeTree(new java.util.HashMap<>(oldCap.getAttributeTree()));
        }
    }

    // =====================
    //   LUNA LLENA
    // =====================

    @SubscribeEvent
    public static void onPlayerTickMoon(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        // Solo comprobar cada 20 ticks (1 segundo) para no sobrecargar
        if (player.tickCount % 20 != 0) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf()) return;

        long dayTime = player.level().getDayTime() % 24000;
        boolean isNight = dayTime >= 13000 && dayTime <= 23000;
        boolean isFullMoon = player.level().getMoonPhase() == 0;
        boolean shouldBeForced = isNight && isFullMoon;

        if (shouldBeForced && !cap.isTransformed()) {
            // Forzar transformación
            cap.setTransformed(true);
            cap.setMoonForced(true);
            WerewolfAttributeHandler.applyAllModifiers(player, cap);
            syncToClient(player);
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§c☾ The full moon rises... the beast takes control!"
                    )
            );
        } else if (shouldBeForced && !cap.isMoonForced()) {
            // Estaba transformado manualmente, marcar como forzado
            cap.setMoonForced(true);
            syncToClient(player);
        } else if (!shouldBeForced && cap.isMoonForced()) {
            // La noche terminó — destransformar solo si fue forzado por la luna
            cap.setTransformed(false);
            cap.setMoonForced(false);
            WerewolfAttributeHandler.removeAllModifiers(player);
            syncToClient(player);
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§7The moon sets... you return to yourself."
                    )
            );
        }

        // Re-forzar si intenta destransformarse durante la luna llena
        if (shouldBeForced && !cap.isTransformed()) {
            cap.setTransformed(true);
            WerewolfAttributeHandler.applyAllModifiers(player, cap);
            syncToClient(player);
        }
    }

    // =====================
    //   XP AL MATAR
    // =====================

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf() || !cap.isTransformed()) return;

        LivingEntity killed = event.getEntity();
        int xpGained = 0;

        if (killed instanceof Player) {
            xpGained = 50;
        } else if (killed instanceof Monster) {
            xpGained = 15;
        } else if (killed instanceof Animal) {
            xpGained = 5;
        }

        if (xpGained <= 0) return;

        int levelBefore = cap.getLevel();
        cap.addExperience(xpGained);
        int levelAfter = cap.getLevel();

        if (levelAfter > levelBefore) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§6⚠ The beast grows stronger! Level " + levelAfter
                    )
            );
        }

        syncToClient(player);
    }

    // =====================
    //   HELPER
    // =====================

    private static void syncToClient(ServerPlayer player) {
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        PacketDistributor.sendToPlayer(player, new SyncWerewolfPacket(
                cap.isWerewolf(),
                cap.isTransformed(),
                cap.getLevel(),
                cap.getExperience(),
                cap.getUsedAttributePoints(),
                cap.getAttributeTree()
        ));
    }
}