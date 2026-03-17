// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WerewolfReplacedModel extends GeoModel<WerewolfAnimatable> {

    @Override
    public ResourceLocation getModelResource(WerewolfAnimatable animatable) {
        return ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "geo/werewolf.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WerewolfAnimatable animatable) {
        return ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "textures/entity/werewolf.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WerewolfAnimatable animatable) {
        return ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "animations/werewolf.animation.json");
    }
}