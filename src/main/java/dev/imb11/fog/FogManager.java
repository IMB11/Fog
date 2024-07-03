package dev.imb11.fog;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;

public class FogManager implements ClientTickEvents.EndWorldTick {
    public static FogManager INSTANCE = new FogManager();
    public final InterpolatedValue raininess = new InterpolatedValue(0.0f, 0.03f);
    public final InterpolatedValue undergroundness = new InterpolatedValue(0.0f, 0.25f);
    public final InterpolatedValue fogStart = new InterpolatedValue(0.05f, 0.05f);
    public final InterpolatedValue fogEnd = new InterpolatedValue(0.85f, 0.05f);
    public final InterpolatedValue darkness = new InterpolatedValue(0.0f, 0.1f);
    public final InterpolatedValue fogColorRed = new InterpolatedValue((float) 0x33 / 255f, 0.05f);
    public final InterpolatedValue fogColorGreen = new InterpolatedValue((float) 0x33 / 255f, 0.05f);
    public final InterpolatedValue fogColorBlue = new InterpolatedValue((float) 0x33 / 255f, 0.05f);
    public final InterpolatedValue currentSkyLight = new InterpolatedValue(16.0F);
    public final InterpolatedValue currentBlockLight = new InterpolatedValue(16.0F);
    public final InterpolatedValue currentLight = new InterpolatedValue(16.0F);

    public static FogManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEndTick(ClientWorld world) {
        MinecraftClient client = MinecraftClient.getInstance();
        float deltaTick = client.getTickDelta();

        if(world.isRaining()) {
            raininess.interpolate(1.0f);
        } else {
            raininess.interpolate(0.0f);
        }

        BlockPos pos = client.player.getBlockPos();
        Vec3d eyePos = client.player.getEyePos();

        boolean isAboveGround = eyePos.getY() + 0.5f > client.world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ()) || eyePos.getY() + 0.5f > client.world.getSeaLevel();

        if (isAboveGround) {
            this.undergroundness.interpolate(0.0F, 0.05f);
        } else {
            this.undergroundness.interpolate(1.0F);
        }

        boolean isFogDense = client.world.getDimensionEffects().useThickFog(pos.getX(), pos.getZ()) || client.inGameHud.getBossBarHud().shouldThickenFog();
        float density = isFogDense ? 0.9F : 1.0F;

        DarknessCalculation darknessCalculation = DarknessCalculation.of(client, fogStart.getDefaultValue(), fogEnd.getDefaultValue() * density, deltaTick);

        BiomeColourEntry biomeColourEntry = BiomeColourEntry.getOrDefault(client.world.getBiome(client.player.getBlockPos()).getKey().get().getValue());

        this.fogColorRed.interpolate(biomeColourEntry.fogR());
        this.fogColorGreen.interpolate(biomeColourEntry.fogG());
        this.fogColorBlue.interpolate(biomeColourEntry.fogB());

        this.fogStart.interpolate(darknessCalculation.fogStart());
        this.fogEnd.interpolate(darknessCalculation.fogEnd());
        this.darkness.interpolate(darknessCalculation.darknessValue());

        this.currentSkyLight.interpolate(client.world.getLightLevel(LightType.SKY, pos));
        this.currentBlockLight.interpolate(client.world.getLightLevel(LightType.BLOCK, pos));
        this.currentLight.interpolate(client.world.getBaseLightLevel(pos, 0));
    }

    public static float mapRange(float fromMin, float fromMax, float toMin, float toMax, float value) {
        float clampedValue = MathHelper.clamp(value, fromMin, fromMax);
        return toMin + (clampedValue - fromMin) * (toMax - toMin) / (fromMax - fromMin);
    }

    public float getUndergroundFactor(MinecraftClient client, float deltaTicks) {
        Entity cameraEntity = client.cameraEntity;
        if (cameraEntity == null) return 0.0F; // Handle case where cameraEntity is null

        float y = (float) cameraEntity.getY();
        float seaLevel = client.world.getSeaLevel();

        // Map y to a factor between 0 and 1 based on sea level +/- 32
        float yFactor = mapRange(seaLevel - 32.0F, seaLevel + 32.0F, 1.0F, 0.0F, y);
        yFactor = MathHelper.clamp(yFactor, 0.0F, 1.0F); // Clamp yFactor to ensure it's within [0, 1]

        float undergroundnessValue = this.undergroundness.get(deltaTicks);
        float skyLight = this.currentSkyLight.get(deltaTicks);

        // Calculate underground factor using lerping between yFactor, undergroundness, and sky light
        float undergroundFactor = MathHelper.lerp(yFactor, 1.0F - undergroundnessValue, skyLight / 16.0F);

        return undergroundFactor;
    }

    public FogSettings getFogSettings(float tickDelta, float viewDistance) {
        float fogStartValue = fogStart.get(tickDelta) * viewDistance;

        // Calculate underground fog multiplier
        float undergroundFogMultiplier = 1.0F;
        if (true) {
            undergroundFogMultiplier = MathHelper.lerp(this.undergroundness.get(tickDelta), 0.75F, 1.0F);
            float darkness = this.darkness.get(tickDelta);
            undergroundFogMultiplier = MathHelper.lerp(darkness, undergroundFogMultiplier, 1.0F);
        }

        // Calculate fog end considering raininess and underground fog multiplier
        float raininessValue = raininess.get(tickDelta);
        float fogEndValue = viewDistance * (fogEnd.get(tickDelta)) * undergroundFogMultiplier;

        // Adjust fog end based on raininess
        if (raininessValue > 0.0f) {
            fogEndValue /= 1.0f + 0.5f * raininessValue;
        }

        float fogDensity = undergroundness.get(tickDelta);

        // Adjust fog color based on darkness
        float fogRed = fogColorRed.get(tickDelta);
        float fogGreen = fogColorGreen.get(tickDelta);
        float fogBlue = fogColorBlue.get(tickDelta);
        float darknessValue = darkness.get(tickDelta);

        fogRed *= 1 - darknessValue;
        fogGreen *= 1 - darknessValue;
        fogBlue *= 1 - darknessValue;

        return new FogSettings(fogStartValue, fogEndValue, fogDensity, fogRed, fogGreen, fogBlue);
    }

    public static record FogSettings(double fogStart, double fogEnd, double fogDensity, float fogR, float fogG, float fogB) {}
}
