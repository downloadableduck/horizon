package com.jeff.horizon.skybox.decorations;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public abstract class RenderStateShard {
    public static final double MAX_ENCHANTMENT_GLINT_SPEED_MILLIS = (double)8.0F;
    protected final String name;
    private final Runnable setupState;
    private final Runnable clearState;
    public static final TextureStateShard BLOCK_SHEET_MIPPED;
    public static final TextureStateShard BLOCK_SHEET;
    public static final EmptyTextureStateShard NO_TEXTURE;
    public static final TexturingStateShard DEFAULT_TEXTURING;
    public static final LightmapStateShard LIGHTMAP;
    public static final LightmapStateShard NO_LIGHTMAP;
    public static final OverlayStateShard OVERLAY;
    public static final OverlayStateShard NO_OVERLAY;
    public static final LayeringStateShard NO_LAYERING;
    public static final LayeringStateShard VIEW_OFFSET_Z_LAYERING;
    public static final LayeringStateShard VIEW_OFFSET_Z_LAYERING_FORWARD;
    public static final OutputStateShard MAIN_TARGET;
    public static final OutputStateShard OUTLINE_TARGET;
    public static final OutputStateShard WEATHER_TARGET;
    public static final OutputStateShard ITEM_ENTITY_TARGET;
    public static final LineStateShard DEFAULT_LINE;

    public RenderStateShard(String name, Runnable setupState, Runnable clearState) {
        this.name = name;
        this.setupState = setupState;
        this.clearState = clearState;
    }

    public void setupRenderState() {
        this.setupState.run();
    }

    public void clearRenderState() {
        this.clearState.run();
    }

    public String toString() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }

    static {
        BLOCK_SHEET_MIPPED = new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, true);
        BLOCK_SHEET = new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false);
        NO_TEXTURE = new EmptyTextureStateShard();
        DEFAULT_TEXTURING = new TexturingStateShard("default_texturing", () -> {
        }, () -> {
        });
        LIGHTMAP = new LightmapStateShard(true);
        NO_LIGHTMAP = new LightmapStateShard(false);
        OVERLAY = new OverlayStateShard(true);
        NO_OVERLAY = new OverlayStateShard(false);
        NO_LAYERING = new LayeringStateShard("no_layering", () -> {
        }, () -> {
        });
        VIEW_OFFSET_Z_LAYERING = new LayeringStateShard("view_offset_z_layering", () -> {
            Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
            matrix4fstack.pushMatrix();
            RenderSystem.getProjectionType().applyLayeringTransform(matrix4fstack, 1.0F);
        }, () -> {
            Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
            matrix4fstack.popMatrix();
        });
        VIEW_OFFSET_Z_LAYERING_FORWARD = new LayeringStateShard("view_offset_z_layering_forward", () -> {
            Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
            matrix4fstack.pushMatrix();
            RenderSystem.getProjectionType().applyLayeringTransform(matrix4fstack, -1.0F);
        }, () -> {
            Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
            matrix4fstack.popMatrix();
        });
        MAIN_TARGET = new OutputStateShard("main_target", () -> Minecraft.getInstance().getMainRenderTarget());
        OUTLINE_TARGET = new OutputStateShard("outline_target", () -> {
            RenderTarget rendertarget = Minecraft.getInstance().levelRenderer.entityOutlineTarget();
            return rendertarget != null ? rendertarget : Minecraft.getInstance().getMainRenderTarget();
        });
        WEATHER_TARGET = new OutputStateShard("weather_target", () -> {
            RenderTarget rendertarget = Minecraft.getInstance().levelRenderer.getWeatherTarget();
            return rendertarget != null ? rendertarget : Minecraft.getInstance().getMainRenderTarget();
        });
        ITEM_ENTITY_TARGET = new OutputStateShard("item_entity_target", () -> {
            RenderTarget rendertarget = Minecraft.getInstance().levelRenderer.getItemEntityTarget();
            return rendertarget != null ? rendertarget : Minecraft.getInstance().getMainRenderTarget();
        });
        DEFAULT_LINE = new LineStateShard(OptionalDouble.of((double)1.0F));
    }

    public static class BooleanStateShard extends RenderStateShard {
        private final boolean enabled;

        public BooleanStateShard(String name, Runnable setupState, Runnable clearState, boolean enabled) {
            super(name, setupState, clearState);
            this.enabled = enabled;
        }

        public String toString() {
            return this.name + "[" + this.enabled + "]";
        }
    }

    public static class EmptyTextureStateShard extends RenderStateShard {
        public EmptyTextureStateShard(Runnable setupState, Runnable clearState) {
            super("texture", setupState, clearState);
        }

        EmptyTextureStateShard() {
            super("texture", () -> {
            }, () -> {
            });
        }

        protected Optional<Identifier> cutoutTexture() {
            return Optional.empty();
        }
    }

    public static class LayeringStateShard extends RenderStateShard {
        public LayeringStateShard(String p_110267_, Runnable p_110268_, Runnable p_110269_) {
            super(p_110267_, p_110268_, p_110269_);
        }
    }

    public static class LightmapStateShard extends BooleanStateShard {
        public LightmapStateShard(boolean useLightmap) {
            super("lightmap", () -> {
                if (useLightmap) {
                }

            }, () -> {
                if (useLightmap) {
                }

            }, useLightmap);
        }
    }

    public static class LineStateShard extends RenderStateShard {
        private final OptionalDouble width;

        public LineStateShard(OptionalDouble width) {
            super("line_width", () -> {
                if (!Objects.equals(width, OptionalDouble.of((double)1.0F))) {
                    if (width.isPresent()) {
                    } else {
                    }
                }

            }, () -> {
                if (!Objects.equals(width, OptionalDouble.of((double)1.0F))) {
                }

            });
            this.width = width;
        }

        public String toString() {
            String var10000 = this.name;
            return var10000 + "[" + String.valueOf(this.width.isPresent() ? this.width.getAsDouble() : "window_scale") + "]";
        }
    }

    public static class MultiTextureStateShard extends EmptyTextureStateShard {
        private final Optional<Identifier> cutoutTexture;

        MultiTextureStateShard(List<Entry> entries) {
            super(() -> {
                for(int i = 0; i < entries.size(); ++i) {
                    Entry renderstateshard$multitexturestateshard$entry = (Entry)entries.get(i);
                    TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
                    AbstractTexture abstracttexture = texturemanager.getTexture(renderstateshard$multitexturestateshard$entry.id);
                }

            }, () -> {
            });
            this.cutoutTexture = entries.isEmpty() ? Optional.empty() : Optional.of(((Entry)entries.getFirst()).id);
        }

        protected Optional<Identifier> cutoutTexture() {
            return this.cutoutTexture;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private final ImmutableList.Builder<Entry> builder = new ImmutableList.Builder();

            public Builder add(Identifier id, boolean mipmap) {
                this.builder.add(new Entry(id, mipmap));
                return this;
            }

            public MultiTextureStateShard build() {
                return new MultiTextureStateShard(this.builder.build());
            }
        }

        public static record Entry(Identifier id, boolean mipmap) {
        }
    }

    public static class OutputStateShard extends RenderStateShard {
        private final Supplier<RenderTarget> renderTargetSupplier;

        public OutputStateShard(String name, Supplier<RenderTarget> renderTargetSupplier) {
            super(name, () -> {
            }, () -> {
            });
            this.renderTargetSupplier = renderTargetSupplier;
        }

        public RenderTarget getRenderTarget() {
            return (RenderTarget)this.renderTargetSupplier.get();
        }
    }

    public static class OverlayStateShard extends BooleanStateShard {
        public OverlayStateShard(boolean useOverlay) {
            super("overlay", () -> {
                if (useOverlay) {
                    Minecraft.getInstance().gameRenderer.overlayTexture().getTextureView();
                }

            }, () -> {
                if (useOverlay) {
                    Minecraft.getInstance().gameRenderer.overlayTexture().getTextureView();
                }

            }, useOverlay);
        }
    }

    public static class TextureStateShard extends EmptyTextureStateShard {
        private final Optional<Identifier> texture;
        protected boolean mipmap;

        public TextureStateShard(Identifier texture, boolean mipmap) {
            super(() -> {
                TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
                AbstractTexture abstracttexture = texturemanager.getTexture(texture);
            }, () -> {
            });
            this.texture = Optional.of(texture);
            this.mipmap = mipmap;
        }

        public String toString() {
            String var10000 = this.name;
            return var10000 + "[" + String.valueOf(this.texture) + "(mipmap=" + this.mipmap + ")]";
        }

        protected Optional<Identifier> cutoutTexture() {
            return this.texture;
        }
    }

    public static class TexturingStateShard extends RenderStateShard {
        public TexturingStateShard(String p_110349_, Runnable p_110350_, Runnable p_110351_) {
            super(p_110349_, p_110350_, p_110351_);
        }
    }
}
