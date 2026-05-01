// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = HowlingMoon.MODID)
public class WerewolfWeaknessHandler {

    public static final TagKey<Item> SILVER_ITEMS = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "silver_items"));

    public static final TagKey<Item> SILVER_INGOTS = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("c", "ingots/silver"));

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    /**
     * Checks if a player is currently a transformed werewolf.
     */
    private static boolean isTransformedWerewolf(ServerPlayer player) {
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        return cap.isWerewolf() && cap.isTransformed();
    }

    // =====================
    // DAÑO DE PLATA
    // =====================

    @SubscribeEvent
    public static void onSilverDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();

        boolean isWerewolf = (victim instanceof ServerPlayer sp && isTransformedWerewolf(sp))
                || victim instanceof WerewolfEntity;

        if (!isWerewolf)
            return;

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker))
            return;

        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty() || (!weapon.is(SILVER_ITEMS) && !weapon.is(SILVER_INGOTS)))
            return;

        event.setAmount(event.getAmount() + 8.0f);
        victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, true));
        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, true));
    }

    // =====================
    // NO PUEDES DORMIR
    // =====================

    @SubscribeEvent
    public static void onSleep(CanPlayerSleepEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        if (!isTransformedWerewolf(player))
            return;

        event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
        player.sendSystemMessage(Component
                .translatable("message.howlingmoonrising.will_not_rest").withStyle(ChatFormatting.RED));
    }

    // =====================
    // MOBS PASIVOS HUYEN
    // =====================

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide())
            return;

        if (event.getEntity() instanceof net.minecraft.world.entity.animal.IronGolem golem) {
            golem.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                    golem, Player.class, true,
                    target -> target instanceof ServerPlayer sp && isTransformedWerewolf(sp)));
            return;
        }

        if (event.getEntity() instanceof PathfinderMob mob
                && !(event.getEntity() instanceof Monster)) {
            mob.goalSelector.addGoal(1, new AvoidWerewolfGoal(mob, 10.0f, 1.0, 1.2));
        }
    }

    // =====================
    // SIN ARMADURA
    // =====================

    @SubscribeEvent
    public static void onPlayerTickArmor(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        if (player.tickCount % 20 != 0)
            return;
        if (!isTransformedWerewolf(player))
            return;

        boolean msgSent = false;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armor = player.getItemBySlot(slot);
            if (armor.isEmpty())
                continue;

            // Intentamos añadir la armadura al inventario
            if (!player.getInventory().add(armor.copy())) {
                // Si el inventario está lleno, la soltamos en el mundo
                ItemEntity drop = player.drop(armor.copy(), false);
                if (drop != null) {
                    drop.setPickUpDelay(40); // 2 segundos de delay para que no la recoja al instante
                }
            }
            player.setItemSlot(slot, ItemStack.EMPTY);

            if (!msgSent) {
                player.sendSystemMessage(Component
                        .translatable("message.howlingmoonrising.rejects_armor")
                        .withStyle(ChatFormatting.RED));
                msgSent = true;
            }
        }
    }
}