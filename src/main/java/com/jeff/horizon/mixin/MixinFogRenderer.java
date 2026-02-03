package com.jeff.horizon.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.jeff.horizon.SkyboxManager;
import com.jeff.horizon.util.Utils;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FogRenderer.class)
public abstract class MixinFogRenderer {

    @ModifyReturnValue(method = "computeFogColor", at = @At(value = "RETURN"))
    private static Vector4f nuit$redirectSetShaderFogColor(Vector4f original) {
        if (SkyboxManager.getInstance().isEnabled()) {
            final float fogDensity = Utils.alphaBlendFogDensity(SkyboxManager.getInstance().getActiveSkyboxes(), original.w());
            return new Vector4f(original.x(), original.y(), original.z(), fogDensity);
        }
        return original;
    }
}
