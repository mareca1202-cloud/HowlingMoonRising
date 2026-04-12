// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.network.PacketDistributor; // IMPORTACIÓN CORREGIDA

@EventBusSubscriber(modid = HowlingMoon.MODID)
public class WolfsbaneHandler {

    @SubscribeEvent
    public static void onItemFinishedUsing(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        ItemStack stack = event.getItem();
        if (!stack.is(HMItems.WOLFSBANE_POTION.get()))
            return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);

        // Chequea si es lobo O si está infectado para poder curarse
        if (!cap.isWerewolf() && !cap.isInfected()) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§7The potion has no effect on your human blood."));
            return;
        }

        // 1. Quitar modificadores si estaba transformado
        if (cap.isTransformed()) {
            WerewolfAttributeHandler.removeAllModifiers(player);
        }

        // 2. Limpiar todos los efectos (incluyendo la náusea de la infección)
        player.removeAllEffects();

        // 3. Reset total (Limpia niveles, XP, habilidades, senda e infección)
        cap.reset();

        // 4. Sincronizar los cambios con el cliente de forma inmediata
        PacketDistributor.sendToPlayer(player, SyncWerewolfPacket.fromCap(cap));

        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                        "§aThe wolfsbane purifies your blood... the curse has been lifted."));
    }
}