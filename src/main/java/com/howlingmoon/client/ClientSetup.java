// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.client;

import com.howlingmoon.HMEntities;
import com.howlingmoon.HowlingMoon;
import com.howlingmoon.WerewolfReplacedRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = HowlingMoon.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    public static final ModelLayerLocation WEREWOLF_PARTS =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "werewolf_parts"), "main");

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(HMEntities.HUNTER.get(), HunterRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WEREWOLF_PARTS, WerewolfPartsModel::createBodyLayer);
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : PlayerSkin.Model.values()) {
            var renderer = event.getSkin(skin);
            if (renderer == null) continue;

            WerewolfPartsModel partsModel = new WerewolfPartsModel(
                    event.getEntityModels().bakeLayer(WEREWOLF_PARTS));

            var layer = new WerewolfPartsLayer(
                    (net.minecraft.client.renderer.entity.RenderLayerParent<AbstractClientPlayer,
                            net.minecraft.client.model.PlayerModel<AbstractClientPlayer>>) renderer,
                    partsModel);

            ((net.minecraft.client.renderer.entity.player.PlayerRenderer) renderer).addLayer(layer);
        }
    }
}