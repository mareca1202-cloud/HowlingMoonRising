// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.mixin;

import com.howlingmoon.WerewolfAttachment;
import com.howlingmoon.WerewolfCapability;
import com.howlingmoon.WerewolfPlayerRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class PlayerSkinMixin {

    @Inject(method = "getSkin()Lnet/minecraft/client/resources/PlayerSkin;", at = @At("RETURN"), cancellable = true, remap = false)
    private void onGetSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (cap.isTransformed()) {
            System.out.println("WEREWOLF SKIN APPLIED!");
            PlayerSkin original = cir.getReturnValue();
            PlayerSkin werewolfSkin = new PlayerSkin(
                    WerewolfPlayerRenderer.WEREWOLF_SKIN,
                    original.textureUrl(),
                    original.capeTexture(),
                    original.elytraTexture(),
                    original.model(),
                    original.secure()
            );
            cir.setReturnValue(werewolfSkin);
        }
    }
}