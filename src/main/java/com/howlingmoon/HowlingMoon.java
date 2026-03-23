// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(HowlingMoon.MODID)
public class HowlingMoon {

    public static final String MODID = "howlingmoonrising";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HOWLING_MOON_TAB =
            CREATIVE_MODE_TABS.register("howlingmoon_tab", () -> CreativeModeTab.builder()
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
                    }).build());

    public HowlingMoon(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Howling Moon is awakening...");
        HMItems.ITEMS.register(modEventBus);
        HMBlocks.BLOCKS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        WerewolfAttachment.ATTACHMENT_TYPES.register(modEventBus);
        HMEntities.ENTITIES.register(modEventBus);
        HMSounds.SOUNDS.register(modEventBus);
        modEventBus.addListener(HMEntities::registerAttributes);
        modEventBus.addListener(HowlingMoon::registerPackets);
    }

    private static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0");

        // Servidor → Cliente
        registrar.playToClient(
                SyncWerewolfPacket.TYPE,
                SyncWerewolfPacket.STREAM_CODEC,
                SyncWerewolfPacket::handle
        );

        // Cliente → Servidor
        registrar.playToServer(
                UpgradeAttributePacket.TYPE,
                UpgradeAttributePacket.STREAM_CODEC,
                UpgradeAttributePacket::handle
        );

        registrar.playToServer(
                TransformPacket.TYPE,
                TransformPacket.STREAM_CODEC,
                TransformPacket::handle
        );
    }
}