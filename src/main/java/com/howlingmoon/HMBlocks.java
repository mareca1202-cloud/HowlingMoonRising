// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HMBlocks {

        public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HowlingMoon.MODID);

        public static final DeferredBlock<Block> SILVER_ORE = registerBlock("silver_ore",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .requiresCorrectToolForDrops()
                                        .strength(3.0f, 3.0f)
                                        .sound(SoundType.STONE)));

        public static final DeferredBlock<Block> DEEPSLATE_SILVER_ORE = registerBlock("deepslate_silver_ore",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.DEEPSLATE)
                                        .requiresCorrectToolForDrops()
                                        .strength(4.5f, 3.0f)
                                        .sound(SoundType.DEEPSLATE)));

        public static final DeferredBlock<Block> WOLFSBANE_FLOWER = registerBlock("wolfsbane_flower",
                        () -> new FlowerBlock(MobEffects.CONFUSION, 10,
                                        BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY)
                                                        .mapColor(MapColor.COLOR_PURPLE)));

        private static DeferredBlock<Block> registerBlock(String name, java.util.function.Supplier<Block> block) {
                DeferredBlock<Block> toReturn = BLOCKS.register(name, block);
                HMItems.ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
                return toReturn;
        }
}