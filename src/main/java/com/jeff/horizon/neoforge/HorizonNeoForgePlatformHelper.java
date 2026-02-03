package com.jeff.horizon.neoforge;

import com.jeff.horizon.api.HorizonPlatformHelper;
import com.jeff.horizon.api.skyboxes.Skybox;
import com.jeff.horizon.skybox.SkyboxType;
import net.minecraft.core.Registry;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class HorizonNeoForgePlatformHelper implements HorizonPlatformHelper {
    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Registry<SkyboxType<? extends Skybox>> getSkyboxTypeRegistry() {
        return HorizonNeoForge.REGISTRY;
    }
}
