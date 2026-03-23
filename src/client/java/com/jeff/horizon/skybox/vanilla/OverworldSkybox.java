package com.jeff.horizon.skybox.vanilla;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.jeff.horizon.api.HorizonApi;
import com.jeff.horizon.components.Conditions;
import com.jeff.horizon.components.Properties;
import com.jeff.horizon.skybox.AbstractSkybox;
import com.jeff.horizon.skybox.decorations.DecorationBox;
import com.jeff.horizon.util.BufferUploader;
import com.jeff.horizon.util.DynamicTransformsBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.SkyRenderState;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class OverworldSkybox extends AbstractSkybox {

    public float sunAngle;
    int color;
    public static Codec<OverworldSkybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Properties.CODEC.optionalFieldOf("properties", Properties.of()).forGetter(AbstractSkybox::getProperties),
            Conditions.CODEC.optionalFieldOf("conditions", Conditions.of()).forGetter(AbstractSkybox::getConditions)
    ).apply(instance, OverworldSkybox::new));

    public OverworldSkybox(Properties properties, Conditions conditions) {
        super(properties, conditions);
    }

    public void skyRenderStateAccessor(SkyRenderState skyRenderState) {
        sunAngle = skyRenderState.sunAngle;
        color = skyRenderState.sunriseAndSunsetColor;
    }

    public void renderSkyDisc(float f, float g, float h, SkyRenderer skyRenderer) {
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(f, g, h, 1.0F), new Vector3f(), new Matrix4f());
        GpuTextureView gpuTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView gpuTextureView2 = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sky disc", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty())) {
            renderPass.setPipeline(RenderPipelines.SKY);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, skyRenderer.topSkyBuffer);
            renderPass.draw(0, 10);
        }

    }


    @Override
    public void render(SkyRenderer skyRendererAccessor, Matrix4fStack matrix4fStack, float tickDelta, Camera camera, GpuBufferSlice fogParameters, MultiBufferSource bufferSource) {
        RenderSystem.setShaderFog(fogParameters);

        this.skyRenderStateAccessor(new SkyRenderState());

        ClientLevel level = (ClientLevel) camera.entity().level();
        float timeOfDay = DeltaTracker.ONE.getGameTimeDeltaTicks();
        int sunriseOrSunsetColor = EnvironmentAttributes.SUNRISE_SUNSET_COLOR.defaultValue();
        int skyColor = OverworldBiomes.calculateSkyColor(tickDelta);

        // Light Sky
        this.renderSkyDisc(ARGB.redFloat(skyColor), ARGB.greenFloat(skyColor), ARGB.blueFloat(skyColor), skyRendererAccessor);
        if (HorizonApi.getInstance().getActiveSkyboxes().stream().anyMatch(skybox -> skybox instanceof DecorationBox decorationBox && decorationBox.getProperties().rotation().skyboxRotation())) {
            sunAngle = Mth.positiveModulo(level.getDayTime() / 24000F + 0.75F, 1);
        }

        this.renderSunriseAndSunset(matrix4fStack, sunAngle, sunriseOrSunsetColor);

        // Dark Sky
        double eyeHeight = camera.entity().getEyePosition(tickDelta).y - level.getLevelData().getHorizonHeight(level);
        if (eyeHeight < 0.0) {
            skyRendererAccessor.renderDarkDisc();
        }
    }

    private void renderSunriseAndSunset(Matrix4fStack matrix4fStack, float sunAngle, int sunriseOrSunsetColor) {
        matrix4fStack.pushMatrix();

        // Rotate to orient the effect properly
        matrix4fStack.rotate(Axis.XP.rotationDegrees(90.0F));
        float zRotation = Mth.sin(sunAngle) < 0.0F ? 180.0F : 0.0F;
        matrix4fStack.rotate(Axis.ZP.rotationDegrees(zRotation));
        matrix4fStack.rotate(Axis.ZP.rotationDegrees(90.0F));

        RenderPipeline pipeline = RenderPipelines.SUNRISE_SUNSET;
        ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(pipeline.getVertexFormat().getVertexSize() * 17);
        BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());

        float alpha = ARGB.alphaFloat(sunriseOrSunsetColor) * this.alpha;
        bufferBuilder.addVertex(matrix4fStack, 0.0F, 100.0F, 0.0F).setColor(sunriseOrSunsetColor);

        int transparentColor = ARGB.transparent(sunriseOrSunsetColor);
        for (int i = 0; i <= 16; i++) {
            float angleRadians = (float) i * ((float) Math.PI * 2F) / 16.0F;
            float x = Mth.sin(angleRadians);
            float y = Mth.cos(angleRadians);
            float z = -y * 40.0F * alpha;
            bufferBuilder.addVertex(matrix4fStack, x * 120.0F, y * 120.0F, z).setColor(transparentColor);
        }
        GpuBufferSlice dynamicTransforms = new DynamicTransformsBuilder().build();
        BufferUploader.drawWithShader(pipeline, bufferBuilder.buildOrThrow(), (pass) -> pass.setUniform("DynamicTransforms", dynamicTransforms));
        matrix4fStack.popMatrix();
    }
}
