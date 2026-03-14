// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.client;

import com.howlingmoon.HMEntities;
import com.howlingmoon.HowlingMoon;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = HowlingMoon.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(HMEntities.HUNTER.get(), HunterRenderer::new);
    }
}