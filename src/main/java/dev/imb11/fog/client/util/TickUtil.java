package dev.imb11.fog.client.util;

import net.minecraft.client.MinecraftClient;

public class TickUtil {
	public static float getTickDelta() {
		/*? if <1.21 {*/
		/*return client.getTickDelta();*/
		/*?} else if >=1.21.5 {*/
		return MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(true);
		/*?} else {*/
		/*return MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);*/
		/*?}*/
	}
}
