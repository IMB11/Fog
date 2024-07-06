package dev.imb11.fog.mixin.client.rendering;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.imb11.fog.client.FogManager;
import dev.imb11.fog.client.util.math.HazeCalculator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
	@Shadow
	private static float red;
	@Shadow
	private static float green;
	@Shadow
	private static float blue;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V", remap = false, shift = At.Shift.BEFORE))
	private static void fog$modifyFogColors(@NotNull Camera camera, float tickDelta, @NotNull ClientWorld world, int viewDistance, float skyDarkness, @NotNull CallbackInfo ci) {
		@NotNull var fogSettings = FogManager.getInstance().getFogSettings(tickDelta, viewDistance);
		fogSettings = HazeCalculator.applyHaze(fogSettings, (int) world.getTimeOfDay());
		red = fogSettings.fogR();
		green = fogSettings.fogG();
		blue = fogSettings.fogB();
	}

	@Inject(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V", remap = false, shift = At.Shift.BEFORE))
	private static void fog$fogRenderEvent(@NotNull Camera camera, @NotNull BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float deltaTick, @NotNull CallbackInfo ci, @Local @NotNull BackgroundRenderer.FogData fogData) {
		if (camera.getSubmersionType() != CameraSubmersionType.NONE) {
			return;
		}

		@NotNull var fogSettings = FogManager.getInstance().getFogSettings(deltaTick, viewDistance);
		fogData.fogStart = (float) fogSettings.fogStart();
		fogData.fogEnd = (float) fogSettings.fogEnd();
		fogData.fogShape = FogShape.SPHERE;
	}

	/**
	 * Changes the color of the seam/transition in the sky.
	 */
	@Inject(method = "setFogBlack", at = @At("HEAD"))
	private static void fog$setFogBlackChangeClearColor(@NotNull CallbackInfo ci) {
		@NotNull final var client = MinecraftClient.getInstance();
		@Nullable final var clientWorld = client.world;
		if (clientWorld == null) {
			return;
		}

		@NotNull var fogSettings = FogManager.getInstance().getFogSettings(
				client.getTickDelta(),
				client.options.getViewDistance().getValue()
		);
		fogSettings = HazeCalculator.applyHaze(fogSettings, (int) clientWorld.getTimeOfDay());
		RenderSystem.clearColor(fogSettings.fogR(), fogSettings.fogG(), fogSettings.fogB(), 1.0F);
	}
}
