package com.jeff.horizon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jeff.horizon.HorizonClient;
import com.mojang.blaze3d.platform.InputConstants;
import com.jeff.horizon.SkyboxManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import javax.swing.text.JTextComponent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class HorizonConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.PRIVATE).create();
    public final GeneralSettings generalSettings = new GeneralSettings();
    private final KeyBindingImpl keyBinding = new KeyBindingImpl();
    private File file;

    public static HorizonConfig load(File file) {
        HorizonConfig config;
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = GSON.fromJson(reader, HorizonConfig.class);
            } catch (Exception e) {
                HorizonClient.getLogger().error("Could not parse config, falling back to defaults!", e);
                config = new HorizonConfig();
            }
        } else {
            config = new HorizonConfig();
        }

        config.file = file;
        config.save();
        return config;
    }

    public KeyBindingImpl getKeyBinding() {
        return this.keyBinding;
    }

    public void save() {
        File dir = this.file.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Could not create parent directories");
            }
        } else if (!dir.isDirectory()) {
            throw new RuntimeException("The parent file is not a directory");
        }

        try (FileWriter writer = new FileWriter(this.file)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not save configuration file", e);
        }
    }

    public static class GeneralSettings {
        public boolean enable = true;
        public int unexpectedTransitionDuration = 20;
        public long fadeCacheDuration = 24000;

        public boolean debugMode = false;
        public boolean debugHud = false;
    }

    public static class KeyBindingImpl {

        public final KeyMapping toggleNuit = new KeyMapping("key.nuit.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, "category.nuit");
        public final KeyMapping toggleSkyboxDebugHud = new KeyMapping("key.nuit.toggle.debug_hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "category.nuit");

        public void tick(Minecraft client) {
            while (this.toggleNuit.consumeClick()) {
                HorizonClient.config().generalSettings.enable = !HorizonClient.config().generalSettings.enable;
                HorizonClient.config().save();
                SkyboxManager.getInstance().setEnabled(HorizonClient.config().generalSettings.enable);
                LocalPlayer player = Objects.requireNonNull(client.player);
                if (SkyboxManager.getInstance().isEnabled()) {
                    player.sendSystemMessage(Component.translatable("nuit.message.enabled"));
                } else {
                    player.sendSystemMessage(Component.translatable("nuit.message.disabled"));
                }
            }

            while (this.toggleSkyboxDebugHud.consumeClick()) {
                HorizonClient.config().generalSettings.debugHud = !HorizonClient.config().generalSettings.debugHud;
                HorizonClient.config().save();
            }
        }
    }
}
