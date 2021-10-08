package de.dhbw.rahmlab.nativelibloader.impl.dependencies;

import de.dhbw.rahmlab.nativelibloader.impl.jogamp.common.Debug;

/**
 *
 * @author fabian
 */
public class DebugService {

    private DebugService() {
    }

    public static void print(String s) {
        if (Debug.debugAll()) {
            System.err.println(s);
        }
    }
}
