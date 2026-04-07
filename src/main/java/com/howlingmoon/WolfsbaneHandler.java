// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

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

        if (!cap.isWerewolf()) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§7The potion has no effect on your human blood."));
            return;
        }

        // 1. Quitar modificadores físicos antes de resetear
        WerewolfAttributeHandler.removeAllModifiers(player);

        // 2. Limpiar todos los efectos secundarios (Night Vision, Luck, etc.)
        player.removeAllEffects();

        // 3. Reset total de la Capability (Nivel, XP, Skills y SENDA)
        cap.reset();

        // 4. Sincronizar con el cliente
        WerewolfCommand.syncToClient(player, cap);

        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                        "§aThe wolfsbane purifies your blood... the beast is gone, and your path is reset."));
    }
}