package com.howlingmoon;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(HowlingMoon.MODID)
public class HowlingMoon {

    public static final String MODID = "howlingmoon";
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
                    }).build());

    public HowlingMoon(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Howling Moon is awakening...");
        HMItems.ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}