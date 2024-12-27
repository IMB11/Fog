package dev.imb11.fog.mixin.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.imb11.fog.client.FogManager;
import dev.imb11.fog.client.util.math.CloudCalculator;
import dev.imb11.fog.config.FogConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.Fog;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.minecraft.client.render.CloudRenderer")
public class CloudRendererMixin {
	//? if >=1.21.2 {
	@Inject(method = "renderClouds(ILnet/minecraft/client/option/CloudRenderMode;FLorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/util/math/Vec3d;F)V", at = @At(value = "HEAD"))
	private void fog$whiteClouds(int color, CloudRenderMode cloudRenderMode, float cloudHeight, Matrix4f positionMatrix, Matrix4f projectionMatrix, Vec3d cameraPos, float ticks, CallbackInfo ci) {
		@NotNull var client = MinecraftClient.getInstance();
		@Nullable var camera = client.gameRenderer.getCamera();
		if (camera == null || client.world == null || FogConfig.getInstance().disableMod
				|| !(client.world.getDimensionEffects() instanceof DimensionEffects.Overworld)
				|| FogConfig.getInstance().disableCloudWhitening
				|| client.world.getDimension().hasFixedTime() || FogManager.isInDisabledBiome()) {
			return;
		}

		RenderSystem.setShaderFog(Fog.DUMMY);
	}
	//?}
}
