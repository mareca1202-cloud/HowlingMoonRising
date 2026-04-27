// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

public class HMEntities {

        public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE,
                        HowlingMoon.MODID);

        public static final DeferredHolder<EntityType<?>, EntityType<WerewolfEntity>> WEREWOLF = ENTITIES
                        .register("werewolf", () -> EntityType.Builder
                                        .<WerewolfEntity>of(WerewolfEntity::new, MobCategory.MONSTER)
                                        .sized(0.8f, 2.5f)
                                        .build("werewolf"));

        public static final DeferredHolder<EntityType<?>, EntityType<HunterEntity>> HUNTER = ENTITIES.register("hunter",
                        () -> EntityType.Builder
                                        .<HunterEntity>of(HunterEntity::new, MobCategory.CREATURE)
                                        .sized(0.6f, 1.95f)
                                        .build("hunter"));

        public static void registerAttributes(EntityAttributeCreationEvent event) {
                event.put(WEREWOLF.get(), WerewolfEntity.createAttributes().build());
                event.put(HUNTER.get(), HunterEntity.createAttributes().build());
        }

        public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
                event.register(WEREWOLF.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WerewolfEntity::checkWerewolfSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
                event.register(HUNTER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, HunterEntity::checkHunterSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        }
}