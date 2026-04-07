// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class WerewolfScreen extends Screen {

    private static final int WIDTH = 240;
    private static final int HEIGHT = 310;

    private static final int COLOR_BG = 0xF0100810;
    private static final int COLOR_BORDER = 0xFF8B0000;
    private static final int COLOR_TITLE = 0xFFFF4500;
    private static final int COLOR_TEXT = 0xFFE8D5B0;
    private static final int COLOR_TEXT_DIM = 0xFF8A7A60;
    private static final int COLOR_XP_BG = 0xFF2A1A00;
    private static final int COLOR_XP_FILL = 0xFFFF8C00;
    private static final int COLOR_ATTR_HOVER = 0xFF2A1010;
    private static final int COLOR_DOT_FILLED = 0xFFFF4500;
    private static final int COLOR_DOT_EMPTY = 0xFF3A2020;
    private static final int COLOR_POINTS = 0xFFFFD700;

    private enum Tab {
        ATTRIBUTES, ABILITIES, INCLINATIONS
    }

    private Tab currentTab = Tab.ATTRIBUTES;
    private List<FormattedCharSequence> activeTooltip = null;

    public WerewolfScreen() {
        super(Component.literal("Werewolf"));
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    protected void init() {
        super.init();
        int left = (this.width - WIDTH) / 2;
        int top = (this.height - HEIGHT) / 2;

        WerewolfCapability cap = getCapability();
        if (cap == null)
            return;

        // --- INICIALIZACIÓN DE BOTONES SEGÚN TAB ---
        if (currentTab == Tab.ATTRIBUTES) {
            WereAttribute[] attrs = WereAttribute.values();
            for (int i = 0; i < attrs.length; i++) {
                WereAttribute attr = attrs[i];
                int btnY = top + 72 + i * 16 - 3;
                Button btn = Button.builder(Component.literal("+"), b -> {
                    PacketDistributor.sendToServer(new UpgradeAttributePacket(attr.name()));
                    this.clearWidgets();
                    this.init();
                })
                        .bounds(left + WIDTH - 22, btnY, 14, 10).build();
                btn.active = cap.isWerewolf() && cap.getAvailableAttributePoints() > 0 && cap.canUpgradeAttribute(attr);
                this.addRenderableWidget(btn);
            }
        } else if (currentTab == Tab.ABILITIES) {
            WereAbility[] abilities = WereAbility.values();
            for (int i = 0; i < abilities.length; i++) {
                WereAbility ability = abilities[i];
                if (!cap.getUnlockedAbilities().contains(ability)) {
                    int btnY = top + 72 + i * 18 - 3;
                    Button btn = Button.builder(Component.literal("Unlock"), b -> {
                        PacketDistributor.sendToServer(new com.howlingmoon.network.UnlockAbilityPacket(ability));
                        this.clearWidgets();
                        this.init();
                    })
                            .bounds(left + WIDTH - 55, btnY, 45, 12).build();
                    btn.active = cap.getAvailableAbilityPoints() >= ability.getCost();
                    this.addRenderableWidget(btn);
                }
            }
        } else if (currentTab == Tab.INCLINATIONS) {
            WereInclination[] inclinations = WereInclination.values();
            for (int i = 0; i < inclinations.length; i++) {
                WereInclination incl = inclinations[i];
                if (incl == WereInclination.NEUTRAL)
                    continue;
                int btnY = top + 72 + (i - 1) * 45 + 15;
                Button btn = Button.builder(Component.literal("Select"), b -> {
                    PacketDistributor.sendToServer(new com.howlingmoon.network.SelectInclinationPacket(incl));
                    this.clearWidgets();
                    this.init();
                })
                        .bounds(left + WIDTH - 60, btnY, 50, 14).build();
                btn.active = cap.getInclination() == WereInclination.NEUTRAL && cap.getLevel() >= 5;
                this.addRenderableWidget(btn);
            }
        }

        // --- BOTONES DE PESTAÑAS (SUBIDOS PARA EVITAR SOLAPAMIENTO) ---
        int tabY = top + HEIGHT - 28;
        this.addRenderableWidget(Button.builder(Component.literal("Attributes"), b -> {
            currentTab = Tab.ATTRIBUTES;
            this.clearWidgets();
            this.init();
        }).bounds(left + 10, tabY, 70, 16).build()).active = (currentTab != Tab.ATTRIBUTES);

        this.addRenderableWidget(Button.builder(Component.literal("Abilities"), b -> {
            currentTab = Tab.ABILITIES;
            this.clearWidgets();
            this.init();
        }).bounds(left + 85, tabY, 70, 16).build()).active = (currentTab != Tab.ABILITIES);

        this.addRenderableWidget(Button.builder(Component.literal("Paths"), b -> {
            currentTab = Tab.INCLINATIONS;
            this.clearWidgets();
            this.init();
        }).bounds(left + 160, tabY, 70, 16).build()).active = (currentTab != Tab.INCLINATIONS);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        activeTooltip = null;
        int left = (this.width - WIDTH) / 2;
        int top = (this.height - HEIGHT) / 2;

        // Fondo y Bordes
        gfx.fill(left, top, left + WIDTH, top + HEIGHT, COLOR_BG);
        gfx.renderOutline(left, top, WIDTH, HEIGHT, COLOR_BORDER);

        WerewolfCapability cap = getCapability();
        if (cap == null) {
            gfx.drawCenteredString(this.font, "§cYou are not a werewolf.", left + WIDTH / 2, top + HEIGHT / 2,
                    0xFFFFFFFF);
            super.render(gfx, mouseX, mouseY, partialTick);
            return;
        }

        // --- HEADER ---
        gfx.drawString(this.font, "§c⚡ WEREWOLF", left + 12, top + 8, COLOR_TITLE, false);

        // STATUS (MOVIDO A LA DERECHA PARA NO MOLESTAR)
        String status = cap.isTransformed() ? "§c[ TRANSFORMED ]" : "§7[ HUMAN FORM ]";
        gfx.drawString(this.font, status, left + WIDTH - this.font.width(status) - 12, top + 8, 0xFFFFFFFF, false);

        gfx.fill(left + 8, top + 18, left + WIDTH - 8, top + 19, COLOR_BORDER);

        // Nivel y Puntos
        gfx.drawString(this.font, "§7Level  §f" + cap.getLevel() + " §7/ 20", left + 12, top + 24, COLOR_TEXT, false);
        String ptsName = currentTab == Tab.ABILITIES ? "Ability Points: " : "Attr Points: ";
        String pts = ptsName + "§f"
                + (currentTab == Tab.ABILITIES ? cap.getAvailableAbilityPoints() : cap.getAvailableAttributePoints());
        gfx.drawString(this.font, pts, left + WIDTH - 12 - this.font.width(pts), top + 24, COLOR_POINTS, false);

        // Barra XP
        gfx.fill(left + 12, top + 37, left + WIDTH - 12, top + 45, COLOR_XP_BG);
        float xpRatio = cap.getLevel() >= 20 ? 1.0f : (float) cap.getExperience() / cap.expNeededForNextLevel();
        if (xpRatio > 0)
            gfx.fill(left + 12, top + 37, left + 12 + (int) ((WIDTH - 24) * xpRatio), top + 45, COLOR_XP_FILL);
        String xpStr = cap.getLevel() >= 20 ? "MAX" : cap.getExperience() + " / " + cap.expNeededForNextLevel() + " XP";
        gfx.drawCenteredString(this.font, "§7" + xpStr, left + WIDTH / 2, top + 38, COLOR_TEXT_DIM);

        gfx.fill(left + 8, top + 49, left + WIDTH - 8, top + 50, COLOR_BORDER);

        // --- CONTENIDO DE TABS ---
        if (currentTab == Tab.ATTRIBUTES) {
            gfx.drawString(this.font, "§7Attribute", left + 12, top + 55, COLOR_TEXT_DIM, false);
            gfx.drawString(this.font, "§7Level", left + WIDTH - 80, top + 55, COLOR_TEXT_DIM, false);

            WereAttribute[] attrs = WereAttribute.values();
            for (int i = 0; i < attrs.length; i++) {
                int rowY = top + 72 + i * 16;
                if (mouseX >= left + 8 && mouseX <= left + WIDTH - 8 && mouseY >= rowY - 3 && mouseY <= rowY + 12)
                    gfx.fill(left + 8, rowY - 3, left + WIDTH - 8, rowY + 12, COLOR_ATTR_HOVER);

                gfx.drawString(this.font, Component.translatable(attrs[i].getKey()), left + 14, rowY, COLOR_TEXT,
                        false);
                for (int d = 0; d < attrs[i].getMaxLevel(); d++)
                    gfx.fill(left + WIDTH - 84 + d * 8, rowY, left + WIDTH - 79 + d * 8, rowY + 5,
                            d < cap.getAttributeLevel(attrs[i]) ? COLOR_DOT_FILLED : COLOR_DOT_EMPTY);
            }
        } else if (currentTab == Tab.ABILITIES) {
            gfx.drawString(this.font, "§7Ability", left + 12, top + 55, COLOR_TEXT_DIM, false);
            WereAbility[] abilities = WereAbility.values();
            for (int i = 0; i < abilities.length; i++) {
                int rowY = top + 72 + i * 18;
                boolean unlocked = cap.getUnlockedAbilities().contains(abilities[i]);
                if (mouseX >= left + 8 && mouseX <= left + WIDTH - 8 && mouseY >= rowY - 3 && mouseY <= rowY + 14) {
                    gfx.fill(left + 8, rowY - 3, left + WIDTH - 8, rowY + 14, COLOR_ATTR_HOVER);
                    List<Component> t = new ArrayList<>();
                    t.add(Component.literal("§6" + abilities[i].getDisplayName()));
                    t.add(Component.literal("§7" + abilities[i].getDescription()));
                    activeTooltip = buildTooltipLines(t);
                }
                gfx.drawString(this.font, (unlocked ? "§6" : "§7") + abilities[i].getDisplayName(), left + 14, rowY,
                        COLOR_TEXT, false);
                gfx.drawString(this.font, unlocked ? "§a✔" : "§e" + abilities[i].getCost(), left + WIDTH - 75, rowY,
                        COLOR_TEXT, false);
            }
        } else if (currentTab == Tab.INCLINATIONS) {
            gfx.drawString(this.font, "§7Choose your path", left + 12, top + 55, COLOR_TEXT_DIM, false);
            WereInclination[] incls = WereInclination.values();
            for (int i = 0; i < incls.length; i++) {
                if (incls[i] == WereInclination.NEUTRAL)
                    continue;
                int rowY = top + 72 + (i - 1) * 45;
                boolean isCurrent = cap.getInclination() == incls[i];
                if (mouseX >= left + 8 && mouseX <= left + WIDTH - 8 && mouseY >= rowY - 3 && mouseY <= rowY + 41) {
                    gfx.fill(left + 8, rowY - 3, left + WIDTH - 8, rowY + 41, COLOR_ATTR_HOVER);
                    List<Component> t = new ArrayList<>();
                    t.add(Component.literal("§6" + incls[i].getDisplayName()));
                    for (String s : incls[i].getDescription().split("\n"))
                        t.add(Component.literal("§7" + s));
                    activeTooltip = buildTooltipLines(t);
                }
                gfx.drawString(this.font, (isCurrent ? "§a" : "§f") + incls[i].getDisplayName(), left + 14, rowY,
                        COLOR_TEXT, false);
                gfx.drawString(this.font, isCurrent ? "§a[ Selected ]" : "§7Hover for info...", left + 14, rowY + 12,
                        0xFFFFFFFF, false);
            }
            // MENSAJE DE NIVEL 5 (MÁS ABAJO Y CLARO)
            if (cap.getInclination() == WereInclination.NEUTRAL && cap.getLevel() < 5) {
                gfx.drawCenteredString(this.font, "§6⚠ Paths unlock at Level 5 ⚠", left + WIDTH / 2, top + HEIGHT - 65,
                        0xFFFFD700);
            }
        }

        super.render(gfx, mouseX, mouseY, partialTick);
        if (activeTooltip != null)
            gfx.renderTooltip(this.font, activeTooltip, mouseX, mouseY);
    }

    private List<FormattedCharSequence> buildTooltipLines(List<Component> components) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component c : components)
            lines.addAll(this.font.split(c, 200));
        return lines;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private WerewolfCapability getCapability() {
        if (Minecraft.getInstance().player == null)
            return null;
        return Minecraft.getInstance().player.getData(WerewolfAttachment.WEREWOLF_DATA);
    }
}