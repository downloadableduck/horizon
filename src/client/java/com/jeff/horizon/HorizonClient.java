package com.jeff.horizon;

import com.jeff.horizon.api.HorizonPlatformHelper;
import com.jeff.horizon.config.HorizonConfig;
import com.jeff.horizon.resource.SkyboxResourceListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HorizonClient {

    public static final String MOD_ID = "nuit";
    private static final SkyboxResourceListener skyboxResourceListener = new SkyboxResourceListener();
    private static Logger LOGGER;
    private static HorizonConfig CONFIG;

    public static void init() {
        SkyboxManager.getInstance().setEnabled(config().generalSettings.enable);
    }

    public static Logger getLogger() {
        if (LOGGER == null) {
            LOGGER = LogManager.getLogger("Nuit");
        }

        return LOGGER;
    }

    public static HorizonConfig config() {
        if (CONFIG == null) {
            CONFIG = loadConfig();
        }

        return CONFIG;
    }

    public static SkyboxResourceListener skyboxResourceListener() {
        return skyboxResourceListener;
    }

    private static HorizonConfig loadConfig() {
        return HorizonConfig.load(HorizonPlatformHelper.INSTANCE.getConfigDir().resolve("nuit-config.json").toFile());
    }
}
