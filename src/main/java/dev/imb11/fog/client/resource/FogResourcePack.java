package dev.imb11.fog.client.resource;

import net.minecraft.resource.*;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Attribution: <a href="https://github.com/gbl/CrowdinTranslate">CrowdinTranslate</a> and <a href="https://github.com/IMB11/Loqui">Loqui</a>.
 */
public class FogResourcePack implements ResourcePack {
	@Override
	public @Nullable InputSupplier<InputStream> openRoot(String... strings) {
		String fileName = String.join("/", strings);

		Path packPath = FogResourceUnpacker.UNPACKED_PATH;
		Path filePath = packPath.resolve(fileName);

		if (filePath.toFile().exists()) {
			return () -> Files.newInputStream(filePath);
		}

		return null;
	}

	@Override
	public @Nullable InputSupplier<InputStream> open(ResourceType type, Identifier id) {
		return this.openRoot(type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath());
	}

	@Override
	public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
		String start = FogResourceUnpacker.UNPACKED_PATH + "/" + type.getDirectory() + "/" + namespace + "/" + prefix;
		String[] files = new File(start).list();

		if (files == null || files.length == 0) {
			return;
		}

		List<Identifier> resultList = Arrays.stream(files)
		                                    .map(file -> Identifier.of(namespace, prefix + "/" + file))
		                                    .toList();

		for (Identifier result : resultList) {
			consumer.accept(result, open(type, result));
		}
	}

	@Override
	public @NotNull Set<String> getNamespaces(ResourceType packType) {
		return FogResourceUnpacker.getNamespaces();
	}

	@Override
	public @Nullable <T> T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
		InputSupplier<InputStream> inputSupplier = this.openRoot("pack.mcmeta");

		if (inputSupplier != null) {
			try (InputStream input = inputSupplier.get()) {
				return AbstractFileResourcePack.parseMetadata(metaReader, input);
			}
		} else {
			return null;
		}
	}

	/*? if >=1.20.6 {*/
	@Override
	public ResourcePackInfo getInfo() {
		return new ResourcePackInfo("fog", Text.literal("Fog Definitions"), ResourcePackSource.BUILTIN, Optional.empty());
	}
	/*?}*/

	/*? if <=1.20.4 {*/
	/*@SuppressWarnings("CommentedOutCode")
	@Override
	public String getName() {
		return "Fog Definitions";
	}
	*//*?} else {*/
	@SuppressWarnings("CommentedOutCode")
	@Override
	public String getId() {
		return "fog";
	}
	/*?}*/

	@Override
	public void close() {
	}
}
