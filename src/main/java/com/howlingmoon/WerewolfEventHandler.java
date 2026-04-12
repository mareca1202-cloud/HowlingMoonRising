// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HowlingMoon.MODID)
public class WerewolfEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf() && !cap.isInfected())
            return;

        if (cap.isWerewolf() && cap.isTransformed()) {
            if (player.isInWater()) {
                player.setSwimming(false);
                if (player.getPose() == Pose.SWIMMING)
                    player.setPose(Pose.STANDING);
                if (player.isSprinting())
                    player.setSprinting(false);
            }
            processClimbLogic(player, cap);
        }

        if (player instanceof ServerPlayer sp) {
            if (cap.isWerewolf())
                WerewolfAbilityHandler.tick(sp);
            processMoonAndProgression(sp, cap);
        }
    }

    private static void processMoonAndProgression(ServerPlayer player, WerewolfCapability cap) {
        long dayTime = player.level().getDayTime() % 24000;
        boolean isNight = dayTime >= 13000 && dayTime <= 23000;
        boolean isFullMoon = player.level().getMoonPhase() == 0;
        boolean shouldBeForced = isNight && isFullMoon;

        if (cap.isInfected() && shouldBeForced) {
            cap.setInfected(false);
            cap.setWerewolf(true);
            cap.setTransformed(true);
            cap.setMoonForced(true);
            WerewolfAttributeHandler.applyAllModifiers(player, cap);
            playTransformEffects(player, true);
            syncToClient(player);
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§5Your blood boils... THE CURSE HAS AWAKENED!"));
            return;
        }

        if (cap.isWerewolf() && shouldBeForced && cap.isTransformed()
                && cap.getInclination() == WereInclination.PREDATOR) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 2, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 1, false, false));
        }

        if (player.tickCount % 20 != 0)
            return;

        if (cap.isWerewolf()) {
            if (shouldBeForced && !cap.isTransformed()) {
                cap.setTransformed(true);
                cap.setMoonForced(true);
                WerewolfAttributeHandler.applyAllModifiers(player, cap);
                playTransformEffects(player, true);
                syncToClient(player);
                player.sendSystemMessage(net.minecraft.network.chat.Component
                        .literal("§c☾ The full moon rises... the beast takes control!"));
            } else if (shouldBeForced && cap.isTransformed() && !cap.isMoonForced()) {
                cap.setMoonForced(true);
                syncToClient(player);
            } else if (!shouldBeForced && cap.isMoonForced()) {
                cap.setTransformed(false);
                cap.setMoonForced(false);
                WerewolfAttributeHandler.removeAllModifiers(player);
                playTransformEffects(player, false);
                syncToClient(player);
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("§7The moon sets... you return to yourself."));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);

        if (!cap.isWerewolf() && !cap.isInfected()) {
            boolean infectedSuccess = false;
            if (event.getSource().getEntity() instanceof Wolf wolf && !wolf.isTame()) {
                if (player.getRandom().nextFloat() < 0.12f)
                    infectedSuccess = true;
            } else if (event.getSource().getEntity() instanceof WerewolfEntity) {
                if (player.getRandom().nextFloat() < 0.25f)
                    infectedSuccess = true;
            }

            if (infectedSuccess) {
                cap.setInfected(true);
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        HMSounds.HEARTBEAT.get(), SoundSource.PLAYERS, 2.0F, 1.0F);
                player.sendSystemMessage(net.minecraft.network.chat.Component
                        .literal("§8[§5⚠§8] §dThe wolf's bite feels strange... your veins burn."));
                syncToClient(player);
            }
        }
    }

    public static void playTransformEffects(ServerPlayer player, boolean isTransforming) {
        ServerLevel level = player.serverLevel();
        double px = player.getX();
        double py = player.getY() + 1.0;
        double pz = player.getZ();
        if (isTransforming) {
            level.sendParticles(ParticleTypes.SQUID_INK, px, py, pz, 60, 0.4, 0.8, 0.4, 0.1);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, px, py, pz, 40, 0.6, 0.2, 0.6, 0.08);
            level.sendParticles(ParticleTypes.ASH, px, py + 0.5, pz, 50, 0.5, 1.0, 0.5, 0.02);
            level.playSound(null, px, py, pz, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.PLAYERS, 1.2F, 0.7F);
            level.playSound(null, px, py, pz, SoundEvents.PHANTOM_SWOOP, SoundSource.PLAYERS, 1.5F, 0.5F);
        } else {
            level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, px, py - 0.5, pz, 30, 0.4, 0.2, 0.4, 0.05);
            level.sendParticles(ParticleTypes.WHITE_ASH, px, py, pz, 40, 0.5, 0.8, 0.5, 0.01);
            level.playSound(null, px, py, pz, SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 1.0F, 0.9F);
            level.playSound(null, px, py, pz, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0F, 0.6F);
        }
    }

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (event.getEntityMounting() instanceof Player player) {
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
            if (cap.isTransformed() && event.isMounting())
                event.setCanceled(true);
        }
    }

    private static void processClimbLogic(Player player, WerewolfCapability cap) {
        if (!cap.getUnlockedAbilities().contains(WereAbility.CLIMB) || !player.hasEffect(MobEffects.CONDUIT_POWER))
            return;
        if (!player.level().noCollision(player, player.getBoundingBox().inflate(0.15, 0.0, 0.15)) && !player.onGround()
                && !player.isInWater()) {
            Vec3 delta = player.getDeltaMovement();
            if (player.zza > 0.01f)
                player.setDeltaMovement(delta.x, 0.30, delta.z);
            else if (player.zza < -0.01f || player.isCrouching())
                player.setDeltaMovement(delta.x, -0.25, delta.z);
            else
                player.setDeltaMovement(delta.x, -0.005, delta.z);
            player.fallDistance = 0.0f;
            if (player.level().isClientSide())
                player.hurtMarked = true;
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            syncToClient(sp);
            if (sp.getData(WerewolfAttachment.WEREWOLF_DATA).isTransformed()) {
                WerewolfAttributeHandler.applyAllModifiers(sp, sp.getData(WerewolfAttachment.WEREWOLF_DATA));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp)
            syncToClient(sp);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        WerewolfCapability oldCap = event.getOriginal().getData(WerewolfAttachment.WEREWOLF_DATA);
        WerewolfCapability newCap = event.getEntity().getData(WerewolfAttachment.WEREWOLF_DATA);
        newCap.setWerewolf(oldCap.isWerewolf());
        newCap.setInfected(oldCap.isInfected());
        newCap.setLevel(oldCap.getLevel());
        newCap.setExperience(oldCap.getExperience());
        newCap.setUsedAttributePoints(oldCap.getUsedAttributePoints());
        newCap.setUsedAbilityPoints(oldCap.getUsedAbilityPoints());
        newCap.setUnlockedAbilities(new java.util.HashSet<>(oldCap.getUnlockedAbilities()));
        newCap.setSelectedAbility(oldCap.getSelectedAbility());
        newCap.setAttributeTree(new java.util.HashMap<>(oldCap.getAttributeTree()));
        newCap.setInclination(oldCap.getInclination());
        newCap.setCompletedTrials(new java.util.HashSet<>(oldCap.getCompletedTrials()));
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player))
            return;
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf() || !cap.isTransformed())
            return;
        LivingEntity killed = event.getEntity();
        int xp = killed instanceof Player ? 50 : (killed instanceof Monster ? 15 : 5);
        if (cap.getInclination() == WereInclination.PREDATOR) {
            xp *= 2;
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 1));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0));
            player.getFoodData().eat(1, 0.4f);
            player.heal(1.0f);
        }
        cap.addExperience(xp);
        syncToClient(player);
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        int level = cap.getLevel();
        if (level % 5 == 0 && !cap.hasCompletedTrialFor(level)) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).is(HMItems.MOON_PEARL.get())) {
                    player.getInventory().getItem(i).shrink(1);
                    cap.completeTrial(level);
                    syncToClient(player);
                    player.sendSystemMessage(net.minecraft.network.chat.Component
                            .literal("§d✨ The Moon Pearl resonates... your path forward is clear!"));
                    return;
                }
            }
        }
    }

    private static void syncToClient(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player,
                SyncWerewolfPacket.fromCap(player.getData(WerewolfAttachment.WEREWOLF_DATA)));
    }
}