package com.jeff.horizon.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.jeff.horizon.SkyboxManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class MixinLevelRenderer {
    @Unique
    public SkyRenderer skyRenderer;

    @Unique
    @Final
    public RenderBuffers renderBuffers;

    @Unique
    private float nuit$tickDelta;

    @Unique
    private GpuBufferSlice nuit$fogParameters;

    @Inject(method = "addSkyPass*", remap = false, at = @At(value = "HEAD", remap = false))
    private void nuit$preAddSkyPass(FrameGraphBuilder frameGraphBuilder, Camera camera, GpuBufferSlice gpuBufferSlice, CallbackInfo ci) {
        this.nuit$tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
        this.nuit$fogParameters = RenderSystem.getShaderFog();
    }

    /**
     * Contains the logic for when skyboxes should be rendered.
     */
    @Inject(method = {"lambda$addSkyPass$8"}, require = 1, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFog(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", shift = At.Shift.AFTER), cancellable = true, remap = false)    private void nuit$renderCustomSkyboxes(CallbackInfo ci) {
        SkyboxManager skyboxManager = SkyboxManager.getInstance();
        if (skyboxManager.isEnabled() && !skyboxManager.getActiveSkyboxes().isEmpty()) {
            skyboxManager.renderSkyboxes(
                    skyRenderer,
                    RenderSystem.getModelViewStack(),
                    this.nuit$tickDelta,
                    Minecraft.getInstance().gameRenderer.getMainCamera(),
                    this.nuit$fogParameters,
                    this.renderBuffers.bufferSource()
            );
            ci.cancel();
        }
    }
}
