package dev.imb11.fog.config;

import com.google.gson.GsonBuilder;
import dev.imb11.fog.client.FogClient;
import dev.imb11.fog.client.FogManager;
import dev.imb11.fog.client.util.FogConfigHelper;
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
	public boolean enableMod = true;
	/**
	 * The Nether has good fog, it doesn't need changing unless the player really wants to.
	 */
	@SerialEntry
	public @NotNull List<String> disabledBiomes = List.of(
			"minecraft:nether_wastes",
			"minecraft:crimson_forest",
			"minecraft:warped_forest",
			"minecraft:soul_sand_valley",
			"minecraft:basalt_deltas"
	);

	@SerialEntry
	public float rainFogMultiplier = 0.9f;
	@SerialEntry
	public float undergroundFogMultiplier = 0.5f;
	@SerialEntry
	public boolean enableBiomeSpecificFogColors = true;
	@SerialEntry
	public boolean enableCloudWhitening = false;
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
	public boolean enableMoonFogColorInfluence = true;
	@SerialEntry
	public Color newMoonColor = new Color(0, 0, 0, 255);
	@SerialEntry
	public boolean enableSunFogColorInfluence = true;
	@SerialEntry
	public boolean enableHighAltitudeFogColorInfluence = true;
	@SerialEntry
	public boolean prioritizePolytoneFogDefinitions = true;
	@SerialEntry
	public boolean disableModWhenIrisShaderPackIsEnabled = true;

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
				                        .name(HELPER.getText(EntryType.CATEGORY_NAME, "general"))
				                        .option(Option.<Boolean>createBuilder()
				                                      .name(HELPER.getText(EntryType.OPTION_NAME, "disable_mod"))
				                                      .description(unused -> OptionDescription.createBuilder()
				                                                                              .text(HELPER.getText(
						                                                                              EntryType.OPTION_DESCRIPTION,
						                                                                              "disable_mod"
				                                                                              ))
				                                                                              .build())
				                                      .binding(
						                                      defaults.enableMod, () -> enableMod,
						                                      newEnableMod -> enableMod = newEnableMod
				                                      )
				                                      .controller(BooleanControllerBuilder::create)
				                                      .build()
				                        )
				                        .option(ButtonOption.createBuilder()
	                                            .name(HELPER.getText(EntryType.OPTION_NAME, "reset_fog_modifications"))
						                        .description(OptionDescription.of(HELPER.getText(EntryType.OPTION_DESCRIPTION, "reset_fog_modifications")))
						                        .action((yaclScreen, buttonOption) -> {
							                        FogConfig.load();
							                        FogManager.INSTANCE = new FogManager();
						                        })
						                        .build()
				                        )
				                        .option(LabelOption.createBuilder()
				                                           .line(Text.literal(""))
				                                           .build()
				                        )
				                        .option(HELPER.get(
						                        "enable_biome_specific_fog_colors", defaults.enableBiomeSpecificFogColors,
						                        () -> config.enableBiomeSpecificFogColors, val -> config.enableBiomeSpecificFogColors = val
				                        ))
				                        .option(HELPER.get("enable_high_altitude_fog_color_influence", defaults.enableHighAltitudeFogColorInfluence,
						                        () -> config.enableHighAltitudeFogColorInfluence, val -> config.enableHighAltitudeFogColorInfluence = val
				                        ))
				                        .option(HELPER.get(
						                        "enable_sun_fog_color_influence", defaults.enableSunFogColorInfluence,
						                        () -> config.enableSunFogColorInfluence, val -> config.enableSunFogColorInfluence = val
				                        ))
				                        .option(HELPER.get(
						                        "enable_moon_fog_color_influence", defaults.enableMoonFogColorInfluence,
						                        () -> config.enableMoonFogColorInfluence,
						                        val -> config.enableMoonFogColorInfluence = val
				                        ))
				                        .option(HELPER.get(
						                        "new_moon_color", defaults.newMoonColor, () -> config.newMoonColor,
						                        val -> config.newMoonColor = val
				                        ))
				                        .option(Option.<Boolean>createBuilder()
				                                      .name(HELPER.getText(EntryType.OPTION_NAME, "enable_cloud_whitening"))
				                                      .description(unused -> OptionDescription.createBuilder()
				                                                                              .text(HELPER.getText(
						                                                                              EntryType.OPTION_DESCRIPTION,
						                                                                              "enable_cloud_whitening"
				                                                                              ))
				                                                                              .build())
				                                      .binding(
						                                      defaults.enableCloudWhitening, () -> enableCloudWhitening,
						                                      newDisableCloudWhitening -> enableCloudWhitening = newDisableCloudWhitening
				                                      )
				                                      .controller(option -> BooleanControllerBuilder.create(option).coloured(true).trueFalseFormatter())
				                                      .available(!FogClient.isModInstalled("sodium"))
				                                      .build()
				                        )
						                .option(LabelOption.createBuilder()
			                                    .line(Text.literal(""))
			                                    .build()
						                )
				                        .option(HELPER.getSlider(
						                        "rain_fog_multiplier", 0f, 1f, 0.05f, defaults.rainFogMultiplier,
						                        () -> config.rainFogMultiplier, val -> config.rainFogMultiplier = val
				                        ))
				                        .option(HELPER.getSlider(
						                        "underground_fog_multiplier", 0f, 1f, 0.05f,defaults.undergroundFogMultiplier,
						                        () -> config.undergroundFogMultiplier,
						                        val -> config.undergroundFogMultiplier = val
				                        ))
				                        .option(LabelOption.createBuilder()
				                                           .line(Text.literal(""))
				                                           .build()
				                        )
				                        .group(ListOption.<String>createBuilder()
				                                         .name(Text.translatable(
						                                         String.format("%s.config.option.disabled_biomes", MOD_ID)))
				                                         .binding(
						                                         defaults.disabledBiomes, () -> disabledBiomes,
						                                         val -> disabledBiomes = val
				                                         )
				                                         .controller(StringControllerBuilder::create)
				                                         .initial("mod_id:dimension_id")
				                                         .collapsed(true)
				                                         .build()
				                        )
				                        .group(OptionGroup.createBuilder()
				                                          .name(Text.translatable(
						                                          String.format("%s.config.group.initial_values", MOD_ID)))
				                                          .option(HELPER.getSlider(
						                                          "initial_fog_start", 0f, 1f, 0.05f, defaults.initialFogStart, () -> config.initialFogStart,
						                                          val -> config.initialFogStart = val
				                                          ))
				                                          .option(HELPER.getSlider(
						                                          "initial_fog_end", 0f, 1f, 0.05f, defaults.initialFogEnd, () -> config.initialFogEnd,
						                                          val -> config.initialFogEnd = val
				                                          ))
				                                          .collapsed(true)
				                                          .build()
				                        )
				                        .build()
				)
				.category(ConfigCategory.createBuilder()
				                        .name(HELPER.getText(EntryType.CATEGORY_NAME, "transition_speeds"))
				                        .option(HELPER.getFieldTDP(
						                        "fog_start_transition_speed", 0.001f, 0.5f, defaults.fogStartTransitionSpeed,
						                        () -> config.fogStartTransitionSpeed,
						                        val -> config.fogStartTransitionSpeed = val
				                        ))
				                        .option(HELPER.getFieldTDP(
						                        "fog_end_transition_speed", 0.001f, 0.5f, defaults.fogEndTransitionSpeed,
						                        () -> config.fogEndTransitionSpeed,
						                        val -> config.fogEndTransitionSpeed = val
				                        ))
				                        .option(HELPER.getFieldTDP(
						                        "fog_color_transition_speed", 0.001f, 0.5f, defaults.fogColorTransitionSpeed,
						                        () -> config.fogColorTransitionSpeed,
						                        val -> config.fogColorTransitionSpeed = val
				                        ))
				                        .option(LabelOption.createBuilder()
				                                           .line(Text.literal(""))
				                                           .build()
				                        )
				                        .option(HELPER.getFieldTDP(
						                        "raininess_transition_speed", 0.001f, 0.5f, defaults.raininessTransitionSpeed,
						                        () -> config.raininessTransitionSpeed,
						                        val -> config.raininessTransitionSpeed = val
				                        ))
				                        .option(HELPER.getFieldTDP(
						                        "undergroundness_transition_speed", 0.001f, 0.5f, defaults.undergroundnessTransitionSpeed,
						                        () -> config.undergroundnessTransitionSpeed,
						                        val -> config.undergroundnessTransitionSpeed = val
				                        ))
				                        .option(HELPER.getFieldTDP(
						                        "darkness_transition_speed", 0.001f, 0.5f, defaults.darknessTransitionSpeed,
						                        () -> config.darknessTransitionSpeed,
						                        val -> config.darknessTransitionSpeed = val
				                        ))
				                        .option(LabelOption.createBuilder()
				                                           .line(Text.literal(""))
				                                           .build()
				                        )
										.group(OptionGroup.createBuilder()
												.name(Text.translatable(
														String.format("%s.config.group.multipliers", MOD_ID)))
												.option(HELPER.getFieldTDP(
														"start_multiplier_transition_speed", 0.001f, 0.5f, defaults.startMultiplierTransitionSpeed,
														() -> config.startMultiplierTransitionSpeed,
														val -> config.startMultiplierTransitionSpeed = val
												))
												.option(HELPER.getFieldTDP(
														"end_multiplier_transition_speed", 0.001f, 0.5f, defaults.endMultiplierTransitionSpeed,
														() -> config.endMultiplierTransitionSpeed,
														val -> config.endMultiplierTransitionSpeed = val
												))
												.collapsed(true)
												.build()
										)
				                        .build()
				).category(ConfigCategory.createBuilder()
				                         .name(HELPER.getText(EntryType.CATEGORY_NAME, "compatibility"))
				                         .option(Option.<Boolean>createBuilder()
				                                       .name(HELPER.getText(EntryType.OPTION_NAME, "disable_mod_when_iris_shader_pack_is_enabled"))
				                                       .description(unused -> OptionDescription.createBuilder()
				                                                                               .text(HELPER.getText(
						                                                                               EntryType.OPTION_DESCRIPTION,
						                                                                               "disable_mod_when_iris_shader_pack_is_enabled"
				                                                                               ))
				                                                                               .build())
				                                       .binding(
						                                       defaults.disableModWhenIrisShaderPackIsEnabled, () -> disableModWhenIrisShaderPackIsEnabled,
						                                       newDisableModWhenIrisShaderPackIsEnabled -> disableModWhenIrisShaderPackIsEnabled = newDisableModWhenIrisShaderPackIsEnabled
				                                       )
				                                       .controller(option -> BooleanControllerBuilder.create(option).coloured(true).trueFalseFormatter())
				                                       .available(FogClient.isModInstalled("iris"))
				                                       .build()
				                         )
				                         .option(Option.<Boolean>createBuilder()
				                                       .name(HELPER.getText(EntryType.OPTION_NAME, "prioritize_polytone_fog_definitions"))
				                                       .description(unused -> OptionDescription.createBuilder()
				                                                                               .text(HELPER.getText(
						                                                                               EntryType.OPTION_DESCRIPTION,
						                                                                               "prioritize_polytone_fog_definitions"
				                                                                               ))
				                                                                               .build())
				                                       .binding(
						                                       defaults.prioritizePolytoneFogDefinitions, () -> prioritizePolytoneFogDefinitions,
						                                       newPrioritizePolytoneFogDefinitions -> prioritizePolytoneFogDefinitions = newPrioritizePolytoneFogDefinitions
				                                       )
				                                       .controller(option -> BooleanControllerBuilder.create(option).coloured(true).trueFalseFormatter())
				                                       .available(FogClient.isModInstalled("polytone"))
				                                       .build()
				                         )
				                         .build()
				)
		));
	}
}
