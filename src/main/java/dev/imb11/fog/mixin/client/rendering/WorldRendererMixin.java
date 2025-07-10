package dev.imb11.fog.mixin.client.rendering;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.render.*;

//? if <1.21.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.imb11.fog.client.FogManager;
import dev.imb11.fog.client.util.math.CloudCalculator;
import dev.imb11.fog.client.compat.polytone.IrisCompat;
import dev.imb11.fog.config.FogConfig;

import net.minecraft.client.MinecraftClient;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.math.Vec3d;

*///?}

@Mixin(value = WorldRenderer.class)
public abstract class WorldRendererMixin {
	//? if <1.21.2 {
	/*@Shadow
	private @Nullable ClientWorld world;

	@WrapOperation(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getCloudsColor(F)Lnet/minecraft/util/math/Vec3d;"))
	public @NotNull Vec3d fog$whiteClouds(ClientWorld instance, float tickDelta, @NotNull Operation<Vec3d> original) {
		if (this.world == null
				|| !FogConfig.getInstance().enableMod
				|| !(this.world.getDimensionEffects() instanceof DimensionEffects.Overworld)
				|| !FogConfig.getInstance().enableCloudWhitening
				|| FogManager.isInDisabledBiome()
				|| this.world.getDimension().hasFixedTime()
				|| IrisCompat.shouldDisableMod()
		) {
			return original.call(this.world, tickDelta);
		}

		// TODO: Move constants to FogConfig
		float color = CloudCalculator.getCloudColor(this.world.getTimeOfDay() % 24000);
		return new Vec3d(color, color, color);
	}

	@WrapOperation(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;applyFogColor()V"))
	public void fog$removeFogFromClouds(@NotNull Operation<Void> original, @Local(argsOnly = true) float tickDelta) {
		if (this.world == null
				|| !FogConfig.getInstance().enableMod
				|| !(this.world.getDimensionEffects() instanceof DimensionEffects.Overworld)
				|| !FogConfig.getInstance().enableCloudWhitening
				|| FogManager.isInDisabledBiome()
				|| this.world.getDimension().hasFixedTime()
				|| IrisCompat.shouldDisableMod()
		) {
			original.call();
			return;
		}

		// Force clouds to be white
		RenderSystem.setShaderFogStart(10000F);

		@NotNull var cloudsColor = this.world.getCloudsColor(tickDelta);
		RenderSystem.setShaderFogColor((float) cloudsColor.getX(), (float) cloudsColor.getY(), (float) cloudsColor.getZ());
	}

	@Inject(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/VertexBuffer;draw(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/gl/ShaderProgram;)V", shift = At.Shift.BEFORE))
	public void fog$whiteCloudsDisableFog(MatrixStack matrices, Matrix4f matrix4f, Matrix4f matrix4f2, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (this.world == null
				|| !FogConfig.getInstance().enableMod
				|| !(world.getDimensionEffects() instanceof DimensionEffects.Overworld)
				|| !FogConfig.getInstance().enableCloudWhitening
				|| FogManager.isInDisabledBiome()
				|| world.getDimension().hasFixedTime()
				|| IrisCompat.shouldDisableMod()) {
			return;
		}

		// Force clouds to be white
		RenderSystem.setShaderFogStart(MinecraftClient.getInstance().options.getViewDistance().getValue());
	}
	*///?}
}
