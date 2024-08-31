package dev.imb11.fog.mixin.client.ui;

import dev.imb11.fog.client.util.UpdateWarningHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
	@Shadow
	private boolean doBackgroundFade;

	@Unique
	private static boolean hasShown = false;

	@Inject(at = @At("HEAD"), method = "render")
	public void showAfterLoaded(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (this.doBackgroundFade && !hasShown) {
			UpdateWarningHelper.checkVersion();
			hasShown = true;
		}
	}
}
