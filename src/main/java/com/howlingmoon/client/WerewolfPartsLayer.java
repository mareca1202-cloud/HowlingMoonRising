// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.client;

import com.howlingmoon.WerewolfAttachment;
import com.howlingmoon.WerewolfCapability;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class WerewolfPartsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private final WerewolfPartsModel partsModel;

    public WerewolfPartsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer,
                              WerewolfPartsModel partsModel) {
        super(renderer);
        this.partsModel = partsModel;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isTransformed()) return;

        int overlay = net.minecraft.client.renderer.entity.LivingEntityRenderer.getOverlayCoords(player, 0.0f);
        PlayerModel<AbstractClientPlayer> playerModel = this.getParentModel();

        partsModel.animateTail(ageInTicks, limbSwing, limbSwingAmount);

        poseStack.pushPose();
        playerModel.getHead().translateAndRotate(poseStack);
        partsModel.renderSnout(poseStack, buffer, packedLight, overlay);
        poseStack.popPose();

        poseStack.pushPose();
        playerModel.body.translateAndRotate(poseStack);
        partsModel.renderTail(poseStack, buffer, packedLight, overlay);
        poseStack.popPose();
    }
}