package com.ahmet.accountmanager.client.mixin;

import com.ahmet.accountmanager.client.AccountManagerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin extends Screen {

	protected JoinMultiplayerScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void accountManager$addButton(CallbackInfo ci) {

		this.addRenderableWidget(
				Button.builder(
						Component.literal("Account Manager"),
						button -> {
							if (this.minecraft != null) {
								this.minecraft.setScreen(
										new AccountManagerScreen(
												(Screen) (Object) this
										)
								);
							}
						}
				).bounds(
						10,
						6,
						130,
						20
				).build()
		);
	}
}