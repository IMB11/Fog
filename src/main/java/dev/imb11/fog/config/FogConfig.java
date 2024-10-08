package dev.imb11.fog.config;

import com.google.gson.GsonBuilder;
import dev.imb11.fog.client.FogClient;
import dev.imb11.fog.client.FogManager;
import dev.imb11.mru.yacl.ConfigHelper;
import dev.imb11.mru.yacl.EntryType;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static dev.imb11.fog.client.FogClient.MOD_ID;

public class FogConfig {
	private static final String CONFIG_FILE_NAME = "config";
	private static final String CONFIG_FILE_EXTENSION = "json";
	private static final ConfigClassHandler<FogConfig> HANDLER = ConfigClassHandler
			.createBuilder(FogConfig.class)
			.id(Identifier.of(MOD_ID, CONFIG_FILE_NAME))
			.serializer(config -> GsonConfigSerializerBuilder
					.create(config)
					.setPath(FogClient.getConfigPath(CONFIG_FILE_NAME, CONFIG_FILE_EXTENSION))
					.appendGsonBuilder(GsonBuilder::setPrettyPrinting)
					.build())
			.build();
	private static final ConfigHelper HELPER = new ConfigHelper(MOD_ID, "config");
	@SerialEntry
	public float initialFogStart = 0.1f;
	@SerialEntry
	public float initialFogEnd = 0.85f;
	@SerialEntry
	public boolean disableRaininessEffect = false;
	@SerialEntry
	public boolean disableUndergroundFogMultiplier = false;
	@SerialEntry
	public boolean disableBiomeFogColour = false;
	@SerialEntry
	public boolean disableCloudWhitening = false;
	/**
	 * Nether has pretty good Fog, it doesn't need changing unless player really wants to.
	 */
	@SerialEntry
	public boolean disableNether = true;
	@SerialEntry
	public boolean disableMod = false;

	public static @NotNull FogConfig getInstance() {
		return HANDLER.instance();
	}

	public static void load() {
		HANDLER.load();
	}

	public static void save() {
		HANDLER.save();
	}

	public @NotNull YetAnotherConfigLib getYetAnotherConfigLibInstance() {
		return YetAnotherConfigLib.create(HANDLER, ((defaults, config, builder) -> builder
				.title(Text.empty())
				.save(() -> {
					HANDLER.save();

					FogManager.INSTANCE = new FogManager();
				})
				.category(ConfigCategory.createBuilder()
				                        .name(HELPER.getText(EntryType.CATEGORY_NAME, "fog_calculations"))
				                        .option(LabelOption.create(HELPER.getText(EntryType.OPTION_NAME, "fog_calculations.warning")))
				                        .option(HELPER.getSlider(
						                        "initial_fog_start", 0f, 1f, 0.05f, defaults.initialFogStart, () -> config.initialFogStart,
						                        val -> config.initialFogStart = val
				                        ))
				                        .option(HELPER.getSlider(
						                        "initial_fog_end", 0f, 1f, 0.05f, defaults.initialFogEnd, () -> config.initialFogEnd,
						                        val -> config.initialFogEnd = val
				                        ))
				                        .option(HELPER.get(
						                        "disable_raininess_effect", defaults.disableRaininessEffect,
						                        () -> config.disableRaininessEffect, val -> config.disableRaininessEffect = val
				                        ))
				                        .option(HELPER.get(
						                        "disable_underground_fog_multiplier", defaults.disableUndergroundFogMultiplier,
						                        () -> config.disableUndergroundFogMultiplier,
						                        val -> config.disableUndergroundFogMultiplier = val
				                        ))
				                        .option(HELPER.get(
						                        "disable_biome_fog_colour", defaults.disableBiomeFogColour,
						                        () -> config.disableBiomeFogColour, val -> config.disableBiomeFogColour = val
				                        ))
				                        .option(Option.<Boolean>createBuilder().name(
						                        HELPER.getText(EntryType.OPTION_NAME, "disable_cloud_whitening")).description(
						                        initialFogStart -> OptionDescription.createBuilder().text(
								                        HELPER.getText(
										                        EntryType.OPTION_DESCRIPTION,
										                        "disable_cloud_whitening"
								                        )).build()).binding(
						                        defaults.disableCloudWhitening, () -> disableCloudWhitening,
						                        newDisableCloudWhitening -> disableCloudWhitening = newDisableCloudWhitening
				                        ).controller(BooleanControllerBuilder::create).available(
						                        !FogClient.isModInstalled("sodium")).build())
				                        .option(HELPER.get(
						                        "disable_nether", defaults.disableNether, () -> config.disableNether,
						                        val -> config.disableNether = val
				                        ))
				                        .option(Option.<Boolean>createBuilder().name(
						                        HELPER.getText(EntryType.OPTION_NAME, "disable_mod")).description(
						                        initialFogStart -> OptionDescription.createBuilder().text(
								                        HELPER.getText(
										                        EntryType.OPTION_DESCRIPTION,
										                        "disable_mod"
								                        )).build()).binding(
						                        defaults.disableMod, () -> disableMod,
						                        newDisableMod -> disableMod = newDisableMod
				                        ).controller(BooleanControllerBuilder::create).build())
				                        .build())
		));
	}
}
