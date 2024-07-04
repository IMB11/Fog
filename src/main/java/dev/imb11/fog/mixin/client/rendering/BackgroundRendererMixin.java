package dev.imb11.fog.mixin.client.rendering;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.imb11.fog.FogManager;
import dev.imb11.fog.HazeCalculator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
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
	private static void modifyFogColors(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
		FogManager fogManager = FogManager.getInstance();
		FogManager.FogSettings settings = fogManager.getFogSettings(tickDelta, viewDistance);
		settings = HazeCalculator.applyHaze(settings, (int) world.getTimeOfDay());
		red = settings.fogR();
		green = settings.fogG();
		blue = settings.fogB();
	}

	@Inject(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V", remap = false, shift = At.Shift.BEFORE))
	private static void fogRenderEvent(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float deltaTick, CallbackInfo ci, @Local BackgroundRenderer.FogData fogData) {
		FogManager fogManager = FogManager.getInstance();
		FogManager.FogSettings settings = fogManager.getFogSettings(deltaTick, viewDistance);

		if (camera.getSubmersionType() == CameraSubmersionType.NONE) {
			fogData.fogStart = (float) settings.fogStart();
			fogData.fogEnd = (float) settings.fogEnd();
			fogData.fogShape = FogShape.SPHERE;
		}
	}

	// Changes the color of the seam/transition in the sky
	@Inject(method = "setFogBlack", at = @At("HEAD"))
	private static void setFogBlackInject(CallbackInfo ci) {
		FogManager fogManager = FogManager.getInstance();
		MinecraftClient client = MinecraftClient.getInstance();
		float deltaTick = client.getTickDelta();
		int viewDistance = client.options.getViewDistance().getValue();
		FogManager.FogSettings settings = fogManager.getFogSettings(deltaTick, viewDistance);
		settings = HazeCalculator.applyHaze(settings, (int) client.world.getTimeOfDay());
		RenderSystem.clearColor(settings.fogR(), settings.fogG(), settings.fogB(), 1.0F);
	}
}
