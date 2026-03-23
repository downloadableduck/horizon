package com.jeff.horizon.api.skyboxes;

import com.jeff.horizon.components.Conditions;
import com.jeff.horizon.components.Properties;
import net.minecraft.client.multiplayer.ClientLevel;

public interface HorizonSkybox extends Skybox {
    float getAlpha();

    void updateAlpha(ClientLevel level);

    Properties getProperties();

    Conditions getConditions();
}

