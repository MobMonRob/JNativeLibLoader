package de.dhbw.rahmlab.nativelibloader.api;

import de.dhbw.rahmlab.nativelibloader.impl.jogamp.os.Platform;
import de.dhbw.rahmlab.nativelibloader.impl.jogamp.util.cache.TempJarCache;
import de.dhbw.rahmlab.nativelibloader.impl.util.DebugService;
import de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding.NativeLib;
import de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding.SortedNativeLibProviderService;
import java.util.List;
import java.util.Objects;

/**
 * @author Fabian Hinderer
 */
public class NativeLibLoader {

    private static NativeLibLoader instance = null;

    private NativeLibLoader() {
        Platform.initSingleton();
        TempJarCache.initSingleton();
    }

    /**
     * Initialization of the native lib loader.
     * 
     * @param debug if debug set to true debug messages are print to System.err
     * @throws RuntimeException if the method is invoked more than once
     */
    public static void init(boolean debug) throws RuntimeException {
        if (instance != null) {
            throw new RuntimeException("Can only be set once!");
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
     * 
     * @return Instance of native lib loader
     * @throws java.lang.RuntimeException if the method is invoked before init
     */
    public static NativeLibLoader getInstance() throws RuntimeException {
        if (instance == null) {
            throw new RuntimeException("Only possible after 'initSetDebug' Method!");
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
        // Not static in order to avoid check if inited.

        Objects.requireNonNull(markerClass);

        final List<NativeLib> sortedLibs = SortedNativeLibProviderService.getSortedNativeLibs(markerClass);

        // Load libs into the JVM.
        for (NativeLib lib : sortedLibs) {
            String name = lib.getName().toString();
            String path = lib.getPath().toString();
            DebugService.print("Load next lib: " + name + " - " + path);
            System.load(path);
        }

        DebugService.print("Native lib loading finished.");
    }
}
