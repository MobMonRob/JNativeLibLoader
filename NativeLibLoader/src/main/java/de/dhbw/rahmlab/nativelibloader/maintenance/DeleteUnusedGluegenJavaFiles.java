/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dhbw.rahmlab.nativelibloader.maintenance;

import com.thoughtworks.qdox.model.JavaClass;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author fabian
 */
public class DeleteUnusedGluegenJavaFiles {

    public static void delete() {
        List<JavaClass> unusedClasses = UnusedGluegenClasses.get_unusedGluegenClasses();

        System.out.println("-------------");
        System.out.println("---UnusedClasses:");
        unusedClasses.forEach(cl -> System.out.println(cl.getCanonicalName()));

        List<String> unusedPath = unusedClasses
            .stream()
            .map(cl -> cl.getSource().getURL().getPath())
            .collect(Collectors.toCollection(ArrayList::new));

        for (String path : unusedPath) {
            File file = new File(path);
            file.delete();
        }

        PatchGluegenClasses.patch();
    }
}
