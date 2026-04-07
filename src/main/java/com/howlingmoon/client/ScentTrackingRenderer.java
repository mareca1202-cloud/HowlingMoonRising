// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.client;

import com.howlingmoon.WerewolfAttachment;
import com.howlingmoon.WerewolfCapability;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ScentTrackingRenderer {

    // Efecto Premium de Rastro: Puntos de nube mística
    private static class ScentPoint {
        Vec3 pos;
        int age;
        int maxAge;
        float r, g, b;
        float randScaleOffset;
        float floatOffset; // Para movimiento senoidal Y

        ScentPoint(Vec3 pos, int maxAge, float r, float g, float b) {
            // Añadimos pequeña dispersión inicial X y Z
            this.pos = pos.add((Math.random() - 0.5) * 0.6, 0, (Math.random() - 0.5) * 0.6);
            this.age = 0;
            this.maxAge = maxAge;
            this.r = r;
            this.g = g;
            this.b = b;
            this.randScaleOffset = (float) (Math.random() * Math.PI * 2);
            this.floatOffset = (float) (Math.random() * Math.PI * 2);
        }
    }

    private static final List<ScentPoint> scentPoints = new ArrayList<>();
    private static int activationTimer = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused())
            return;
        WerewolfCapability cap = mc.player.getData(WerewolfAttachment.WEREWOLF_DATA);

        boolean isTracking = cap.isTransformed() && mc.player.hasEffect(MobEffects.LUCK);

        if (isTracking) {
            activationTimer++;
            // Tras 2 segundos...
            if (activationTimer > 40) {
                // Creamos rastros MUCHO MÁS SEGUIDOS para formar verdaderos 'Trails'
                if (mc.player.tickCount % 3 == 0) {
                    mc.level.getEntitiesOfClass(LivingEntity.class, mc.player.getBoundingBox().inflate(45.0),
                            e -> e != mc.player).forEach(entity -> {

                                float r = 0.9f, g = 0.7f, b = 0.1f; // Naranja Neutro
                                int duration = 90; // Duran menos, pero al haber más crean un camino fluido

                                if (entity instanceof Animal) {
                                    r = 0.0f;
                                    g = 1.0f;
                                    b = 0.4f;
                                } else if (entity instanceof Monster) {
                                    r = 1.0f;
                                    g = 0.05f;
                                    b = 0.2f;
                                } else if (entity instanceof Player) {
                                    r = 0.6f;
                                    g = 0.2f;
                                    b = 1.0f;
                                    duration = 150;
                                } // Rastros de players morados duran mas

                                scentPoints.add(new ScentPoint(entity.position().add(0, entity.getBbHeight() * 0.4, 0),
                                        duration, r, g, b));
                            });
                }
            }
        } else {
            activationTimer = 0;
            if (!scentPoints.isEmpty()) {
                scentPoints.removeIf(p -> {
                    p.age += 10;
                    return p.age > p.maxAge;
                }); // Se esfuman super rapido, en vez de corte brusco
            }
        }

        Iterator<ScentPoint> it = scentPoints.iterator();
        while (it.hasNext()) {
            ScentPoint p = it.next();
            p.age++;
            // Simular el rastro de humo subiendo sutilmente
            p.pos = p.pos.add(0, 0.005, 0);
            if (p.age >= p.maxAge)
                it.remove();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES)
            return;
        if (scentPoints.isEmpty())
            return;

        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        Quaternionf cameraRotation = mc.gameRenderer.getMainCamera().rotation(); // ROTACION PARA EL BILLBOARD

        float partialTick = event.getPartialTick().getGameTimeDeltaTicks();
        long time = mc.level.getGameTime();

        PoseStack poseStack = event.getPoseStack();

        // 1. Configuracion Magistral del Render
        RenderSystem.enableBlend();
        // BLEND MAGICO: En lugar de colores opacos, esto crea resplandores (Additive
        // Blending)
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        RenderSystem.disableDepthTest(); // Magia a través de las paredes
        RenderSystem.depthMask(false); // Para que las nubes se dibujen perfectas entre sí
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (ScentPoint p : scentPoints) {
            float ageProgress = (float) p.age / p.maxAge;
            // Formula Mística: Entrada rápida, y difuminado hiper suave de salida (Campana
            // de Gauss simulada)
            float alphaCurve = (float) Math.sin(ageProgress * Math.PI) * 1.5f;
            float finalAlpha = Mth.clamp(alphaCurve, 0f, 0.8f);

            // Latido constante del "aura" y balanceo horizontal
            float sizePulse = Mth.sin((time + partialTick) * 0.2f + p.randScaleOffset) * 0.08f;
            float xOffset = Mth.cos((time + partialTick) * 0.05f + p.floatOffset) * 0.1f;
            float baseSize = 0.22f + sizePulse;

            float renderX = (float) (p.pos.x - cameraPos.x + xOffset);
            float renderY = (float) (p.pos.y - cameraPos.y);
            float renderZ = (float) (p.pos.z - cameraPos.z);

            poseStack.pushPose();
            // Viajamos a la posicion exacta del Rastro Místico
            poseStack.translate(renderX, renderY, renderZ);
            // El modelo AHORA MIRA A LA CÁMARA EXACTAMENTE (Tecnica Billboard Completa)
            poseStack.mulPose(cameraRotation);

            Matrix4f matrix = poseStack.last().pose();

            // Dibujar capa principal (Aura Espectral exterior con Additive)
            drawGlowingQuad(buffer, matrix, baseSize * 1.2f, p.r * 0.4f, p.g * 0.4f, p.b * 0.4f, finalAlpha * 0.3f);

            // Dibujar núcleo concentrado brillante
            drawGlowingQuad(buffer, matrix, baseSize * 0.5f, p.r, p.g, p.b, finalAlpha);

            // Trazos energéticos en X (pequeños cortes salvajes de depredador dentro de las
            // nubes)
            drawDiagonalGlow(buffer, matrix, baseSize * 0.9f, p.r, p.g, p.b, finalAlpha * 0.6f);

            poseStack.popPose();
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        // Dejar Minecraft como nos lo encontramos para no romper renders
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    // Dibujamos un quad simple, pero siempre va a mirar a la camara por la matriz y
    // estara en aditivo
    private static void drawGlowingQuad(BufferBuilder buffer, Matrix4f matrix, float s, float r, float g, float b,
            float a) {
        buffer.addVertex(matrix, -s, -s, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, s, -s, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, s, s, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, -s, s, 0).setColor(r, g, b, a);
    }

    private static void drawDiagonalGlow(BufferBuilder buffer, Matrix4f matrix, float s, float r, float g, float b,
            float a) {
        float h = s * 0.15f; // Groso de la garra / marca energética
        // Marca diagonal principal
        buffer.addVertex(matrix, -s, -s - h, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, s, s - h, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, s, s + h, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, -s, -s + h, 0).setColor(r, g, b, a);

        // Contracara (forma una equis estirada)
        buffer.addVertex(matrix, s, -s - h, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, -s, s - h, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, -s, s + h, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, s, -s + h, 0).setColor(r, g, b, a);
    }
}