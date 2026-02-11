package com.jeff.horizon.skybox.decorations;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static com.mojang.blaze3d.systems.RenderSystem.assertOnRenderThread;

public abstract class RenderType extends RenderStateShard {

    private static final Vector3f modelOffset = new Vector3f();
    public static Vector3f getModelOffset() {
        assertOnRenderThread();
        return modelOffset;
    }

    protected static final OutputStateShard PARTICLES_TARGET = new OutputStateShard("particles_target", () -> {
        RenderTarget renderTarget = Minecraft.getInstance().levelRenderer.getParticlesTarget();
        return renderTarget != null ? renderTarget : Minecraft.getInstance().getMainRenderTarget();
    });

    private static final Function<Identifier, RenderType> CELESTIAL;
    private final int bufferSize;
    private final boolean affectsCrumbling;
    private final boolean sortOnUpload;

    private static Function<Identifier, RenderType> createWeather(RenderPipeline renderPipeline) {
        return Util.memoize((Identifier) -> create("weather", 1536, false, false, renderPipeline, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(Identifier, false)).setOutputState(WEATHER_TARGET).setLightmapState(LIGHTMAP).createCompositeState(false)));
    }

    public static RenderType celestial(Identifier Identifier) {
        return CELESTIAL.apply(Identifier);
    }

    public RenderType(String string, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
        super(string, runnable, runnable2);
        this.bufferSize = i;
        this.affectsCrumbling = bl;
        this.sortOnUpload = bl2;
    }

    static CompositeRenderType create(String string, int i, RenderPipeline renderPipeline, CompositeState compositeState) {
        return create(string, i, false, false, renderPipeline, compositeState);
    }

    private static CompositeRenderType create(String string, int i, boolean bl, boolean bl2, RenderPipeline renderPipeline, CompositeState compositeState) {
        return new CompositeRenderType(string, i, bl, bl2, renderPipeline, compositeState);
    }

    public abstract void draw(MeshData meshData);

    public abstract VertexFormat format();

    public abstract VertexFormat.Mode mode();

    public boolean canConsolidateConsecutiveGeometry() {
        return !this.mode().connectedPrimitives;
    }

    public boolean sortOnUpload() {
        return this.sortOnUpload;
    }

        static {
        CELESTIAL = Util.memoize((Identifier) -> create("celestial", 1536, false, false, RenderPipelines.CELESTIAL, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(Identifier, false)).createCompositeState(false)));
        }


    protected enum OutlineProperty {
        NONE("none"),
        IS_OUTLINE("is_outline"),
        AFFECTS_OUTLINE("affects_outline");

        private final String name;

        OutlineProperty(final String string2) {
            this.name = string2;
        }

        public String toString() {
            return this.name;
        }
    }

    protected static final class CompositeState {
        final RenderStateShard.EmptyTextureStateShard textureState;
        final RenderStateShard.OutputStateShard outputState;
        final OutlineProperty outlineProperty;
        final ImmutableList<RenderStateShard> states;

        CompositeState(RenderStateShard.EmptyTextureStateShard emptyTextureStateShard, RenderStateShard.LightmapStateShard lightmapStateShard, RenderStateShard.OverlayStateShard overlayStateShard, RenderStateShard.LayeringStateShard layeringStateShard, RenderStateShard.OutputStateShard outputStateShard, RenderStateShard.TexturingStateShard texturingStateShard, RenderStateShard.LineStateShard lineStateShard, OutlineProperty outlineProperty) {
            this.textureState = emptyTextureStateShard;
            this.outputState = outputStateShard;
            this.outlineProperty = outlineProperty;
            this.states = ImmutableList.of(emptyTextureStateShard, lightmapStateShard, overlayStateShard, layeringStateShard, outputStateShard, texturingStateShard, lineStateShard);
        }

        public String toString() {
            String var10000 = String.valueOf(this.states);
            return "CompositeState[" + var10000 + ", outlineProperty=" + String.valueOf(this.outlineProperty) + "]";
        }

        public static CompositeStateBuilder builder() {
            return new CompositeStateBuilder();
        }

        public static class CompositeStateBuilder {
            private RenderStateShard.EmptyTextureStateShard textureState;
            private RenderStateShard.LightmapStateShard lightmapState;
            private RenderStateShard.OverlayStateShard overlayState;
            private RenderStateShard.LayeringStateShard layeringState;
            private RenderStateShard.OutputStateShard outputState;
            private RenderStateShard.TexturingStateShard texturingState;
            private RenderStateShard.LineStateShard lineState;

            CompositeStateBuilder() {
                this.textureState = RenderStateShard.NO_TEXTURE;
                this.lightmapState = RenderStateShard.NO_LIGHTMAP;
                this.overlayState = RenderStateShard.NO_OVERLAY;
                this.layeringState = RenderStateShard.NO_LAYERING;
                this.outputState = RenderStateShard.MAIN_TARGET;
                this.texturingState = RenderStateShard.DEFAULT_TEXTURING;
                this.lineState = RenderStateShard.DEFAULT_LINE;
            }

            protected CompositeStateBuilder setTextureState(RenderStateShard.EmptyTextureStateShard emptyTextureStateShard) {
                this.textureState = emptyTextureStateShard;
                return this;
            }

            protected CompositeStateBuilder setLightmapState(RenderStateShard.LightmapStateShard lightmapStateShard) {
                this.lightmapState = lightmapStateShard;
                return this;
            }

            protected CompositeStateBuilder setOverlayState(RenderStateShard.OverlayStateShard overlayStateShard) {
                this.overlayState = overlayStateShard;
                return this;
            }

            protected CompositeStateBuilder setLayeringState(RenderStateShard.LayeringStateShard layeringStateShard) {
                this.layeringState = layeringStateShard;
                return this;
            }

            protected CompositeStateBuilder setOutputState(RenderStateShard.OutputStateShard outputStateShard) {
                this.outputState = outputStateShard;
                return this;
            }

            protected CompositeStateBuilder setTexturingState(RenderStateShard.TexturingStateShard texturingStateShard) {
                this.texturingState = texturingStateShard;
                return this;
            }

            protected CompositeStateBuilder setLineState(RenderStateShard.LineStateShard lineStateShard) {
                this.lineState = lineStateShard;
                return this;
            }

            protected CompositeState createCompositeState(boolean bl) {
                return this.createCompositeState(bl ? RenderType.OutlineProperty.AFFECTS_OUTLINE : RenderType.OutlineProperty.NONE);
            }

            protected CompositeState createCompositeState(OutlineProperty outlineProperty) {
                return new CompositeState(this.textureState, this.lightmapState, this.overlayState, this.layeringState, this.outputState, this.texturingState, this.lineState, outlineProperty);
            }
        }
    }

    static final class CompositeRenderType extends RenderType {
        static final BiFunction<Identifier, Boolean, RenderType> OUTLINE = Util.memoize((Identifier, boolean_) -> RenderType.create("outline", 1536, boolean_ ? RenderPipelines.OUTLINE_CULL : RenderPipelines.OUTLINE_NO_CULL, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(Identifier, false)).setOutputState(OUTLINE_TARGET).createCompositeState(RenderType.OutlineProperty.IS_OUTLINE)));
        private final CompositeState state;
        private final RenderPipeline renderPipeline;
        private final boolean isOutline;

        CompositeRenderType(String string, int i, boolean bl, boolean bl2, RenderPipeline renderPipeline, CompositeState compositeState) {
            super(string, i, bl, bl2, () -> compositeState.states.forEach(RenderStateShard::setupRenderState), () -> compositeState.states.forEach(RenderStateShard::clearRenderState));
            this.state = compositeState;
            this.renderPipeline = renderPipeline;
            this.isOutline = compositeState.outlineProperty == RenderType.OutlineProperty.IS_OUTLINE;
        }

        public VertexFormat format() {
            return this.renderPipeline.getVertexFormat();
        }

        public VertexFormat.Mode mode() {
            return this.renderPipeline.getVertexFormatMode();
        }

        public void draw(MeshData meshData) {
            this.setupRenderState();
            GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), getModelOffset(), RenderSystem.getModelViewMatrix());
            MeshData var3 = meshData;

            try {
                GpuBuffer gpuBuffer = this.renderPipeline.getVertexFormat().uploadImmediateVertexBuffer(meshData.vertexBuffer());
                GpuBuffer gpuBuffer2;
                VertexFormat.IndexType indexType;
                if (meshData.indexBuffer() == null) {
                    RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(meshData.drawState().mode());
                    gpuBuffer2 = autoStorageIndexBuffer.getBuffer(meshData.drawState().indexCount());
                    indexType = autoStorageIndexBuffer.type();
                } else {
                    gpuBuffer2 = this.renderPipeline.getVertexFormat().uploadImmediateIndexBuffer(meshData.indexBuffer());
                    indexType = meshData.drawState().indexType();
                }

                RenderTarget renderTarget = this.state.outputState.getRenderTarget();
                GpuTextureView gpuTextureView = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : renderTarget.getColorTextureView();
                GpuTextureView gpuTextureView2 = renderTarget.useDepth ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : renderTarget.getDepthTextureView()) : null;

                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Immediate draw for " + this.getName(), gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty())) {
                    renderPass.setPipeline(this.renderPipeline);
                    ScissorState scissorState = RenderSystem.getScissorStateForRenderTypeDraws();
                    if (scissorState.enabled()) {
                        renderPass.enableScissor(scissorState.x(), scissorState.y(), scissorState.width(), scissorState.height());
                    }

                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                    renderPass.setVertexBuffer(0, gpuBuffer);


                    renderPass.setIndexBuffer(gpuBuffer2, indexType);
                    renderPass.drawIndexed(0, 0, meshData.drawState().indexCount(), 1);
                }
            } catch (Throwable var17) {
                if (meshData != null) {
                    try {
                        var3.close();
                    } catch (Throwable var14) {
                        var17.addSuppressed(var14);
                    }
                }

                throw var17;
            }

            if (meshData != null) {
                meshData.close();
            }

            this.clearRenderState();
        }

        public @NotNull String toString() {
            String var10000 = this.name;
            return "RenderType[" + var10000 + ":" + String.valueOf(this.state) + "]";
        }
    }
}
