package de.dhbw.rahmlab.nativelibloader.api;

import de.dhbw.rahmlab.nativelibloader.impl.BundleInfoImpl;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.jvm.JNILibLoaderBase;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.DynamicLibraryBundle;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.DynamicLibraryBundleInfo;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.Platform;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.util.cache.TempJarCache;
import de.dhbw.rahmlab.nativelibloader.impl.jogamp.common.Debug;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fabian
 */
public class NativeLibLoader {

    private final List<DynamicLibraryBundle> dynamicLibraryBundles = new ArrayList<>();

    private static NativeLibLoader instance = null;

    private NativeLibLoader() {
        //Init
        Platform.initSingleton();
        TempJarCache.initSingleton();
    }

    public static NativeLibLoader getInstanceAndSetDebugIfFirstInvokation(boolean debug) {
        if (instance != null) {
            return instance;
        }

        if (debug == true) {
            System.setProperty("jogamp.debug", "true");
            System.setProperty("jogamp.verbose", "true");
        }

        instance = new NativeLibLoader();
        return instance;
    }

    public boolean onWindows() {
        return Platform.OS_TYPE == Platform.OSType.WINDOWS;
    }

    public boolean onLinux() {
        return Platform.OS_TYPE == Platform.OSType.LINUX;
    }

    /**
     * Important: To load correctly on windows, libs which depends on each other
     * needs to be given in the order of the topological sorting of their
     * dependency graph with the deepest dependency at the beginning.
     *
     * @param libNames Names of native libs. -> "name" for libname.so
     * @param MarkerClass A class which is in the same JAR as the native libs
     * which are wanted to be loaded.
     */
    public void load(List<String> libNames, Class MarkerClass) {
        // Prepare parameter
        DynamicLibraryBundleInfo dynamicLibraryBundleInfo = new BundleInfoImpl(libNames);
        final Class[] classesFromJavaJars = new Class[]{MarkerClass};

        // Loads all natives from JAR which contains classesFromJavaJars into TempJarCache.
        JNILibLoaderBase.addNativeJarLibs(classesFromJavaJars, null);

        // Loads LibNames from TempJarCache into the JVM
        DynamicLibraryBundle dynamicLibraryBundle = new DynamicLibraryBundle(dynamicLibraryBundleInfo);

        dynamicLibraryBundles.add(dynamicLibraryBundle);

        if (!dynamicLibraryBundle.isLibComplete()) {
            System.err.println("Native lib loading failed");
        } else if (Debug.debugAll()) {
            System.err.println("Native lib loading succeeded");
        }
    }
}
