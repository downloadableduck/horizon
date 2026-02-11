package com.jeff.horizon.skybox;

import com.jeff.horizon.HorizonClient;
import com.jeff.horizon.api.skyboxes.HorizonSkybox;
import com.jeff.horizon.api.skyboxes.Skybox;
import com.jeff.horizon.components.Conditions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Objects;

public class DefaultHandler {
    public static final Identifier DEFAULT = Identifier.tryBuild(HorizonClient.MOD_ID, "default");

    /**
     * Stores DimensionType Conditions instance concatenated from all Conditions instances in skyboxes in SkyboxManager.
     * Includes skyboxes from both skyboxMap and permanentSkyboxMap.
     * Default skyboxes check against the inverse of concatConditions.
     */
    private static Conditions concatConditions = Conditions.of();

    /**
     * Concatenates conditions from DimensionType skybox to concatConditions.
     * Should be called whenever DimensionType skybox is added to SkyboxManager.
     *
     * @param skybox the skybox containing the conditions to be added to concatConditions.
     */
    public static void addConditions(Skybox skybox) {
        if (skybox instanceof HorizonSkybox horizonSkybox) {
            addConditions(horizonSkybox.getConditions());
        }
    }

    public static void addConditions(Conditions conditions) {
        for (Identifier location : conditions.getBiomes().entries()) {
            if (!concatConditions.getBiomes().entries().contains(location)) {
                concatConditions.getBiomes().entries().add(location);
            }
        }

        for (Identifier Identifier : conditions.getWorlds().entries()) {
            if (!concatConditions.getWorlds().entries().contains(Identifier)) {
                concatConditions.getWorlds().entries().add(Identifier);
            }
        }

        for (Identifier resource : conditions.getDimensions().entries()) {
            if (!concatConditions.getDimensions().entries().contains(resource)) {
                concatConditions.getDimensions().entries().add(resource);
            }
        }
    }

    /**
     * Clears all conditions from concatConditions.
     */
    private static void clearConditions() {
        concatConditions = Conditions.of();
    }

    /**
     * Clears all conditions from concatConditions, then readds all conditions contained in the skyboxes in exceptions.
     */
    public static void clearConditionsExcept(Collection<Skybox> exceptions) {
        clearConditions();
        exceptions.forEach(DefaultHandler::addConditions);
    }

    /**
     * @return true if the current biome is not listed as DimensionType condition in any loaded skybox.
     */
    public static boolean checkFallbackBiomes() {
        Minecraft client = Minecraft.getInstance();
        Objects.requireNonNull(client.level);
        Objects.requireNonNull(client.player);
        return !concatConditions.getBiomes().entries().contains(client.level.registryAccess().lookupOrThrow(Registries.BIOME).getKey(client.level.getBiome(client.player.blockPosition()).value()));
    }

    /**
     * @return true if the current world is not listed as DimensionType condition in any loaded skybox.
     */
    public static boolean checkFallbackWorlds() {
        Minecraft client = Minecraft.getInstance();
        Objects.requireNonNull(client.level);
        return !concatConditions.getWorlds().entries().contains(client.level.dimensionType());
        //this used to be client.level.dimensionType.effectsLocation()
    }

    /**
     * @return true if the current dimension is not listed as DimensionType condition in any loaded skybox.
     */
    public static boolean checkFallbackDimensions() {
        Minecraft client = Minecraft.getInstance();
        Objects.requireNonNull(client.level);
        return !concatConditions.getDimensions().entries().contains(client.level.dimension().identifier());
    }
}
