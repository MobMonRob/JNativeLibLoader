package de.dhbw.rahmlab.nativelibloader.api;

import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.NativeLibrary;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.Platform;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.util.cache.TempJarCache;
import de.dhbw.rahmlab.nativelibloader.impl.util.DebugService;
import de.dhbw.rahmlab.nativelibloader.impl._tmp.MutualBundleDependencyReverseTopologicalSortingService;
import de.dhbw.rahmlab.nativelibloader.impl._tmp.NativeLibsDependenciesGetterService;
import de.dhbw.rahmlab.nativelibloader.impl._tmp.NativeLibsPathsFinderService;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author fabian
 */
public class NativeLibLoader {

    private static NativeLibLoader instance = null;

    private NativeLibLoader() {
        Platform.initSingleton();
        TempJarCache.initSingleton();
    }

    public static void init(boolean debug) throws Exception {
        if (instance != null) {
            throw new Exception("Can only be set once!");
        }

        if (debug == true) {
            System.setProperty("jogamp.debug", "true");
            System.setProperty("jogamp.verbose", "true");
        }

        instance = new NativeLibLoader();
    }

    /**
     * {@link #initSetDebug(boolean) initSetDebug} needs to be invoked before
     * you can use this method!
     */
    public static NativeLibLoader getInstance() throws Exception {
        if (instance == null) {
            throw new Exception("Only possible after 'initSetDebug' Method!");
        }

        return instance;
    }

    /**
     * OS must be Windows or Linux
     *
     * @param markerClass A class within the same JAR as the native libs which
     * are wanted to be loaded.
     */
    public void load(Class markerClass) throws Exception {
        // Retrieve native lib paths
        final Set<Path> nativeLibsPaths = NativeLibsPathsFinderService.findNativeLibsPaths(markerClass);

        // Map normalized lib names to their paths
        Map<String, Path> libNamesToPaths = new HashMap<String, Path>(nativeLibsPaths.size());
        for (Path nativeLibPath : nativeLibsPaths) {
            String normalizedLibName = NativeLibrary.isValidNativeLibraryName(nativeLibPath.toString(), false);
            if (Objects.isNull(normalizedLibName)) {
                DebugService.print("Not a valid library name, therefore excluded: " + nativeLibPath);
                continue;
            }
            libNamesToPaths.put(normalizedLibName, nativeLibPath);
        }

        // Map libs to their bundle dependencies
        Map<String, Set<String>> libsToDeps = new HashMap();
        for (Entry<String, Path> libNameToPath : libNamesToPaths.entrySet()) {
            String libName = libNameToPath.getKey();
            Path libPath = libNameToPath.getValue();
            Set<String> deps = NativeLibsDependenciesGetterService.getDeps(libPath.toString());

            // Keep only those dependencies which are part of the bundle
            Set<String> bundleDeps = deps.stream()
                .map(dep -> NativeLibrary.isValidNativeLibraryName(dep, false))
                .filter(Objects::nonNull)
                .filter(dep -> libNamesToPaths.containsKey(dep))
                .collect(Collectors.toCollection(HashSet<String>::new));

            libsToDeps.put(libName, bundleDeps);
        }

        List<String> sortedLibs = MutualBundleDependencyReverseTopologicalSortingService.sort(libsToDeps);

        // Loads LibNames from TempJarCache into the JVM
        for (String lib : sortedLibs) {
            final String path = libNamesToPaths.get(lib).toString();
            DebugService.print("Load next lib: " + lib + " - " + path);
            System.load(path);
        }

        DebugService.print("Native lib loading finished.");
    }
}
