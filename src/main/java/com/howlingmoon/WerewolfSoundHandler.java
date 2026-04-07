// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = HowlingMoon.MODID, value = Dist.CLIENT)
public class WerewolfSoundHandler {

    private static boolean wasTransformed = false;
    private static boolean wasMoonForced = false;
    private static int heartbeatTick = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused() || mc.player == null || mc.level == null)
            return;

        LocalPlayer player = mc.player;
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);

        boolean isTransformed = cap.isTransformed();
        boolean isMoonForced = cap.isMoonForced();

        // 1. AULLIDOS DE TRANSFORMACIÓN
        if (isMoonForced && !wasMoonForced) {
            playSound(mc, HMSounds.HOWL.get(), 0.7f, 0.85f); // Aullido lejano/forzado
        } else if (isTransformed && !wasTransformed && !isMoonForced) {
            playSound(mc, HMSounds.HOWL.get(), 1.0f, 1.0f); // Aullido voluntario potente
        }

        // 2. SISTEMA DE LATIDO DINÁMICO (SENSACIÓN PREMIUM)
        if (isTransformed) {
            float healthPct = player.getHealth() / player.getMaxHealth();

            if (healthPct <= 0.45f) { // Solo suena si tiene menos del 45% de vida
                heartbeatTick++;

                // El intervalo se acorta según la vida (entre 10 y 35 ticks)
                int interval = 10 + (int) (healthPct * 50);

                if (heartbeatTick >= interval) {
                    heartbeatTick = 0;
                    float volume = 0.5f + (0.45f - healthPct); // Sube el volumen al morir
                    float pitch = 0.8f + (0.45f - healthPct); // Sube el tono (estrés)

                    // Alternar entre sonido de latido y eco
                    if ((player.tickCount / interval) % 2 == 0) {
                        playSound(mc, HMSounds.HEARTBEAT.get(), volume, pitch);
                    } else {
                        playSound(mc, HMSounds.HEARTBEAT_DELAY.get(), volume * 0.8f, pitch);
                    }
                }
            }
        } else {
            heartbeatTick = 0;
        }

        wasTransformed = isTransformed;
        wasMoonForced = isMoonForced;
    }

    private static void playSound(Minecraft mc, net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }
}