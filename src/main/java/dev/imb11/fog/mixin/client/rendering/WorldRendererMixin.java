package dev.imb11.fog.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.imb11.fog.client.util.math.CloudCalculator;

import dev.imb11.fog.config.FogConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/*? if >=1.20.6 {*/
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.math.Vec3d;
/*?} else {*/
/*import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.injection.Inject;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*//*?}*/

@Mixin(value = WorldRenderer.class)
public abstract class WorldRendererMixin {
	@Shadow
	private @Nullable ClientWorld world;

	/*? if <1.20.6 {*/
	/*@Inject(method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FDDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/VertexBuffer;draw(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/gl/ShaderProgram;)V", shift = At.Shift.BEFORE))
	public void fog$whiteClouds(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (this.world == null
			|| FogConfig.getInstance().disableMod
			|| FogConfig.getInstance().disableCloudWhitening
			|| !(world.getDimensionEffects() instanceof DimensionEffects.Overworld)
			|| world.getDimension().hasFixedTime()) {
			return;
		}

		// Force clouds to be white
		RenderSystem.setShaderFogStart(10000F);

		float color = CloudCalculator.getCloudColor(this.world.getTimeOfDay() % 24000);
		RenderSystem.setShaderFogColor(color, color, color);
	}
	*//*?} else {*/
	/*? if =1.20.6 {*/
	/*@ModifyArgs(method = "renderClouds(Lnet/minecraft/client/render/BufferBuilder;DDDLnet/minecraft/util/math/Vec3d;)Lnet/minecraft/client/render/BufferBuilder$BuiltBuffer;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;color(FFFF)Lnet/minecraft/client/render/VertexConsumer;"))
	public void fog$whiteClouds(Args args) {
		if (this.world == null
				|| FogConfig.getInstance().disableMod
				|| !(world.getDimensionEffects() instanceof DimensionEffects.Overworld)
				|| FogConfig.getInstance().disableCloudWhitening
				|| world.getDimension().hasFixedTime()) {
			return;
		}

		// TODO: Move constants to FogConfig
		float color = CloudCalculator.getCloudColor(this.world.getTimeOfDay() % 24000);
		args.set(0, color);
		args.set(1, color);
		args.set(2, color);
		args.set(3, 0.5F);
	}
	*//*?} else {*/
	@WrapOperation(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getCloudsColor(F)Lnet/minecraft/util/math/Vec3d;"))
	public @NotNull Vec3d fog$whiteClouds(ClientWorld instance, float tickDelta, @NotNull Operation<Vec3d> original) {
		if (this.world == null
				|| FogConfig.getInstance().disableMod
				|| !(this.world.getDimensionEffects() instanceof DimensionEffects.Overworld)
				|| FogConfig.getInstance().disableCloudWhitening
				|| this.world.getDimension().hasFixedTime()
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
				|| FogConfig.getInstance().disableMod
				|| !(this.world.getDimensionEffects() instanceof DimensionEffects.Overworld)
				|| FogConfig.getInstance().disableCloudWhitening
				|| this.world.getDimension().hasFixedTime()
		) {
			original.call();
			return;
		}

		// Force clouds to be white
		RenderSystem.setShaderFogStart(10000F);

		@NotNull var cloudsColor = this.world.getCloudsColor(tickDelta);
		RenderSystem.setShaderFogColor((float) cloudsColor.getX(), (float) cloudsColor.getY(), (float) cloudsColor.getZ());
	}
	/*?}*/

	@Inject(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/VertexBuffer;draw(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/gl/ShaderProgram;)V", shift = At.Shift.BEFORE))
	public void fog$whiteCloudsDisableFog(MatrixStack matrices, Matrix4f matrix4f, Matrix4f matrix4f2, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (this.world == null
				|| FogConfig.getInstance().disableMod
				|| !(world.getDimensionEffects() instanceof DimensionEffects.Overworld)
				|| FogConfig.getInstance().disableCloudWhitening
				|| world.getDimension().hasFixedTime()) {
			return;
		}

		// Force clouds to be white
		RenderSystem.setShaderFogStart(MinecraftClient.getInstance().options.getViewDistance().getValue());
	}
	/*?}*/
}
