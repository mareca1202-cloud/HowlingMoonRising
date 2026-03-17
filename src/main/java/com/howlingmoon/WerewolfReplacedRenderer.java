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

    private static float vanillaHeadX, vanillaHeadY;
    private static float vanillaRightArmX, vanillaRightArmY, vanillaRightArmZ;
    private static float vanillaLeftArmX,  vanillaLeftArmY,  vanillaLeftArmZ;
    private static float vanillaRightLegX, vanillaRightLegY, vanillaRightLegZ;
    private static float vanillaLeftLegX,  vanillaLeftLegY,  vanillaLeftLegZ;
    private static float vanillaBodyX, vanillaBodyY, vanillaBodyZ;

    public WerewolfReplacedRenderer(EntityRendererProvider.Context context) {
        super(context, new WerewolfReplacedModel(), WerewolfAnimatable.getInstance());
    }

    public static void extractVanillaBones(AbstractClientPlayer player, float partialTick) {
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        var vanillaRenderer = dispatcher.getRenderer(player);

        if (vanillaRenderer instanceof PlayerRenderer playerRenderer) {
            PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();

            float walkPos   = player.walkAnimation.position(partialTick);
            float walkSpeed = player.walkAnimation.speed(partialTick);
            float tick      = player.tickCount + partialTick;
            float headYaw   = Mth.clamp(
                    Mth.lerp(partialTick, player.yHeadRotO, player.yHeadRot)
                            - Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot),
                    -45f, 45f);
            float headPitch = player.getXRot();

            model.attackTime = player.getAttackAnim(partialTick);
            model.prepareMobModel(player, walkPos, walkSpeed, partialTick);
            model.setupAnim(player, walkPos, walkSpeed, tick, headYaw, headPitch);

            vanillaHeadX =  model.head.xRot;
            vanillaHeadY = -model.head.yRot;

            // Con 180° flip en Y: izquierda/derecha se invierten
            // X e Z también se invierten para que las rotaciones sean correctas
            vanillaRightArmX = -model.leftArm.xRot;
            vanillaRightArmY =  model.leftArm.yRot;
            vanillaRightArmZ = -model.leftArm.zRot;
            vanillaLeftArmX  = -model.rightArm.xRot;
            vanillaLeftArmY  =  model.rightArm.yRot;
            vanillaLeftArmZ  = -model.rightArm.zRot;

            vanillaRightLegX = -model.leftLeg.xRot;
            vanillaRightLegY =  model.leftLeg.yRot;
            vanillaRightLegZ = -model.leftLeg.zRot;
            vanillaLeftLegX  = -model.rightLeg.xRot;
            vanillaLeftLegY  =  model.rightLeg.yRot;
            vanillaLeftLegZ  = -model.rightLeg.zRot;

            vanillaBodyX = model.body.xRot;
            vanillaBodyY = model.body.yRot;
            vanillaBodyZ = model.body.zRot;
        }
    }

    @Override
    public void applyRenderLayersForBone(PoseStack poseStack, WerewolfAnimatable animatable,
                                         GeoBone bone, RenderType renderType, MultiBufferSource bufferSource,
                                         VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        switch (bone.getName()) {
            case "head_22" -> {
                bone.setRotX(vanillaHeadX);
                bone.setRotY(vanillaHeadY);
            }
            case "left_arm_43" -> {
                bone.setRotX(vanillaLeftArmX);
                bone.setRotY(vanillaLeftArmY);
                bone.setRotZ(vanillaLeftArmZ);
            }
            case "right_arm_54" -> {
                bone.setRotX(vanillaRightArmX);
                bone.setRotY(vanillaRightArmY);
                bone.setRotZ(vanillaRightArmZ);
            }
            case "left_leg_65" -> {
                bone.setRotX(vanillaLeftLegX);
                bone.setRotY(vanillaLeftLegY);
                bone.setRotZ(vanillaLeftLegZ);
            }
            case "right_leg_76" -> {
                bone.setRotX(vanillaRightLegX);
                bone.setRotY(vanillaRightLegY);
                bone.setRotZ(vanillaRightLegZ);
            }
            case "body_32" -> {
                bone.setRotX(vanillaBodyX);
                bone.setRotY(vanillaBodyY);
                bone.setRotZ(vanillaBodyZ);
            }
        }

        super.applyRenderLayersForBone(poseStack, animatable, bone, renderType,
                bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }
}