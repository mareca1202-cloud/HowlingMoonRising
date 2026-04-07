// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import com.howlingmoon.client.ClientSetup;
import com.howlingmoon.client.ScentTrackingRenderer;
import com.howlingmoon.client.WerewolfOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Punto de entrada del mod exclusivo del cliente.
 */
@Mod(value = HowlingMoon.MODID, dist = Dist.CLIENT)
public class HowlingMoonClient {

    public HowlingMoonClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(ClientSetup::registerRenderers);
        modEventBus.addListener(WerewolfKeyBindings::onRegisterKeyMappings);

        // Registros de eventos globales
        NeoForge.EVENT_BUS.register(ScentTrackingRenderer.class);
        NeoForge.EVENT_BUS.register(WerewolfOverlay.class); // NUEVO
    }
}