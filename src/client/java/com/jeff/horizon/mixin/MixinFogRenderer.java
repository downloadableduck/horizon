package com.jeff.horizon.mixin;

import com.jeff.horizon.SkyboxManager;
import com.jeff.horizon.util.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {

    @Inject(method = "computeFogColor", at = @At(value = "RETURN"))
    private static void nuit$redirectSetShaderFogColor(final Camera camera, final float partialTicks, final ClientLevel level, final int renderDistance, float darkenWorldAmount, final Vector4f original, CallbackInfo ci) {
        if (SkyboxManager.getInstance().isEnabled()) {
            darkenWorldAmount = Utils.alphaBlendFogDensity(SkyboxManager.getInstance().getActiveSkyboxes(), original.w());
            original.add(original.x(), original.y(), original.z(), darkenWorldAmount);
        }
    }
}
