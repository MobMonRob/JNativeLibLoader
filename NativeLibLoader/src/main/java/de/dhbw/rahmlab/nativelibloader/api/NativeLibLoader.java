package de.dhbw.rahmlab.nativelibloader.api;

import de.dhbw.rahmlab.nativelibloader.impl.jogamp.os.Platform;
import de.dhbw.rahmlab.nativelibloader.impl.jogamp.util.cache.TempJarCache;
import de.dhbw.rahmlab.nativelibloader.impl.util.DebugService;
import de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding.NativeLib;
import de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding.SortedNativeLibProviderService;
import java.util.List;
import java.util.Objects;

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
