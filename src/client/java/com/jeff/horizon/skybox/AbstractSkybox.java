package com.jeff.horizon.skybox;

import com.jeff.horizon.SkyboxManager;
import com.jeff.horizon.api.skyboxes.Skybox;
import com.jeff.horizon.skybox.decorations.DecorationBox;
import com.mojang.blaze3d.systems.TimerQuery;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import com.jeff.horizon.HorizonClient;
import com.jeff.horizon.api.skyboxes.HorizonSkybox;
import com.jeff.horizon.components.Conditions;
import com.jeff.horizon.components.Properties;
import com.jeff.horizon.components.Weather;
import com.jeff.horizon.util.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.properties.numeric.Time;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Tuple;
import net.minecraft.util.datafix.fixes.DayTimeToClockFix;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import net.minecraft.world.timeline.Timeline;

import java.util.Map;
import java.util.Objects;

/**
 * All classes that implement {@link AbstractSkybox} should
 * have DimensionType default constructor as it is required when checking
 * the type of the skybox.
 */
public abstract class AbstractSkybox implements HorizonSkybox {
    /**
     * Why don't we use the fade's keyframes map directly?
     * Because findClosestKeyframes has DimensionType O(n) time complexity operation. Adding to that will just make it worse.
     */
    private final Map<Long, Float> cachedKeyFrames = new Long2FloatOpenHashMap();
    /**
     * The current alpha for the skybox. Expects all skyboxes extending this to accommodate this.
     * This variable is responsible for fading in/out skyboxes.
     */
    public transient float alpha ;
    protected Properties properties = Properties.of();
    protected Conditions conditions = Conditions.of();
    protected boolean unexpectedConditionTransition = false;
    protected long lastTime = -2;
    protected float conditionAlpha = 0f;

    protected AbstractSkybox() {
    }

    protected AbstractSkybox(Properties properties, Conditions conditions) {
        this.properties = properties;
        this.conditions = conditions;
    }

    @Override
    public void tick(ClientLevel level) {
        this.updateAlpha(level);
    }

    /**
     * Calculates the alpha value for the current time and conditions and returns it.
     */
    @Override
    public void updateAlpha(ClientLevel level) {

        long currentTime = level.getOverworldClockTime() % this.properties.fade().duration();

        boolean condition = this.checkConditions();
        /**this is causing the skyboxes to render as completely black.*/
        float fadeAlpha = 0;
        if (this.properties.fade().keyFrames().isEmpty()) {
            this.conditionAlpha = Utils.calculateConditionAlphaValue(1f, 0f, this.conditionAlpha, condition ? this.properties.transitionInDuration() : this.properties.transitionOutDuration(), condition);
        } else {
            if (this.properties.fade().duration() <= HorizonClient.config().generalSettings.fadeCacheDuration) {
                fadeAlpha = this.cachedKeyFrames.computeIfAbsent(currentTime, time -> {
                    Tuple<Long, Long> keyFrames = Utils.findClosestKeyframes(this.properties.fade().keyFrames(), time).orElseThrow();
                    return Utils.calculateInterpolatedAlpha(
                            time,
                            this.properties.fade().duration(),
                            keyFrames.getA(),
                            keyFrames.getB(),
                            this.properties.fade().keyFrames().get(keyFrames.getA()),
                            this.properties.fade().keyFrames().get(keyFrames.getB())
                    );
                });
            } else {
                Tuple<Long, Long> keyFrames = Utils.findClosestKeyframes(this.properties.fade().keyFrames(), currentTime).orElseThrow();
                fadeAlpha = Utils.calculateInterpolatedAlpha(
                        currentTime,
                        this.properties.fade().duration(),
                        keyFrames.getA(),
                        keyFrames.getB(),
                        this.properties.fade().keyFrames().get(keyFrames.getA()),
                        this.properties.fade().keyFrames().get(keyFrames.getB())
                );
            }

            if ((this.lastTime == currentTime - 1 || this.lastTime == currentTime) && !this.unexpectedConditionTransition) { // Check if time is ticking or if time is same (doDaylightCycle gamerule)
                this.conditionAlpha = Utils.calculateConditionAlphaValue(1f, 0f, this.conditionAlpha, condition ? this.properties.transitionInDuration() : this.properties.transitionOutDuration(), condition);
            } else {
                this.unexpectedConditionTransition = true;
                this.conditionAlpha = Utils.calculateConditionAlphaValue(1f, 0f, this.conditionAlpha, HorizonClient.config().generalSettings.unexpectedTransitionDuration, condition);
                if (this.unexpectedConditionTransition && (this.conditionAlpha == 0f || this.conditionAlpha == 1f)) {
                    this.unexpectedConditionTransition = false;
                }
            }
        }

        this.alpha = fadeAlpha * this.conditionAlpha;
        this.lastTime = currentTime;
    }

    /**
     * @return Whether all conditions were met
     */
    protected boolean checkConditions() {
        return this.checkDimensions() && this.checkWorlds() && this.checkBiomes() && this.checkXRanges() &&
                this.checkYRanges() && this.checkZRanges() && this.checkWeather() && this.checkEffects() && this.checkProperties();
    }

    /**
     * @return Whether the current biomes and dimensions are valid for this skybox.
     */
    protected boolean checkBiomes() {
        Minecraft client = Minecraft.getInstance();
        Objects.requireNonNull(client.level);
        Objects.requireNonNull(client.player);
        return this.conditions.getBiomes().entries().isEmpty() || this.conditions.getBiomes().excludes() ^ (
                this.conditions.getBiomes().entries().contains(client.level.getBiome(client.player.blockPosition()).unwrapKey().orElseThrow().identifier()) ||
                        this.conditions.getBiomes().entries().contains(DefaultHandler.DEFAULT) && DefaultHandler.checkFallbackBiomes());
    }

    /**
     * @return Whether the current dimension identifier is valid for this skybox
     */
    protected boolean checkDimensions() {
        Minecraft client = Minecraft.getInstance();
        Objects.requireNonNull(client.level);
        return this.conditions.getDimensions().entries().isEmpty() || this.conditions.getDimensions().excludes() ^ (
                this.conditions.getDimensions().entries().contains(client.level.dimension().identifier()) ||
                        this.conditions.getDimensions().entries().contains(DefaultHandler.DEFAULT) && DefaultHandler.checkFallbackDimensions());
    }

    /**
     * @return Whether the current dimension sky effect is valid for this skybox
     * When it is off, the sun and moon render. When it is on, they do not, which is exactly what we don't want.
     */
    protected boolean checkWorlds() {
        Minecraft client = Minecraft.getInstance();
        Objects.requireNonNull(client.level);
        return this.conditions.getWorlds().entries().isEmpty() || this.conditions.getWorlds().excludes() ^ (
                this.conditions.getWorlds().entries().contains(client.level.dimension().identifier()) ||
                        this.conditions.getWorlds().entries().contains(DefaultHandler.DEFAULT) && DefaultHandler.checkFallbackWorlds());
    }

    /**
     * @return Check if an effect that should prevent skybox from showing
     */
    protected boolean checkEffects() {
        Minecraft client = Minecraft.getInstance();
        Objects.requireNonNull(client.level);

        Camera camera = client.gameRenderer.getMainCamera();
        if (this.conditions.getEffects().entries().isEmpty()) {
            return !(camera.entity() instanceof LivingEntity livingEntity) || (!livingEntity.hasEffect(MobEffects.BLINDNESS) && !livingEntity.hasEffect(MobEffects.DARKNESS));
        } else {
            if (camera.entity() instanceof LivingEntity livingEntity) {
                return (this.conditions.getEffects().excludes() ^ this.conditions.getEffects().entries().stream().noneMatch(resourceLocation -> client.level.registryAccess().lookupOrThrow(Registries.MOB_EFFECT).get(resourceLocation).isPresent() && livingEntity.hasEffect(client.level.registryAccess().lookupOrThrow(Registries.MOB_EFFECT).wrapAsHolder(client.level.registryAccess().lookupOrThrow(Registries.MOB_EFFECT).get(resourceLocation).get().value()))));
            }
        }

        return true;
    }

    /**
     * @return Whether vanilla checks are performed
     */
    protected boolean checkProperties() {
        Minecraft client = Minecraft.getInstance();
        Objects.requireNonNull(client.level);

        Camera camera = client.gameRenderer.getMainCamera();
        FogType cameraSubmersionType = camera.getFluidInCamera();

        boolean visibleUnderwater = this.properties.visibleUnderwater() || cameraSubmersionType != FogType.WATER;

        /*boolean thickFog = client.level.effects().isFoggyAt(
                Mth.floor(camera.getPosition().x()),
                Mth.floor(camera.getPosition().y())
        ) || client.gui.getBossOverlay().shouldCreateWorldFog();

        boolean showInDenseFog = !thickFog || this.properties.fog().isShowInDenseFog();*/

        boolean notInBlockedFog = cameraSubmersionType != FogType.POWDER_SNOW && cameraSubmersionType != FogType.LAVA;

        return visibleUnderwater/* && showInDenseFog*/ && notInBlockedFog;
    }

    /**
     * @return Whether the current x values are valid for this skybox.
     */
    protected boolean checkXRanges() {
        double playerX = Objects.requireNonNull(Minecraft.getInstance().player).getX();
        return Utils.checkRanges(playerX, this.conditions.getXRanges().entries(), this.conditions.getXRanges().excludes());
    }

    /**
     * @return Whether the current y values are valid for this skybox.
     */
    protected boolean checkYRanges() {
        double playerY = Objects.requireNonNull(Minecraft.getInstance().player).getY();
        return Utils.checkRanges(playerY, this.conditions.getYRanges().entries(), this.conditions.getYRanges().excludes());
    }

    /**
     * @return Whether the current z values are valid for this skybox.
     */
    protected boolean checkZRanges() {
        double playerZ = Objects.requireNonNull(Minecraft.getInstance().player).getZ();
        return Utils.checkRanges(playerZ, this.conditions.getZRanges().entries(), this.conditions.getZRanges().excludes());
    }

    /**
     * @return Whether the current weather is valid for this skybox.
     */
    protected boolean checkWeather() {
        ClientLevel world = Objects.requireNonNull(Minecraft.getInstance().level);
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        Biome.Precipitation precipitation = world.getBiome(player.blockPosition()).value().getPrecipitationAt(player.blockPosition(), world.getSeaLevel());
        if (this.conditions.getWeathers().entries().isEmpty()) {
            return true;
        }

        if ((this.conditions.getWeathers().excludes() ^ this.conditions.getWeathers().entries().contains(Weather.WORLD_PRECIPITATION)) && world.isRaining() && precipitation == Biome.Precipitation.NONE && !world.isThundering()) {
            return true;
        }

        if ((this.conditions.getWeathers().excludes() ^ this.conditions.getWeathers().entries().contains(Weather.WORLD_THUNDERSTORM)) && world.isRaining() && world.isThundering() && precipitation == Biome.Precipitation.NONE) {
            return true;
        }

        if ((this.conditions.getWeathers().excludes() ^ this.conditions.getWeathers().entries().contains(Weather.RAIN_IN_BIOME)) && world.isRaining() && precipitation == Biome.Precipitation.RAIN && !world.isThundering()) {
            return true;
        }

        if ((this.conditions.getWeathers().excludes() ^ this.conditions.getWeathers().entries().contains(Weather.THUNDER_IN_RAIN_BIOME)) && world.isRaining() && precipitation == Biome.Precipitation.RAIN && world.isThundering()) {
            return true;
        }

        if ((this.conditions.getWeathers().excludes() ^ this.conditions.getWeathers().entries().contains(Weather.SNOW_IN_BIOME)) && world.isRaining() && precipitation == Biome.Precipitation.SNOW && !world.isThundering()) {
            return true;
        }

        if ((this.conditions.getWeathers().excludes() ^ this.conditions.getWeathers().entries().contains(Weather.THUNDER_IN_SNOW_BIOME)) && world.isRaining() && precipitation == Biome.Precipitation.SNOW && world.isThundering()) {
            return true;
        }

        return (this.conditions.getWeathers().excludes() ^ this.conditions.getWeathers().entries().contains(Weather.NO_PRECIPITATION)) && !world.isRaining() && !world.isThundering();
    }

    @Override
    public Properties getProperties() {
        return this.properties;
    }

    @Override
    public Conditions getConditions() {
        return this.conditions;
    }

    @Override
    public float getAlpha() {
        return alpha;
    }

    @Override
    public int getLayer() {
        return this.properties.layer();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("[layer=%s, alpha=%s, dimension=%s, world=%s, biomes=%s, xranges=%s, yranges=%s, zranges=%s, weather=%s, effects=%s]", getProperties().layer(), getAlpha(), checkDimensions(), checkWorlds(), checkBiomes(), checkXRanges(), checkYRanges(), checkZRanges(), checkWeather(), checkEffects());
    }
}
