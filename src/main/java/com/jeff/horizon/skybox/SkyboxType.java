package com.jeff.horizon.skybox;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import com.jeff.horizon.HorizonClient;
import com.jeff.horizon.api.skyboxes.Skybox;
import com.jeff.horizon.skybox.decorations.DecorationBox;
import com.jeff.horizon.skybox.textured.MultiTexturedSkybox;
import com.jeff.horizon.skybox.textured.SquareTexturedSkybox;
import com.jeff.horizon.skybox.vanilla.EndSkybox;
import com.jeff.horizon.skybox.vanilla.OverworldSkybox;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SkyboxType<T extends Skybox> {
    public static final Codec<ResourceLocation> SKYBOX_ID_CODEC;
    public static final ResourceKey<Registry<SkyboxType<? extends Skybox>>> SKYBOX_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.tryBuild(HorizonClient.MOD_ID, "skybox_type"));
    public static final SkyboxType<OverworldSkybox> OVERWORLD;
    public static final SkyboxType<EndSkybox> END;
    public static final SkyboxType<MonoColorSkybox> MONO_COLOR_SKYBOX;
    public static final SkyboxType<SquareTexturedSkybox> SQUARE_TEXTURED_SKYBOX;
    public static final SkyboxType<MultiTexturedSkybox> MULTI_TEXTURED_SKYBOX;
    public static final SkyboxType<DecorationBox> DECORATION_BOX;
    private static final Map<ResourceLocation, SkyboxType<?>> SKYBOX_TYPES = new ConcurrentHashMap<>();

    static {
        SKYBOX_ID_CODEC = Codec.STRING.xmap((s) -> {
            if (!s.contains(":")) {
                return ResourceLocation.tryBuild(HorizonClient.MOD_ID, s);
            }
            return ResourceLocation.tryParse(s);
        }, (id) -> {
            if (id.getNamespace().equals(HorizonClient.MOD_ID)) {
                return id.getPath();
            }
            return id.toString();
        });

        OVERWORLD = register(new SkyboxType<>("overworld", 1, OverworldSkybox.CODEC));
        END = register(new SkyboxType<>("end", 1, EndSkybox.CODEC));

        MONO_COLOR_SKYBOX = register(new SkyboxType<>("monocolor", 1, MonoColorSkybox.CODEC));
        SQUARE_TEXTURED_SKYBOX = register(new SkyboxType<>("square-textured", 1, SquareTexturedSkybox.CODEC));
        MULTI_TEXTURED_SKYBOX = register(new SkyboxType<>("multi-textured", 1, MultiTexturedSkybox.CODEC));

        DECORATION_BOX = register(new SkyboxType<>("decorations", 1, DecorationBox.CODEC));
    }

    private final BiMap<Integer, Codec<T>> codecBiMap;
    private final ResourceLocation name;

    private SkyboxType(String name, int schemaVersion, Codec<T> codec) {
        this(ResourceLocation.tryBuild(HorizonClient.MOD_ID, name), schemaVersion, codec);
    }

    public SkyboxType(ResourceLocation name, int schemaVersion, Codec<T> codec) {
        this(ImmutableBiMap.<Integer, Codec<T>>builder().put(schemaVersion, codec).build(), name);
    }

    public SkyboxType(BiMap<Integer, Codec<T>> codecBiMap, ResourceLocation name) {
        this.codecBiMap = codecBiMap;
        this.name = name;
    }

    public static <T extends Skybox> SkyboxType<T> register(SkyboxType<T> type) {
        if (SKYBOX_TYPES.putIfAbsent(type.name, type) != null) {
            throw new IllegalStateException("SkyboxType with name '" + type.name + "' already registered!");
        }
        return type;
    }

    public static void registerAll(Consumer<SkyboxType<?>> function) {
        SKYBOX_TYPES.values().forEach(function);
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public Codec<T> getCodec(int schemaVersion) {
        return Objects.requireNonNull(this.codecBiMap.get(schemaVersion), String.format("Unsupported schema version '%d' for skybox type %s", schemaVersion, this.name));
    }
}
