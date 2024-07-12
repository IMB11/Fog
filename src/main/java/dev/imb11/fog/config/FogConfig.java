package dev.imb11.fog.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FogConfig {
	public static final ConfigClassHandler<FogConfig> HANDLER = ConfigClassHandler
			.createBuilder(FogConfig.class)
			.id(Identifier.of("fog", "config"))
			.serializer(config -> GsonConfigSerializerBuilder
					.create(config)
					.setPath(FabricLoader.getInstance().getConfigDir().resolve("fog/config.json"))
					.appendGsonBuilder(GsonBuilder::setPrettyPrinting)
					.build())
			.build();

	@SerialEntry
	public final float initialFogStart = 0.1f;
	@SerialEntry
	public final float initialFogEnd = 0.85f;
	@SerialEntry
	public boolean disableRaininessEffect = false;
	@SerialEntry
	public final boolean disableUndergroundFogMultiplier = false;

	public static FogConfig get() {
		return HANDLER.instance();
	}

	public static void load() {
		HANDLER.load();
	}

	public static YetAnotherConfigLib getInstance() {
		return YetAnotherConfigLib.create(HANDLER, ((defaults, config, builder) -> builder
				.title(Text.empty())
				.category(ConfigCategory.createBuilder()
						.name(Text.translatable("fog.config.fog_calculations"))
						.option(LabelOption.create(Text.translatable("fog.config.fog_calculations.warning")))
				                        .build())
		));
	}
}
