package dev.imb11.fog.mixin.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.imb11.fog.client.FogManager;
import dev.imb11.fog.client.registry.FogRegistry;
import dev.imb11.fog.client.resource.CustomFogDefinition;
import dev.imb11.fog.client.util.color.Color;
import dev.imb11.fog.client.util.math.HazeCalculator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Identifier;
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
public class WorldRendererMixin {
	@Shadow
	private @Nullable ClientWorld world;

	@Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("TAIL"))
	public void fog$renderSky(@NotNull MatrixStack matrixStack, @NotNull Matrix4f projectionMatrix, float deltaTick, @NotNull Camera camera, boolean isFoggy, @NotNull Runnable setupFog, @NotNull CallbackInfo ci) {
		// TODO: Check if Iris shaders active before rendering here
		if (world == null) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		FogManager fogManager = FogManager.getInstance();
		FogManager.FogSettings settings = fogManager.getFogSettings(deltaTick, client.options.getViewDistance().getValue());

		settings = HazeCalculator.applyHaze(settings, (int) this.world.getTimeOfDay());
		float fogColorR = settings.fogR();
		float fogColorG = settings.fogG();
		float fogColorB = settings.fogB();

		float darkness = fogManager.darkness.get(deltaTick);
		float undergroundFactor = 1 - MathHelper.lerp(darkness, fogManager.getUndergroundFactor(client, deltaTick), 1.0F);

		undergroundFactor *= undergroundFactor * undergroundFactor * undergroundFactor;

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		float timeOfDay = MathHelper.clamp(
				MathHelper.cos(this.world.getSkyAngle(deltaTick) * ((float) Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
		Vec3d samplePos = camera.getPos().subtract(2.0D, 2.0D, 2.0D).multiply(0.25D);
		Vec3d skyFogColor = CubicSampler.sampleColor(
				samplePos, (x, y, z) -> this.world.getDimensionEffects().adjustFogColor(
						Vec3d.unpackRgb(this.world.getBiomeAccess().getBiomeForNoiseGen(x, y, z).value().getFogColor()), timeOfDay));

		@NotNull final var bufferbuilder = Tessellator.getInstance().getBuffer();
		float radius = 5.0F;
		fog$renderCone(matrixStack, bufferbuilder, 32, true, radius, -30.0F,
				fogColorR, fogColorG, fogColorB, undergroundFactor,
				0.0F, (float) (fogColorR * skyFogColor.x), (float) (fogColorG * skyFogColor.y), (float) (fogColorB * skyFogColor.z),
				undergroundFactor
		);
		fog$renderCone(matrixStack, bufferbuilder, 32, false, radius, 30.0F,
				fogColorR, fogColorG, fogColorB, undergroundFactor * 0.2F,
				0.0F, (float) (fogColorR * skyFogColor.x), (float) (fogColorG * skyFogColor.y), (float) (fogColorB * skyFogColor.z),
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
