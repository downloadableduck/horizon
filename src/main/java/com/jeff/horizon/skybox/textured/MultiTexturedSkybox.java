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
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.ArrayList;
import java.util.List;

public class MultiTexturedSkybox extends TexturedSkybox {
    public static Codec<MultiTexturedSkybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Properties.CODEC.optionalFieldOf("properties", Properties.of()).forGetter(AbstractSkybox::getProperties),
            Conditions.CODEC.optionalFieldOf("conditions", Conditions.of()).forGetter(AbstractSkybox::getConditions),
            Blend.CODEC.optionalFieldOf("blend", Blend.normal()).forGetter(TexturedSkybox::getBlend),
            AnimatableTexture.CODEC.listOf().optionalFieldOf("animatableTextures", new ArrayList<>()).forGetter(MultiTexturedSkybox::getAnimations)
    ).apply(instance, MultiTexturedSkybox::new));
    protected final List<AnimatableTexture> animatableTextures;

    private final float quadSize = 100F;
    private final UVRange quad = new UVRange(-this.quadSize, -this.quadSize, this.quadSize, this.quadSize);

    public MultiTexturedSkybox(Properties properties, Conditions conditions, Blend blend, List<AnimatableTexture> animatableTextures) {
        super(properties, conditions, blend);
        this.animatableTextures = animatableTextures;
    }

    @Override
    public void renderSkybox(SkyRenderer skyRendererAccess, Matrix4fStack matrix4fStack, float tickDelta, Camera camera, DynamicTransformsBuilder transformsBuilder, GpuBufferSlice fogParameters, MultiBufferSource.BufferSource bufferSource) {
        RenderSystem.setShaderFog(fogParameters);
        GpuBufferSlice dynamicTransforms = transformsBuilder.build();
        for (int face = 0; face < 6; ++face) {
            Matrix4f matrix4f = Utils.getMatrixForRotatedFace(face);

            // List of UV ranges for each face of the cube
            UVRange faceUVRange = Utils.TEXTURE_FACES[face];
            for (AnimatableTexture animatableTexture : this.animatableTextures) {
                animatableTexture.tick();
                UVRange intersect = Utils.findUVIntersection(faceUVRange, animatableTexture.getUvRange()); // todo: cache this intersections so we don't waste gpu cycles
                if (intersect != null && animatableTexture.getCurrentFrame() != null) {
                    UVRange intersectionOnCurrentTexture = Utils.mapUVRanges(faceUVRange, this.quad, intersect);
                    UVRange intersectionOnCurrentFrame = Utils.mapUVRanges(animatableTexture.getUvRange(), animatableTexture.getCurrentFrame(), intersect);

                    RenderPipeline pipeline = TEXTURED_SKYBOX_PIPELINE_CONSUMER.apply(this.getBlend().getBlendFunction());
                    ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(pipeline.getVertexFormat().getVertexSize() * 4);
                    BufferBuilder builder = new BufferBuilder(byteBufferBuilder, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
                    builder.addVertex(matrix4f, intersectionOnCurrentTexture.minU(), -this.quadSize, intersectionOnCurrentTexture.minV()).setUv(intersectionOnCurrentFrame.minU(), intersectionOnCurrentFrame.minV());
                    builder.addVertex(matrix4f, intersectionOnCurrentTexture.minU(), -this.quadSize, intersectionOnCurrentTexture.maxV()).setUv(intersectionOnCurrentFrame.minU(), intersectionOnCurrentFrame.maxV());
                    builder.addVertex(matrix4f, intersectionOnCurrentTexture.maxU(), -this.quadSize, intersectionOnCurrentTexture.maxV()).setUv(intersectionOnCurrentFrame.maxU(), intersectionOnCurrentFrame.maxV());
                    builder.addVertex(matrix4f, intersectionOnCurrentTexture.maxU(), -this.quadSize, intersectionOnCurrentTexture.minV()).setUv(intersectionOnCurrentFrame.maxU(), intersectionOnCurrentFrame.minV());

                    GpuTextureView textureView = Minecraft.getInstance().getTextureManager().getTexture(animatableTexture.getTexture().getTextureId()).getTextureView();
                    GpuSampler gpuSampler = Minecraft.getInstance().getTextureManager().getTexture(animatableTexture.getTexture().getTextureId()).getSampler();
                    BufferUploader.drawWithShader(pipeline, builder.buildOrThrow(), (pass) -> {
                        pass.setUniform("DynamicTransforms", dynamicTransforms);
                        pass.bindTexture("Sampler0", textureView, gpuSampler);
                    });
                }
            }
        }
    }

    public List<AnimatableTexture> getAnimations() {
        return this.animatableTextures;
    }

    @Override
    public List<Identifier> getTexturesToRegister() {
        return this.animatableTextures.stream().map(texture -> texture.getTexture().getTextureId()).toList();
    }
}
