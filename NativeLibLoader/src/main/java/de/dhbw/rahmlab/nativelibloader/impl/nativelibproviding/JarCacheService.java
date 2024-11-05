package de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding;

import de.dhbw.rahmlab.nativelibloader.impl.util.Platform;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class JarCacheService {

	/**
	 * Precondition: markerClassURL.getProtocol() == "jar".
	 */
	public static Set<Path> cacheLibs(URL markerClassURL, String nativesFolderName) throws IOException {
		String prefix = String.format("%s/%s/", nativesFolderName, Platform.PLATFORM_DIR_NAME);

		Set<Path> cachedLibs = new HashSet<>();
		try (var jarFile = ((JarURLConnection) markerClassURL.openConnection()).getJarFile()) {
			var jarEntries = jarFile.stream()
				.filter(e -> !e.isDirectory())
				.filter(e -> e.getName().startsWith(prefix))
				.toList();
			// jarEntries.forEach(System.out::println);

			// UUID.randomUUID().toString()
			// There could be multiple parallel usages of JNativeLibLoader.
			// ToDo: Find a solution to delete all directories which are not in active use any more and failed to deleteOnExit.
			var tmpDir = Files.createTempDirectory("JNativeLibLoader_").toFile();
			var tmpDirPath = tmpDir.toPath();

			for (var jarEntry : jarEntries) {
				Path entryTargetPath = tmpDirPath.resolve(jarEntry.getName());
				try (InputStream entryInputStream = jarFile.getInputStream(jarEntry)) {
					Files.createDirectories(entryTargetPath.getParent());
					Files.copy(entryInputStream, entryTargetPath);
				}
				cachedLibs.add(entryTargetPath);
			}

			// Note: deleteOnExit does not always work.
			Files.walk(tmpDirPath).forEach(p -> p.toFile().deleteOnExit());
		}

		return cachedLibs;
	}
}
