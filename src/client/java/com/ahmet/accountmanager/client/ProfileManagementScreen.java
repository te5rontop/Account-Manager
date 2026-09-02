package com.ahmet.accountmanager.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.ahmet.accountmanager.client.auth.MicrosoftAuthService;

public class ProfileManagementScreen extends Screen {

    private static final int PANEL_WIDTH = 500;
    private static final int PANEL_HEIGHT = 340;

    private final Screen parent;

    private PurpleButton changeIgnButton;
    private PurpleButton changeSkinButton;
    private PlayerSkinWidget skinWidget;
    private PurpleButton refreshButton;
    private boolean refreshingProfile = false;
    private String refreshMessage = "";

    public ProfileManagementScreen(Screen parent) {
        super(Component.literal("Profile Management"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        int buttonX = panelX + 25;
        int buttonWidth = 270;

// REFRESH PROFILE
        refreshButton =
                new PurpleButton(
                        buttonX,
                        panelY + 205,
                        buttonWidth,
                        20,
                        Component.literal("Refresh Minecraft Profile"),
                        this.font,
                        this::refreshMinecraftProfile
                );

        refreshButton.active =
                AccountState.microsoftSignedIn
                        && MicrosoftAuthService.canRefreshMinecraftProfile();

        this.addRenderableWidget(refreshButton);

        // CHANGE IGN
        changeIgnButton =
                new PurpleButton(
                        buttonX,
                        panelY + 235,
                        buttonWidth,
                        20,
                        Component.literal("Change Minecraft IGN"),
                        this.font,
                        this::openIgnEditor
                );

        changeIgnButton.active =
                AccountState.minecraftProfileLoaded;

        this.addRenderableWidget(changeIgnButton);


        // CHANGE SKIN
        changeSkinButton =
                new PurpleButton(
                        buttonX,
                        panelY + 265,
                        buttonWidth,
                        20,
                        Component.literal("Change Skin"),
                        this.font,
                        this::openSkinEditor
                );

        changeSkinButton.active = true;

        this.addRenderableWidget(changeSkinButton);


        // BACK
        this.addRenderableWidget(
                new PurpleButton(
                        buttonX,
                        panelY + 305,
                        buttonWidth,
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


        // CURRENT SKIN
        if (this.minecraft != null) {

            skinWidget =
                    new PlayerSkinWidget(
                            120,
                            150,
                            this.minecraft.getEntityModels(),
                            this.minecraft.getSkinManager().createLookup(
                                    this.minecraft.getGameProfile(),
                                    false
                            )
                    );

            skinWidget.setX(
                    panelX + PANEL_WIDTH - 155
            );

            skinWidget.setY(
                    panelY + 65
            );

            this.addRenderableWidget(skinWidget);
        }
    }

    private void refreshMinecraftProfile() {

        if (refreshingProfile) {
            return;
        }

        if (!MicrosoftAuthService.canRefreshMinecraftProfile()) {
            refreshMessage = "Microsoft sign-in is required.";
            return;
        }

        refreshingProfile = true;
        refreshMessage = "Refreshing Minecraft profile...";

        if (refreshButton != null) {
            refreshButton.active = false;
            refreshButton.setMessage(
                    Component.literal("Refreshing...")
            );
        }

        MicrosoftAuthService.refreshMinecraftProfile()
                .whenComplete((result, error) -> {

                    if (this.minecraft == null) {
                        return;
                    }

                    this.minecraft.execute(() -> {

                        refreshingProfile = false;

                        if (error != null || result == null) {

                            refreshMessage =
                                    "Profile refresh failed.";

                        } else {

                            AccountState.microsoftSignedIn =
                                    result.microsoftSignedIn();

                            AccountState.minecraftProfileLoaded =
                                    result.minecraftProfileLoaded();

                            AccountState.minecraftIgn =
                                    result.minecraftIgn() != null
                                            ? result.minecraftIgn()
                                            : "";

                            AccountState.minecraftUuid =
                                    result.minecraftUuid() != null
                                            ? result.minecraftUuid()
                                            : "";

                            AccountState.statusMessage =
                                    result.message();

                            AccountState.minecraftAuthorizationRejected =
                                    result.microsoftSignedIn()
                                            && !result.minecraftProfileLoaded()
                                            && result.message() != null
                                            && result.message()
                                            .toLowerCase()
                                            .contains("app registration");

                            if (result.minecraftProfileLoaded()) {

                                refreshMessage =
                                        "Minecraft profile refreshed.";

                            } else if (AccountState.minecraftAuthorizationRejected) {

                                refreshMessage =
                                        "App Not Authorized.";

                            } else {

                                refreshMessage =
                                        "Minecraft profile unavailable.";
                            }
                        }

                        if (refreshButton != null) {

                            refreshButton.setMessage(
                                    Component.literal(
                                            "Refresh Minecraft Profile"
                                    )
                            );

                            refreshButton.active =
                                    MicrosoftAuthService
                                            .canRefreshMinecraftProfile();
                        }
                    });
                });
    }

    private void openIgnEditor() {

        if (!AccountState.minecraftProfileLoaded) {
            return;
        }

        if (this.minecraft != null) {
            this.minecraft.setScreen(
                    new IgnEditorScreen(this)
            );
        }
    }

    private void openSkinEditor() {

        if (this.minecraft != null) {

            this.minecraft.setScreen(
                    new SkinManagementScreen(this)
            );
        }

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
                0xD915151D
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

        int left = panelX + 25;

        String title = "PROFILE MANAGEMENT";

        int titleX =
                panelX
                        + (PANEL_WIDTH - this.font.width(title)) / 2;

        graphics.text(
                this.font,
                Component.literal(title)
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                titleX,
                panelY + 17,
                0xFFFFFFFF,
                true
        );

        graphics.text(
                this.font,
                Component.literal("Minecraft Profile")
                        .withStyle(ChatFormatting.GRAY),
                left,
                panelY + 58,
                0xFFFFFFFF,
                true
        );

        if (AccountState.minecraftProfileLoaded) {

            graphics.text(
                    this.font,
                    Component.literal("● Connected")
                            .withStyle(ChatFormatting.GREEN),
                    left,
                    panelY + 76,
                    0xFFFFFFFF,
                    true
            );

        } else if (AccountState.minecraftAuthorizationRejected) {

            graphics.text(
                    this.font,
                    Component.literal("● App Not Authorized")
                            .withStyle(ChatFormatting.YELLOW),
                    left,
                    panelY + 76,
                    0xFFFFFFFF,
                    true
            );

            graphics.text(
                    this.font,
                    Component.literal(
                            "Minecraft Services rejected this app registration."
                    ).withStyle(ChatFormatting.DARK_GRAY),
                    left,
                    panelY + 94,
                    0xFFFFFFFF,
                    true
            );

        } else {

            graphics.text(
                    this.font,
                    Component.literal("● Profile unavailable")
                            .withStyle(ChatFormatting.YELLOW),
                    left,
                    panelY + 76,
                    0xFFFFFFFF,
                    true
            );
        }

        String ign =
                AccountState.minecraftIgn.isEmpty()
                        ? "Unavailable"
                        : AccountState.minecraftIgn;

        graphics.text(
                this.font,
                Component.literal("IGN: ")
                        .withStyle(ChatFormatting.DARK_PURPLE)
                        .append(
                                Component.literal(ign)
                                        .withStyle(
                                                AccountState.minecraftProfileLoaded
                                                        ? ChatFormatting.GREEN
                                                        : ChatFormatting.DARK_GRAY
                                        )
                        ),
                left,
                panelY + 105,
                0xFFFFFFFF,
                true
        );

        String uuid =
                AccountState.minecraftUuid.isEmpty()
                        ? "Unavailable"
                        : AccountState.minecraftUuid;

        graphics.text(
                this.font,
                Component.literal("UUID: ")
                        .withStyle(ChatFormatting.DARK_PURPLE)
                        .append(
                                Component.literal(uuid)
                                        .withStyle(ChatFormatting.GRAY)
                        ),
                left,
                panelY + 125,
                0xFFFFFFFF,
                true
        );

        String skinTitle = "Current Skin";

        int skinAreaX =
                panelX + PANEL_WIDTH - 165;

        int skinTitleX =
                skinAreaX
                        + (120 - this.font.width(skinTitle)) / 2;

        graphics.text(
                this.font,
                Component.literal(skinTitle)
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                skinTitleX,
                panelY + 52,
                0xFFFFFFFF,
                true
        );

        if (!AccountState.minecraftProfileLoaded) {

            graphics.text(
                    this.font,
                    Component.literal(
                            "IGN changes require Minecraft profile access."
                    ).withStyle(ChatFormatting.DARK_GRAY),
                    left,
                    panelY + 158,
                    0xFFFFFFFF,
                    true
            );
            graphics.text(
                    this.font,
                    Component.literal(
                            "Skin preview is available; upload requires profile access."
                    ).withStyle(ChatFormatting.DARK_GRAY),
                    left,
                    panelY + 176,
                    0xFFFFFFFF,
                    true
            );
        }

        if (!refreshMessage.isEmpty()) {

            ChatFormatting refreshColor;

            if (refreshingProfile) {
                refreshColor = ChatFormatting.YELLOW;

            } else if (AccountState.minecraftProfileLoaded) {
                refreshColor = ChatFormatting.GREEN;

            } else if (AccountState.minecraftAuthorizationRejected) {
                refreshColor = ChatFormatting.YELLOW;

            } else {
                refreshColor = ChatFormatting.RED;
            }

            graphics.text(
                    this.font,
                    Component.literal(refreshMessage)
                            .withStyle(refreshColor),
                    left,
                    panelY + 194,
                    0xFFFFFFFF,
                    true
            );
        }
    }
}

