package com.ahmet.accountmanager.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;

public class AccountManagerClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {

		ScreenEvents.AFTER_INIT.register(
				(client, screen, scaledWidth, scaledHeight) -> {

					ScreenEvents.afterExtract(screen).register(
							(currentScreen, graphics, mouseX, mouseY, tickProgress) -> {

								boolean showWatermark =
										currentScreen instanceof net.minecraft.client.gui.screens.TitleScreen
												|| currentScreen instanceof JoinMultiplayerScreen
												|| currentScreen instanceof AccountManagerScreen
												|| currentScreen instanceof ProfileManagementScreen
												|| currentScreen instanceof SkinManagementScreen
												|| currentScreen instanceof IgnEditorScreen;

								if (showWatermark) {

									WatermarkRenderer.render(
											graphics,
											currentScreen.getFont(),
											scaledWidth,
											scaledHeight
									);
								}

								if (currentScreen instanceof JoinMultiplayerScreen) {

									String username =
											client.getUser().getName();

									Component accountText =
											Component.literal("Logged in as: ")
													.withStyle(ChatFormatting.DARK_PURPLE)
													.append(
															Component.literal(username)
																	.withStyle(ChatFormatting.GREEN)
													);

									graphics.text(
											currentScreen.getFont(),
											accountText,
											150,
											12,
											0xFFFFFFFF,
											true
									);
								}
							}
					);
				}
		);
	}
}