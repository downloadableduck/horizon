package com.jeff.horizon.skybox.decorations;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.jeff.horizon.components.Blend;
import com.jeff.horizon.components.Conditions;
import com.jeff.horizon.components.Properties;
import com.jeff.horizon.skybox.AbstractSkybox;
import com.jeff.horizon.util.OverrideUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.MoonPhase;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL46C;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class DecorationBox extends AbstractSkybox {

    public static final Identifier SUN_LOCATION = Identifier.withDefaultNamespace("textures/environment/sun.png");
    public static final Identifier END_LIGHT_LOCATION = Identifier.withDefaultNamespace("textures/environment/end_flash.png");
    public static final Identifier MOON_LOCATION = Identifier.withDefaultNamespace("textures/environment/moon_phases.png");
    public static final Identifier END_SKY_LOCATION = Identifier.withDefaultNamespace("textures/environment/end_sky.png");

    public static Codec<DecorationBox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Properties.CODEC.optionalFieldOf("properties", Properties.decorations()).forGetter(DecorationBox::getProperties),
            Conditions.CODEC.optionalFieldOf("conditions", Conditions.of()).forGetter(DecorationBox::getConditions),
            Identifier.CODEC.optionalFieldOf("sun", SUN_LOCATION).forGetter(DecorationBox::getSunTexture),
            Identifier.CODEC.optionalFieldOf("moon", MOON_LOCATION).forGetter(DecorationBox::getMoonTexture),
            Codec.BOOL.optionalFieldOf("showSun", false).forGetter(DecorationBox::isSunEnabled),
            Codec.BOOL.optionalFieldOf("showMoon", false).forGetter(DecorationBox::isMoonEnabled),
            Codec.BOOL.optionalFieldOf("showStars", false).forGetter(DecorationBox::isStarsEnabled),
            Blend.CODEC.optionalFieldOf("blend", Blend.decorations()).forGetter(DecorationBox::getBlend)
    ).apply(instance, DecorationBox::new));

    private final Identifier sunTexture;
    private final Identifier moonTexture;
    private final boolean sunEnabled;
    private final boolean moonEnabled;
    private final boolean starsEnabled;
    private final Blend blend;

    public DecorationBox(Properties properties, Conditions conditions, Identifier sun, Identifier moon, boolean sunEnabled, boolean moonEnabled, boolean starsEnabled, Blend blend) {
        this.properties = properties;
        this.conditions = conditions;
        this.sunTexture = sun;
        this.moonTexture = moon;
        this.sunEnabled = sunEnabled;
        this.moonEnabled = moonEnabled;
        this.starsEnabled = starsEnabled;
        this.blend = blend;
    }

    private static final Function<Identifier, RenderType> CELESTIAL = Util.memoize((resourceLocation) -> create("celestial", 1536, false, false, RenderPipelines.CELESTIAL, RenderType.create(resourceLocation.toShortString(), RenderSetup.builder(RenderPipelines.CELESTIAL).createRenderSetup())));

    public RenderType celestial(Identifier resourceLocation) {
        return CELESTIAL.apply(resourceLocation);
    }

    public static RenderType create(String string, int i, RenderPipeline renderPipeline, RenderType compositeState) {
        return create(string, i, false, false, renderPipeline, compositeState);
    }

    public static RenderType create(String string, int i, boolean bl, boolean bl2, RenderPipeline renderPipeline, RenderType compositeState) {
        return RenderType.create(string, RenderSetup.builder(renderPipeline).createRenderSetup());
    }

    @Override
    public void render(SkyRenderer skyRendererAccessor, Matrix4fStack matrix4fStack, float tickDelta, Camera camera, GpuBufferSlice fogParameters, MultiBufferSource.BufferSource bufferSource) {
        PoseStack poseStack = new PoseStack();
        RenderSystem.setShaderFog(fogParameters);
        ClientLevel level = Objects.requireNonNull((ClientLevel) camera.entity().level());

        OverrideUtils.enableBlendingOverride(this.blend.getBlendFunction());

        matrix4fStack.pushMatrix();
        this.properties.rotation().apply(matrix4fStack, level);
        SkyRenderState state = Minecraft.getInstance().levelRenderer.levelRenderState.skyRenderState;

        // poseStack.mulPose(Axis.YP.rotation(-90F));
        // poseStack.mulPose(Axis.YP.rotation(level.getTimeOfDay(tickDelta) * 360.0F));
        // Iris Compat
        // poseStack.mulPose(Axis.ZP.rotationDegrees(IrisCompat.getSunPathRotation()));
        // poseStack.mulPose(Axis.XP.rotationDegrees(level.getSunAngle(tickDelta) * 360.0F * this.properties.rotation().speed()));

        if (this.sunEnabled) {
            //skyRendererAccessor.renderSun(1, poseStack);
           //this.renderSun(bufferSource, poseStack);
        }

        if (this.moonEnabled) {
            skyRendererAccessor.renderMoon(MoonPhase.FULL_MOON, 1, poseStack);
            //his.renderMoon((int) level.getDefaultClockTime(), bufferSource, poseStack);
        }

        if (this.sunEnabled || this.moonEnabled) {
            Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource().endBatch();
        }

        if (this.starsEnabled) {
            //PoseStack poseStack = new PoseStack();
            poseStack.mulPose(matrix4fStack);
            skyRendererAccessor.renderStars(5, poseStack);
        }

        matrix4fStack.popMatrix();
        OverrideUtils.disableBlendingOverride();
        GL46C.glBlendEquation(GL46C.GL_FUNC_ADD);
    }

    private void renderSun(MultiBufferSource.BufferSource multiBufferSource, PoseStack poseStack) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(celestial(this.sunTexture));
        int i = ARGB.white(this.alpha);
        Matrix4f matrix4f = poseStack.last().pose();
        vertexConsumer.addVertex(matrix4f, -30.0F, 100.0F, -30.0F).setUv(0.0F, 0.0F).setColor(i);
        vertexConsumer.addVertex(matrix4f, 30.0F, 100.0F, -30.0F).setUv(1.0F, 0.0F).setColor(i);
        vertexConsumer.addVertex(matrix4f, 30.0F, 100.0F, 30.0F).setUv(1.0F, 1.0F).setColor(i);
        vertexConsumer.addVertex(matrix4f, -30.0F, 100.0F, 30.0F).setUv(0.0F, 1.0F).setColor(i);
    }

    private void renderMoon(int moonPhase, MultiBufferSource multiBufferSource, PoseStack poseStack) {
        int xCoord = moonPhase % 4;
        int yCoord = moonPhase / 4 % 2;
        float startX = xCoord / 4.0F;
        float startY = yCoord / 2.0F;
        float endX = (xCoord + 1) / 4.0F;
        float endY = (yCoord + 1) / 2.0F;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(celestial(this.moonTexture));
        int p = ARGB.white(this.alpha);
        Matrix4f matrix4f = poseStack.last().pose();
        vertexConsumer.addVertex(matrix4f, -20.0F, -100.0F, 20.0F).setUv(endX, endY).setColor(p);
        vertexConsumer.addVertex(matrix4f, 20.0F, -100.0F, 20.0F).setUv(startX, endY).setColor(p);
        vertexConsumer.addVertex(matrix4f, 20.0F, -100.0F, -20.0F).setUv(startX, startY).setColor(p);
        vertexConsumer.addVertex(matrix4f, -20.0F, -100.0F, -20.0F).setUv(endX, startY).setColor(p);
    }

    public Identifier getSunTexture() {
        return this.sunTexture;
    }

    public Identifier getMoonTexture() {
        return this.moonTexture;
    }

    public boolean isSunEnabled() {
        return this.sunEnabled;
    }

    public boolean isMoonEnabled() {
        return this.moonEnabled;
    }

    public boolean isStarsEnabled() {
        return this.starsEnabled;
    }

    public Blend getBlend() {
        return this.blend;
    }
}