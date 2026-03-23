package com.jeff.horizon.skybox.vanilla;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.jeff.horizon.components.Conditions;
import com.jeff.horizon.components.Properties;
import com.jeff.horizon.skybox.AbstractSkybox;
import com.jeff.horizon.util.BufferUploader;
import com.jeff.horizon.util.DynamicTransformsBuilder;
import com.jeff.horizon.util.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class EndSkybox extends AbstractSkybox {
    public static Codec<EndSkybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Properties.CODEC.optionalFieldOf("properties", Properties.of()).forGetter(AbstractSkybox::getProperties),
            Conditions.CODEC.optionalFieldOf("conditions", Conditions.of()).forGetter(AbstractSkybox::getConditions)
    ).apply(instance, EndSkybox::new));

    public static final Identifier END_SKY_LOCATION = Identifier.withDefaultNamespace("textures/environment/end_sky.png");

    public EndSkybox(Properties properties, Conditions conditions) {
        super(properties, conditions);
    }

    @Override
    public void render(SkyRenderer skyRendererAccess, Matrix4fStack matrix4fStack, float tickDelta, Camera camera, GpuBufferSlice fogParameters, MultiBufferSource bufferSource) {
        RenderPipeline pipeline = RenderPipelines.END_SKY;
        ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(pipeline.getVertexFormat().getVertexSize() * 24);
        BufferBuilder builder = new BufferBuilder(byteBufferBuilder, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
        for (int face = 0; face < 6; ++face) {
            int color = ARGB.color(0x282828, (int) (255 * this.alpha));
            Matrix4f matrix4f = Utils.getMatrixForRotatedFace(face);
            builder.addVertex(matrix4f, -100.0F, -100.0F, -100.0F).setUv(0.0F, 0.0F).setColor(color);
            builder.addVertex(matrix4f, -100.0F, -100.0F, 100.0F).setUv(0.0F, 16.0F).setColor(color);
            builder.addVertex(matrix4f, 100.0F, -100.0F, 100.0F).setUv(16.0F, 16.0F).setColor(color);
            builder.addVertex(matrix4f, 100.0F, -100.0F, -100.0F).setUv(16.0F, 0.0F).setColor(color);
        }

        GpuBufferSlice dynamicTransforms = new DynamicTransformsBuilder().build();
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture abstractTexture = textureManager.getTexture(END_SKY_LOCATION);
        GpuSampler gpuSampler = abstractTexture.getSampler();
        GpuTextureView endSkyTextureView = abstractTexture.getTextureView();
        BufferUploader.drawWithShader(pipeline, builder.buildOrThrow(), (pass) -> {
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            pass.bindTexture("Sampler0", endSkyTextureView, gpuSampler);
        });
    }
}

