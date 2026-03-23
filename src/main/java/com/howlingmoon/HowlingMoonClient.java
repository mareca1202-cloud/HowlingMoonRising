// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import com.howlingmoon.client.ClientSetup;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Punto de entrada del mod exclusivo del cliente.
 * Registra todos los listeners del mod-bus que solo deben ejecutarse en el cliente,
 * evitando el uso del @EventBusSubscriber(bus = Bus.MOD) deprecado.
 */
@Mod(value = HowlingMoon.MODID, dist = Dist.CLIENT)
public class HowlingMoonClient {

    public HowlingMoonClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(ClientSetup::registerRenderers);
        modEventBus.addListener(ClientSetup::registerLayerDefinitions);
        modEventBus.addListener((EntityRenderersEvent.AddLayers event) -> ClientSetup.addPlayerLayers(event));
        modEventBus.addListener(WerewolfKeyBindings::onRegisterKeyMappings);
    }
}
