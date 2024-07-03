package dev.imb11.fog.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.imb11.fog.FogManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BackgroundRenderer.class)
public class FogMixin {
    @Shadow private static float red;

    @Shadow private static float green;

    @Shadow
    private static float blue;

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V", remap = false))
    private static void modifyFogColors(Args args, Camera camera, float deltaTick, ClientWorld level, int viewDistance, float bossColorModifier) {
            FogManager fogManager = FogManager.getInstance();
            FogManager.FogSettings settings = fogManager.getFogSettings(deltaTick, viewDistance);

            float undergroundFactor = 1 - fogManager.getUndergroundFactor(MinecraftClient.getInstance(), deltaTick);
            red = settings.fogR();
            green = settings.fogG();
            blue = settings.fogB();
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
}
