package dev.imb11.fog.client.resource.json;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class Fog {
	@SuppressWarnings("unused")
	private float startMultiplier;
	@SuppressWarnings("unused")
	private float endMultiplier;
	@SuppressWarnings("unused")
	private @Nullable FogColors colors;
}
