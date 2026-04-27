// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import com.howlingmoon.network.*;

@Mod(HowlingMoon.MODID)
public class HowlingMoon {

        public static final String MODID = "howlingmoonrising";
        public static final Logger LOGGER = LogUtils.getLogger();

        public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
                        .create(Registries.CREATIVE_MODE_TAB, MODID);

        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HOWLING_MOON_TAB = CREATIVE_MODE_TABS
                        .register("howlingmoon_tab", () -> CreativeModeTab.builder()
                                        .title(Component.translatable("itemGroup.howlingmoon"))
                                        .withTabsBefore(CreativeModeTabs.COMBAT)
                                        .icon(() -> HMItems.MOON_PEARL.get().getDefaultInstance())
                                        .displayItems((parameters, output) -> {
                                                output.accept(HMItems.MOON_PEARL.get());
                                                output.accept(HMItems.SILVER_INGOT.get());
                                                output.accept(HMItems.WOLFSBANE_POTION.get());
                                                output.accept(HMItems.SILVER_SWORD.get());
                                                output.accept(HMBlocks.SILVER_ORE.get().asItem());
                                                output.accept(HMBlocks.DEEPSLATE_SILVER_ORE.get().asItem());
                                                output.accept(HMBlocks.WOLFSBANE_FLOWER.get().asItem());
                                                output.accept(HMItems.WEREWOLF_SPAWN_EGG.get());
                                                output.accept(HMItems.HUNTER_SPAWN_EGG.get());
                                        }).build());

        public HowlingMoon(IEventBus modEventBus, ModContainer modContainer) {
                HMItems.ITEMS.register(modEventBus);
                HMBlocks.BLOCKS.register(modEventBus);
                CREATIVE_MODE_TABS.register(modEventBus);
                WerewolfAttachment.ATTACHMENT_TYPES.register(modEventBus);
                HMEntities.ENTITIES.register(modEventBus);
                HMSounds.SOUNDS.register(modEventBus);

                modEventBus.addListener(HMEntities::registerAttributes);
                modEventBus.addListener(HMEntities::registerSpawnPlacements);
                modEventBus.addListener(HowlingMoon::registerPackets);

                // --- CORRECCIÓN DE BUS ---
                // RegisterBrewingRecipesEvent pertenece al Game Bus, no al Mod Bus.
                NeoForge.EVENT_BUS.addListener(HowlingMoon::registerBrewingRecipes);
        }

        private static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
                event.getBuilder().addRecipe(new IBrewingRecipe() {
                        @Override
                        public boolean isInput(ItemStack input) {
                                if (!input.is(Items.POTION))
                                        return false;
                                PotionContents contents = input.get(DataComponents.POTION_CONTENTS);
                                return contents != null && contents.is(Potions.AWKWARD);
                        }

                        @Override
                        public boolean isIngredient(ItemStack ingredient) {
                                return ingredient.is(HMBlocks.WOLFSBANE_FLOWER.get().asItem());
                        }

                        @Override
                        public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
                                if (isInput(input) && isIngredient(ingredient)) {
                                        return new ItemStack(HMItems.WOLFSBANE_POTION.get());
                                }
                                return ItemStack.EMPTY;
                        }
                });
        }

        private static void registerPackets(RegisterPayloadHandlersEvent event) {
                PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0");
                registrar.playToClient(SyncWerewolfPacket.TYPE, SyncWerewolfPacket.STREAM_CODEC,
                                SyncWerewolfPacket::handle);
                registrar.playToClient(AbilityCooldownPacket.TYPE, AbilityCooldownPacket.STREAM_CODEC,
                                AbilityCooldownPacket::handle);
                registrar.playToServer(UpgradeAttributePacket.TYPE, UpgradeAttributePacket.STREAM_CODEC,
                                UpgradeAttributePacket::handle);
                registrar.playToServer(TransformPacket.TYPE, TransformPacket.STREAM_CODEC, TransformPacket::handle);
                registrar.playToServer(UseAbilityPacket.TYPE, UseAbilityPacket.STREAM_CODEC, UseAbilityPacket::handle);
                registrar.playToServer(UnlockAbilityPacket.TYPE, UnlockAbilityPacket.STREAM_CODEC,
                                UnlockAbilityPacket::handle);
                registrar.playToServer(SelectInclinationPacket.TYPE, SelectInclinationPacket.STREAM_CODEC,
                                SelectInclinationPacket::handle);
                registrar.playToServer(SelectAbilityPacket.TYPE, SelectAbilityPacket.STREAM_CODEC,
                                SelectAbilityPacket::handle);
        }
}