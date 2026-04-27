// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import net.minecraft.world.entity.monster.Monster;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WerewolfEntity extends Monster implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public boolean forceMoving = false;

    public WerewolfEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            if (state.isMoving() || forceMoving) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("Walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("Idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static boolean checkWerewolfSpawnRules(EntityType<WerewolfEntity> type, net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.entity.MobSpawnType reason, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        if (level.getLevel().getMoonPhase() != 0) {
            return false;
        }
        if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            return false;
        }
        if (!net.minecraft.world.entity.monster.Monster.isDarkEnoughToSpawn(level, pos, random)) {
            return false;
        }
        return checkMobSpawnRules(type, level, reason, pos, random);
    }
}