/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding;

import de.dhbw.rahmlab.nativelibloader.impl.jogamp.jvm.JNILibLoaderBase;
import de.dhbw.rahmlab.nativelibloader.impl.jogamp.net.Uri;
import de.dhbw.rahmlab.nativelibloader.impl.jogamp.os.NativeLibrary;
import de.dhbw.rahmlab.nativelibloader.impl.jogamp.util.IOUtil;
import de.dhbw.rahmlab.nativelibloader.impl.jogamp.util.cache.TempJarCache;
import de.dhbw.rahmlab.nativelibloader.impl.util.DebugService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static Set<Path> findNativeLibsPaths(Class markerClass) throws NullPointerException, IOException, IllegalArgumentException, URISyntaxException, NoSuchElementException, URISyntaxException {
        Objects.requireNonNull(markerClass);

        final Uri markerClassInternalUri = Uri.valueOf(IOUtil.getClassURL(markerClass.getName(), markerClass.getClassLoader()));

        Set<Path> nativeLibsPaths;

        if (markerClassInternalUri.isJarScheme()) {
            // Loads all natives from JAR which contains classesFromJavaJars into TempJarCache.
            final Set<String> addedLibs = JNILibLoaderBase.addNativeJarLibs(markerClass, null).orElseThrow();

            nativeLibsPaths = new HashSet(addedLibs.size());

            for (String addedLib : addedLibs) {
                String StringLibPath = TempJarCache.findLibrary(addedLib);
                Path libPath = Paths.get(Uri.valueOfFilepath(StringLibPath).toURI());
                nativeLibsPaths.add(libPath);
            }

        } else if (markerClassInternalUri.isFileScheme()) {
            final Path nativesPath = findNativesDirectoryPath(Paths.get(markerClassInternalUri.toURI()));
            nativeLibsPaths = findNativeLibsPath(nativesPath);

        } else {
            throw new IllegalArgumentException("Uri is not of a knows scheme: " + markerClassInternalUri.toString());
        }

        // For safe use of the return value
        nativeLibsPaths.remove(null);

        return nativeLibsPaths;
    }

    private static Set<Path> findNativeLibsPath(final Path nativesPath) throws IOException {
        final Set<Path> nativeLibsPaths = Files.walk(nativesPath)
            .filter(Files::isRegularFile)
            .filter(path -> Objects.nonNull(NativeLibrary.isValidNativeLibraryName(path.toString(), false)))
            .collect(Collectors.toCollection(HashSet<Path>::new));

        return nativeLibsPaths;
    }

    private static Path findNativesDirectoryPath(final Path markerClassPath) throws IOException, NoSuchElementException {
        Optional<Path> nativesPath = Optional.empty();

        Path currentSearchPath = markerClassPath.getParent();
        while (Objects.nonNull(currentSearchPath)) {
            DebugService.print("currentSearchPath: " + currentSearchPath.toString());

            Optional<Path> possibleNativesPath = Files.walk(currentSearchPath, 1).parallel()
                .filter(Files::isDirectory)
                .filter(path -> path.getFileName().toString().equals("natives"))
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
