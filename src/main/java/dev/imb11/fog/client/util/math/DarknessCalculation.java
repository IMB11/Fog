package dev.imb11.fog.client.util.math;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public record DarknessCalculation(float fogStart, float fogEnd, float darknessValue) {
    public static DarknessCalculation of(@NotNull MinecraftClient client, float fogStart, float fogEnd, float deltaTick) {
        float renderDistance = client.gameRenderer.getViewDistance() * 16;
        Entity entity = client.cameraEntity;
        float darknessValue = 0.0F;

        if (entity instanceof LivingEntity e) {
            if (e.hasStatusEffect(StatusEffects.BLINDNESS)) {
                fogStart = (4 * 16) / renderDistance;
                fogEnd = (8 * 16) / renderDistance;
                darknessValue = 1.0F;
            } else if (e.hasStatusEffect(StatusEffects.DARKNESS)) {
                StatusEffectInstance effect = e.getStatusEffect(StatusEffects.DARKNESS);
                if (effect != null && effect.getFactorCalculationData().isPresent()) {
                    float factor = client.options.getDarknessEffectScale().getValue().floatValue();
                    float intensity = effect.getFactorCalculationData().get().lerp(e, deltaTick) * factor;
                    float darknessScale = calculateDarknessScale(e, deltaTick);
                    fogStart = ((8.0F * 16) / renderDistance) * (1 - darknessScale);
                    fogEnd = (15.0F * 16) / renderDistance;
                    darknessValue = intensity;
                }
            }
        }

        return new DarknessCalculation(fogStart, fogEnd, darknessValue);
    }

    private static float calculateDarknessScale(@NotNull LivingEntity entity, float deltaTick) {
        float darknessFactor = entity.getStatusEffect(StatusEffects.DARKNESS)
                .getFactorCalculationData()
                .get()
                .lerp(entity, deltaTick);

        float factor = 0.45F * darknessFactor;
        return Math.max(0.0F, MathHelper.cos((entity.age - deltaTick) * (float) Math.PI * 0.025F) * factor);
    }
}
