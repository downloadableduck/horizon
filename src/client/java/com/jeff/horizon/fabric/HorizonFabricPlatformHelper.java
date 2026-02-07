package com.jeff.horizon.fabric;

import com.jeff.horizon.api.HorizonPlatformHelper;
import com.jeff.horizon.api.skyboxes.Skybox;
import com.jeff.horizon.skybox.SkyboxType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;

import java.nio.file.Path;

public class HorizonFabricPlatformHelper implements HorizonPlatformHelper {
    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Registry<SkyboxType<? extends Skybox>> getSkyboxTypeRegistry() {
        return HorizonClientFabric.REGISTRY;
    }
}
