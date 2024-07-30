package dev.imb11.fog.mixin.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.imb11.fog.client.util.math.HazeCalculator;

import dev.imb11.fog.config.FogConfig;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
	@Shadow
	private @Nullable ClientWorld world;

	@Unique
	private static final float HAZE_COLOR_ADDITION = 0.5F;

	@Inject(method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FDDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/VertexBuffer;draw(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/gl/ShaderProgram;)V", shift = At.Shift.BEFORE))
	public void fog$whiteClouds(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (this.world == null || FogConfig.getInstance().disableMod || (world.getDimensionEffects() instanceof DimensionEffects.Nether && FogConfig.getInstance().disableNether)) {
			return;
		}

		// Force clouds to be white
		RenderSystem.setShaderFogStart(10000F);

		if (world.getDimension().hasFixedTime() && world.getDimensionEffects() instanceof DimensionEffects.End) {
			return;
		}

		float haze = (float) HazeCalculator.getHaze((int) this.world.getTimeOfDay());
		RenderSystem.setShaderFogColor(haze + HAZE_COLOR_ADDITION, haze + HAZE_COLOR_ADDITION, haze + HAZE_COLOR_ADDITION);
	}
}
