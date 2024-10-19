package dev.imb11.fog.client.util;

import dev.imb11.mru.yacl.ConfigHelper;
import dev.imb11.mru.yacl.EntryType;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.ValueFormatter;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FogConfigHelper extends ConfigHelper {
	/**
	 * Create a new ConfigHelper.
	 *
	 * @param modID                The mod ID.
	 * @param configTranslationKey The translation key for the config.
	 */
	public FogConfigHelper(@NotNull String modID, @NotNull String configTranslationKey) {
		super(modID, configTranslationKey);
	}

	public static final ValueFormatter<Float> THREE_DECIMAL_FORMATTER = value -> Text.literal(String.format("%.3f", value));

	public @NotNull Option<Float> getFieldTDP(
			@NotNull String name,
			float min,
			float max,
			float defaultValue,
			Supplier<Float> getter,
			Consumer<Float> setter,
			ValueFormatter<Float> formatter,
			boolean withImage
	) {
		return Option.<Float>createBuilder()
		             .name(getText(EntryType.OPTION_NAME, name))
		             .description(get(name, withImage))
		             .binding(defaultValue, getter, setter)
		             .controller(opt -> FloatFieldControllerBuilder.create(opt)
		                                                           .min(min)
		                                                           .max(max)
		                                                           .formatValue(formatter))
		             .build();
	}

	// Overloaded method without withImage parameter (defaults to false)
	public @NotNull Option<Float> getFieldTDP(
			@NotNull String name,
			float min,
			float max,
			float defaultValue,
			Supplier<Float> getter,
			Consumer<Float> setter
	) {
		return getFieldTDP(name, min, max, defaultValue, getter, setter, THREE_DECIMAL_FORMATTER, false);
	}
}
