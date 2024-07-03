package dev.imb11.fog.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.imb11.fog.BiomeColourEntry;
import dev.imb11.fog.FogManager;
import dev.imb11.fog.HazeCalculator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.source.BiomeAccess;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
//    @ModifyVariable(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/world/ClientWorld;getSkyColor(Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;"))
//    @SuppressWarnings("InvalidInjectorMethodSignature")
//    public Vec3d preventHorizonBreak(Vec3d original) {
//        MinecraftClient client = MinecraftClient.getInstance();
//        ClientWorld world = client.world;
//        FogManager fogManager = FogManager.getInstance();
//        FogManager.FogSettings settings = fogManager.getFogSettings(client.getTickDelta(), client.options.getViewDistance().getValue());
//        double hazeValue = HazeCalculator.getHaze((int) world.getTimeOfDay());
//        BiomeColourEntry defaultEntry = new BiomeColourEntry(Identifier.of("default", "default"), 0.68f, 0.83f, 1f);
//        float fogColorR = (float) MathHelper.lerp(hazeValue, defaultEntry.fogR(), settings.fogR());
//        float fogColorG = (float) MathHelper.lerp(hazeValue, defaultEntry.fogG(), settings.fogG());
//        float fogColorB = (float) MathHelper.lerp(hazeValue, defaultEntry.fogB(), settings.fogB());
//        return original;
//    }

    @Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("TAIL"))
    public void renderSky(MatrixStack matrixStack, Matrix4f projectionMatrix, float deltaTick, Camera camera, boolean isFoggy, Runnable setupFog, CallbackInfo ci) {
        // TODO: Check if Iris shaders active before rendering here.
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        FogManager fogManager = FogManager.getInstance();

        FogManager.FogSettings settings = fogManager.getFogSettings(deltaTick, client.options.getViewDistance().getValue());

        double hazeValue = HazeCalculator.getHaze((int) world.getTimeOfDay());
        BiomeColourEntry defaultEntry = new BiomeColourEntry(Identifier.of("default", "default"), 0.68f, 0.83f, 1f);
        float fogColorR = (float) MathHelper.lerp(hazeValue, defaultEntry.fogR(), settings.fogR());
        float fogColorG = (float) MathHelper.lerp(hazeValue, defaultEntry.fogG(), settings.fogG());
        float fogColorB = (float) MathHelper.lerp(hazeValue, defaultEntry.fogB(), settings.fogB());

        float darkness = fogManager.darkness.get(deltaTick);
        float undergroundFactor = 1 - MathHelper.lerp(darkness, fogManager.getUndergroundFactor(client, deltaTick), 1.0F);

        undergroundFactor *= undergroundFactor * undergroundFactor * undergroundFactor;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        float timeOfDay = MathHelper.clamp(MathHelper.cos(world.getSkyAngle(deltaTick) * ((float)Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
        BiomeAccess biomemanager = world.getBiomeAccess();
        Vec3d samplePos = camera.getPos().subtract(2.0D, 2.0D, 2.0D).multiply(0.25D);
        Vec3d skyFogColor = CubicSampler.sampleColor(samplePos, (x, y, z) -> world.getDimensionEffects().adjustFogColor(Vec3d.unpackRgb(biomemanager.getBiomeForNoiseGen(x, y, z).value().getFogColor()), timeOfDay));

        float radius = 5.0F;
        renderCone(matrixStack, bufferbuilder, 32, true, radius, -30.0F,
                fogColorR, fogColorG, fogColorB, undergroundFactor,
                0.0F, (float) (fogColorR * skyFogColor.x), (float) (fogColorG * skyFogColor.y), (float) (fogColorB * skyFogColor.z), undergroundFactor);
        renderCone(matrixStack, bufferbuilder, 32, false, radius, 30.0F,
                fogColorR, fogColorG, fogColorB, undergroundFactor * 0.2F,
                0.0F, (float) (fogColorR * skyFogColor.x), (float) (fogColorG * skyFogColor.y), (float) (fogColorB * skyFogColor.z), undergroundFactor);

        RenderSystem.depthMask(true);
    }

    @Unique
    private static void renderCone(MatrixStack poseStack, BufferBuilder bufferBuilder, int resolution, boolean normal, float radius, float topVertexHeight, float topR, float topG, float topB, float topA, float bottomVertexHeight, float bottomR, float bottomG, float bottomB, float bottomA) {
        Matrix4f matrix = poseStack.peek().getPositionMatrix();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, 0.0F, topVertexHeight, 0.0F).color(topR, topG, topB, topA).next();

        for(int vertex = 0; vertex <= resolution; ++vertex) {
            float angle = (float)vertex * ((float)Math.PI * 2F) / ((float)resolution);
            float x = MathHelper.sin(angle) * radius;
            float z = MathHelper.cos(angle) * radius;

            bufferBuilder.vertex(matrix, x, bottomVertexHeight, normal ? z : -z).color(bottomR, bottomG, bottomB, bottomA).next();
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
}
