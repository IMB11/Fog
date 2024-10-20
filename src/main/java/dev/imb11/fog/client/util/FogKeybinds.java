package dev.imb11.fog.client.util;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.imb11.fog.config.FogConfig;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FogKeybinds {
	public static KeyBinding toggleKeybind;

	public static void init() {

		toggleKeybind = new KeyBinding(
				"key.fog.toggle",
				InputUtil.UNKNOWN_KEY.getCode(),
				"key.categories.misc"
		);

		KeyMappingRegistry.register(toggleKeybind);

		ClientTickEvent.CLIENT_POST.register(client -> {
			while (toggleKeybind.wasPressed()) {
				FogConfig config = FogConfig.getInstance();
				config.disableMod = !config.disableMod;

				FogConfig.save();

				client.inGameHud.getChatHud().addMessage(Text.literal("§b§7[§rFog§b§7]§r ").append(
						Text.translatable("fog.command.toggle." + (config.disableMod ? "disabled" : "enabled")).formatted(Formatting.GOLD)));
			}
		});
	}
}
