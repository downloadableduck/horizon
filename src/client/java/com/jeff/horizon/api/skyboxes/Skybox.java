package com.jeff.horizon.api.skyboxes;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SkyRenderer;
import org.joml.Matrix4fStack;

public interface Skybox {
    default int getLayer() {
        return 0;
    }

    void render(SkyRenderer skyRendererAccessor, Matrix4fStack matrix4fStack, float tickDelta, Camera camera, GpuBufferSlice fogParameters, MultiBufferSource bufferSource);

    void tick(ClientLevel clientLevel);

    boolean isActive();
}
