package dev.imb11.fog.config;

import com.google.gson.GsonBuilder;
import dev.imb11.fog.client.FogClient;
import dev.imb11.fog.client.FogManager;
import dev.imb11.fog.client.util.math.HazeCalculator;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

import static dev.imb11.fog.client.FogClient.MOD_ID;

public class FogConfig {
	private static final String CONFIG_FILE_NAME = "config";
	private static final String CONFIG_FILE_EXTENSION = "json";
	private static final String CONFIG_TRANSLATION_KEY = "config";
	private static final ConfigClassHandler<FogConfig> HANDLER = ConfigClassHandler
			.createBuilder(FogConfig.class)
			.id(Identifier.of(MOD_ID, CONFIG_FILE_NAME))
			.serializer(config -> GsonConfigSerializerBuilder
					.create(config)
					.setPath(FogClient.getConfigPath(CONFIG_FILE_NAME, CONFIG_FILE_EXTENSION))
					.appendGsonBuilder(GsonBuilder::setPrettyPrinting)
					.build())
			.build();
	@SerialEntry
	public Map<Integer, Float> timeToHazeMap = Map.of(
			0, 0.85f,
			500, 0.25f,
			1500, 0.25f,
			11500, 0.25f,
			12500, 0.85f,
			13500, 0.5f,
			22500, 0.5f,
			23500, 0.85f
	);
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
	public boolean disableHazeCalculation = false;

	private enum EntryType {
		CATEGORY_NAME,
		GROUP_NAME,
		OPTION_NAME,
		OPTION_DESCRIPTION,
	}

	public static @NotNull FogConfig getInstance() {
		return HANDLER.instance();
	}

	public static void load() {
		HANDLER.load();

		HazeCalculator.initialize();
	}

	public @NotNull YetAnotherConfigLib getYetAnotherConfigLibInstance() {
		return YetAnotherConfigLib.create(HANDLER, ((defaults, config, builder) -> builder
				.title(Text.empty())
				.save(() -> {
					HANDLER.save();

					FogManager.INSTANCE = new FogManager();
					HazeCalculator.initialize();
				})
				.category(ConfigCategory.createBuilder()
				                        .name(getText(EntryType.CATEGORY_NAME, "fog_calculations"))
				                        .option(LabelOption.create(getText(EntryType.OPTION_NAME, "fog_calculations.warning")))
				                        .option(Option.<Float>createBuilder().name(
						                        getText(EntryType.OPTION_NAME, "initial_fog_start")).description(
						                        initialFogStart -> OptionDescription.createBuilder().text(
								                        getText(EntryType.OPTION_DESCRIPTION, "initial_fog_start")).build()).binding(
						                        defaults.initialFogStart, () -> initialFogStart,
						                        newInitialFogStart -> initialFogStart = newInitialFogStart
				                        ).controller(option -> FloatSliderControllerBuilder.create(option).range(0.0F, 1.0F).step(
						                        0.0001F).formatValue(value -> Text.of(String.format("%.2f%%", value * 100.0F)))).build())
				                        .option(Option.<Float>createBuilder().name(
						                        getText(EntryType.OPTION_NAME, "initial_fog_end")).description(
						                        initialFogStart -> OptionDescription.createBuilder().text(
								                        getText(EntryType.OPTION_DESCRIPTION, "initial_fog_end")).build()).binding(
						                        defaults.initialFogEnd, () -> initialFogEnd,
						                        newInitialFogEnd -> initialFogEnd = newInitialFogEnd
				                        ).controller(option -> FloatSliderControllerBuilder.create(option).range(0.0F, 1.0F).step(
						                        0.0001F).formatValue(value -> Text.of(String.format("%.2f%%", value * 100.0F)))).build())
				                        .option(Option.<Boolean>createBuilder().name(
						                        getText(EntryType.OPTION_NAME, "disable_raininess_effect")).description(
						                        initialFogStart -> OptionDescription.createBuilder().text(
								                        getText(
										                        EntryType.OPTION_DESCRIPTION,
										                        "disable_raininess_effect"
								                        )).build()).binding(
						                        defaults.disableRaininessEffect, () -> disableRaininessEffect,
						                        newDisableRaininessEffect -> disableRaininessEffect = newDisableRaininessEffect
				                        ).controller(BooleanControllerBuilder::create).build())
				                        .option(Option.<Boolean>createBuilder().name(
						                        getText(EntryType.OPTION_NAME, "disable_underground_fog_multiplier")).description(
						                        initialFogStart -> OptionDescription.createBuilder().text(
								                        getText(
										                        EntryType.OPTION_DESCRIPTION,
										                        "disable_underground_fog_multiplier"
								                        )).build()).binding(
						                        defaults.disableRaininessEffect, () -> disableRaininessEffect,
						                        newDisableRaininessEffect -> disableRaininessEffect = newDisableRaininessEffect
				                        ).controller(BooleanControllerBuilder::create).build())
				                        .option(Option.<Boolean>createBuilder().name(
						                        getText(EntryType.OPTION_NAME, "disable_biome_fog_colour")).description(
						                        initialFogStart -> OptionDescription.createBuilder().text(
								                        getText(
										                        EntryType.OPTION_DESCRIPTION,
										                        "disable_biome_fog_colour"
								                        )).build()).binding(
						                        defaults.disableBiomeFogColour, () -> disableBiomeFogColour,
						                        newDisableBiomeFogColour -> disableBiomeFogColour = newDisableBiomeFogColour
				                        ).controller(BooleanControllerBuilder::create).build())
				                        .option(Option.<Boolean>createBuilder().name(
						                        getText(EntryType.OPTION_NAME, "disable_haze_calculation")).description(
						                        initialFogStart -> OptionDescription.createBuilder().text(
								                        getText(
										                        EntryType.OPTION_DESCRIPTION,
										                        "disable_haze_calculation"
								                        )).build()).binding(
						                        defaults.disableHazeCalculation, () -> disableHazeCalculation,
						                        newDisableHazeCalculation -> disableHazeCalculation = newDisableHazeCalculation
				                        ).controller(BooleanControllerBuilder::create).build())
				                        .build())
		));
	}

	/**
	 * @param entryType        The type of the entry that requires a translation key,
	 * @param configOptionName The name of the entry that requires a translation key,
	 * @return The {@link Text} with substituted values in the translation key.
	 */
	private static @NotNull Text getText(@NotNull FogConfig.EntryType entryType, @NotNull String configOptionName) {
		@NotNull String entryText;
		switch (entryType) {
			case CATEGORY_NAME -> entryText = "category";
			case GROUP_NAME -> entryText = "group";
			case OPTION_NAME -> entryText = "option";
			case OPTION_DESCRIPTION -> entryText = "option.description";
			default -> throw new IllegalArgumentException("TextType is invalid.");
		}

		return Text.translatable(String.format("%s.%s.%s.%s", MOD_ID, CONFIG_TRANSLATION_KEY, entryText, configOptionName));
	}
}
