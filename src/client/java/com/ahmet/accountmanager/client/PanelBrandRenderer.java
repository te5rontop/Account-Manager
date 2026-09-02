package com.ahmet.accountmanager.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class PanelBrandRenderer {

    private static final String PREFIX = "Made by ";
    private static final String NAME = "te5r.";

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BRIGHT_RED = 0xFFFF1A1A;

    private PanelBrandRenderer() {
    }

    public static void render(
            GuiGraphicsExtractor graphics,
            Font font,
            int panelX,
            int panelY
    ) {

        int x = panelX + 10;
        int y = panelY + 9;

        int nameX =
                x + font.width(PREFIX);

        graphics.text(
                font,
                Component.literal(PREFIX),
                x,
                y,
                WHITE,
                true
        );

        graphics.text(
                font,
                Component.literal(NAME),
                nameX,
                y,
                BRIGHT_RED,
                true
        );
    }
}