package com.ahmet.accountmanager.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class PurpleButton extends AbstractWidget {

    private final Font font;
    private final Runnable onPress;

    public PurpleButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            Font font,
            Runnable onPress
    ) {
        super(x, y, width, height, message);

        this.font = font;
        this.onPress = onPress;
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {

        int backgroundColor;
        int borderColor;
        int textColor;

        if (!this.active) {

            backgroundColor = 0xFF24202A;
            borderColor = 0xFF4A4050;
            textColor = 0xFF777777;

        } else if (this.isHovered()) {

            backgroundColor = 0xFF7A2E8E;
            borderColor = 0xFFD45CFF;
            textColor = 0xFFFFFFFF;

        } else {

            backgroundColor = 0xFF3B1747;
            borderColor = 0xFF7A2E8E;
            textColor = 0xFFE6D5EA;
        }

        graphics.fill(
                getX(),
                getY(),
                getX() + this.width,
                getY() + this.height,
                backgroundColor
        );

        graphics.outline(
                getX(),
                getY(),
                this.width,
                this.height,
                borderColor
        );

        int textX =
                getX()
                        + (this.width - font.width(getMessage())) / 2;

        int textY =
                getY()
                        + (this.height - font.lineHeight) / 2;

        graphics.text(
                font,
                getMessage(),
                textX,
                textY,
                textColor,
                true
        );
    }

    @Override
    public void onClick(
            MouseButtonEvent event,
            boolean doubleClick
    ) {

        if (this.active) {
            onPress.run();
        }
    }

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput output
    ) {

        this.defaultButtonNarrationText(output);
    }
}