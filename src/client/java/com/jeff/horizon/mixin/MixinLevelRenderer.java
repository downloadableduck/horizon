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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class MixinLevelRenderer {
    @Unique
    private SkyRenderer skyRenderer;

    @Unique
    @Final
    private RenderBuffers renderBuffers;

    @Unique
    private static float nuit$tickDelta;

    @Unique
    private static GpuBufferSlice nuit$fogParameters;

    @Inject(method = "addSkyPass*", remap = false, at = @At(value = "HEAD", remap = false))
    private void nuit$preAddSkyPass(FrameGraphBuilder frameGraphBuilder, Camera camera, GpuBufferSlice gpuBufferSlice, CallbackInfo ci) {
        nuit$tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
        nuit$fogParameters = RenderSystem.getShaderFog();
    }

    /**
     * Contains the logic for when skyboxes should be rendered.
     */
    @Inject(method = {"method_62215", "addSkyPass"}, require = 1, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFog(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", shift = At.Shift.AFTER), cancellable = true)
    private static void nuit$renderCustomSkyboxes(CallbackInfo ci) {
        SkyboxManager skyboxManager = SkyboxManager.getInstance();
        Minecraft instance = Minecraft.getInstance();
        if (skyboxManager.isEnabled() && !skyboxManager.getActiveSkyboxes().isEmpty()) {
            LevelRenderer levelRenderer = instance.levelRenderer;
            skyboxManager.renderSkyboxes(
                    levelRenderer.skyRenderer,
                    RenderSystem.getModelViewStack(),
                    nuit$tickDelta,
                    Minecraft.getInstance().gameRenderer.getMainCamera(),
                    nuit$fogParameters,
                    levelRenderer.renderBuffers.bufferSource()
            );
            ci.cancel();
        }
    }
}
