package com.ahmet.accountmanager.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.ahmet.accountmanager.client.auth.MinecraftProfileService;
import com.ahmet.accountmanager.client.auth.MicrosoftAuthService;

public class IgnEditorScreen extends Screen {

    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 180;

    private final Screen parent;

    private EditBox ignInput;
    private PurpleButton applyButton;

    private String statusMessage =
            "Enter a new Minecraft IGN.";

    public IgnEditorScreen(Screen parent) {
        super(Component.literal("Change Minecraft IGN"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        ignInput =
                new EditBox(
                        this.font,
                        panelX + 30,
                        panelY + 65,
                        PANEL_WIDTH - 60,
                        20,
                        Component.literal("Minecraft IGN")
                );

        ignInput.setMaxLength(16);

        if (!AccountState.minecraftIgn.isEmpty()) {
            ignInput.setValue(AccountState.minecraftIgn);
        }

        this.addRenderableWidget(ignInput);


        applyButton =
                new PurpleButton(
                        panelX + 30,
                        panelY + 105,
                        145,
                        20,
                        Component.literal("Change IGN"),
                        this.font,
                        this::changeIgn
                );

        this.addRenderableWidget(applyButton);
        applyButton.active =
                AccountState.minecraftProfileLoaded
                        && MinecraftProfileService.hasActiveMinecraftSession();


        this.addRenderableWidget(
                new PurpleButton(
                        panelX + 185,
                        panelY + 105,
                        145,
                        20,
                        Component.literal("Back"),
                        this.font,
                        () -> {
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(parent);
                            }
                        }
                )
        );
    }

    private void changeIgn() {

        String ign = ignInput.getValue().trim();

        if (ign.length() < 3 || ign.length() > 16) {
            statusMessage = "IGN must be 3-16 characters.";
            return;
        }

        if (!ign.matches("[A-Za-z0-9_]+")) {
            statusMessage = "Only letters, numbers and _ are allowed.";
            return;
        }

        if (!AccountState.minecraftProfileLoaded
                || !MinecraftProfileService.hasActiveMinecraftSession()) {

            statusMessage =
                    "Minecraft profile access is required.";

            return;
        }

        applyButton.active = false;
        statusMessage = "Changing Minecraft IGN...";

        Thread.ofVirtual().start(() -> {

            try {

                MinecraftProfileService.changeMinecraftName(ign);

// İsim değişikliğinden sonra profili Minecraft Services'ten yeniden çek.
                MicrosoftAuthService.LoginResult refreshResult =
                        MicrosoftAuthService
                                .refreshMinecraftProfile()
                                .join();

                if (this.minecraft != null) {
                    this.minecraft.execute(() -> {

                        AccountState.microsoftSignedIn =
                                refreshResult.microsoftSignedIn();

                        AccountState.minecraftProfileLoaded =
                                refreshResult.minecraftProfileLoaded();

                        AccountState.minecraftIgn =
                                refreshResult.minecraftIgn() != null
                                        ? refreshResult.minecraftIgn()
                                        : ign;

                        AccountState.minecraftUuid =
                                refreshResult.minecraftUuid() != null
                                        ? refreshResult.minecraftUuid()
                                        : AccountState.minecraftUuid;

                        AccountState.statusMessage =
                                refreshResult.message();

                        AccountState.minecraftAuthorizationRejected =
                                refreshResult.microsoftSignedIn()
                                        && !refreshResult.minecraftProfileLoaded()
                                        && refreshResult.message() != null
                                        && refreshResult.message()
                                        .toLowerCase()
                                        .contains("app registration");

                        if (refreshResult.minecraftProfileLoaded()) {

                            statusMessage =
                                    "IGN changed and profile refreshed.";

                        } else {

                            AccountState.minecraftIgn = ign;

                            statusMessage =
                                    "IGN changed, but profile refresh failed.";
                        }

                        applyButton.active = true;
                    });
                }

            } catch (Exception exception) {

                if (this.minecraft != null) {
                    this.minecraft.execute(() -> {

                        String message = exception.getMessage();

                        statusMessage =
                                message != null
                                        ? message
                                        : "IGN change failed.";

                        applyButton.active = true;
                    });
                }
            }
        });
    }

    @Override
    public void onClose() {

        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {

        super.extractBackground(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        graphics.fill(
                panelX,
                panelY,
                panelX + PANEL_WIDTH,
                panelY + PANEL_HEIGHT,
                0xE015151D
        );

        graphics.outline(
                panelX,
                panelY,
                PANEL_WIDTH,
                PANEL_HEIGHT,
                0xFF7A2E8E
        );

        graphics.fill(
                panelX + 20,
                panelY + 38,
                panelX + PANEL_WIDTH - 20,
                panelY + 39,
                0xFF5C3566
        );
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {

        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        PanelBrandRenderer.render(
                graphics,
                this.font,
                panelX,
                panelY
        );

        String title = "CHANGE MINECRAFT IGN";

        graphics.text(
                this.font,
                Component.literal(title)
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                panelX
                        + (PANEL_WIDTH - this.font.width(title)) / 2,
                panelY + 16,
                0xFFFFFFFF,
                true
        );

        graphics.text(
                this.font,
                Component.literal("New IGN:")
                        .withStyle(ChatFormatting.GRAY),
                panelX + 30,
                panelY + 52,
                0xFFFFFFFF,
                true
        );

        if (!statusMessage.isEmpty()) {

            graphics.text(
                    this.font,
                    Component.literal(statusMessage)
                            .withStyle(
                                    statusMessage.contains("valid.")
                                            ? ChatFormatting.GREEN
                                            : ChatFormatting.RED
                            ),
                    panelX + 30,
                    panelY + 140,
                    0xFFFFFFFF,
                    true
            );
        }
    }
}
