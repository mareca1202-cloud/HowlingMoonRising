// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import com.howlingmoon.network.AbilityCooldownPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class WerewolfAbilityHandler {

    public static void tick(ServerPlayer player) {
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        cap.tickCooldowns();

        // --- MECÁNICA RAM (EMBESTIDA) ---
        if (player.hasEffect(MobEffects.DIG_SPEED)) {
            // Calculamos la velocidad horizontal del jugador
            Vec3 delta = player.getDeltaMovement();
            double horizontalSpeed = Math.sqrt(delta.x * delta.x + delta.z * delta.z);

            // Si el jugador ha frenado (fin de la embestida o choque contra pared),
            // cancelamos el modo RAM
            if (horizontalSpeed < 0.3 && !player.onGround()) {
                // Pequeña tolerancia para cuando recién empieza a saltar
            } else if (horizontalSpeed < 0.1) {
                player.removeEffect(MobEffects.DIG_SPEED);
            } else {
                // Si sigue yendo rápido, buscamos enemigos para atropellar
                AABB hitbox = player.getBoundingBox().inflate(0.5);
                boolean hitSomeone = false;

                for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitbox,
                        e -> e != player)) {
                    // Empujón brutal y daño por impacto
                    Vec3 pushDir = player.getLookAngle().normalize().scale(2.5).add(0, 0.5, 0);
                    target.setDeltaMovement(pushDir);
                    target.hurt(player.damageSources().mobAttack(player), 8.0f);
                    target.hurtMarked = true;
                    hitSomeone = true;
                }

                if (hitSomeone) {
                    player.removeEffect(MobEffects.DIG_SPEED); // Consumir la embestida al golpear
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            net.minecraft.sounds.SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, player.getSoundSource(), 1.0F,
                            0.5F);
                    // Frenar un poco al jugador por el impacto
                    player.setDeltaMovement(delta.scale(0.4));
                    player.hurtMarked = true;
                }
            }
        }
    }

    public static void handleAbilityUse(ServerPlayer player, WereAbility ability) {
        if (player == null || ability == null)
            return;
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);

        if (!cap.isTransformed() || !cap.getUnlockedAbilities().contains(ability))
            return;
        if (isOnCooldown(player, ability))
            return;

        boolean success = executeAbility(player, ability);

        if (success) {
            if (cap.getInclination() == WereInclination.SKILLFUL) {
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 80, 1));
            }

            if (ability.getBaseCooldown() > 0) {
                int cd = ability.getBaseCooldown();
                if (cap.getInclination() == WereInclination.SKILLFUL) {
                    cd = (int) (cd * 0.75);
                    if (player.getRandom().nextFloat() < 0.15f) {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§b✨ Zen Reset!"));
                        return;
                    }
                }
                setCooldown(player, ability, cd);
            }
        }
    }

    private static boolean executeAbility(ServerPlayer player, WereAbility ability) {
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);

        switch (ability) {
            case HOWL -> {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), HMSounds.HOWL.get(),
                        player.getSoundSource(), 1.2F, 1.0F);
                AABB area = player.getBoundingBox().inflate(12);
                player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player).forEach(e -> {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                    Vec3 escapeVec = e.position().subtract(player.position()).normalize().scale(1.5);
                    e.setDeltaMovement(escapeVec.x, 0.4, escapeVec.z);
                    e.hurtMarked = true;
                });
                return true;
            }
            case LEAP -> {
                Vec3 look = player.getLookAngle();
                double power = cap.getInclination() == WereInclination.SKILLFUL ? 1.8 : 1.5;
                player.setDeltaMovement(look.x * power, 0.6, look.z * power);
                player.hurtMarked = true;
                cap.setLeaping(true);
                return true;
            }
            case BITE -> {
                double range = cap.getInclination() == WereInclination.SKILLFUL ? 4.5 : 3.0;
                LivingEntity target = getTargetInFront(player, range);
                if (target != null) {
                    target.hurt(player.damageSources().mobAttack(player), 12.0F);
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    player.heal(2.0F);
                    return true;
                }
                return false;
            }
            case NIGHT_VISION -> {
                if (player.hasEffect(MobEffects.NIGHT_VISION)) {
                    player.removeEffect(MobEffects.NIGHT_VISION);
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 0, false, false));
                }
                return true;
            }
            case SCENT_TRACKING -> {
                if (player.hasEffect(MobEffects.LUCK)) {
                    player.removeEffect(MobEffects.LUCK);
                    return false;
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 1200, 0, false, false, false));
                    return true;
                }
            }
            case CLIMB -> {
                if (player.hasEffect(MobEffects.CONDUIT_POWER)) {
                    player.removeEffect(MobEffects.CONDUIT_POWER);
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7Claws retracted."));
                    return false;
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 999999, 0, false, false, false));
                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal("§6Claws extended for climbing."));
                    return true;
                }
            }
            case RAM -> {
                Vec3 look = player.getLookAngle();
                player.setDeltaMovement(look.x * 2.2, 0.2, look.z * 2.2);
                player.hurtMarked = true;
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 0, false, false, false));
                return true;
            }
            case MAIM -> {
                double range = cap.getInclination() == WereInclination.SKILLFUL ? 4.5 : 3.0;
                LivingEntity target = getTargetInFront(player, range);
                if (target != null) {
                    target.hurt(player.damageSources().mobAttack(player), 14.0F);
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 3));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2));
                    return true;
                }
                return false;
            }
            case LIFT -> {
                LivingEntity target = getTargetInFront(player, 3.5);
                if (target != null) {
                    Vec3 look = player.getLookAngle();
                    target.setDeltaMovement(look.x * 2.0, 1.2, look.z * 2.0);
                    target.hurtMarked = true;
                    return true;
                }
                return false;
            }
            case SHRED -> {
                AABB area = player.getBoundingBox().inflate(3.5);
                player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player).forEach(e -> {
                    e.hurt(player.damageSources().mobAttack(player), 5.0F);
                    e.invulnerableTime = 0;
                });
                return true;
            }
            case FEAR -> {
                AABB area = player.getBoundingBox().inflate(10);
                player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player).forEach(e -> {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 4));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 3));
                });
                return true;
            }
            case BERSERK -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 2));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 1));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static LivingEntity getTargetInFront(ServerPlayer player, double reach) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 reachVec = eyePos.add(look.x * reach, look.y * reach, look.z * reach);
        AABB box = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0D);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                player.level(), player, eyePos, reachVec, box,
                entity -> !entity.isSpectator() && entity.isPickable() && entity instanceof LivingEntity);

        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            return (LivingEntity) hitResult.getEntity();
        }
        return null;
    }

    public static boolean isOnCooldown(ServerPlayer player, WereAbility ability) {
        return player.getData(WerewolfAttachment.WEREWOLF_DATA).getCooldown(ability) > 0;
    }

    public static void setCooldown(ServerPlayer player, WereAbility ability, int ticks) {
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        cap.setCooldown(ability, ticks);
        PacketDistributor.sendToPlayer(player, new AbilityCooldownPacket(ability, ticks));
    }
}