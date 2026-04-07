// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.*;

@EventBusSubscriber(modid = HowlingMoon.MODID)
public class WerewolfAttributeHandler {

    private static final ResourceLocation RL_PROTECTION = ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID,
            "were_protection");
    private static final ResourceLocation RL_SPEED = ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID,
            "were_speed");
    private static final ResourceLocation RL_KNOCKBACK = ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID,
            "were_knockback");
    private static final ResourceLocation RL_KNOCKRESIST = ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID,
            "were_knockresist");
    private static final ResourceLocation RL_JUMP = ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID,
            "were_jump");
    private static final ResourceLocation RL_SCALE = ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID,
            "were_scale");

    private static final Set<Holder<MobEffect>> NEGATIVE_EFFECTS = Set.of(MobEffects.POISON, MobEffects.WITHER,
            MobEffects.BLINDNESS, MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.HUNGER);
    private static final Set<Item> MEAT_FOODS = Set.of(Items.BEEF, Items.COOKED_BEEF, Items.PORKCHOP,
            Items.COOKED_PORKCHOP, Items.CHICKEN, Items.COOKED_CHICKEN, Items.MUTTON, Items.COOKED_MUTTON, Items.RABBIT,
            Items.COOKED_RABBIT, Items.COD, Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON, Items.TROPICAL_FISH,
            Items.ROTTEN_FLESH);

    private static final Map<UUID, Boolean> lastTransformState = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);

        boolean isTransformed = cap.isTransformed();
        if (isTransformed != lastTransformState.getOrDefault(player.getUUID(), false)) {
            lastTransformState.put(player.getUUID(), isTransformed);
            if (isTransformed)
                applyAllModifiers(player, cap);
            else
                removeAllModifiers(player);
        }

        if (!isTransformed)
            return;

        // Regeneración Pasiva
        if (player.tickCount % 40 == 0) {
            int regen = cap.getAttributeLevel(WereAttribute.REGENERATION);
            if (regen > 0 && player.getHealth() < player.getMaxHealth())
                player.heal(regen * 0.5f);
        }

        // Hambre y Metabolismo (Predator disadvantage 15%)
        if (player.tickCount % 20 == 0) {
            int metabolism = cap.getAttributeLevel(WereAttribute.HUNGER);
            if (metabolism > 0) {
                player.getFoodData().addExhaustion(-(metabolism * 0.04f));
            }
            if (cap.getInclination() == WereInclination.PREDATOR) {
                player.getFoodData().addExhaustion(0.015f); // 15% drain
            }
        }
    }

    // Ventaja Mastery: Minado con manos desnudas
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player))
            return;
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (cap.isTransformed() && cap.getInclination() == WereInclination.MASTERY
                && player.getMainHandItem().isEmpty()) {
            if (event.getState().is(BlockTags.MINEABLE_WITH_PICKAXE)
                    || event.getState().is(BlockTags.MINEABLE_WITH_AXE)) {
                ItemStack fakeIronPick = new ItemStack(Items.IRON_PICKAXE);
                var drops = net.minecraft.world.level.block.Block.getDrops(event.getState(),
                        (ServerLevel) event.getLevel(), event.getPos(), null, player, fakeIronPick);
                drops.forEach(d -> net.minecraft.world.level.block.Block.popResource((ServerLevel) event.getLevel(),
                        event.getPos(), d));
            }
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        WerewolfCapability cap = event.getEntity().getData(WerewolfAttachment.WEREWOLF_DATA);
        if (cap.isTransformed() && cap.getInclination() == WereInclination.MASTERY
                && event.getEntity().getMainHandItem().isEmpty()) {
            if (event.getState().is(BlockTags.MINEABLE_WITH_PICKAXE)
                    || event.getState().is(BlockTags.MINEABLE_WITH_AXE)) {
                event.setNewSpeed(10.0f);
            }
        }
    }

    // Ataque de Barrido (Sweep) y Daño de Fuerza
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.getMainHandItem().isEmpty())
            return;
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isTransformed())
            return;

        int str = cap.getAttributeLevel(WereAttribute.STRENGTH);
        boolean isMastery = cap.getInclination() == WereInclination.MASTERY;
        float multiplier = isMastery ? 1.4f : 1.0f;

        if (str > 0 && event.getTarget() instanceof LivingEntity target) {
            float totalDamage = str * 3.0f * multiplier;
            target.hurt(player.damageSources().playerAttack(player), totalDamage);

            // Daño en área real para el Mastery
            if (isMastery) {
                float sweepDamage = 1.0f + (totalDamage * 0.5f);
                List<LivingEntity> nearby = player.level().getEntitiesOfClass(LivingEntity.class,
                        target.getBoundingBox().inflate(1.5, 0.25, 1.5),
                        e -> e != player && e != target && !player.isAlliedTo(e));
                for (LivingEntity secondary : nearby) {
                    secondary.knockback(0.4, (double) Mth.sin(player.getYRot() * ((float) Math.PI / 180F)),
                            (double) (-Mth.cos(player.getYRot() * ((float) Math.PI / 180F))));
                    secondary.hurt(player.damageSources().playerAttack(player), sweepDamage);
                }
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0f, 1.0f);
                ((ServerLevel) player.level()).sendParticles(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK,
                        target.getX(), target.getY(0.5), target.getZ(), 1, 0, 0, 0, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isTransformed())
            return;

        // Resistencia por atributo Toughness
        int resLevel = cap.getAttributeLevel(WereAttribute.RESISTANCE);
        if (resLevel > 0) {
            float[] reductions = { 0.15f, 0.30f, 0.45f };
            event.setAmount(event.getAmount() * (1.0f - reductions[Math.min(resLevel - 1, 2)]));
        }

        // Resistencia pasiva a proyectiles para Mastery
        if (cap.getInclination() == WereInclination.MASTERY && event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            event.setAmount(event.getAmount() * 0.7f);
        }
    }

    @SubscribeEvent
    public static void onFallDamage(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);

        if (cap.isLeaping()) {
            event.setDistance(0);
            event.setCanceled(true);
            cap.setLeaping(false);
            return;
        }

        if (!cap.isTransformed())
            return;
        int fall = cap.getAttributeLevel(WereAttribute.FALL);
        if (fall == 1)
            event.setDistance(event.getDistance() * 0.7f);
        else if (fall == 2)
            event.setDistance(event.getDistance() * 0.4f);
        else if (fall >= 3) {
            event.setDistance(0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf())
            return;

        ItemStack stack = event.getItem();
        if (stack.get(DataComponents.FOOD) != null && !MEAT_FOODS.contains(stack.getItem())) {
            event.setCanceled(true);
            player.displayClientMessage(Component.literal("§cThe beast demands flesh."), true);
        }
    }

    private static final ThreadLocal<Boolean> IS_ADAPTING = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent
    public static void onEffectApplied(MobEffectEvent.Applicable event) {
        if (IS_ADAPTING.get() || !(event.getEntity() instanceof ServerPlayer player))
            return;
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isTransformed())
            return;

        int clarity = cap.getAttributeLevel(WereAttribute.CLARITY);
        if (clarity > 0 && NEGATIVE_EFFECTS.contains(event.getEffectInstance().getEffect())) {
            float m = cap.getInclination() == WereInclination.MASTERY ? 1.25f : 1.0f;
            int dur = (int) (event.getEffectInstance().getDuration() * (1.0f - clarity * 0.2f * m));

            if (dur <= 0) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            } else {
                IS_ADAPTING.set(true);
                try {
                    player.addEffect(new MobEffectInstance(event.getEffectInstance().getEffect(), dur,
                            event.getEffectInstance().getAmplifier(), event.getEffectInstance().isAmbient(),
                            event.getEffectInstance().isVisible()));
                } finally {
                    IS_ADAPTING.set(false);
                }
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            }
        }
    }

    public static void applyAllModifiers(ServerPlayer player, WerewolfCapability cap) {
        float masteryBonus = cap.getInclination() == WereInclination.MASTERY ? 1.5f : 1.0f;

        // Velocidad Base por Path
        double baseSpeed = 0.0;
        if (cap.getInclination() == WereInclination.PREDATOR)
            baseSpeed = 0.04;
        else if (cap.getInclination() == WereInclination.SKILLFUL)
            baseSpeed = 0.02;

        addMod(player, Attributes.ARMOR, RL_PROTECTION,
                cap.getAttributeLevel(WereAttribute.PROTECTION) * 2.5 * masteryBonus);
        addMod(player, Attributes.MOVEMENT_SPEED, RL_SPEED,
                baseSpeed + (cap.getAttributeLevel(WereAttribute.SPEED) * 0.02));
        addMod(player, Attributes.KNOCKBACK_RESISTANCE, RL_KNOCKRESIST,
                (cap.getAttributeLevel(WereAttribute.KNOCKRESIST) * 0.1)
                        + (cap.getInclination() == WereInclination.MASTERY ? 0.35 : 0.0));
        addMod(player, Attributes.JUMP_STRENGTH, RL_JUMP, cap.getAttributeLevel(WereAttribute.JUMP) * 0.1);
        addMod(player, Attributes.SCALE, RL_SCALE, 0.15);
    }

    public static void removeAllModifiers(ServerPlayer player) {
        remMod(player, Attributes.ARMOR, RL_PROTECTION);
        remMod(player, Attributes.MOVEMENT_SPEED, RL_SPEED);
        remMod(player, Attributes.ATTACK_KNOCKBACK, RL_KNOCKBACK);
        remMod(player, Attributes.KNOCKBACK_RESISTANCE, RL_KNOCKRESIST);
        remMod(player, Attributes.JUMP_STRENGTH, RL_JUMP);
        remMod(player, Attributes.SCALE, RL_SCALE);
    }

    private static void addMod(ServerPlayer p, Holder<Attribute> a, ResourceLocation id, double v) {
        var i = p.getAttribute(a);
        if (i != null) {
            i.removeModifier(id);
            i.addTransientModifier(new AttributeModifier(id, v, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void remMod(ServerPlayer p, Holder<Attribute> a, ResourceLocation id) {
        var i = p.getAttribute(a);
        if (i != null)
            i.removeModifier(id);
    }
}