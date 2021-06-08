package de.dhbw.rahmlab.nativelibloader.api;

import de.dhbw.rahmlab.nativelibloader.impl.BundleInfoImpl;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.jvm.JNILibLoaderBase;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.DynamicLibraryBundle;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.DynamicLibraryBundleInfo;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.Platform;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.util.cache.TempJarCache;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fabian
 */
public class NativeLibLoader {

    private static final List<DynamicLibraryBundle> dynamicLibraryBundles = new ArrayList<>();

    private static NativeLibLoader instance = null;

    private NativeLibLoader() {
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
        Platform.initSingleton();
        return Platform.OS_TYPE == Platform.OSType.WINDOWS;
    }

    public boolean onLinux() {
        Platform.initSingleton();
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
        List<DynamicLibraryBundleInfo> dynamicLibraryBundleInfos = new ArrayList<>();
        dynamicLibraryBundleInfos.add(new BundleInfoImpl(libNames));

        //Init
        Platform.initSingleton();
        TempJarCache.initSingleton();

        /**
         * Hier werden die .so Dateien aus einem JAR in den JarCache geladen.
         *
         * Die Klasse in classesFromJavaJars fungiert dabei als ein Marker. In
         * dem JAR, wo sich die korrespondierende .class Datei befindet, wird
         * nach .so Dateien gesucht. Alle dort vorhandenen .so Dateien werden in
         * den JarCache geladen. Es muss sich dabei nicht um die Klasse handeln,
         * die mit native Aufrufen von den .so Dateien abhängig ist.
         *
         * Wenn die richtigen .so Dateien schon aus einem JAR in
         * Platform.initSingleton() geladen wurden (wegen gluegen_rt.so), dann
         * nicht unbedingt notwendig.
         *
         * Debug Info: Dort werden die .so Dateien geladen:
         * //JNILibLoaderbase::addNativeJarLibsImpl(...) [Line 186] (gleiche
         * Datei) Da werden sie dann in den JarCache geladen: //ok =
         * TempJarCache.addNativeLibs(classFromJavaJar, nativeJarURI,
         * nativeLibraryPath); [Line 229]
         */
        final Class[] classesFromJavaJars = new Class[]{MarkerClass};
        JNILibLoaderBase.addNativeJarLibs(classesFromJavaJars, null);

        dynamicLibraryBundles.addAll(
            dynamicLibraryBundleInfos
                .stream()
                .map(bi -> {
                    //Hier werden die .so Datein (vom JarCache) in die JVM geladen.
                    return new DynamicLibraryBundle(bi);
                })
                .collect(Collectors.toCollection(ArrayList::new)));

        dynamicLibraryBundles.forEach(b -> {
            if (!b.isLibComplete()) {
                System.out.println("Native lib loading failed: " + b.getBundleInfo().getClass().getCanonicalName());
            } else {
                System.out.println("Native lib loading succeeded: " + b.getBundleInfo().getClass().getCanonicalName());
            }
        });
    }
}
