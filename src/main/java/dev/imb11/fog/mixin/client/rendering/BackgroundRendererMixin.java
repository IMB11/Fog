package dev.imb11.fog.mixin.client.rendering;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.imb11.fog.client.FogManager;
import dev.imb11.fog.client.util.math.EnvironmentCalculations;
import dev.imb11.fog.config.FogConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.block.enums.CameraSubmersionType;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
	//? if <1.21.3 {
	/*@Shadow
	private static float red;
	@Shadow
	private static float green;
	@Shadow
	private static float blue;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V", remap = false, shift = At.Shift.BEFORE))
	private static void fog$modifyFogColors(@NotNull Camera camera, float tickDelta, @NotNull ClientWorld world, int viewDistance, float skyDarkness, @NotNull CallbackInfo ci) {
		if (FogConfig.getInstance().disableMod
				|| FogConfig.getInstance().disabledDimensions.contains(world.getRegistryKey().getValue().toString())
				|| camera.getSubmersionType() != CameraSubmersionType.NONE
		) {
			return;
		}

		@NotNull var fogManager = FogManager.getInstance();
		@NotNull var fogSettings = fogManager.getFogSettings(tickDelta, viewDistance);
		if (!world.getDimension().hasFixedTime() || !(world.getDimensionEffects() instanceof DimensionEffects.End)) {
			fogSettings = EnvironmentCalculations.apply(
					fogManager.getUndergroundFactor(MinecraftClient.getInstance(), tickDelta), fogSettings, tickDelta
			);
		}

		red = fogSettings.fogRed();
		green = fogSettings.fogGreen();
		blue = fogSettings.fogBlue();
	}

	@Inject(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V", remap = false, shift = At.Shift.BEFORE))
	private static void fog$fogRenderEvent(@NotNull Camera camera, @NotNull BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float deltaTick, @NotNull CallbackInfo ci, @Local @NotNull BackgroundRenderer.FogData fogData) {
		@Nullable final var clientWorld = MinecraftClient.getInstance().world;
		if (clientWorld == null
				|| FogConfig.getInstance().disableMod
				|| FogConfig.getInstance().disabledDimensions.contains(clientWorld.getRegistryKey().getValue().toString())
				|| camera.getSubmersionType() != CameraSubmersionType.NONE
		) {
			return;
		}

		@NotNull var fogSettings = FogManager.getInstance().getFogSettings(deltaTick, viewDistance);
		fogData.fogStart = (float) fogSettings.fogStart();
		fogData.fogEnd = (float) fogSettings.fogEnd();
		fogData.fogShape = FogShape.SPHERE;
	}

	/^*
	 * Changes the color of the seam/transition in the sky.
	 ^/
	@WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"))
	private static boolean fog$setFogBlackChangeClearColor(float red, float green, float blue, float alpha) {
		if (FogConfig.getInstance().disableMod) {
			return true;
		}

		@NotNull final var client = MinecraftClient.getInstance();
		@Nullable final var clientWorld = client.world;
		@Nullable final var camera = client.gameRenderer.getCamera();
		if (clientWorld == null || camera == null
				|| FogConfig.getInstance().disabledDimensions.contains(clientWorld.getRegistryKey().getValue().toString())
				|| camera.getSubmersionType() != CameraSubmersionType.NONE
		) {
			return true;
		}

		float tickDelta = client.getRenderTickCounter().getTickDelta(true);
		@NotNull var fogManager = FogManager.getInstance();
		@NotNull var fogSettings = fogManager.getFogSettings(
				tickDelta,
				client.options.getViewDistance().getValue()
		);
		if (!clientWorld.getDimension().hasFixedTime() || !(clientWorld.getDimensionEffects() instanceof DimensionEffects.End)) {
			fogSettings = EnvironmentCalculations.apply(
					fogManager.getUndergroundFactor(client, tickDelta), fogSettings, tickDelta);
		}
		RenderSystem.clearColor(fogSettings.fogRed(), fogSettings.fogGreen(), fogSettings.fogBlue(), 1.0F);
		return false;
	}
	*///?} else {
	@Inject(method = "applyFog", at = @At(value = "TAIL"), cancellable = true)
	private static void $apply_fog_modifications(Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
		var client = MinecraftClient.getInstance();
		var world = client.world;

		if (world == null
				|| FogConfig.getInstance().disableMod
				|| FogConfig.getInstance().disabledDimensions.contains(world.getRegistryKey().getValue().toString())
				|| camera.getSubmersionType() != CameraSubmersionType.NONE
		) {
			return;
		}

		var fogManager = FogManager.getInstance();
		var fogSettings = fogManager.getFogSettings(tickDelta, viewDistance);

		if (!world.getDimension().hasFixedTime() || !(world.getDimensionEffects() instanceof DimensionEffects.End)) {
			fogSettings = EnvironmentCalculations.apply(
					fogManager.getUndergroundFactor(client, tickDelta),
					fogSettings,
					tickDelta
			);
		}

		Fog customFog = new Fog(
				(float) fogSettings.fogStart(),
				(float) fogSettings.fogEnd(),
				FogShape.SPHERE,
				fogSettings.fogRed(),
				fogSettings.fogGreen(),
				fogSettings.fogBlue(),
				1.0F
		);

//		cir.setReturnValue(customFog);
	}
	//?}
}
