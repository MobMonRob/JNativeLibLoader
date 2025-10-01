package de.dhbw.rahmlab.nativelibloader.impl.util;

/**
 *
 * @author fabian
 */
public class DebugService {

	private DebugService() {
	}

    private static boolean debug = false;

	public static void setDebug(boolean debug) {
		DebugService.debug = debug;
	}

	public static void print(String s) {
        // if (debug) {
			System.err.println(s);
        // }
	}
    
    public static boolean isDebug() {
        return debug;
    }
}
