package dev.imb11.fog.client.command;

import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.imb11.fog.client.FogManager;
import dev.imb11.fog.client.util.color.Color;
import dev.imb11.fog.config.FogConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	private static int outputDebug(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> commandContext) {
		MinecraftClient client = MinecraftClient.getInstance();

		/*? if <1.21 {*/
		/*float tickDelta = client.getTickDelta();
		*//*?} else {*/
		float tickDelta = client.getRenderTickCounter().getTickDelta(true);
		/*?}*/

		FogManager manager = FogManager.INSTANCE;

		String hexColor = Integer.toHexString(
				new Color((int) (manager.fogColorRed.get(tickDelta) * 255), (int) (manager.fogColorGreen.get(tickDelta) * 255),
						(int) (manager.fogColorBlue.get(tickDelta) * 255)
				).toInt());
		hexColor = "§c" + hexColor.substring(0, 2) + "§a" + hexColor.substring(2, 4) + "§9" + hexColor.substring(4);

		@Nullable ClientWorld clientWorld = client.world;
		if (clientWorld == null) {
			commandContext.getSource().arch$sendFailure(Text.translatable("fog.command.debug.failure"));
			return 0;
		}

		@SuppressWarnings("TextBlockMigration") @NotNull String debugInfoTable = String.format(
				"§b§7[§rFog§b§7]§r Current Fog Manager State:\n" +
						"§b§7[§rFog§b§7]§r Raininess: §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r \"Undergroundness\": §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r Fog Start: §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r Fog End: §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r Darkness: §6%.2f§r\n" +
						"§b§7[§rFog§b§7]§r Fog Color (Before Haze): §6#%s§r\n" +
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
				manager.currentSkyLight.get(tickDelta),
				manager.currentBlockLight.get(tickDelta),
				manager.currentLight.get(tickDelta),
				manager.currentStartMultiplier.get(tickDelta),
				manager.currentEndMultiplier.get(tickDelta)
		);

		commandContext.getSource().arch$sendSuccess(() -> Text.literal(debugInfoTable), false);
		return 1;
	}

	private static int reset(@NotNull CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> commandContext) {
		FogConfig.load();
		FogManager.INSTANCE = new FogManager();
		commandContext.getSource().arch$sendSuccess(
				() -> Text.literal("§b§7[§rFog§b§7]§r ").append(Text.translatable("fog.command.reset").formatted(Formatting.GOLD)), false);
		return 1;
	}

	private static int toggle(@NotNull CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> commandContext) {
		FogConfig config = FogConfig.getInstance();
		config.disableMod = !config.disableMod;

		FogConfig.save();

		commandContext.getSource().arch$sendSuccess(() -> Text.literal("§b§7[§rFog§b§7]§r ").append(
				Text.translatable("fog.command.toggle." + (config.disableMod ? "disabled" : "enabled")).formatted(Formatting.GOLD)), false);

		return 1;
	}
}
