package com.ahmet.accountmanager.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class WatermarkRenderer {

    private static final String LABEL = "Discord:";
    private static final String HANDLE = "@te5rontop";

    private static final int DARK_BLUE = 0xFF1E3A8A;
    private static final int WHITE = 0xFFFFFFFF;

    private static final float SCALE = 1.5F;

    private static final double ONE_WAY_SECONDS = 12.0;

    private WatermarkRenderer() {
    }

    public static void render(
            GuiGraphicsExtractor graphics,
            Font font,
            int screenWidth,
            int screenHeight
    ) {

        int gap = 4;

        int labelWidth = font.width(LABEL);
        int handleWidth = font.width(HANDLE);

        int totalWidth =
                labelWidth + gap + handleWidth;

        float scaledWidth =
                screenWidth / SCALE;

        float scaledHeight =
                screenHeight / SCALE;

        double currentTime =
                System.nanoTime() / 1_000_000_000.0;

        graphics.pose().pushMatrix();
        graphics.pose().scale(SCALE, SCALE);

        int trailCount = 4;

        for (int i = trailCount; i >= 1; i--) {

            double delayedTime =
                    currentTime - (i * 0.015);

            float[] position =
                    getPosition(
                            delayedTime,
                            scaledWidth,
                            scaledHeight,
                            totalWidth,
                            font.lineHeight
                    );

            int alpha =
                    20 + ((trailCount - i) * 12);

            int trailBlue =
                    (alpha << 24) | 0x001E3A8A;

            int trailWhite =
                    (alpha << 24) | 0x00FFFFFF;

            graphics.text(
                    font,
                    Component.literal(LABEL),
                    (int) position[0],
                    (int) position[1],
                    trailBlue,
                    true
            );

            graphics.text(
                    font,
                    Component.literal(HANDLE),
                    (int) position[0] + labelWidth + gap,
                    (int) position[1],
                    trailWhite,
                    true
            );
        }

        float[] position =
                getPosition(
                        currentTime,
                        scaledWidth,
                        scaledHeight,
                        totalWidth,
                        font.lineHeight
                );

        graphics.text(
                font,
                Component.literal(LABEL),
                (int) position[0],
                (int) position[1],
                DARK_BLUE,
                true
        );

        graphics.text(
                font,
                Component.literal(HANDLE),
                (int) position[0] + labelWidth + gap,
                (int) position[1],
                WHITE,
                true
        );

        graphics.pose().popMatrix();
    }

    private static float[] getPosition(
            double time,
            float screenWidth,
            float screenHeight,
            int totalWidth,
            int fontHeight
    ) {

        double raw =
                (time % (ONE_WAY_SECONDS * 2.0))
                        / ONE_WAY_SECONDS;

        double progress =
                raw <= 1.0
                        ? raw
                        : 2.0 - raw;

        progress =
                (1.0 - Math.cos(progress * Math.PI))
                        / 2.0;

        float startX =
                screenWidth - totalWidth - 8;

        float startY = 8;

        float endX = 8;

        float endY =
                screenHeight - fontHeight - 8;

        float x =
                (float) (
                        startX
                                + (endX - startX)
                                * progress
                );

        float y =
                (float) (
                        startY
                                + (endY - startY)
                                * progress
                );

        return new float[]{x, y};
    }
}