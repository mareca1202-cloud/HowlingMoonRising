// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.client.player.AbstractClientPlayer;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WerewolfAnimatable implements GeoAnimatable {

    private static final WerewolfAnimatable INSTANCE = new WerewolfAnimatable();
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static WerewolfAnimatable getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            AbstractClientPlayer player = WerewolfReplacedRenderer.currentPlayer;
            if (player != null && player.walkAnimation.speed() > 0.01f) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.werewolf.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.werewolf.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object object) {
        AbstractClientPlayer player = WerewolfReplacedRenderer.currentPlayer;
        if (player != null) {
            return player.tickCount;
        }
        return 0;
    }
}