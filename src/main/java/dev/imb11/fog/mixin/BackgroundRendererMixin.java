package dev.imb11.fog.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.imb11.fog.BiomeColourEntry;
import dev.imb11.fog.FogManager;
import dev.imb11.fog.HazeCalculator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
    @Shadow private static float red;

    @Shadow private static float green;

    @Shadow
    private static float blue;

    @Shadow
    public static void clearFog() {
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V", remap = false, shift = At.Shift.BEFORE))
    private static void modifyFogColors(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
            FogManager fogManager = FogManager.getInstance();
            FogManager.FogSettings settings = fogManager.getFogSettings(tickDelta, viewDistance);

            // TODO: Do this in getFogSettings.
            double hazeValue = HazeCalculator.getHaze((int) world.getTimeOfDay());
            BiomeColourEntry defaultEntry = new BiomeColourEntry(Identifier.of("default", "default"), 0.68f, 0.83f, 1f);
            red = (float) MathHelper.lerp(hazeValue, defaultEntry.fogR(), settings.fogR());
            green = (float) MathHelper.lerp(hazeValue, defaultEntry.fogG(), settings.fogG());
            blue = (float) MathHelper.lerp(hazeValue, defaultEntry.fogB(), settings.fogB());
    }
    @Inject(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V", remap = false, shift = At.Shift.BEFORE))
    private static void fogRenderEvent(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float deltaTick, CallbackInfo ci, @Local BackgroundRenderer.FogData fogData) {
        FogManager fogManager = FogManager.getInstance();
        FogManager.FogSettings settings = fogManager.getFogSettings(deltaTick, viewDistance);

        if (camera.getSubmersionType() == CameraSubmersionType.NONE) {
            fogData.fogStart = (float) settings.fogStart();
            fogData.fogEnd = (float) settings.fogEnd();
            fogData.fogShape = FogShape.SPHERE;
        }
    }

    // Changes the color of the seam/transition in the sky
    @Inject(method = "setFogBlack", at = @At("HEAD"))
    private static void setFogBlackInject(CallbackInfo ci) {
        FogManager fogManager = FogManager.getInstance();
        MinecraftClient client = MinecraftClient.getInstance();
        float deltaTick = client.getTickDelta();
        int viewDistance = client.options.getViewDistance().getValue();
        FogManager.FogSettings settings = fogManager.getFogSettings(deltaTick, viewDistance);

        // TODO: Do this in getFogSettings.
        double hazeValue = HazeCalculator.getHaze((int) client.world.getTimeOfDay());
        BiomeColourEntry defaultEntry = new BiomeColourEntry(Identifier.of("default", "default"), 0.68f, 0.83f, 1f);
        float r = (float) MathHelper.lerp(hazeValue, defaultEntry.fogR(), settings.fogR());
        float g = (float) MathHelper.lerp(hazeValue, defaultEntry.fogG(), settings.fogG());
        float b = (float) MathHelper.lerp(hazeValue, defaultEntry.fogB(), settings.fogB());
        RenderSystem.clearColor(r, g, b, 1.0F);
    }
}
