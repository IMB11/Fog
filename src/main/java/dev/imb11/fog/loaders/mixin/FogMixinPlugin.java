package dev.imb11.fog.loaders.mixin;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static dev.imb11.fog.client.FogClient.isModInstalled;

public class FogMixinPlugin implements IMixinConfigPlugin {
	private static final @NotNull Supplier<Boolean> TRUE = () -> true;
	private static final @NotNull String SODIUM_MOD_ID = "sodium";
	private static final @NotNull String EMBEDDIUM_MOD_ID = "embeddium";
	private static final @NotNull Map<String, Supplier<Boolean>> CONDITIONS = ImmutableMap.of(
			"dev.imb11.fog.mixin.client.rendering.WorldRendererMixin",
			() -> !isModInstalled(SODIUM_MOD_ID) && !isModInstalled(EMBEDDIUM_MOD_ID)
	);

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return CONDITIONS.getOrDefault(mixinClassName, TRUE).get();
	}

	@Override
	public void onLoad(String mixinPackage) {}

	@Override
	public @Nullable String getRefMapperConfig() {
		return null;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

	@Override
	public @Nullable List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
