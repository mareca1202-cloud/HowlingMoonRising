// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.client;

import com.howlingmoon.WerewolfAttachment;
import com.howlingmoon.WerewolfCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public class WerewolfOverlay {

    private static final int BAR_WIDTH = 80;
    private static final int BAR_HEIGHT = 6;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.level == null)
            return;

        WerewolfCapability cap = mc.player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf())
            return;

        GuiGraphics gfx = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int x = screenWidth - BAR_WIDTH - 10;
        int y = screenHeight - 40;

        // 1. Fondo y Barra de XP
        gfx.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF000000);
        gfx.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF2A1A00);
        float xpRatio = cap.getLevel() >= 20 ? 1.0f : (float) cap.getExperience() / cap.expNeededForNextLevel();
        gfx.fill(x, y, x + (int) (BAR_WIDTH * xpRatio), y + BAR_HEIGHT, cap.isTransformed() ? 0xFFFF8C00 : 0xFF8A7A60);

        // 2. Nivel
        gfx.drawString(mc.font, "Lvl " + cap.getLevel(), x, y - 10, 0xFFE8D5B0, true);

        // --- CALENDARIO LUNAR PREMIUM ---
        int phase = mc.level.getMoonPhase();
        long dayTime = mc.level.getDayTime() % 24000;
        boolean isNight = dayTime >= 13000 && dayTime <= 23000;

        String moonName;
        int moonColor;

        switch (phase) {
            case 0 -> {
                moonName = isNight ? "§4☾ FULL MOON" : "§6☾ Full Moon Tonight";
                moonColor = isNight ? 0xFFFF0000 : 0xFFFFCC00;
            }
            case 1 -> {
                moonName = "§7☾ Waning Gibbous";
                moonColor = 0xFFAAAAAA;
            }
            case 2 -> {
                moonName = "§7☾ Last Quarter";
                moonColor = 0xFFAAAAAA;
            }
            case 3 -> {
                moonName = "§7☾ Waning Crescent";
                moonColor = 0xFFAAAAAA;
            }
            case 4 -> {
                moonName = "§2☾ New Moon";
                moonColor = 0xFF55FF55;
            } // Luna Nueva = Seguro
            case 5 -> {
                moonName = "§f☾ Waxing Crescent";
                moonColor = 0xFFFFFFFF;
            }
            case 6 -> {
                moonName = "§f☾ First Quarter";
                moonColor = 0xFFFFFFFF;
            }
            case 7 -> {
                moonName = "§e☾ Waxing Gibbous";
                moonColor = 0xFFFFFF55;
            } // Casi llena = Aviso
            default -> {
                moonName = "Unknown";
                moonColor = 0xFFFFFFFF;
            }
        }

        // Dibujar el nombre de la fase con su color
        gfx.drawString(mc.font, moonName, screenWidth - mc.font.width(moonName) - 10, y - 20, moonColor, true);

        // 3. Habilidad Seleccionada
        if (cap.isTransformed() && cap.getSelectedAbility() != null) {
            String abilityName = cap.getSelectedAbility().getDisplayName();
            int cooldown = ClientAbilityData.getCooldown(cap.getSelectedAbility());
            String text = abilityName + (cooldown > 0 ? " §c(" + (cooldown / 20) + "s)" : " §a[V]");
            gfx.drawString(mc.font, text, screenWidth - mc.font.width(text) - 10, y + 12, 0xFFFFFFFF, true);
        }
    }
}