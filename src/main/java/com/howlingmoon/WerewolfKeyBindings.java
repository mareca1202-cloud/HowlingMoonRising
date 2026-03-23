// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;


public class WerewolfKeyBindings {

    public static final KeyMapping OPEN_MENU = new KeyMapping(
            "key.howlingmoonrising.open_menu",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_H,
            "key.categories.howlingmoonrising"
    );

    public static final KeyMapping TRANSFORM = new KeyMapping(
            "key.howlingmoonrising.transform",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_J,
            "key.categories.howlingmoonrising"
    );


    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU);
        event.register(TRANSFORM);
    }
}