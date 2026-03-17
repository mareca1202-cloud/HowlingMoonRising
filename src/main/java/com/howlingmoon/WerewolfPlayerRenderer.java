// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = HowlingMoon.MODID, value = Dist.CLIENT)
public class WerewolfPlayerRenderer {

    public static final ResourceLocation WEREWOLF_SKIN =
            ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "textures/entity/werewolf.png");

    private static WerewolfReplacedRenderer renderer = null;

    private static WerewolfReplacedRenderer getRenderer() {
        if (renderer == null) {
            Minecraft mc = Minecraft.getInstance();
            EntityRendererProvider.Context context = new EntityRendererProvider.Context(
                    mc.getEntityRenderDispatcher(),
                    mc.getItemRenderer(),
                    mc.getBlockRenderer(),
                    mc.gameRenderer.itemInHandRenderer,
                    mc.getResourceManager(),
                    mc.getEntityModels(),
                    mc.font
            );
            renderer = new WerewolfReplacedRenderer(context);
        }
        return renderer;
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (cap.isTransformed() && player instanceof AbstractClientPlayer clientPlayer) {
            event.setCanceled(true);

            WerewolfReplacedRenderer.currentPlayer = clientPlayer;
            WerewolfReplacedRenderer.extractVanillaBones(clientPlayer, event.getPartialTick());

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180f));

            // Nado — igual que vanilla
            float swimAmount = clientPlayer.getSwimAmount(event.getPartialTick());
            if (swimAmount > 0f) {
                float xRot = clientPlayer.getXRot();
                // Vanilla: lerp(swimAmount, xRot, -90 - xRot)
                float swimAngleDeg = Mth.lerp(swimAmount, xRot, -90f - xRot);
                float swimAngleRad = swimAngleDeg * Mth.DEG_TO_RAD;
                poseStack.translate(0, -1.0f * swimAmount, 0.3f * swimAmount);
                poseStack.mulPose(Axis.XP.rotation(swimAngleRad));
            }

            getRenderer().render(
                    clientPlayer,
                    clientPlayer.getYRot(),
                    event.getPartialTick(),
                    poseStack,
                    event.getMultiBufferSource(),
                    event.getPackedLight()
            );
            poseStack.popPose();

            WerewolfReplacedRenderer.currentPlayer = null;
        }
    }
}