package dev.imb11.fog.mixin.client.resources;

import dev.imb11.fog.client.resource.FogResourcePack;
import dev.imb11.fog.client.resource.FogResourceUnpacker;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(LifecycledResourceManagerImpl.class)
public class ReloadableResourceManagerImplMixin {

	@ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
	private static List<ResourcePack> fog$modifyResourcePackList(List<ResourcePack> packs) {
		FogResourceUnpacker.walkNamespaces();
		List<ResourcePack> list = new ArrayList<>(packs);
		list.add(0, new FogResourcePack());
		return list;
	}
}
