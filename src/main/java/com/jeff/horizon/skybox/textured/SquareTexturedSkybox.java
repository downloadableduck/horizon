package com.jeff.horizon.skybox.textured;

import com.jeff.horizon.components.*;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.jeff.horizon.components.*;
import com.jeff.horizon.skybox.AbstractSkybox;
import com.jeff.horizon.util.BufferUploader;
import com.jeff.horizon.util.DynamicTransformsBuilder;
import com.jeff.horizon.util.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.List;

import static com.jeff.horizon.skybox.vanilla.EndSkybox.END_SKY_LOCATION;

public class SquareTexturedSkybox extends TexturedSkybox {
    public static Codec<SquareTexturedSkybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Properties.CODEC.optionalFieldOf("properties", Properties.of()).forGetter(AbstractSkybox::getProperties),
            Conditions.CODEC.optionalFieldOf("conditions", Conditions.of()).forGetter(AbstractSkybox::getConditions),
            Blend.CODEC.optionalFieldOf("blend", Blend.normal()).forGetter(TexturedSkybox::getBlend),
            Texture.CODEC.fieldOf("texture").forGetter(SquareTexturedSkybox::getTexture)
    ).apply(instance, SquareTexturedSkybox::new));

    protected Texture texture;

    public SquareTexturedSkybox(Properties properties, Conditions conditions, Blend blend, Texture texture) {
        super(properties, conditions, blend);
        this.texture = texture;
    }


    @Override
    public void renderSkybox(SkyRenderer skyRendererAccess, Matrix4fStack matrix4fStack, float tickDelta, Camera camera, DynamicTransformsBuilder transformsBuilder, GpuBufferSlice fogParameters, MultiBufferSource.BufferSource bufferSource) {
        RenderSystem.setShaderFog(fogParameters);
        RenderPipeline pipeline = TEXTURED_SKYBOX_PIPELINE_CONSUMER.apply(this.getBlend().getBlendFunction());
        ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(pipeline.getVertexFormat().getVertexSize() * 24);
        BufferBuilder builder = new BufferBuilder(byteBufferBuilder, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
        for (int face = 0; face < 6; face++) {
            UVRange tex = Utils.TEXTURE_FACES[face];
            Matrix4f matrix4f = Utils.getMatrixForRotatedFace(face);
            builder.addVertex(matrix4f, -100.0F, -100.0F, -100.0F).setUv(tex.minU(), tex.minV());
            builder.addVertex(matrix4f, -100.0F, -100.0F, 100.0F).setUv(tex.minU(), tex.maxV());
            builder.addVertex(matrix4f, 100.0F, -100.0F, 100.0F).setUv(tex.maxU(), tex.maxV());
            builder.addVertex(matrix4f, 100.0F, -100.0F, -100.0F).setUv(tex.maxU(), tex.minV());
        }
        GpuBufferSlice dynamicTransforms = transformsBuilder.build();
        GpuTextureView textureView = Minecraft.getInstance().getTextureManager().getTexture(this.texture.getTextureId()).getTextureView();
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        GpuSampler gpuSampler = textureManager.getTexture(END_SKY_LOCATION).getSampler();
        //if there is DimensionType crash this might be the issue, end_sky_location is just DimensionType temporary placeholder
        BufferUploader.drawWithShader(pipeline, builder.buildOrThrow(), (pass) -> {
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            pass.bindTexture("Sampler0", textureView, gpuSampler);
        });
    }

    @Override
    public List<Identifier> getTexturesToRegister() {
        return List.of(this.texture.getTextureId());
    }

    public Texture getTexture() {
        return this.texture;
    }
}
