package de.dhbw.rahmlab.nativelibloader.api;

import de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding.SortedNativeLibProviderService;
import de.dhbw.rahmlab.nativelibloader.impl.util.DebugService;
import java.util.List;
import java.util.Objects;

public class NativeLibLoader {

	private NativeLibLoader() {
	}

	@Deprecated
	public static void init(boolean debug) {
		setDebug(debug);
	}

	@Deprecated
	public static NativeLibLoader getInstance() {
		return new NativeLibLoader();
	}

	/**
	 * @param debug if debug set to true debug messages are print to System.err
	 */
	public static void setDebug(boolean debug) {
		DebugService.setDebug(debug);
	}

	/**
	 * <pre>
	 * Caution: Using a folder named "natives" can lead to problems with projects depending (indirectly) on Jogamp.
	 * Loads libs from a folder "natives".
	 * OS must be Windows or Linux.
	 * </pre>
	 *
	 * @param markerClass A class within the same JAR as the native libs which are wanted to be loaded.
	 * @return The loaded libs.
	 */
	@Deprecated
	public static List<NativeLib> load(Class markerClass) throws Exception {
		return load(markerClass, "natives");
	}

	/**
	 * <pre>
	 * Loads libs from a folder "nativeLibs".
	 * OS must be Windows or Linux.
	 * <pre>
	 *
	 * @param markerClass A class within the same JAR as the native libs which are wanted to be loaded.
	 */
	public static List<NativeLib> loadLibs(Class markerClass) throws Exception {
		return load(markerClass, "nativeLibs");
	}

	/**
	 * OS must be Windows or Linux.
	 *
	 * @param markerClass A class within the same JAR as the native libs which are wanted to be loaded.
	 * @param nativesFolderName name of the folder containing the platform specific natives folders.
	 * @return The loaded libs.
	 */
	public static synchronized List<NativeLib> load(Class markerClass, String nativesFolderName) throws Exception {
		// Not static in order to avoid check if inited.

		Objects.requireNonNull(markerClass);

		final List<NativeLib> sortedLibs = SortedNativeLibProviderService.getSortedNativeLibs(markerClass, nativesFolderName);

		// Load libs into the JVM.
		for (NativeLib lib : sortedLibs) {
			String name = lib.getName().toString();
			String path = lib.getPath().toString();
			DebugService.print("Load next lib: " + name + " - " + path);
			System.load(path);
		}

		DebugService.print("Native lib loading finished.");

		return sortedLibs;
	}
}
