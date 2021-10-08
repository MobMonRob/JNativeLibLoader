/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dhbw.rahmlab.nativelibloader.impl.util;

import de.dhbw.rahmlab.nativelibloader.impl.jogamp.other.Debug;

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
