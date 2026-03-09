package com.howlingmoon;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HMItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(HowlingMoon.MODID);

    public static final DeferredItem<Item> MOON_PEARL =
            ITEMS.registerSimpleItem("moon_pearl", new Item.Properties());

    public static final DeferredItem<Item> SILVER_INGOT =
            ITEMS.registerSimpleItem("silver_ingot", new Item.Properties());

    public static final DeferredItem<Item> WOLFSBANE_POTION =
            ITEMS.registerSimpleItem("wolfsbane_potion", new Item.Properties());

    public static final DeferredItem<Item> SILVER_SWORD =
            ITEMS.registerSimpleItem("silver_sword", new Item.Properties());
}