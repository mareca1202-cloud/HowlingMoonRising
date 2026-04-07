// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

public class WerewolfReplacedRenderer extends GeoReplacedEntityRenderer<AbstractClientPlayer, WerewolfAnimatable> {

    public static AbstractClientPlayer currentPlayer = null;

    private static float headX, headY, headZ;
    private static float rightArmX, rightArmY, rightArmZ;
    private static float leftArmX, leftArmY, leftArmZ;
    private static float rightLegX, rightLegY, rightLegZ;
    private static float leftLegX, leftLegY, leftLegZ;
    private static float bodyX, bodyY, bodyZ;

    public WerewolfReplacedRenderer(EntityRendererProvider.Context context) {
        super(context, new WerewolfReplacedModel(), WerewolfAnimatable.getInstance());
    }

    public static void extractVanillaBones(AbstractClientPlayer player, float partialTick) {
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        var vanillaRenderer = dispatcher.getRenderer(player);

        if (vanillaRenderer instanceof PlayerRenderer playerRenderer) {
            PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();

            float walkPos = player.walkAnimation.position(partialTick);
            float walkSpeed = player.walkAnimation.speed(partialTick);
            float tick = player.tickCount + partialTick;

            // Lógica Anti-Bloqueo de Cabeza
            float headYaw = Mth.lerp(partialTick, player.yHeadRotO, player.yHeadRot);
            float bodyYaw = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot);
            float netHeadYaw = Mth.wrapDegrees(headYaw - bodyYaw);
            netHeadYaw = Mth.clamp(netHeadYaw, -50.0f, 50.0f);

            float headPitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());

            model.attackTime = player.getAttackAnim(partialTick);
            model.prepareMobModel(player, walkPos, walkSpeed, partialTick);
            model.setupAnim(player, walkPos, walkSpeed, tick, netHeadYaw, headPitch);

            headX = headPitch * ((float) Math.PI / 180F);
            headY = netHeadYaw * ((float) Math.PI / 180F);
            headZ = model.head.zRot;

            rightArmX = model.rightArm.xRot;
            rightArmY = model.rightArm.yRot;
            rightArmZ = model.rightArm.zRot;

            leftArmX = model.leftArm.xRot;
            leftArmY = model.leftArm.yRot;
            leftArmZ = model.leftArm.zRot;

            rightLegX = model.rightLeg.xRot;
            rightLegY = model.rightLeg.yRot;
            rightLegZ = model.rightLeg.zRot;

            leftLegX = model.leftLeg.xRot;
            leftLegY = model.leftLeg.yRot;
            leftLegZ = model.leftLeg.zRot;

            bodyX = model.body.xRot;
            bodyY = model.body.yRot;
            bodyZ = model.body.zRot;
        }
    }

    @Override
    public void applyRenderLayersForBone(PoseStack poseStack, WerewolfAnimatable animatable,
            GeoBone bone, RenderType renderType, MultiBufferSource bufferSource,
            VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        switch (bone.getName()) {
            case "head_22" -> {
                bone.setRotX(-headX);
                bone.setRotY(-headY);
                bone.setRotZ(headZ);
            }
            case "left_arm_43" -> {
                bone.setRotX(-rightArmX);
                bone.setRotY(-rightArmY);
                bone.setRotZ(-rightArmZ);
            }
            case "right_arm_54" -> {
                bone.setRotX(-leftArmX);
                bone.setRotY(-leftArmY);
                bone.setRotZ(-leftArmZ);
            }
            case "left_leg_65" -> {
                bone.setRotX(-rightLegX);
                bone.setRotY(-rightLegY);
                bone.setRotZ(-rightLegZ);
            }
            case "right_leg_76" -> {
                bone.setRotX(-leftLegX);
                bone.setRotY(-leftLegY);
                bone.setRotZ(-leftLegZ);
            }
            case "body_32" -> {
                bone.setRotX(-bodyX);
                bone.setRotY(-bodyY);
                bone.setRotZ(bodyZ);
            }
        }

        super.applyRenderLayersForBone(poseStack, animatable, bone, renderType,
                bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }
}