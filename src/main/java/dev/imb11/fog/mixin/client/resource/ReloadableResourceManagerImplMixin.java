package dev.imb11.fog.mixin.client.resource;

import dev.imb11.fog.client.FogClient;
import dev.imb11.fog.client.resource.FogResourcePack;
import dev.imb11.fog.client.resource.FogResourceUnpacker;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableResourceManagerImpl.class)
public class ReloadableResourceManagerImplMixin {
	@Shadow
	@Final
	private ResourceType type;

	@ModifyArg(
			method = "reload",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/LifecycledResourceManagerImpl;<init>(Lnet/minecraft/resource/ResourceType;Ljava/util/List;)V"),
			index = 1
	)
	private List<ResourcePack> onPostReload(List<ResourcePack> packs) {
		FogClient.LOGGER.info("onPostReload: " + this.type);
		if (this.type != ResourceType.CLIENT_RESOURCES)
			return packs;

		FogClient.LOGGER.info("Preparing namespace walking.");
		FogResourceUnpacker.walkNamespaces();
		FogClient.LOGGER.info("Walked namespaces.");
		List<ResourcePack> list = new ArrayList<>(packs);
		list.add(0, new FogResourcePack());
		FogClient.LOGGER.info("Added Fog Resource Pack to the resource pack list.");
		return list;
	}
}
