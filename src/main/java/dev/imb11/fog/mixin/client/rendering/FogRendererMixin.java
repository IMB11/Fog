package dev.imb11.fog.mixin.client.rendering;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

//? if >=1.21.6 {
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.imb11.fog.client.util.TickUtil;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import dev.imb11.fog.client.FogManager;
import dev.imb11.fog.client.compat.polytone.IrisCompat;
import dev.imb11.fog.client.util.math.EnvironmentCalculations;
import dev.imb11.fog.config.FogConfig;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DimensionEffects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//?}

@Pseudo
@Mixin(targets = "net.minecraft.client.render.fog.FogRenderer")
public class FogRendererMixin {
	//? if >=1.21.6 {
	@Inject(method = "applyFog(Lnet/minecraft/client/render/Camera;IZLnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/fog/FogRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"))
	private void fog$modifyFog(Camera camera, int viewDistance, boolean thick, RenderTickCounter tickCounter, float skyDarkness, @Nullable ClientWorld world, CallbackInfoReturnable<Vector4f> cir, @Local @NotNull LocalRef<FogData> fogData) {
		@NotNull var client = MinecraftClient.getInstance();
		if (world == null
				|| FogConfig.getInstance().disableMod
				|| FogManager.isInDisabledBiome()
				|| camera.getSubmersionType() != CameraSubmersionType.NONE
				|| IrisCompat.shouldDisableMod()
		) {
			return;
		}

		var tickDelta = TickUtil.getTickDelta();
		@NotNull var fogManager = FogManager.getInstance();
		@NotNull var fogSettings = fogManager.getFogSettings(tickDelta, viewDistance);
		if (!world.getDimension().hasFixedTime() || !(world.getDimensionEffects() instanceof DimensionEffects.End)) {
			fogSettings = EnvironmentCalculations.apply(fogManager.getUndergroundFactor(client, tickDelta), fogSettings, tickDelta);
		}

		@NotNull var customFogData = new FogData();
		customFogData.environmentalStart = fogData.get().environmentalStart;
		customFogData.renderDistanceStart = (float) fogSettings.fogStart();
		customFogData.environmentalEnd = fogData.get().environmentalEnd;
		customFogData.renderDistanceEnd = (float) fogSettings.fogEnd();
		customFogData.skyEnd = fogData.get().skyEnd;
		customFogData.cloudEnd = fogData.get().cloudEnd;
		fogData.set(customFogData);
	}

	@Inject(method = "getFogColor", at = @At(value = "HEAD"), cancellable = true)
	private void fog$modifyFogColor(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, boolean thick, @NotNull CallbackInfoReturnable<Vector4f> cir) {
		if (world == null
				|| FogConfig.getInstance().disableMod
				|| FogManager.isInDisabledBiome()
				|| camera.getSubmersionType() != CameraSubmersionType.NONE
				|| IrisCompat.shouldDisableMod()
		) {
			return;
		}

		@NotNull var client = MinecraftClient.getInstance();
		@NotNull var fogManager = FogManager.getInstance();
		@NotNull var fogSettings = fogManager.getFogSettings(tickDelta, viewDistance);
		if (!world.getDimension().hasFixedTime() || !(world.getDimensionEffects() instanceof DimensionEffects.End)) {
			fogSettings = EnvironmentCalculations.apply(fogManager.getUndergroundFactor(client, tickDelta), fogSettings, tickDelta);
		}

		cir.setReturnValue(new Vector4f(fogSettings.fogRed(), fogSettings.fogGreen(), fogSettings.fogBlue(), 1.0F));
	}
	//?}
}
