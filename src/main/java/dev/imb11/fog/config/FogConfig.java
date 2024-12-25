package dev.imb11.fog.config;

import com.google.gson.GsonBuilder;
import dev.imb11.fog.client.FogClient;
import dev.imb11.fog.client.FogManager;
import dev.imb11.fog.client.util.FogConfigHelper;
import dev.imb11.mru.yacl.ConfigHelper;
import dev.imb11.mru.yacl.EntryType;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

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
	private static final FogConfigHelper HELPER = new FogConfigHelper(MOD_ID, "config");

	@SerialEntry
	public boolean disableMod = false;
	/**
	 * Nether has pretty good Fog, it doesn't need changing unless player really wants to.
	 */
	@SerialEntry
	public @NotNull List<String> disabledDimensions = List.of(String.format("%s:the_nether", Identifier.DEFAULT_NAMESPACE));
	@SerialEntry
	public boolean disableRaininessEffect = false;
	@SerialEntry
	public boolean disableUndergroundFogMultiplier = false;
	@SerialEntry
	public boolean disableBiomeFogColour = false;
	@SerialEntry
	public boolean disableCloudWhitening = false;
	@SerialEntry
	public float initialFogStart = 0.1f;
	@SerialEntry
	public float initialFogEnd = 0.85f;

	@SerialEntry
	public float raininessTransitionSpeed = 0.005f;
	@SerialEntry
	public float undergroundnessTransitionSpeed = 0.005f;
	@SerialEntry
	public float fogStartTransitionSpeed = 0.005f;
	@SerialEntry
	public float fogEndTransitionSpeed = 0.005f;
	@SerialEntry
	public float darknessTransitionSpeed = 0.005f;
	@SerialEntry
	public float fogColorTransitionSpeed = 0.025f;
	@SerialEntry
	public float startMultiplierTransitionSpeed = 0.0075f;
	@SerialEntry
	public float endMultiplierTransitionSpeed = 0.0075f;
	@SerialEntry
	public boolean disableMoonPhaseColorTransition = false;
	@SerialEntry
	public Color newMoonColor = new Color(0, 0, 0, 255);

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
				                        .option(HELPER.getFieldTDP(
						                        "fog_start_transition_speed", 0.001f, 0.5f, defaults.fogStartTransitionSpeed, () -> config.fogStartTransitionSpeed,
						                        val -> config.fogStartTransitionSpeed = val
				                        ))
				                        .option(HELPER.getFieldTDP(
						                        "fog_end_transition_speed", 0.001f, 0.5f, defaults.fogEndTransitionSpeed, () -> config.fogEndTransitionSpeed,
						                        val -> config.fogEndTransitionSpeed = val
				                        ))
				                        .option(HELPER.getFieldTDP(
						                        "darkness_transition_speed", 0.001f, 0.5f, defaults.darknessTransitionSpeed, () -> config.darknessTransitionSpeed,
						                        val -> config.darknessTransitionSpeed = val
				                        ))
				                        .option(HELPER.getFieldTDP(
						                        "fog_color_transition_speed", 0.001f, 0.5f, defaults.fogColorTransitionSpeed, () -> config.fogColorTransitionSpeed,
						                        val -> config.fogColorTransitionSpeed = val
				                        ))
				                        .option(HELPER.getFieldTDP(
						                        "start_multiplier_transition_speed", 0.001f, 0.5f, defaults.startMultiplierTransitionSpeed, () -> config.startMultiplierTransitionSpeed,
						                        val -> config.startMultiplierTransitionSpeed = val
				                        ))
				                        .option(HELPER.getFieldTDP(
						                        "end_multiplier_transition_speed", 0.001f, 0.5f, defaults.endMultiplierTransitionSpeed, () -> config.endMultiplierTransitionSpeed,
						                        val -> config.endMultiplierTransitionSpeed = val
				                        ))
				                        .option(HELPER.getFieldTDP(
						                        "raininess_transition_speed", 0.001f, 0.5f, defaults.raininessTransitionSpeed, () -> config.raininessTransitionSpeed,
						                        val -> config.raininessTransitionSpeed = val
				                        ))
				                        .option(HELPER.getFieldTDP(
						                        "undergroundness_transition_speed", 0.001f, 0.5f, defaults.undergroundnessTransitionSpeed, () -> config.undergroundnessTransitionSpeed,
						                        val -> config.undergroundnessTransitionSpeed = val
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
				                        .option(HELPER.get(
						                        "disable_moon_phase_color_transition", defaults.disableMoonPhaseColorTransition,
						                        () -> config.disableMoonPhaseColorTransition, val -> config.disableMoonPhaseColorTransition = val
				                        ))
				                        .option(HELPER.get(
						                        "new_moon_color", defaults.newMoonColor, () -> config.newMoonColor, val -> config.newMoonColor = val
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
				                        .group(ListOption.<String>createBuilder()
				                                         .name(Text.translatable(
						                                         String.format("%s.config.option.disabled_dimensions", MOD_ID)))
				                                         .binding(
						                                         disabledDimensions, () -> disabledDimensions,
						                                         val -> disabledDimensions = val
				                                         )
				                                         .controller(StringControllerBuilder::create)
				                                         .initial("mod_id:dimension_id")
				                                         .build())
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
