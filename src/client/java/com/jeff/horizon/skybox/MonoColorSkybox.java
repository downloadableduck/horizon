package com.jeff.horizon.skybox;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.jeff.horizon.HorizonClient;
import com.jeff.horizon.components.Blend;
import com.jeff.horizon.components.Conditions;
import com.jeff.horizon.components.Properties;
import com.jeff.horizon.components.RGBA;
import com.jeff.horizon.util.BufferUploader;
import com.jeff.horizon.util.DynamicTransformsBuilder;
import com.jeff.horizon.util.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;

import java.util.Objects;
import java.util.function.Function;

public class MonoColorSkybox extends AbstractSkybox {
    private static final Function<BlendFunction, RenderPipeline> MONO_COLOR_SKYBOX_PIPELINE_CONSUMER = (blendFunction) -> {
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET);
        builder.withLocation(Objects.requireNonNull(Identifier.tryBuild(HorizonClient.MOD_ID, "pipeline/mono_color_skybox")));
        builder.withVertexShader("core/position_color");
        builder.withFragmentShader("core/position_color");
        builder.withDepthWrite(false);
        if (blendFunction != null) {
            builder.withBlend(blendFunction);
        } else {
            builder.withoutBlend();
        }
        builder.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS);
        return builder.build();
    };
    public static Codec<MonoColorSkybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Properties.CODEC.optionalFieldOf("properties", Properties.of()).forGetter(AbstractSkybox::getProperties),
            Conditions.CODEC.optionalFieldOf("conditions", Conditions.of()).forGetter(AbstractSkybox::getConditions),
            RGBA.CODEC.optionalFieldOf("color", RGBA.of()).forGetter(MonoColorSkybox::getColor),
            Blend.CODEC.optionalFieldOf("blend", Blend.normal()).forGetter(MonoColorSkybox::getBlend)
    ).apply(instance, MonoColorSkybox::new));
    public RGBA color;
    public Blend blend;

    public MonoColorSkybox(Properties properties, Conditions conditions, RGBA color, Blend blend) {
        super(properties, conditions);
        this.color = color;
        this.blend = blend;
    }

    @Override
    public void render(SkyRenderer skyRendererAccess, Matrix4fStack modelViewStack, float tickDelta, Camera camera, GpuBufferSlice fogParameters, MultiBufferSource bufferSource) {
        RenderSystem.setShaderFog(fogParameters);
        if (this.alpha > 0) {
            Vector4f colorModifier = this.blend.applyEquationAndGetColor(this.alpha);
            GpuBufferSlice dynamicTransforms = DynamicTransformsBuilder.of()
                    .withShaderColor(colorModifier)
                    .build();
            RenderPipeline pipeline = MONO_COLOR_SKYBOX_PIPELINE_CONSUMER.apply(this.blend.getBlendFunction());
            ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(pipeline.getVertexFormat().getVertexSize() * 24);
            BufferBuilder builder = new BufferBuilder(byteBufferBuilder, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
            for (int face = 0; face < 6; ++face) {
                Matrix4f matrix4f = Utils.getMatrixForRotatedFace(face);
                builder.addVertex(matrix4f, -100.0F, -100.0F, -100.0F).setColor(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.color.getAlpha());
                builder.addVertex(matrix4f, -100.0F, -100.0F, 100.0F).setColor(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.color.getAlpha());
                builder.addVertex(matrix4f, 100.0F, -100.0F, 100.0F).setColor(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.color.getAlpha());
                builder.addVertex(matrix4f, 100.0F, -100.0F, -100.0F).setColor(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.color.getAlpha());
            }
            BufferUploader.drawWithShader(pipeline, builder.buildOrThrow(), (pass) -> pass.setUniform("DynamicTransforms", dynamicTransforms));
        }
    }

    public RGBA getColor() {
        return this.color;
    }

    public Blend getBlend() {
        return this.blend;
    }
}
