// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.client;

import com.howlingmoon.WereAbility;
import com.howlingmoon.WerewolfAttachment;
import com.howlingmoon.WerewolfCapability;
import com.howlingmoon.network.SelectAbilityPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class RadialMenuScreen extends Screen {

    private final WerewolfCapability cap;
    private final List<WereAbility> abilities;
    private int selectedIndex = -1;

    // Paleta de colores Premium
    private static final int COLOR_BG = 0xD0100810; // Fondo oscuro violáceo
    private static final int COLOR_BORDER = 0xFF8B0000; // Rojo Sangre
    private static final int COLOR_GOLD = 0xFFFFD700; // Dorado
    private static final int COLOR_ORANGE = 0xFFFF8C00; // Naranja XP
    private static final int COLOR_GRAY = 0xFF8A7A60; // Texto apagado

    public RadialMenuScreen() {
        super(Component.literal("Radial Menu"));
        this.cap = net.minecraft.client.Minecraft.getInstance().player.getData(WerewolfAttachment.WEREWOLF_DATA);
        this.abilities = new ArrayList<>(cap.getUnlockedAbilities());
    }

    /**
     * ANULACIÓN CRUCIAL: Evitamos que Minecraft dibuje su propio fondo o desenfoque
     * que se coloca por encima de nuestro renderizado personalizado.
     */
    @Override
    public void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        // No llamamos a super. Aquí mandamos nosotros.
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        // 1. DIBUJAR FONDO OSCURECIDO (La capa más profunda)
        gui.fill(0, 0, this.width, this.height, 0x90000000);

        int cx = this.width / 2;
        int cy = this.height / 2;
        int radius = 85;

        if (abilities.isEmpty()) {
            gui.drawCenteredString(this.font, "§cNo abilities learned yet...", cx, cy, 0xFFFFFFFF);
            return;
        }

        // 2. DIBUJAR DECORACIÓN CENTRAL (Bajo las letras)
        gui.fill(cx - 50, cy - 50, cx + 50, cy + 50, COLOR_BG);
        gui.renderOutline(cx - 51, cy - 51, 102, 102, COLOR_BORDER);

        // Lógica de detección de ratón
        double angleStep = 2 * Math.PI / abilities.size();
        double mouseAngle = Math.atan2(mouseY - cy, mouseX - cx);
        if (mouseAngle < 0)
            mouseAngle += 2 * Math.PI;

        double distSq = (mouseX - cx) * (mouseX - cx) + (mouseY - cy) * (mouseY - cy);
        if (distSq > 900 && distSq < 30000) {
            selectedIndex = (int) Math.round(mouseAngle / angleStep) % abilities.size();
        } else {
            selectedIndex = -1;
        }

        // 3. DIBUJAR LOS TEXTOS DE LAS HABILIDADES
        for (int i = 0; i < abilities.size(); i++) {
            WereAbility ability = abilities.get(i);
            double angle = i * angleStep;
            int x = (int) (cx + Math.cos(angle) * radius);
            int y = (int) (cy + Math.sin(angle) * radius);

            boolean hovered = (i == selectedIndex);
            boolean active = (ability == cap.getSelectedAbility());
            boolean onCD = ClientAbilityData.getCooldown(ability) > 0;

            int color = COLOR_GRAY;
            String text = ability.getDisplayName();

            if (onCD) {
                color = 0xFFFF4444;
                text = "§7[" + (ClientAbilityData.getCooldown(ability) / 20) + "s]";
            } else if (hovered) {
                color = COLOR_GOLD;
                text = "» " + text + " «";
            } else if (active) {
                color = COLOR_ORANGE;
                text = "✦ " + text + " ✦";
            }

            gui.drawCenteredString(this.font, text, x, y, color);
        }

        // 4. DIBUJAR INFORMACIÓN CENTRAL (Encima de todo)
        WereAbility info = selectedIndex != -1 ? abilities.get(selectedIndex) : cap.getSelectedAbility();
        if (info != null) {
            gui.drawCenteredString(this.font, "§n" + info.getDisplayName(), cx, cy - 30, COLOR_ORANGE);

            List<net.minecraft.util.FormattedCharSequence> lines = this.font
                    .split(Component.literal("§7" + info.getDescription()), 110);
            for (int i = 0; i < lines.size(); i++) {
                gui.drawString(this.font, lines.get(i), cx - (this.font.width(lines.get(i)) / 2), cy - 10 + (i * 10),
                        0xFFFFFFFF, true);
            }

            String footer = selectedIndex != -1 ? "§8[ Click to Select ]" : "§6[ SELECTED ]";
            gui.drawCenteredString(this.font, footer, cx, cy + 30, selectedIndex != -1 ? 0xFF555555 : COLOR_GOLD);
        }

        // NO llamamos a super.render para evitar que Minecraft dibuje nada más encima.
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedIndex != -1) {
            WereAbility sel = abilities.get(selectedIndex);
            cap.setSelectedAbility(sel);
            PacketDistributor.sendToServer(new SelectAbilityPacket(sel));
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}