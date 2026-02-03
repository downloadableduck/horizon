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

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
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
    private static final RenderType SOLID;
    private static final RenderType CUTOUT_MIPPED;
    private static final RenderType CUTOUT;
    private static final RenderType TRANSLUCENT_MOVING_BLOCK;
    protected static final OutputStateShard PARTICLES_TARGET = new OutputStateShard("particles_target", () -> {
        RenderTarget renderTarget = Minecraft.getInstance().levelRenderer.getParticlesTarget();
        return renderTarget != null ? renderTarget : Minecraft.getInstance().getMainRenderTarget();
    });
    private static final Function<ResourceLocation, RenderType> ARMOR_CUTOUT_NO_CULL;
    private static final Function<ResourceLocation, RenderType> ARMOR_TRANSLUCENT;
    private static final Function<ResourceLocation, RenderType> ENTITY_SOLID;
    private static final Function<ResourceLocation, RenderType> ENTITY_SOLID_Z_OFFSET_FORWARD;
    private static final Function<ResourceLocation, RenderType> ENTITY_CUTOUT;
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_CUTOUT_NO_CULL;
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_CUTOUT_NO_CULL_Z_OFFSET;
    private static final Function<ResourceLocation, RenderType> ITEM_ENTITY_TRANSLUCENT_CULL;
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT;
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT_EMISSIVE;
    private static final Function<ResourceLocation, RenderType> ENTITY_SMOOTH_CUTOUT;
    private static final BiFunction<ResourceLocation, Boolean, RenderType> BEACON_BEAM;
    private static final Function<ResourceLocation, RenderType> ENTITY_DECAL;
    private static final Function<ResourceLocation, RenderType> ENTITY_NO_OUTLINE;
    private static final Function<ResourceLocation, RenderType> ENTITY_SHADOW;
    private static final Function<ResourceLocation, RenderType> DRAGON_EXPLOSION_ALPHA;
    private static final Function<ResourceLocation, RenderType> EYES;
    private static final RenderType LEASH;
    private static final RenderType WATER_MASK;
    private static final RenderType ARMOR_ENTITY_GLINT;
    private static final RenderType GLINT_TRANSLUCENT;
    private static final RenderType GLINT;
    private static final RenderType ENTITY_GLINT;
    private static final Function<ResourceLocation, RenderType> CRUMBLING;
    private static final Function<ResourceLocation, RenderType> TEXT;
    private static final RenderType TEXT_BACKGROUND;
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY;
    private static final Function<ResourceLocation, RenderType> TEXT_POLYGON_OFFSET;
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY_POLYGON_OFFSET;
    private static final Function<ResourceLocation, RenderType> TEXT_SEE_THROUGH;
    private static final RenderType TEXT_BACKGROUND_SEE_THROUGH;
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY_SEE_THROUGH;
    private static final RenderType LIGHTNING;
    private static final RenderType DRAGON_RAYS;
    private static final RenderType DRAGON_RAYS_DEPTH;
    private static final RenderType TRIPWIRE;
    private static final RenderType END_PORTAL;
    private static final RenderType END_GATEWAY;
    public static final CompositeRenderType LINES;
    public static final CompositeRenderType SECONDARY_BLOCK_OUTLINE;
    public static final CompositeRenderType LINE_STRIP;
    private static final Function<Double, CompositeRenderType> DEBUG_LINE_STRIP;
    private static final CompositeRenderType DEBUG_FILLED_BOX;
    private static final CompositeRenderType DEBUG_QUADS;
    private static final CompositeRenderType DEBUG_TRIANGLE_FAN;
    private static final CompositeRenderType DEBUG_STRUCTURE_QUADS;
    private static final CompositeRenderType DEBUG_SECTION_QUADS;
    private static final Function<ResourceLocation, RenderType> OPAQUE_PARTICLE;
    private static final Function<ResourceLocation, RenderType> TRANSLUCENT_PARTICLE;
    private static final Function<ResourceLocation, RenderType> WEATHER_DEPTH_WRITE;
    private static final Function<ResourceLocation, RenderType> WEATHER_NO_DEPTH_WRITE;
    private static final RenderType SUNRISE_SUNSET;
    private static final Function<ResourceLocation, RenderType> CELESTIAL;
    private static final Function<ResourceLocation, RenderType> BLOCK_SCREEN_EFFECT;
    private static final Function<ResourceLocation, RenderType> FIRE_SCREEN_EFFECT;
    private final int bufferSize;
    private final boolean affectsCrumbling;
    private final boolean sortOnUpload;

    private static Function<ResourceLocation, RenderType> createWeather(RenderPipeline renderPipeline) {
        return Util.memoize((resourceLocation) -> create("weather", 1536, false, false, renderPipeline, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setOutputState(WEATHER_TARGET).setLightmapState(LIGHTMAP).createCompositeState(false)));
    }

    public static RenderType celestial(ResourceLocation resourceLocation) {
        return CELESTIAL.apply(resourceLocation);
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
        SOLID = create("solid", 1536, true, false, RenderPipelines.SOLID, RenderType.CompositeState.builder().setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).createCompositeState(true));
        CUTOUT_MIPPED = create("cutout_mipped", 1536, true, false, RenderPipelines.CUTOUT_MIPPED, RenderType.CompositeState.builder().setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).createCompositeState(true));
        CUTOUT = create("cutout", 1536, true, false, RenderPipelines.CUTOUT, RenderType.CompositeState.builder().setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET).createCompositeState(true));
        TRANSLUCENT_MOVING_BLOCK = create("translucent_moving_block", 786432, false, true, RenderPipelines.TRANSLUCENT_MOVING_BLOCK, RenderType.CompositeState.builder().setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(true));
        ARMOR_CUTOUT_NO_CULL = Util.memoize((resourceLocation) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(true);
            return create("armor_cutout_no_cull", 1536, true, false, RenderPipelines.ARMOR_CUTOUT_NO_CULL, compositeState);
        });
        ARMOR_TRANSLUCENT = Util.memoize((resourceLocation) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(true);
            return create("armor_translucent", 1536, true, true, RenderPipelines.ARMOR_TRANSLUCENT, compositeState);
        });
        ENTITY_SOLID = Util.memoize((resourceLocation) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
            return create("entity_solid", 1536, true, false, RenderPipelines.ENTITY_SOLID, compositeState);
        });
        ENTITY_SOLID_Z_OFFSET_FORWARD = Util.memoize((resourceLocation) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING_FORWARD).createCompositeState(true);
            return create("entity_solid_z_offset_forward", 1536, true, false, RenderPipelines.ENTITY_SOLID_Z_OFFSET_FORWARD, compositeState);
        });
        ENTITY_CUTOUT = Util.memoize((resourceLocation) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
            return create("entity_cutout", 1536, true, false, RenderPipelines.ENTITY_CUTOUT, compositeState);
        });
        ENTITY_CUTOUT_NO_CULL = Util.memoize((resourceLocation, boolean_) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(boolean_);
            return create("entity_cutout_no_cull", 1536, true, false, RenderPipelines.ENTITY_CUTOUT_NO_CULL, compositeState);
        });
        ENTITY_CUTOUT_NO_CULL_Z_OFFSET = Util.memoize((resourceLocation, boolean_) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(boolean_);
            return create("entity_cutout_no_cull_z_offset", 1536, true, false, RenderPipelines.ENTITY_CUTOUT_NO_CULL_Z_OFFSET, compositeState);
        });
        ITEM_ENTITY_TRANSLUCENT_CULL = Util.memoize((resourceLocation) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setOutputState(ITEM_ENTITY_TARGET).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
            return create("item_entity_translucent_cull", 1536, true, true, RenderPipelines.ITEM_ENTITY_TRANSLUCENT_CULL, compositeState);
        });
        ENTITY_TRANSLUCENT = Util.memoize((resourceLocation, boolean_) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(boolean_);
            return create("entity_translucent", 1536, true, true, RenderPipelines.ENTITY_TRANSLUCENT, compositeState);
        });
        ENTITY_TRANSLUCENT_EMISSIVE = Util.memoize((resourceLocation, boolean_) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setOverlayState(OVERLAY).createCompositeState(boolean_);
            return create("entity_translucent_emissive", 1536, true, true, RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE, compositeState);
        });
        ENTITY_SMOOTH_CUTOUT = Util.memoize((resourceLocation) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
            return create("entity_smooth_cutout", 1536, RenderPipelines.ENTITY_SMOOTH_CUTOUT, compositeState);
        });
        BEACON_BEAM = Util.memoize((resourceLocation, boolean_) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).createCompositeState(false);
            return create("beacon_beam", 1536, false, true, boolean_ ? RenderPipelines.BEACON_BEAM_TRANSLUCENT : RenderPipelines.BEACON_BEAM_OPAQUE, compositeState);
        });
        ENTITY_DECAL = Util.memoize((resourceLocation) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false);
            return create("entity_decal", 1536, RenderPipelines.ENTITY_DECAL, compositeState);
        });
        ENTITY_NO_OUTLINE = Util.memoize((resourceLocation) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false);
            return create("entity_no_outline", 1536, false, true, RenderPipelines.ENTITY_NO_OUTLINE, compositeState);
        });
        ENTITY_SHADOW = Util.memoize((resourceLocation) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(false);
            return create("entity_shadow", 1536, false, false, RenderPipelines.ENTITY_SHADOW, compositeState);
        });
        DRAGON_EXPLOSION_ALPHA = Util.memoize((resourceLocation) -> {
            CompositeState compositeState = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).createCompositeState(true);
            return create("entity_alpha", 1536, RenderPipelines.DRAGON_EXPLOSION_ALPHA, compositeState);
        });
        EYES = Util.memoize((resourceLocation) -> {
            RenderStateShard.TextureStateShard textureStateShard = new RenderStateShard.TextureStateShard(resourceLocation, false);
            return create("eyes", 1536, false, true, RenderPipelines.EYES, RenderType.CompositeState.builder().setTextureState(textureStateShard).createCompositeState(false));
        });
        LEASH = create("leash", 1536, RenderPipelines.LEASH, RenderType.CompositeState.builder().setTextureState(NO_TEXTURE).setLightmapState(LIGHTMAP).createCompositeState(false));
        WATER_MASK = create("water_mask", 1536, RenderPipelines.WATER_MASK, RenderType.CompositeState.builder().setTextureState(NO_TEXTURE).createCompositeState(false));
        ARMOR_ENTITY_GLINT = create("armor_entity_glint", 1536, RenderPipelines.GLINT, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ARMOR, false)).setTexturingState(ARMOR_ENTITY_GLINT_TEXTURING).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(false));
        GLINT_TRANSLUCENT = create("glint_translucent", 1536, RenderPipelines.GLINT, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ITEM, false)).setTexturingState(GLINT_TEXTURING).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(false));
        GLINT = create("glint", 1536, RenderPipelines.GLINT, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ITEM, false)).setTexturingState(GLINT_TEXTURING).createCompositeState(false));
        ENTITY_GLINT = create("entity_glint", 1536, RenderPipelines.GLINT, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ITEM, false)).setTexturingState(ENTITY_GLINT_TEXTURING).createCompositeState(false));
        CRUMBLING = Util.memoize((resourceLocation) -> {
            RenderStateShard.TextureStateShard textureStateShard = new RenderStateShard.TextureStateShard(resourceLocation, false);
            return create("crumbling", 1536, false, true, RenderPipelines.CRUMBLING, RenderType.CompositeState.builder().setTextureState(textureStateShard).createCompositeState(false));
        });
        TEXT = Util.memoize((resourceLocation) -> create("text", 786432, false, false, RenderPipelines.TEXT, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).createCompositeState(false)));
        TEXT_BACKGROUND = create("text_background", 1536, false, true, RenderPipelines.TEXT_BACKGROUND, RenderType.CompositeState.builder().setTextureState(NO_TEXTURE).setLightmapState(LIGHTMAP).createCompositeState(false));
        TEXT_INTENSITY = Util.memoize((resourceLocation) -> create("text_intensity", 786432, false, false, RenderPipelines.TEXT_INTENSITY, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).createCompositeState(false)));
        TEXT_POLYGON_OFFSET = Util.memoize((resourceLocation) -> create("text_polygon_offset", 1536, false, true, RenderPipelines.TEXT_POLYGON_OFFSET, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).createCompositeState(false)));
        TEXT_INTENSITY_POLYGON_OFFSET = Util.memoize((resourceLocation) -> create("text_intensity_polygon_offset", 1536, false, true, RenderPipelines.TEXT_INTENSITY, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).createCompositeState(false)));
        TEXT_SEE_THROUGH = Util.memoize((resourceLocation) -> create("text_see_through", 1536, false, false, RenderPipelines.TEXT_SEE_THROUGH, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).createCompositeState(false)));
        TEXT_BACKGROUND_SEE_THROUGH = create("text_background_see_through", 1536, false, true, RenderPipelines.TEXT_BACKGROUND_SEE_THROUGH, RenderType.CompositeState.builder().setTextureState(NO_TEXTURE).setLightmapState(LIGHTMAP).createCompositeState(false));
        TEXT_INTENSITY_SEE_THROUGH = Util.memoize((resourceLocation) -> create("text_intensity_see_through", 1536, false, true, RenderPipelines.TEXT_INTENSITY_SEE_THROUGH, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).createCompositeState(false)));
        LIGHTNING = create("lightning", 1536, false, true, RenderPipelines.LIGHTNING, RenderType.CompositeState.builder().setOutputState(WEATHER_TARGET).createCompositeState(false));
        DRAGON_RAYS = create("dragon_rays", 1536, false, false, RenderPipelines.DRAGON_RAYS, RenderType.CompositeState.builder().createCompositeState(false));
        DRAGON_RAYS_DEPTH = create("dragon_rays_depth", 1536, false, false, RenderPipelines.DRAGON_RAYS_DEPTH, RenderType.CompositeState.builder().createCompositeState(false));
        TRIPWIRE = create("tripwire", 1536, true, true, RenderPipelines.TRIPWIRE, RenderType.CompositeState.builder().setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).setOutputState(WEATHER_TARGET).createCompositeState(true));
        END_PORTAL = create("end_portal", 1536, false, false, RenderPipelines.END_PORTAL, RenderType.CompositeState.builder().setTextureState(MultiTextureStateShard.builder().add(TheEndPortalRenderer.END_SKY_LOCATION, false).add(TheEndPortalRenderer.END_PORTAL_LOCATION, false).build()).createCompositeState(false));
        END_GATEWAY = create("end_gateway", 1536, false, false, RenderPipelines.END_GATEWAY, RenderType.CompositeState.builder().setTextureState(MultiTextureStateShard.builder().add(TheEndPortalRenderer.END_SKY_LOCATION, false).add(TheEndPortalRenderer.END_PORTAL_LOCATION, false).build()).createCompositeState(false));
        LINES = create("lines", 1536, RenderPipelines.LINES, RenderType.CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).setLayeringState(VIEW_OFFSET_Z_LAYERING).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(false));
        SECONDARY_BLOCK_OUTLINE = create("secondary_block_outline", 1536, RenderPipelines.SECONDARY_BLOCK_OUTLINE, RenderType.CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of((double)7.0F))).setLayeringState(VIEW_OFFSET_Z_LAYERING).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(false));
        LINE_STRIP = create("line_strip", 1536, RenderPipelines.LINE_STRIP, RenderType.CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).setLayeringState(VIEW_OFFSET_Z_LAYERING).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(false));
        DEBUG_LINE_STRIP = Util.memoize((double_) -> create("debug_line_strip", 1536, RenderPipelines.DEBUG_LINE_STRIP, RenderType.CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(double_))).createCompositeState(false)));
        DEBUG_FILLED_BOX = create("debug_filled_box", 1536, false, true, RenderPipelines.DEBUG_FILLED_BOX, RenderType.CompositeState.builder().setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(false));
        DEBUG_QUADS = create("debug_quads", 1536, false, true, RenderPipelines.DEBUG_QUADS, RenderType.CompositeState.builder().createCompositeState(false));
        DEBUG_TRIANGLE_FAN = create("debug_triangle_fan", 1536, false, true, RenderPipelines.DEBUG_TRIANGLE_FAN, RenderType.CompositeState.builder().createCompositeState(false));
        DEBUG_STRUCTURE_QUADS = create("debug_structure_quads", 1536, false, true, RenderPipelines.DEBUG_STRUCTURE_QUADS, RenderType.CompositeState.builder().createCompositeState(false));
        DEBUG_SECTION_QUADS = create("debug_section_quads", 1536, false, true, RenderPipelines.DEBUG_SECTION_QUADS, RenderType.CompositeState.builder().setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(false));
        OPAQUE_PARTICLE = Util.memoize((resourceLocation) -> create("opaque_particle", 1536, false, false, RenderPipelines.OPAQUE_PARTICLE, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).createCompositeState(false)));
        TRANSLUCENT_PARTICLE = Util.memoize((resourceLocation) -> create("translucent_particle", 1536, false, false, RenderPipelines.TRANSLUCENT_PARTICLE, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setOutputState(PARTICLES_TARGET).setLightmapState(LIGHTMAP).createCompositeState(false)));
        WEATHER_DEPTH_WRITE = createWeather(RenderPipelines.WEATHER_DEPTH_WRITE);
        WEATHER_NO_DEPTH_WRITE = createWeather(RenderPipelines.WEATHER_NO_DEPTH_WRITE);
        SUNRISE_SUNSET = create("sunrise_sunset", 1536, false, false, RenderPipelines.SUNRISE_SUNSET, RenderType.CompositeState.builder().createCompositeState(false));
        CELESTIAL = Util.memoize((resourceLocation) -> create("celestial", 1536, false, false, RenderPipelines.CELESTIAL, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).createCompositeState(false)));
        BLOCK_SCREEN_EFFECT = Util.memoize((resourceLocation) -> create("block_screen_effect", 1536, false, false, RenderPipelines.BLOCK_SCREEN_EFFECT, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).createCompositeState(false)));
        FIRE_SCREEN_EFFECT = Util.memoize((resourceLocation) -> create("fire_screen_effect", 1536, false, false, RenderPipelines.FIRE_SCREEN_EFFECT, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).createCompositeState(false)));
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
        static final BiFunction<ResourceLocation, Boolean, RenderType> OUTLINE = Util.memoize((resourceLocation, boolean_) -> RenderType.create("outline", 1536, boolean_ ? RenderPipelines.OUTLINE_CULL : RenderPipelines.OUTLINE_NO_CULL, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setOutputState(OUTLINE_TARGET).createCompositeState(RenderType.OutlineProperty.IS_OUTLINE)));
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
            GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), getModelOffset(), RenderSystem.getTextureMatrix(), RenderSystem.getShaderLineWidth());
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

                    for(int i = 0; i < 12; ++i) {
                        GpuTextureView gpuTextureView3 = RenderSystem.getShaderTexture(i);
                        if (gpuTextureView3 != null) {
                            renderPass.bindSampler("Sampler" + i, gpuTextureView3);
                        }
                    }

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
