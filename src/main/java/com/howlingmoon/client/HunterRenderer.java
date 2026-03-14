// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.client;

import com.howlingmoon.HunterEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class HunterRenderer extends LivingEntityRenderer<HunterEntity, PlayerModel<HunterEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("howlingmoonrising", "textures/entity/hunter.png");

    public HunterRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(HunterEntity entity) {
        return TEXTURE;
    }

    @Override
    protected boolean shouldShowName(HunterEntity entity) {
        return false;
    }
}