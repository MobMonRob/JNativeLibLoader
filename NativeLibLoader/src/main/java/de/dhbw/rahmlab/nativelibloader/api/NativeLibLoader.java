package de.dhbw.rahmlab.nativelibloader.api;

import de.dhbw.rahmlab.nativelibloader.impl.BundleInfoImpl;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.jvm.JNILibLoaderBase;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.DynamicLibraryBundle;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.Platform;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.util.VersionNumber;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.util.cache.TempJarCache;
import de.dhbw.rahmlab.nativelibloader.impl.dependencies.MutualDependencySortingService;
import de.dhbw.rahmlab.nativelibloader.impl.jogamp.common.Debug;
import de.dhbw.rahmlab.nativelibloader.impl.jogamp.common.os.PlatformPropsImpl;
import static de.dhbw.rahmlab.nativelibloader.impl.jogamp.common.os.PlatformPropsImpl.OS;
import static de.dhbw.rahmlab.nativelibloader.impl.jogamp.common.os.PlatformPropsImpl.OS_TYPE;
import static de.dhbw.rahmlab.nativelibloader.impl.jogamp.common.os.PlatformPropsImpl.OS_VERSION;
import static de.dhbw.rahmlab.nativelibloader.impl.jogamp.common.os.PlatformPropsImpl.OS_VERSION_NUMBER;
import static de.dhbw.rahmlab.nativelibloader.impl.jogamp.common.os.PlatformPropsImpl.OS_lower;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
     * @param MarkerClass A class which is in the same JAR as the native libs
     * which are wanted to be loaded.
     */
    public void load(Class MarkerClass) throws Exception {
        // Loads all natives from JAR which contains classesFromJavaJars into TempJarCache.
        Optional<Set<String>> addedLibs = JNILibLoaderBase.addNativeJarLibs(MarkerClass, null);

        // Can occur if the JAR which contains the MarkerClass was already processed.
        if (addedLibs.isEmpty()) {
            throw new Exception("No libs could be added.");
        }

        MutualDependencySortingService depService = MutualDependencySortingService.getInstance();
        List<String> sortedLibs = depService.mutualDependencyTopologicalSorting(addedLibs.get());

        // Loads LibNames from TempJarCache into the JVM
        /**
         * Important: To load correctly on windows, libs which depends on each
         * other needs to be given in the order of the topological sorting of
         * their dependency graph with the deepest dependency at the beginning.
         */
        DynamicLibraryBundle dynamicLibraryBundle = new DynamicLibraryBundle(new BundleInfoImpl(sortedLibs));

        if (!dynamicLibraryBundle.isLibComplete()) {
            throw new Exception("Native lib loading failed.");
        } else if (Debug.debugAll()) {
            System.err.println("Native lib loading succeeded.");
        }
    }
}
