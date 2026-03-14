// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = HowlingMoon.MODID, bus = EventBusSubscriber.Bus.MOD)
public class HMEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HowlingMoon.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<WerewolfEntity>> WEREWOLF =
            ENTITIES.register("werewolf", () -> EntityType.Builder
                    .<WerewolfEntity>of(WerewolfEntity::new, MobCategory.MONSTER)
                    .sized(0.8f, 2.5f)
                    .build("werewolf"));

    public static final DeferredHolder<EntityType<?>, EntityType<HunterEntity>> HUNTER =
            ENTITIES.register("hunter", () -> EntityType.Builder
                    .<HunterEntity>of(HunterEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f)
                    .build("hunter"));

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(WEREWOLF.get(), WerewolfEntity.createAttributes().build());
        event.put(HUNTER.get(), HunterEntity.createAttributes().build());
    }
}