package com.jeff.horizon.fabric;

import com.jeff.horizon.HorizonClient;
import com.mojang.serialization.Lifecycle;
import com.jeff.horizon.SkyboxManager;
import com.jeff.horizon.api.skyboxes.Skybox;
import com.jeff.horizon.screen.SkyboxDebugScreen;
import com.jeff.horizon.skybox.SkyboxType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
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

        ClientTickEvents.END_WORLD_TICK.register(client -> SkyboxManager.getInstance().tick(client));
        ClientTickEvents.END_CLIENT_TICK.register(client -> HorizonClient.config().getKeyBinding().tick(client));
        SkyboxDebugScreen screen = new SkyboxDebugScreen(Component.nullToEmpty("Skybox Debug Screen"));
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> screen.renderHud(drawContext));
        KeyBindingHelper.registerKeyBinding(HorizonClient.config().getKeyBinding().toggleNuit);
        KeyBindingHelper.registerKeyBinding(HorizonClient.config().getKeyBinding().toggleSkyboxDebugHud);
    }
}