package dev.imb11.fog.client.util;

import dev.imb11.fog.client.command.FogClientCommands;
import dev.imb11.fog.config.FogConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class FogKeybinds {
	public static KeyBinding toggleKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
	    "key.fog.toggle",
		InputUtil.UNKNOWN_KEY.getCode(),
	    "key.categories.misc"
	));

	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
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
