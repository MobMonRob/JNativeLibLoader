/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dhbw.rahmlab.nativelibloader.maintenance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author fabian
 */
public class PatchGluegenClasses {

    private static final String patchPath = "/home/fabian/Schreibtisch/JViconDataStream2/JViconDataStream/src/main/java/de/dhbw/rahmlab/vicon/datastream/maintenance/";
    private static final String javaFilePath = "/home/fabian/Schreibtisch/JViconDataStream2/JViconDataStream/src/main/java/jogamp/common/os/elf/";

    private static final String[] fileNames = new String[]{
        "Ehdr_p1",
        "Ehdr_p2",
        "Shdr"
    };

    public static void patch() {
        for (String fileName : fileNames) {
            try {
                patch_single(fileName);
            } catch (IOException ex) {
                System.out.println("patching failed: " + fileName);
            }
        }
    }

    private static void patch_single(String fileName) throws IOException {
        String fullFileName = javaFilePath + fileName + ".java";
        String fullPatchName = patchPath + fileName + ".patch";

        ProcessBuilder builder = new ProcessBuilder("patch", "-u", "-N",fullFileName, fullPatchName);
        Process process = builder.start();

        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append("\n");
            }
            System.out.println(out);
        }
    }
}
