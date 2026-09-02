package com.ahmet.accountmanager.client;

import com.ahmet.accountmanager.client.auth.MicrosoftAuthService;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import com.ahmet.accountmanager.client.auth.MinecraftProfileService;

public class AccountManagerScreen extends Screen {

    private static final int PANEL_WIDTH = 560;
    private static final int PANEL_HEIGHT = 330;

    private final Screen parent;

    private PurpleButton loginButton;
    private PurpleButton clearButton;
    private PurpleButton backButton;
    private PurpleButton modalButton;
    private PurpleButton profileButton;

    private boolean loginInProgress = false;
    private boolean microsoftSignedIn = false;
    private boolean minecraftProfileLoaded = false;

    private static final int MODAL_WIDTH = 280;
    private static final int MODAL_HEIGHT = 120;

    private boolean loginModalVisible = false;
    private String loginModalMessage = "";
    private String loginModalDetail = "";

    private PlayerSkinWidget skinWidget;

    private String microsoftAccount = "";
    private String minecraftIgn = "";
    private String minecraftUuid = "";
    private String statusMessage = "Ready.";

    public AccountManagerScreen(Screen parent) {

        super(Component.literal("Account Manager"));
        this.parent = parent;

        microsoftSignedIn =
                AccountState.microsoftSignedIn;

        minecraftProfileLoaded =
                AccountState.minecraftProfileLoaded;

        microsoftAccount =
                AccountState.microsoftAccount;

        minecraftIgn =
                AccountState.minecraftIgn;

        minecraftUuid =
                AccountState.minecraftUuid;

        statusMessage =
                AccountState.statusMessage;
    }

    @Override
    protected void init() {

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        int buttonX = panelX + 25;
        int totalButtonWidth = PANEL_WIDTH - 50;

        int halfButtonWidth =
                (totalButtonWidth - 10) / 2;

        if (this.minecraft != null) {

            skinWidget =
                    new PlayerSkinWidget(
                            100,
                            110,
                            this.minecraft.getEntityModels(),
                            this.minecraft.getSkinManager().createLookup(
                                    this.minecraft.getGameProfile(),
                                    false
                            )
                    );

            skinWidget.setX(
                    panelX + PANEL_WIDTH - 145
            );

            skinWidget.setY(
                    panelY + 75
            );

            this.addRenderableWidget(skinWidget);
        }

        profileButton =
                new PurpleButton(
                        buttonX,
                        panelY + 255,
                        halfButtonWidth,
                        20,
                        Component.literal("Profile Management"),
                        this.font,
                        () -> {
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(
                                        new ProfileManagementScreen(this)
                                );
                            }
                        }
                );

        this.addRenderableWidget(profileButton);

        loginButton =
                new PurpleButton(
                        buttonX + halfButtonWidth + 10,
                        panelY + 255,
                        halfButtonWidth,
                        20,
                        Component.literal(
                                microsoftSignedIn
                                        ? "Sign in Again"
                                        : "Login with Microsoft"
                        ),
                        this.font,
                        this::startMicrosoftLogin
                );

        this.addRenderableWidget(loginButton);

        clearButton =
                new PurpleButton(
                        buttonX,
                        panelY + 285,
                        halfButtonWidth,
                        20,
                        Component.literal("Clear Account"),
                        this.font,
                        this::clearAccount
                );

        clearButton.active = microsoftSignedIn;

        this.addRenderableWidget(clearButton);

        backButton =
                new PurpleButton(
                        buttonX + halfButtonWidth + 10,
                        panelY + 285,
                        halfButtonWidth,
                        20,
                        Component.literal("Back"),
                        this.font,
                        () -> {
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(parent);
                            }
                        }
                );

        this.addRenderableWidget(backButton);

        int modalX = (this.width - MODAL_WIDTH) / 2;
        int modalY = (this.height - MODAL_HEIGHT) / 2;

        modalButton =
                new PurpleButton(
                        modalX + 80,
                        modalY + 82,
                        120,
                        20,
                        Component.literal("Hide"),
                        this.font,
                        this::hideLoginModal
                );

        modalButton.visible = false;

        this.addRenderableWidget(modalButton);
    }

    private void startMicrosoftLogin() {

        if (loginInProgress) {
            return;
        }

        loginInProgress = true;
        statusMessage = "Opening Microsoft login...";

        loginModalMessage =
                "Microsoft Sign In";

        loginModalDetail =
                "Complete the sign-in in your browser.";

        showLoginModal();

        if (loginButton != null) {
            loginButton.active = false;
            loginButton.setMessage(
                    Component.literal("Signing in...")
            );
        }

        MicrosoftAuthService.signIn()
                .whenComplete((result, error) -> {

                    if (this.minecraft == null) {
                        return;
                    }

                    this.minecraft.execute(() -> {

                        loginInProgress = false;

                        if (loginButton != null) {
                            loginButton.active = true;

                            loginButton.setMessage(
                                    Component.literal(
                                            microsoftSignedIn
                                                    ? "Sign in Again"
                                                    : "Login with Microsoft"
                                    )
                            );
                        }

                        if (error != null) {

                            microsoftSignedIn = false;
                            minecraftProfileLoaded = false;

                            microsoftAccount = "";
                            minecraftIgn = "";
                            minecraftUuid = "";

                            statusMessage = "Microsoft login failed.";

                            loginModalMessage =
                                    "Sign In Failed";

                            loginModalDetail =
                                    "Microsoft login could not be completed.";

                            if (modalButton != null) {
                                modalButton.setMessage(
                                        Component.literal("Done")
                                );
                            }

                            return;
                        }

                        microsoftSignedIn =
                                result.microsoftSignedIn();

                        if (loginButton != null) {

                            loginButton.active = true;

                            loginButton.setMessage(
                                    Component.literal(
                                            microsoftSignedIn
                                                    ? "Sign in Again"
                                                    : "Login with Microsoft"
                                    )
                            );
                        }

                        if (clearButton != null) {
                            clearButton.active = microsoftSignedIn;
                        }

                        minecraftProfileLoaded =
                                result.minecraftProfileLoaded();

                        AccountState.minecraftAuthorizationRejected =
                                result.microsoftSignedIn()
                                        && !result.minecraftProfileLoaded()
                                        && result.message() != null
                                        && result.message()
                                        .toLowerCase()
                                        .contains("app registration");

                        microsoftAccount =
                                result.microsoftUsername() != null
                                        ? result.microsoftUsername()
                                        : "";

                        microsoftAccount =
                                result.microsoftUsername() != null
                                        ? result.microsoftUsername()
                                        : "";

                        minecraftIgn =
                                result.minecraftIgn() != null
                                        ? result.minecraftIgn()
                                        : "";

                        minecraftUuid =
                                result.minecraftUuid() != null
                                        ? result.minecraftUuid()
                                        : "";

                        statusMessage =
                                result.message() != null
                                        ? result.message()
                                        : "Unknown status.";

                        AccountState.microsoftSignedIn =
                                microsoftSignedIn;

                        AccountState.minecraftProfileLoaded =
                                minecraftProfileLoaded;

                        AccountState.microsoftAccount =
                                microsoftAccount;

                        AccountState.minecraftIgn =
                                minecraftIgn;

                        AccountState.minecraftUuid =
                                minecraftUuid;

                        AccountState.statusMessage =
                                statusMessage;
                    });
                });
    }

    private void clearAccount() {

        AccountState.clear();
        MinecraftProfileService.clearSession();
        MicrosoftAuthService.clearSession();

        microsoftSignedIn = false;
        minecraftProfileLoaded = false;

        microsoftAccount = "";
        minecraftIgn = "";
        minecraftUuid = "";

        statusMessage = "Account information cleared.";

        if (loginButton != null) {
            loginButton.setMessage(
                    Component.literal("Login with Microsoft")
            );
        }

        if (clearButton != null) {
            clearButton.active = false;
        }
    }
        private void showLoginModal() {

            profileButton.visible = false;

            loginModalVisible = true;

            if (loginButton != null) {
                loginButton.visible = false;
            }

            if (clearButton != null) {
                clearButton.visible = false;
            }

            if (backButton != null) {
                backButton.visible = false;
            }

            if (skinWidget != null) {
                skinWidget.visible = false;
            }

            if (modalButton != null) {
                modalButton.visible = true;
                modalButton.active = true;

                modalButton.setMessage(
                        Component.literal(
                                loginInProgress ? "Hide" : "Done"
                        )
                );
            }
        }

        private void hideLoginModal() {

            profileButton.visible = true;

            loginModalVisible = false;

            if (modalButton != null) {
                modalButton.visible = false;
            }

            if (loginButton != null) {

                loginButton.visible = true;
                loginButton.active = !loginInProgress;

                loginButton.setMessage(
                        Component.literal(
                                loginInProgress
                                        ? "Signing in..."
                                        : microsoftSignedIn
                                        ? "Sign in Again"
                                        : "Login with Microsoft"
                        )
                );
            }

            if (clearButton != null) {
                clearButton.visible = true;
                clearButton.active = microsoftSignedIn;
            }

            if (backButton != null) {
                backButton.visible = true;
            }

            if (skinWidget != null) {
                skinWidget.visible = true;
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

        int statusCardX =
                panelX + PANEL_WIDTH - 205;

        int statusCardY =
                panelY + 190;

        int statusCardWidth = 180;
        int statusCardHeight = 55;

        if (!loginModalVisible) {

        graphics.fill(
                statusCardX,
                statusCardY,
                statusCardX + statusCardWidth,
                statusCardY + statusCardHeight,
                0xCC211826
        );

        graphics.outline(
                statusCardX,
                statusCardY,
                statusCardWidth,
                statusCardHeight,
                0xFF7A2E8E
        );

        graphics.fill(
                statusCardX + 8,
                statusCardY + 27,
                statusCardX + statusCardWidth - 8,
                statusCardY + 28,
                0xFF4B3152
        );
    }

        if (loginModalVisible) {

            graphics.fill(
                    0,
                    0,
                    this.width,
                    this.height,
                    0x99000000
            );

            int modalX =
                    (this.width - MODAL_WIDTH) / 2;

            int modalY =
                    (this.height - MODAL_HEIGHT) / 2;

            graphics.fill(
                    modalX,
                    modalY,
                    modalX + MODAL_WIDTH,
                    modalY + MODAL_HEIGHT,
                    0xF0181520
            );

            graphics.outline(
                    modalX,
                    modalY,
                    MODAL_WIDTH,
                    MODAL_HEIGHT,
                    0xFFD45CFF
            );

            graphics.fill(
                    modalX + 15,
                    modalY + 32,
                    modalX + MODAL_WIDTH - 15,
                    modalY + 33,
                    0xFF7A2E8E
            );
        }
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

        if (loginModalVisible) {

            int modalX =
                    (this.width - MODAL_WIDTH) / 2;

            int modalY =
                    (this.height - MODAL_HEIGHT) / 2;

            String modalTitle =
                    "MICROSOFT SIGN IN";

            int modalTitleX =
                    modalX
                            + (MODAL_WIDTH
                            - this.font.width(modalTitle)) / 2;

            graphics.text(
                    this.font,
                    Component.literal(modalTitle)
                            .withStyle(ChatFormatting.LIGHT_PURPLE),
                    modalTitleX,
                    modalY + 14,
                    0xFFFFFFFF,
                    true
            );

            int messageX =
                    modalX
                            + (MODAL_WIDTH
                            - this.font.width(loginModalMessage)) / 2;

            ChatFormatting messageColor;

            if (loginInProgress) {
                messageColor = ChatFormatting.YELLOW;
            } else if (microsoftSignedIn) {
                messageColor = ChatFormatting.GREEN;
            } else {
                messageColor = ChatFormatting.RED;
            }

            graphics.text(
                    this.font,
                    Component.literal(loginModalMessage)
                            .withStyle(messageColor),
                    messageX,
                    modalY + 46,
                    0xFFFFFFFF,
                    true
            );

            int detailX =
                    modalX
                            + (MODAL_WIDTH
                            - this.font.width(loginModalDetail)) / 2;

            graphics.text(
                    this.font,
                    Component.literal(loginModalDetail)
                            .withStyle(ChatFormatting.GRAY),
                    detailX,
                    modalY + 62,
                    0xFFFFFFFF,
                    true
            );

            return;
        }

        int left = panelX + 25;

        String title = "ACCOUNT MANAGER";

        int titleX =
                panelX
                        + (PANEL_WIDTH - this.font.width(title)) / 2;

        graphics.text(
                this.font,
                Component.literal(title)
                        .withStyle(ChatFormatting.DARK_PURPLE),
                titleX,
                panelY + 15,
                0xFFFFFFFF,
                true
        );

        // separator
        graphics.fill(
                panelX + 20,
                panelY + 34,
                panelX + PANEL_WIDTH - 20,
                panelY + 35,
                0xFF5C3566
        );

        graphics.text(
                this.font,
                Component.literal("Current Client")
                        .withStyle(ChatFormatting.GRAY),
                left,
                panelY + 47,
                0xFFFFFFFF,
                true
        );

        String clientUsername =
                this.minecraft != null
                        ? this.minecraft.getUser().getName()
                        : "Unknown";

        graphics.text(
                this.font,
                Component.literal("IGN: ")
                        .withStyle(ChatFormatting.DARK_PURPLE)
                        .append(
                                Component.literal(clientUsername)
                                        .withStyle(ChatFormatting.GREEN)
                        ),
                left,
                panelY + 62,
                0xFFFFFFFF,
                true
        );

        graphics.text(
                this.font,
                Component.literal("Microsoft Account")
                        .withStyle(ChatFormatting.GRAY),
                left,
                panelY + 88,
                0xFFFFFFFF,
                true
        );

        Component microsoftStatus;

        if (loginInProgress) {

            microsoftStatus =
                    Component.literal("● Signing in...")
                            .withStyle(ChatFormatting.YELLOW);

        } else if (microsoftSignedIn) {

            microsoftStatus =
                    Component.literal("● Signed in")
                            .withStyle(ChatFormatting.GREEN);

        } else {

            microsoftStatus =
                    Component.literal("● Not signed in")
                            .withStyle(ChatFormatting.RED);
        }

        graphics.text(
                this.font,
                microsoftStatus,
                left,
                panelY + 103,
                0xFFFFFFFF,
                true
        );

        if (!microsoftAccount.isEmpty()) {

            graphics.text(
                    this.font,
                    Component.literal("Account: ")
                            .withStyle(ChatFormatting.DARK_PURPLE)
                            .append(
                                    Component.literal(microsoftAccount)
                                            .withStyle(ChatFormatting.WHITE)
                            ),
                    left,
                    panelY + 118,
                    0xFFFFFFFF,
                    true
            );
        }

        graphics.text(
                this.font,
                Component.literal("Minecraft Profile")
                        .withStyle(ChatFormatting.GRAY),
                left,
                panelY + 145,
                0xFFFFFFFF,
                true
        );

        if (minecraftProfileLoaded) {

            graphics.text(
                    this.font,
                    Component.literal("● Profile available")
                            .withStyle(ChatFormatting.GREEN),
                    left,
                    panelY + 160,
                    0xFFFFFFFF,
                    true
            );

            graphics.text(
                    this.font,
                    Component.literal("IGN: ")
                            .withStyle(ChatFormatting.DARK_PURPLE)
                            .append(
                                    Component.literal(minecraftIgn)
                                            .withStyle(ChatFormatting.GREEN)
                            ),
                    left,
                    panelY + 175,
                    0xFFFFFFFF,
                    true
            );

            graphics.text(
                    this.font,
                    Component.literal("UUID: ")
                            .withStyle(ChatFormatting.DARK_PURPLE)
                            .append(
                                    Component.literal(minecraftUuid)
                                            .withStyle(ChatFormatting.WHITE)
                            ),
                    left,
                    panelY + 190,
                    0xFFFFFFFF,
                    true
            );

        } else if (microsoftSignedIn) {

            graphics.text(
                    this.font,
                    Component.literal("● Profile unavailable")
                            .withStyle(ChatFormatting.YELLOW),
                    left,
                    panelY + 160,
                    0xFFFFFFFF,
                    true
            );

        } else {

            graphics.text(
                    this.font,
                    Component.literal("● Not checked")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    left,
                    panelY + 160,
                    0xFFFFFFFF,
                    true
            );
        }

        ChatFormatting statusColor;

        if (minecraftProfileLoaded) {
            statusColor = ChatFormatting.GREEN;

        } else if (microsoftSignedIn) {
            statusColor = ChatFormatting.YELLOW;

        } else {
            statusColor = ChatFormatting.GRAY;
        }

        graphics.text(
                this.font,
                Component.literal(statusMessage)
                        .withStyle(statusColor),
                left,
                panelY + 185,
                0xFFFFFFFF,
                true
        );

        String skinTitle = "Current Skin";

        int skinAreaX =
                panelX + PANEL_WIDTH - 145;

        int skinTitleX =
                skinAreaX
                        + (100 - this.font.width(skinTitle)) / 2;

        graphics.text(
                this.font,
                Component.literal(skinTitle)
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                skinTitleX,
                panelY + 58,
                0xFFFFFFFF,
                true
        );

        int statusCardWidth = 180;

        int statusCardX =
                panelX + PANEL_WIDTH - statusCardWidth - 25;

        int statusCardY =
                panelY + 190;

        graphics.text(
                this.font,
                Component.literal("MICROSOFT")
                        .withStyle(ChatFormatting.GRAY),
                statusCardX + 8,
                statusCardY + 6,
                0xFFFFFFFF,
                true
        );

        Component microsoftConnectionText;

        if (loginInProgress) {

            microsoftConnectionText =
                    Component.literal("● Connecting")
                            .withStyle(ChatFormatting.YELLOW);

        } else if (microsoftSignedIn) {

            microsoftConnectionText =
                    Component.literal("● Connected")
                            .withStyle(ChatFormatting.GREEN);

        } else {

            microsoftConnectionText =
                    Component.literal("● Disconnected")
                            .withStyle(ChatFormatting.RED);
        }

        graphics.text(
                this.font,
                microsoftConnectionText,
                statusCardX + 72,
                statusCardY + 6,
                0xFFFFFFFF,
                true
        );

        graphics.text(
                this.font,
                Component.literal("MINECRAFT")
                        .withStyle(ChatFormatting.GRAY),
                statusCardX + 8,
                statusCardY + 34,
                0xFFFFFFFF,
                true
        );

        Component minecraftConnectionText;

        if (minecraftProfileLoaded) {

            minecraftConnectionText =
                    Component.literal("● Connected")
                            .withStyle(ChatFormatting.GREEN);

        } else if (AccountState.minecraftAuthorizationRejected) {

            minecraftConnectionText =
                    Component.literal("● App Not Authorized")
                            .withStyle(ChatFormatting.YELLOW);

        } else if (microsoftSignedIn) {

            minecraftConnectionText =
                    Component.literal("● Unavailable")
                            .withStyle(ChatFormatting.YELLOW);

        } else {

            minecraftConnectionText =
                    Component.literal("● Not Checked")
                            .withStyle(ChatFormatting.DARK_GRAY);
        }

        graphics.text(
                this.font,
                minecraftConnectionText,
                statusCardX + 60,
                statusCardY + 34,
                0xFFFFFFFF,
                true
        );

    }
}

