// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;
import java.util.stream.Stream;

@EventBusSubscriber(modid = HowlingMoon.MODID)
public class HunterSpawnHandler {

    private static final int CHECK_INTERVAL = 600; // Every 30 seconds (600 ticks)
    private static final int MAX_HUNTERS_PER_VILLAGE = 3;
    private static final int VILLAGE_SCAN_RADIUS = 64; // Blocks around player to scan for POIs
    private static final int HUNTER_SCAN_RADIUS = 80; // Blocks around POI center to count existing hunters
    private static final int MIN_POIS_FOR_VILLAGE = 4; // Minimum POIs needed to consider it a "village"

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.getGameTime() % CHECK_INTERVAL != 0) return;

        for (ServerPlayer player : serverLevel.players()) {
            trySpawnHuntersNearPlayer(serverLevel, player);
        }
    }

    private static void trySpawnHuntersNearPlayer(ServerLevel level, ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();
        PoiManager poiManager = level.getPoiManager();

        // Check for village POIs (beds, job sites) around the player
        // We use MEETING type (bell) as the center marker for a village
        Stream<BlockPos> meetingPoints = poiManager.findAll(
                holder -> holder.is(PoiTypes.MEETING),
                pos -> true,
                playerPos,
                VILLAGE_SCAN_RADIUS,
                PoiManager.Occupancy.ANY
        );

        meetingPoints.forEach(bellPos -> {
            // Count all village-type POIs around the bell to confirm it's a real village
            long poiCount = poiManager.findAll(
                    holder -> holder.is(PoiTypes.MEETING)
                            || holder.is(PoiTypes.HOME)
                            || holder.is(PoiTypes.ARMORER)
                            || holder.is(PoiTypes.BUTCHER)
                            || holder.is(PoiTypes.CARTOGRAPHER)
                            || holder.is(PoiTypes.CLERIC)
                            || holder.is(PoiTypes.FARMER)
                            || holder.is(PoiTypes.FISHERMAN)
                            || holder.is(PoiTypes.FLETCHER)
                            || holder.is(PoiTypes.LEATHERWORKER)
                            || holder.is(PoiTypes.LIBRARIAN)
                            || holder.is(PoiTypes.MASON)
                            || holder.is(PoiTypes.SHEPHERD)
                            || holder.is(PoiTypes.TOOLSMITH)
                            || holder.is(PoiTypes.WEAPONSMITH),
                    pos -> true,
                    bellPos,
                    HUNTER_SCAN_RADIUS,
                    PoiManager.Occupancy.ANY
            ).count();

            if (poiCount < MIN_POIS_FOR_VILLAGE) return;

            // Count existing hunters near this village
            AABB searchArea = new AABB(bellPos).inflate(HUNTER_SCAN_RADIUS);
            List<HunterEntity> existingHunters = level.getEntitiesOfClass(HunterEntity.class, searchArea);

            if (existingHunters.size() >= MAX_HUNTERS_PER_VILLAGE) return;

            // Spawn hunters
            int toSpawn = MAX_HUNTERS_PER_VILLAGE - existingHunters.size();
            for (int i = 0; i < toSpawn; i++) {
                BlockPos spawnPos = findSpawnPosition(level, bellPos);
                if (spawnPos != null) {
                    HunterEntity hunter = HMEntities.HUNTER.get().create(level);
                    if (hunter != null) {
                        hunter.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 
                                level.random.nextFloat() * 360f, 0);
                        hunter.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), 
                                MobSpawnType.NATURAL, null);
                        hunter.setPersistenceRequired();
                        level.addFreshEntity(hunter);
                    }
                }
            }
        });
    }

    private static BlockPos findSpawnPosition(ServerLevel level, BlockPos center) {
        for (int attempt = 0; attempt < 15; attempt++) {
            int x = center.getX() + level.random.nextIntBetweenInclusive(-20, 20);
            int z = center.getZ() + level.random.nextIntBetweenInclusive(-20, 20);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);

            // Basic validity: solid ground below, air at feet and head
            if (level.getBlockState(pos.below()).isSolid()
                    && level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.above()).isAir()
                    && level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos) > 0) {
                return pos;
            }
        }
        return null;
    }
}
