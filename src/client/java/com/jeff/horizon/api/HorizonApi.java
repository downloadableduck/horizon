package com.jeff.horizon.api;

import com.google.gson.JsonObject;
import com.jeff.horizon.SkyboxManager;
import com.jeff.horizon.api.skyboxes.Skybox;
import net.minecraft.resources.Identifier;

import java.util.List;

public interface HorizonApi {
    /**
     * @since API v0.0
     */
    static HorizonApi getInstance() {
        return SkyboxManager.getInstance();
    }

    /**
     * Gets the version of this API, This is incremented when changes are implemented
     * without breaking API. Mods can use this to check if given API functionality
     * is available on the current version of installed Nuit.
     *
     * @return The current version of the API
     */
    static int getApiVersion() {
        return 1;
    }

    /**
     * Allows mods to add new skyboxes at runtime.
     *
     * @param Identifier Identifier for skybox.
     * @param skybox           Skybox implementation.
     */
    void addSkybox(Identifier Identifier, Skybox skybox);

    /**
     * Allows mods to add new skyboxes with a {@link JsonObject} at runtime.
     * This method applies {@link SkyboxManager#parseSkyboxJson(Identifier, JsonObject)}
     * serialization and adds the skybox with {@link #addSkybox(Identifier, Skybox)}
     *
     * @param Identifier Identifier for skybox.
     * @param jsonObject       Json Object.
     */
    void addSkybox(Identifier Identifier, JsonObject jsonObject);

    /**
     * Allows mods to add new permanent skyboxes at runtime.
     *
     * @param Identifier Identifier for skybox.
     * @param skybox           Skybox implementation.
     */
    void addPermanentSkybox(Identifier Identifier, Skybox skybox);

    /**
     * Clears all non-permanent skyboxes.
     */
    void clearSkyboxes();

    /**
     * Gets the current skybox that is being rendered.
     *
     * @return Current skybox being render, returns null of nothing is being rendered.
     */
    Skybox getCurrentSkybox();

    /**
     * Gets a list of active skyboxes.
     *
     * @return Current list of active skyboxes.
     */
    List<Skybox> getActiveSkyboxes();
}
