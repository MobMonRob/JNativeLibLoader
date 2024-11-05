package de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding;

import de.dhbw.rahmlab.nativelibloader.impl.util.DebugService;
import de.dhbw.rahmlab.nativelibloader.impl.util.Platform;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author fabian
 */
public class NativeLibsPathsFinderService {

	private static String getClassFileName(final String clazzBinName) {
		// or return clazzBinName.replace('.', File.separatorChar) + ".class"; ?
		return clazzBinName.replace('.', '/') + ".class";
	}

	private static URL getClassURL(Class clazz) throws IOException {
		final String clazzBinName = clazz.getName();
		final ClassLoader cl = clazz.getClassLoader();
		final URL url = cl.getResource(getClassFileName(clazzBinName));
		if (null == url) {
			throw new IOException("Cannot find: " + clazzBinName);
		}
		return url;
	}

	public static Set<Path> findNativeLibsPaths(Class markerClass, String nativesFolderName) throws NullPointerException, IOException, URISyntaxException {
		Objects.requireNonNull(markerClass);

		URL markerClassURL = getClassURL(markerClass);
		DebugService.print(String.format("markerClass URL: %s", markerClassURL));
		String scheme = markerClassURL.getProtocol();

		Set<Path> nativeLibsPaths;

		if (scheme.equals("jar")) {
			nativeLibsPaths = JarCacheService.cacheLibs(markerClassURL, nativesFolderName);

		} else if (scheme.equals("file")) {
			Path nativesPath = findNativesDirectoryPath(Paths.get(markerClassURL.toURI()), nativesFolderName);
			Path archNativesPath = nativesPath.resolve(Platform.PLATFORM_DIR_NAME);
			nativeLibsPaths = findNativeLibsPath(archNativesPath);

		} else {
			throw new IllegalArgumentException("Uri is not of a knows scheme: " + markerClassURL.toString());
		}

		// For safe use of the return value
		nativeLibsPaths.remove(null);

		return Collections.unmodifiableSet(nativeLibsPaths);
	}

	private static Set<Path> findNativeLibsPath(final Path archNativesPath) throws IOException {
		final Set<Path> nativeLibsPaths = Files.walk(archNativesPath)
			.filter(Files::isRegularFile)
			.collect(Collectors.toCollection(HashSet<Path>::new));

		return nativeLibsPaths;
	}

	private static Path findNativesDirectoryPath(final Path markerClassPath, String nativesFolderName) throws IOException, NoSuchElementException {
		Optional<Path> nativesPath = Optional.empty();

		Path currentSearchPath = markerClassPath.getParent();
		while (Objects.nonNull(currentSearchPath)) {
			DebugService.print("currentSearchPath: " + currentSearchPath.toString());

			Optional<Path> possibleNativesPath = Files.walk(currentSearchPath, 1).parallel()
				.filter(Files::isDirectory)
				.filter(path -> path.getFileName().toString().equals(nativesFolderName))
				.findAny();

			if (possibleNativesPath.isPresent()) {
				nativesPath = possibleNativesPath;
				DebugService.print("Found natives directory Path: " + nativesPath.get().toString());
				break;
			}

			currentSearchPath = currentSearchPath.getParent();
		}

		return nativesPath.orElseThrow();
	}
}
