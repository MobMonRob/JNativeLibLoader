package de.dhbw.rahmlab.nativelibloader.api;

import de.dhbw.rahmlab.nativelibloader.impl.BundleInfoImpl;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.jvm.JNILibLoaderBase;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.DynamicLibraryBundle;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.Platform;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.util.cache.TempJarCache;
import de.dhbw.rahmlab.nativelibloader.impl.dependencies.DebugService;
import de.dhbw.rahmlab.nativelibloader.impl.dependencies.MutualBundleDependencySortingService;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
     * @param MarkerClass A class within the same JAR as the native libs which
     * are wanted to be loaded.
     * @throws java.lang.Exception if native libs not found in the jar of the
     * given class or cauld not be loaded from this jar.
     */ 
    public void load(Class MarkerClass) throws Exception {
        // Loads all natives from JAR which contains classesFromJavaJars into TempJarCache.
        // throws Exception
        Optional<Set<String>> addedLibs = JNILibLoaderBase.addNativeJarLibs(MarkerClass, null);

        // Can occur if the JAR which contains the MarkerClass was already processed.
        if (addedLibs.isEmpty()) {
            throw new RuntimeException("No libs could be added.");
        }

        /**
         * To load correctly on windows, libs which depends on each other needs
         * to be given in the order of the topological sorting of their
         * dependency graph with the deepest dependency at the beginning.
         * Otherwise dependend libs will not be found.
         */
        MutualBundleDependencySortingService depService = MutualBundleDependencySortingService.getInstance();
        List<String> sortedLibs = depService.mutualBundleDependencyTopologicalSorting(addedLibs.get());

        // Loads LibNames from TempJarCache into the JVM
        DynamicLibraryBundle dynamicLibraryBundle = new DynamicLibraryBundle(new BundleInfoImpl(sortedLibs));

        if (!dynamicLibraryBundle.isLibComplete()) {
            throw new RuntimeException("Native lib loading failed.");
        } else {
            DebugService.print("Native lib loading succeeded.");
        }
    }
}
