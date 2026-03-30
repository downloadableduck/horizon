package com.jeff.horizon.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.jeff.horizon.util.OverrideUtils;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import net.minecraft.world.level.dimension.DimensionType;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL46;
import org.lwjgl.opengl.GL46C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

/**This mixin is causing the issue which makes the decoration boxes not render.*/
@Mixin(value = GlCommandEncoder.class)
public class MixinGlCommandEncoder {

    /**Controls the blending for the skyboxes.*/
    @WrapOperation(method = {"applyPipelineState"}, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/ColorTargetState;blendFunction()Ljava/util/Optional;", remap = false), remap = false)
    //the target used to be Lcom/mojang/blaze3d/pipeline/RenderPipeline;getBlendFunction()Ljava/util/Optional;, but that was removed

    public Optional<BlendFunction> nuit$overrideBlending(ColorTargetState instance, Operation<Optional<BlendFunction>> original) {
        if (OverrideUtils.isOverridingBlending()) {
            return OverrideUtils.getOverridenBlendFunction() != null ? Optional.of(OverrideUtils.getOverridenBlendFunction()) : Optional.empty();
        }
        else {
            return original.call(instance);
        }
    }
}