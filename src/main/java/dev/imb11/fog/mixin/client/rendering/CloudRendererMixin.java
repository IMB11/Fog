package dev.imb11.fog.mixin.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.imb11.fog.client.util.color.Color;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.minecraft.client.render.CloudRenderer")
public class CloudRendererMixin {
	//? if >=1.21.2 {
	@Unique
	private static @Nullable Fog fog$PREVIOUS_FOG = null;

	@Inject(method = "renderClouds(ILnet/minecraft/client/option/CloudRenderMode;FLorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/util/math/Vec3d;F)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V", ordinal = 0), cancellable = true)
	public void fog$whitenClouds(int color, CloudRenderMode cloudRenderMode, float cloudHeight, Matrix4f positionMatrix, Matrix4f projectionMatrix, Vec3d cameraPos, float ticks, CallbackInfo ci) {
		@NotNull var client = MinecraftClient.getInstance();
		@Nullable var world = client.world;

		if (world == null
				|| FogConfig.getInstance().disableMod
				|| !(world.getDimensionEffects() instanceof DimensionEffects.Overworld)
				|| FogConfig.getInstance().disableCloudWhitening
				|| world.getDimension().hasFixedTime()
		) {
			return;
		}

		ci.cancel();

		// Force clouds to be white
		fog$PREVIOUS_FOG = RenderSystem.getShaderFog();
		var cloudsColor = world.getCloudsColor(ticks);
		//noinspection DataFlowIssue
		RenderSystem.setShaderFog(fog$applyFogChanges(fog$PREVIOUS_FOG, cloudsColor));
	}

	@Unique
	private @NotNull Fog fog$applyFogChanges(@NotNull Fog fog, int cloudsColor) {
		// (float start, float end, FogShape shape, float red, float green, float blue, float alpha)
		@NotNull var color = new Color(cloudsColor);
		return new Fog(fog.start(), fog.end(), fog.shape(), color.red / 255f, color.green / 255f, color.blue / 255f, fog.alpha());
	}

	@Inject(method = "renderClouds(ILnet/minecraft/client/option/CloudRenderMode;FLorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/util/math/Vec3d;F)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V", ordinal = 1))
	public void fog$resetCloudWhitening(int color, CloudRenderMode cloudRenderMode, float cloudHeight, Matrix4f positionMatrix, Matrix4f projectionMatrix, Vec3d cameraPos, float ticks, CallbackInfo ci) {
		if (fog$PREVIOUS_FOG == null) {
			return;
		}

		RenderSystem.setShaderFog(fog$PREVIOUS_FOG);
		fog$PREVIOUS_FOG = null;
	}
	//?}
}
