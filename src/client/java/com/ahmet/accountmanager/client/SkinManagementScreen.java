package com.ahmet.accountmanager.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

import java.io.InputStream;
import java.nio.file.Files;

import com.ahmet.accountmanager.client.auth.MinecraftProfileService;
import com.ahmet.accountmanager.client.auth.MicrosoftAuthService;

import java.io.File;

public class SkinManagementScreen extends Screen {

    private static final int PANEL_WIDTH = 500;
    private static final int PANEL_HEIGHT = 300;

    private static final Identifier PREVIEW_TEXTURE_ID =
            Identifier.fromNamespaceAndPath(
                    "accountmanager",
                    "skin_preview"
            );

    private final Screen parent;

    private File selectedSkinFile;
    private boolean slimModel = false;

    private String selectedFileName = "No skin selected.";
    private String skinDimensions = "";
    private String statusMessage = "Choose a 64x64 PNG skin.";

    private PurpleButton modelButton;
    private PurpleButton uploadButton;

    private PlayerSkinWidget currentSkinWidget;
    private PlayerSkin previewSkin;

    public SkinManagementScreen(Screen parent) {
        super(Component.literal("Skin Management"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        int leftX = panelX + 30;
        int buttonWidth = 270;

        this.addRenderableWidget(
                new PurpleButton(
                        leftX,
                        panelY + 145,
                        buttonWidth,
                        20,
                        Component.literal("Choose Skin PNG"),
                        this.font,
                        this::chooseSkinFile
                )
        );

        modelButton =
                new PurpleButton(
                        leftX,
                        panelY + 175,
                        buttonWidth,
                        20,
                        Component.literal("Model: Classic"),
                        this.font,
                        this::toggleModel
                );

        this.addRenderableWidget(modelButton);

        uploadButton =
                new PurpleButton(
                        leftX,
                        panelY + 205,
                        buttonWidth,
                        20,
                        Component.literal("Upload Skin"),
                        this.font,
                        this::uploadSkin
                );

        updateUploadButton();

        this.addRenderableWidget(uploadButton);

        this.addRenderableWidget(
                new PurpleButton(
                        leftX,
                        panelY + 235,
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

        if (this.minecraft != null) {

            var normalSkin =
                    this.minecraft.getSkinManager().createLookup(
                            this.minecraft.getGameProfile(),
                            false
                    );

            currentSkinWidget =
                    new PlayerSkinWidget(
                            120,
                            150,
                            this.minecraft.getEntityModels(),
                            () -> {
                                if (previewSkin != null) {
                                    return previewSkin;
                                }

                                return normalSkin.get();
                            }
                    );

            currentSkinWidget.setX(
                    panelX + PANEL_WIDTH - 160
            );

            currentSkinWidget.setY(
                    panelY + 70
            );

            this.addRenderableWidget(currentSkinWidget);
        }
    }

    private void chooseSkinFile() {

        statusMessage = "Opening file picker...";

        Thread.ofVirtual().start(() -> {

            try (MemoryStack stack = MemoryStack.stackPush()) {

                PointerBuffer filters = stack.mallocPointer(1);

                filters.put(
                        stack.UTF8("*.png")
                );

                filters.flip();

                String selectedPath =
                        TinyFileDialogs.tinyfd_openFileDialog(
                                "Select Minecraft Skin",
                                "",
                                filters,
                                "PNG Skin (*.png)",
                                false
                        );

                if (selectedPath == null) {

                    if (this.minecraft != null) {
                        this.minecraft.execute(() ->
                                statusMessage =
                                        "Skin selection cancelled."
                        );
                    }

                    return;
                }

                File file =
                        new File(selectedPath);

                validateSkin(file);

            } catch (Exception exception) {

                if (this.minecraft != null) {
                    this.minecraft.execute(() ->
                            statusMessage =
                                    "Could not open file picker."
                    );
                }
            }
        });
    }

    private void validateSkin(File file) {

        try {

            BufferedImage image =
                    ImageIO.read(file);

            if (image == null) {
                setSkinError(
                        "This file is not a valid PNG image."
                );
                return;
            }

            int width = image.getWidth();
            int height = image.getHeight();

            if (width != 64 || height != 64) {

                setSkinError(
                        "Skin must be exactly 64x64 pixels."
                );

                return;
            }

            if (this.minecraft != null) {

                this.minecraft.execute(() -> {

                    selectedSkinFile = file;

                    selectedFileName =
                            file.getName();

                    skinDimensions =
                            width + "x" + height;

                    statusMessage =
                            "Loading skin preview...";

                    loadSkinPreview(file);

                    updateUploadButton();
                });
            }

        } catch (Exception exception) {

            setSkinError(
                    "Could not read the selected skin."
            );
        }
    }

    private void loadSkinPreview(File file) {

        if (this.minecraft == null) {
            return;
        }

        try (InputStream input =
                     Files.newInputStream(file.toPath())) {

            NativeImage image =
                    NativeImage.read(input);

            if (image.getWidth() != 64
                    || image.getHeight() != 64) {

                image.close();

                statusMessage =
                        "Preview failed: skin must be 64x64.";

                return;
            }

            this.minecraft
                    .getTextureManager()
                    .release(PREVIEW_TEXTURE_ID);

            DynamicTexture texture =
                    new DynamicTexture(
                            () -> "Account Manager Skin Preview",
                            image
                    );

            this.minecraft
                    .getTextureManager()
                    .register(
                            PREVIEW_TEXTURE_ID,
                            texture
                    );

            texture.upload();

            ClientAsset.Texture bodyTexture =
                    new ClientAsset.ResourceTexture(
                            PREVIEW_TEXTURE_ID,
                            PREVIEW_TEXTURE_ID
                    );

            previewSkin =
                    PlayerSkin.insecure(
                            bodyTexture,
                            null,
                            null,
                            slimModel
                                    ? PlayerModelType.SLIM
                                    : PlayerModelType.WIDE
                    );

            statusMessage =
                    "Skin PNG is valid - preview loaded.";

        } catch (Exception exception) {

            previewSkin = null;

            statusMessage =
                    "Could not load skin preview.";
        }
    }

    private void setSkinError(String message) {

        if (this.minecraft == null) {
            return;
        }

        this.minecraft.execute(() -> {

            selectedSkinFile = null;

            selectedFileName =
                    "No valid skin selected.";

            skinDimensions = "";

            statusMessage = message;

            updateUploadButton();
        });
    }

    private void toggleModel() {

        slimModel = !slimModel;

        if (modelButton != null) {

            modelButton.setMessage(
                    Component.literal(
                            slimModel
                                    ? "Model: Slim"
                                    : "Model: Classic"
                    )
            );
        }

        if (previewSkin != null) {

            previewSkin =
                    PlayerSkin.insecure(
                            previewSkin.body(),
                            null,
                            null,
                            slimModel
                                    ? PlayerModelType.SLIM
                                    : PlayerModelType.WIDE
                    );
        }
    }

    private void updateUploadButton() {

        if (uploadButton == null) {
            return;
        }

        uploadButton.active =
                selectedSkinFile != null
                        && AccountState.minecraftProfileLoaded
                        && MinecraftProfileService.hasActiveMinecraftSession();
    }

    private void uploadSkin() {

        if (selectedSkinFile == null) {
            statusMessage = "Select a valid skin first.";
            return;
        }

        if (!AccountState.minecraftProfileLoaded
                || !MinecraftProfileService.hasActiveMinecraftSession()) {

            statusMessage =
                    "Minecraft profile access is required.";

            return;
        }

        File skinFile = selectedSkinFile;
        boolean useSlimModel = slimModel;

        uploadButton.active = false;
        statusMessage = "Uploading skin...";

        Thread.ofVirtual().start(() -> {

            try {

                MinecraftProfileService.uploadSkin(
                        skinFile,
                        useSlimModel
                );

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
                                        : AccountState.minecraftIgn;

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
                                    "Skin uploaded and profile refreshed.";

                        } else {

                            statusMessage =
                                    "Skin uploaded, but profile refresh failed.";
                        }

                        updateUploadButton();
                    });
                }

            } catch (Exception exception) {

                if (this.minecraft != null) {
                    this.minecraft.execute(() -> {

                        statusMessage =
                                "Skin upload failed.";

                        updateUploadButton();
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

        int panelX =
                (this.width - PANEL_WIDTH) / 2;

        int panelY =
                (this.height - PANEL_HEIGHT) / 2;

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

        int panelX =
                (this.width - PANEL_WIDTH) / 2;

        int panelY =
                (this.height - PANEL_HEIGHT) / 2;

        PanelBrandRenderer.render(
                graphics,
                this.font,
                panelX,
                panelY
        );

        int left =
                panelX + 30;

        String title =
                "SKIN MANAGEMENT";

        graphics.text(
                this.font,
                Component.literal(title)
                        .withStyle(
                                ChatFormatting.LIGHT_PURPLE
                        ),
                panelX
                        + (PANEL_WIDTH
                        - this.font.width(title)) / 2,
                panelY + 16,
                0xFFFFFFFF,
                true
        );

        graphics.text(
                this.font,
                Component.literal("Selected Skin")
                        .withStyle(ChatFormatting.GRAY),
                left,
                panelY + 58,
                0xFFFFFFFF,
                true
        );

        graphics.text(
                this.font,
                Component.literal(selectedFileName)
                        .withStyle(
                                selectedSkinFile != null
                                        ? ChatFormatting.GREEN
                                        : ChatFormatting.DARK_GRAY
                        ),
                left,
                panelY + 76,
                0xFFFFFFFF,
                true
        );

        if (!skinDimensions.isEmpty()) {

            graphics.text(
                    this.font,
                    Component.literal(
                            "Size: " + skinDimensions
                    ).withStyle(ChatFormatting.GRAY),
                    left,
                    panelY + 94,
                    0xFFFFFFFF,
                    true
            );
        }

        ChatFormatting statusColor;

        if (selectedSkinFile != null) {
            statusColor = ChatFormatting.GREEN;
        } else {
            statusColor = ChatFormatting.YELLOW;
        }

        graphics.text(
                this.font,
                Component.literal(statusMessage)
                        .withStyle(statusColor),
                left,
                panelY + 118,
                0xFFFFFFFF,
                true
        );

        String previewTitle =
                "Current Skin";

        int previewX =
                panelX + PANEL_WIDTH - 160;

        graphics.text(
                this.font,
                Component.literal(previewTitle)
                        .withStyle(
                                ChatFormatting.LIGHT_PURPLE
                        ),
                previewX
                        + (120
                        - this.font.width(previewTitle)) / 2,
                panelY + 52,
                0xFFFFFFFF,
                true
        );

        if (!AccountState.minecraftProfileLoaded) {

            graphics.text(
                    this.font,
                    Component.literal(
                            "Upload disabled: profile unavailable"
                    ).withStyle(ChatFormatting.DARK_GRAY),
                    left,
                    panelY + 270,
                    0xFFFFFFFF,
                    true
            );
        }
    }
}
