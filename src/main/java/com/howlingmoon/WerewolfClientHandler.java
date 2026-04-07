// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import com.howlingmoon.client.RadialMenuScreen;
import com.howlingmoon.client.ClientAbilityData;
import com.howlingmoon.network.UseAbilityPacket;

@EventBusSubscriber(modid = HowlingMoon.MODID, value = Dist.CLIENT)
public class WerewolfClientHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // CORRECCIÓN: Si el juego está pausado (en el menú ESC), no hacemos nada
        if (mc.isPaused() || mc.player == null)
            return;

        // Tick cooldowns (ahora solo descuenta si no está pausado)
        ClientAbilityData.tick();

        // Tecla H — abrir menú
        if (WerewolfKeyBindings.OPEN_MENU.consumeClick()) {
            WerewolfCapability cap = mc.player.getData(WerewolfAttachment.WEREWOLF_DATA);
            if (cap.isWerewolf()) {
                mc.setScreen(new WerewolfScreen());
            }
        }

        // Tecla J — transformarse
        if (WerewolfKeyBindings.TRANSFORM.consumeClick()) {
            WerewolfCapability cap = mc.player.getData(WerewolfAttachment.WEREWOLF_DATA);
            if (cap.isWerewolf()) {
                PacketDistributor.sendToServer(new TransformPacket());
            }
        }

        // Tecla R — menú radial
        if (WerewolfKeyBindings.OPEN_RADIAL.consumeClick()) {
            if (mc.screen == null) {
                WerewolfCapability cap = mc.player.getData(WerewolfAttachment.WEREWOLF_DATA);
                if (cap.isWerewolf() && cap.isTransformed()) {
                    mc.setScreen(new RadialMenuScreen());
                }
            }
        }

        // Tecla V — usar habilidad seleccionada
        if (WerewolfKeyBindings.USE_ABILITY.consumeClick()) {
            WerewolfCapability cap = mc.player.getData(WerewolfAttachment.WEREWOLF_DATA);
            if (cap.isWerewolf() && cap.isTransformed() && cap.getSelectedAbility() != null) {
                if (ClientAbilityData.getCooldown(cap.getSelectedAbility()) <= 0) {
                    PacketDistributor.sendToServer(new UseAbilityPacket(cap.getSelectedAbility()));
                }
            }
        }
    }
}