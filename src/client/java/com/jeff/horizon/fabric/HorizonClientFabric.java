package com.jeff.horizon.fabric;

import com.jeff.horizon.HorizonClient;
import com.jeff.horizon.config.HorizonConfig;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.serialization.Lifecycle;
import com.jeff.horizon.SkyboxManager;
import com.jeff.horizon.api.skyboxes.Skybox;
import com.jeff.horizon.screen.SkyboxDebugScreen;
import com.jeff.horizon.skybox.SkyboxType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.impl.client.rendering.hud.HudLayer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class HorizonClientFabric implements ClientModInitializer {
    public static final Registry<SkyboxType<? extends Skybox>> REGISTRY = FabricRegistryBuilder.from(new MappedRegistry<>(SkyboxType.SKYBOX_TYPE_REGISTRY_KEY, Lifecycle.stable())).buildAndRegister();

    @Override
    public void onInitializeClient() {
        HorizonClient.init();
        SkyboxType.registerAll(skyboxType -> Registry.register(REGISTRY, skyboxType.getName(), skyboxType));
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {

            @Override
            public @NotNull CompletableFuture<Void> reload(SharedState sharedState, Executor executor, PreparationBarrier preparationBarrier, Executor executor2) {
                return HorizonClient.skyboxResourceListener().reload(sharedState, executor, preparationBarrier, executor2);
            }

            @Override
            public Identifier getFabricId() {
                return Identifier.fromNamespaceAndPath(HorizonClient.MOD_ID, "skybox_reader");
            }
        });

        ClientTickEvents.END_LEVEL_TICK.register(client -> SkyboxManager.getInstance().tick(client));
        SkyboxDebugScreen screen = new SkyboxDebugScreen(Component.nullToEmpty("Skybox Debug Screen"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            HorizonClient.config().getKeyBinding().tick(client);
            if (HorizonClient.config().getKeyBinding().toggleSkyboxDebugHud.consumeClick()) {
                screen.renderHud(new GuiGraphicsExtractor(Minecraft.getInstance(), new GuiRenderState(), (int) Minecraft.getInstance().mouseHandler.xpos(), (int) Minecraft.getInstance().mouseHandler.ypos()));
            } if (HorizonClient.config().getKeyBinding().toggleNuit.consumeClick()) {
            }
        });
        KeyMappingHelper.registerKeyMapping(HorizonClient.config().getKeyBinding().toggleNuit);
        KeyMappingHelper.registerKeyMapping(HorizonClient.config().getKeyBinding().toggleSkyboxDebugHud);
    }
}