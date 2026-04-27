// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class HunterEntity extends AbstractIllager implements RangedAttackMob {

    public HunterEntity(EntityType<? extends HunterEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractIllager.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public IllagerArmPose getArmPose() {
        LivingEntity target = this.getTarget();
        if (target != null && this.distanceTo(target) <= 8.0) {
            return IllagerArmPose.ATTACKING;
        }
        return IllagerArmPose.BOW_AND_ARROW;
    }

    @Override
    public void applyRaidBuffs(ServerLevel level, int wave, boolean unused) {
        // Los hunters no participan en raids
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.PILLAGER_CELEBRATE;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedBowAttackGoal<>(this, 1.0, 20, 15.0f));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this, Player.class, 10, true, false, this::isValidWerewolfTarget));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                this, WerewolfEntity.class, 10, true, false, null));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                this, Monster.class, 10, true, false, entity -> !(entity instanceof AbstractIllager)));
    }

    private boolean isValidWerewolfTarget(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return false;
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf()) return false;

        if (this.getOffhandItem().is(HMItems.MOON_PEARL.get())) {
            return true;
        }

        return cap.isTransformed();
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = this.getTarget();
        if (target != null) {
            if (this.distanceTo(target) > 8.0) {
                this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
            } else {
                this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(HMItems.SILVER_SWORD.get()));
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        ItemStack bowStack = new ItemStack(Items.BOW);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, new ItemStack(Items.ARROW), velocity, bowStack);

        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        arrow.shoot(dx, dy + dist * 0.2, dz, 1.6f, 12 - this.level().getDifficulty().getId() * 4);
        this.level().addFreshEntity(arrow);

        applyHunterDebuffs(target);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && target instanceof LivingEntity living) {
            applyHunterDebuffs(living);
            alertNearbyHunters(living);
        }
        return result;
    }

    private void applyHunterDebuffs(LivingEntity target) {
        // Apply debuffs to WerewolfEntity NPCs
        if (target instanceof WerewolfEntity) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            target.hurt(target.damageSources().magic(), 10.0f);
            return;
        }

        // Apply debuffs to werewolf players
        if (!(target instanceof ServerPlayer player)) return;
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf()) return;

        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        target.hurt(target.damageSources().magic(), 10.0f);
    }

    private void alertNearbyHunters(LivingEntity target) {
        AABB searchBox = this.getBoundingBox().inflate(20.0);
        List<HunterEntity> nearby = this.level().getEntitiesOfClass(
                HunterEntity.class, searchBox, h -> h != this && h.getTarget() == null);
        for (HunterEntity hunter : nearby) {
            hunter.setTarget(target);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, SpawnGroupData spawnData) {
        if (this.random.nextFloat() < 0.3f) {
            this.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(HMItems.MOON_PEARL.get()));
        }
        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(HMItems.SILVER_SWORD.get()));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnData);
    }

    public static boolean checkHunterSpawnRules(EntityType<? extends AbstractIllager> type, ServerLevelAccessor level, MobSpawnType reason, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        return checkMobSpawnRules(type, level, reason, pos, random);
    }
}