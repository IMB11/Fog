package dev.imb11.fog.client;

import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.imb11.fog.client.util.color.Color;
import dev.imb11.fog.client.util.math.HazeCalculator;
import dev.imb11.fog.config.FogConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FogClientCommands {
	public static void register() {
		ClientCommandRegistrationEvent.EVENT.register((dispatcher, context) -> {
			var fogNode = ClientCommandRegistrationEvent.literal("fog");

			fogNode = fogNode.then(ClientCommandRegistrationEvent.literal("reset").executes(FogClientCommands::reset));

			fogNode = fogNode.then(ClientCommandRegistrationEvent.literal("toggle").executes(FogClientCommands::toggle));

			fogNode = fogNode.then(ClientCommandRegistrationEvent.literal("debug").executes(FogClientCommands::outputDebug));

			dispatcher.register(fogNode);
		});
	}

	private static int outputDebug(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> e) {
		MinecraftClient client = MinecraftClient.getInstance();
		float tickDelta = client.getTickDelta();
		FogManager manager = FogManager.INSTANCE;

		String hexColor = Integer.toHexString(new Color((int) (manager.fogColorRed.get(tickDelta) * 255), (int) (manager.fogColorGreen.get(tickDelta) * 255), (int) (manager.fogColorBlue.get(tickDelta) * 255)).toInt());
		hexColor = "§c" + hexColor.substring(0, 2) + "§a" + hexColor.substring(2, 4) + "§9" + hexColor.substring(4);

		String table = String.format(
				"§b§7[§rFog§b§7]§r Current Fog Manager State:\n" +
						"§b§7[§rFog§b§7]§r Raininess: §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r \"Undergroundness\": §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r Fog Start: §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r Fog End: §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r Darkness: §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r Fog Color (Before Haze): §6#%s§r\n" +
						"§b§7[§rFog§b§7]§r Haze Value: §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r Current Sky Light: §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r Current Block Light: §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r Current Light: §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r Current Start Multiplier: §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r Current End Multiplier: §6%.2f§r",
				manager.raininess.get(tickDelta),
				manager.undergroundness.get(tickDelta),
				manager.fogStart.get(tickDelta),
				manager.fogEnd.get(tickDelta),
				manager.darkness.get(tickDelta),
				hexColor,
				HazeCalculator.getHaze((int) client.world.getTimeOfDay()),
				manager.currentSkyLight.get(tickDelta),
				manager.currentBlockLight.get(tickDelta),
				manager.currentLight.get(tickDelta),
				manager.currentStartMultiplier.get(tickDelta),
				manager.currentEndMultiplier.get(tickDelta)
		);

		e.getSource().arch$sendSuccess(() -> Text.literal(table), false);

		return 1;
	}

	private static int reset(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> e) {
		FogManager.INSTANCE = new FogManager();
		e.getSource().arch$sendSuccess(() -> Text.literal("§b§7[§rFog§b§7]§r ").append(Text.translatable("fog.command.reset").formatted(Formatting.GOLD)), false);
		return 1;
	}

	private static int toggle(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> e) {
		FogConfig config = FogConfig.getInstance();
		config.disableMod = !config.disableMod;

		FogConfig.save();

		e.getSource().arch$sendSuccess(() -> Text.literal("§b§7[§rFog§b§7]§r ").append(
				Text.translatable("fog.command.toggle." + (config.disableMod ? "disabled" : "enabled")).formatted(Formatting.GOLD)), false);

		return 1;
	}
}
