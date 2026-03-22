package com.jeff.horizon.api;

import com.jeff.horizon.api.skyboxes.Skybox;
import com.jeff.horizon.skybox.SkyboxType;
import com.jeff.horizon.util.Utils;
import net.minecraft.core.Registry;

import java.nio.file.Path;

public interface HorizonPlatformHelper {
    HorizonPlatformHelper INSTANCE = Utils.loadService(HorizonPlatformHelper.class);

    Path getConfigDir();

    Registry<SkyboxType<? extends Skybox>> getSkyboxTypeRegistry();
}
