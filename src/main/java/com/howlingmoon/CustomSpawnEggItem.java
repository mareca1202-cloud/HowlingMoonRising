// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import java.util.function.Supplier;

/**
 * Spawn egg that uses custom textures instead of vanilla tinted egg overlay.
 * Extends DeferredSpawnEggItem so middle-click pick-block works in creative mode.
 */
public class CustomSpawnEggItem extends DeferredSpawnEggItem {

    @SuppressWarnings("unchecked")
    public CustomSpawnEggItem(Supplier<? extends EntityType<?>> entityType, Properties properties) {
        // Pass white colors (0xFFFFFF) so the tint doesn't alter the custom texture
        super((Supplier<? extends EntityType<? extends Mob>>) (Supplier<?>) entityType, 0xFFFFFF, 0xFFFFFF, properties);
    }
}
