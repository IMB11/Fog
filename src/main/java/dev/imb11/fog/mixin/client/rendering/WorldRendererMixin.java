package dev.imb11.fog.mixin.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.imb11.fog.client.FogManager;
import dev.imb11.fog.client.util.math.HazeCalculator;
import dev.imb11.fog.client.util.math.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
	@Shadow
	private @Nullable ClientWorld world;

	@Inject(method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FDDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/VertexBuffer;draw(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/gl/ShaderProgram;)V", shift = At.Shift.BEFORE))
	public void fog$whiteClouds(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (this.world == null) {
			return;
		}

		// TODO: Put the 3 magic numbers into private static final @Unique fields
		float haze = (float) HazeCalculator.getHaze((int) this.world.getTimeOfDay());
		if (haze < 0.8f) {
			haze = 0.8f;
		}

		// Force clouds to be white
		RenderSystem.setShaderFogStart(10000F);
		RenderSystem.setShaderFogColor((haze + 0.5F), (haze + 0.5F), (haze + 0.5F));
	}

	@Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("TAIL"))
	public void fog$renderSky(@NotNull MatrixStack matrixStack, @NotNull Matrix4f projectionMatrix, float deltaTick, @NotNull Camera camera, boolean isFoggy, @NotNull Runnable setupFog, @NotNull CallbackInfo ci) {
		// TODO: Check if Iris shaders active before rendering here
		if (world == null) {
			return;
		}

		@NotNull var fogManager = FogManager.getInstance();
		@NotNull var client = MinecraftClient.getInstance();
		@NotNull var fogSettings = HazeCalculator.applyHaze(
				1f, fogManager.getFogSettings(deltaTick, client.options.getViewDistance().getValue()),
				(int) this.world.getTimeOfDay()
		);

		float fogColorRed = fogSettings.fogRed();
		float fogColorGreen = fogSettings.fogGreen();
		float fogColorBlue = fogSettings.fogBlue();
		float undergroundFactor = MathUtil.cube(fogManager.getUndergroundFactor(client, deltaTick));

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		@NotNull var bufferBuilder = Tessellator.getInstance().getBuffer();
		@NotNull Vec3d samplePosition = camera.getPos().subtract(2.0D, 2.0D, 2.0D).multiply(0.25D);
		float timeOfDay = MathHelper.clamp(
				MathHelper.cos(this.world.getSkyAngle(deltaTick) * ((float) Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
		@NotNull Vec3d skyFogColor = CubicSampler.sampleColor(
				samplePosition, (x, y, z) -> this.world.getDimensionEffects().adjustFogColor(
						Vec3d.unpackRgb(this.world.getBiomeAccess().getBiomeForNoiseGen(x, y, z).value().getFogColor()), timeOfDay));
		float radius = 5.0F;
		fog$renderCone(matrixStack, bufferBuilder, 32, true, radius, -30.0F,
				fogColorRed, fogColorGreen, fogColorBlue, undergroundFactor,
				0.0F, (float) (fogColorRed * skyFogColor.x), (float) (fogColorGreen * skyFogColor.y),
				(float) (fogColorBlue * skyFogColor.z),
				undergroundFactor
		);
		fog$renderCone(matrixStack, bufferBuilder, 32, false, radius, 30.0F,
				fogColorRed, fogColorGreen, fogColorBlue, undergroundFactor * 0.2F,
				0.0F, (float) (fogColorRed * skyFogColor.x), (float) (fogColorGreen * skyFogColor.y),
				(float) (fogColorBlue * skyFogColor.z),
				undergroundFactor
		);
		RenderSystem.depthMask(true);
	}

	@Unique
	private static void fog$renderCone(@NotNull MatrixStack poseStack, @NotNull BufferBuilder bufferBuilder, int resolution, boolean normal, float radius, float topVertexHeight, float topR, float topG, float topB, float topA, float bottomVertexHeight, float bottomR, float bottomG, float bottomB, float bottomA) {
		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

		@NotNull var positionMatrix = poseStack.peek().getPositionMatrix();
		bufferBuilder.vertex(positionMatrix, 0.0F, topVertexHeight, 0.0F).color(topR, topG, topB, topA).next();

		for (int vertex = 0; vertex <= resolution; ++vertex) {
			float angle = (float) vertex * ((float) Math.PI * 2F) / ((float) resolution);
			float x = MathHelper.sin(angle) * radius;
			float z = MathHelper.cos(angle) * radius;
			bufferBuilder.vertex(positionMatrix, x, bottomVertexHeight, normal ? z : -z).color(bottomR, bottomG, bottomB, bottomA).next();
		}

		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}
}
