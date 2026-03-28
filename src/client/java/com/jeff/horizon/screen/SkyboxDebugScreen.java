package com.jeff.horizon.screen;

import com.jeff.horizon.HorizonClient;
import com.jeff.horizon.SkyboxManager;
import com.jeff.horizon.api.skyboxes.Skybox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class SkyboxDebugScreen extends Screen {
    public SkyboxDebugScreen(Component title) {
        super(title);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        this.renderHud(graphics);
    }

    public void renderHud(GuiGraphicsExtractor drawContext) {
        if (HorizonClient.config().generalSettings.debugHud || Minecraft.getInstance().screen == this) {
            int yPadding = 2;
            for (Map.Entry<Identifier, Skybox> skyboxEntry : SkyboxManager.getInstance().getSkyboxMap().entrySet()) {
                Skybox activeSkybox = skyboxEntry.getValue();
                if (activeSkybox.isActive()) {
                    drawContext.textRenderer().accept(TextAlignment.CENTER, 2, yPadding, Component.literal(skyboxEntry.getKey() + activeSkybox.toString()));
                    yPadding += 14;
                }
            }
        }
    }
}
