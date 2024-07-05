package dev.imb11.fog.client.resource.json;

import com.google.gson.annotations.SerializedName;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class Fogs {
	@SuppressWarnings("unused")
	@SerializedName("default")
	private @Nullable Fog defaultFog;
}
