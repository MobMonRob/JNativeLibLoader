/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dhbw.rahmlab.nativelibloader.maintenance;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

//qdox JavaDoc: https://javadoc.io/doc/com.thoughtworks.qdox/qdox/latest/index.html
/**
 * Der Sinn dieser Klasse ist es, automatisiert herauszufinden, welche Klassen
 * von jogamp gluegen nicht notwendig sind.
 *
 * @author fabian
 */
public class UnusedGluegenClasses {

    public static List<JavaClass> get_unusedGluegenClasses() {
        List<JavaClass> allClasses = get_AllClasses("/home/fabian/Schreibtisch/JViconDataStream2/JViconDataStream/src/main/java/");
        List<String> firstImports = get_firstClassImports(allClasses, "JViconDataStreamBundleInfo");
        allClasses = allClasses
            .stream()
            .filter(cl -> !cl.getCanonicalName().startsWith("de.dhbw."))
            .collect(Collectors.toCollection(ArrayList::new));

        Map<String, JavaClass> namesOfClasses = get_namesOfClasses(allClasses);

        //Not found via import
        String[] definitelyNeededClassesHint = new String[]{
            "jogamp.common.os.elf.IOUtils",
            "jogamp.common.os.UnixDynamicLinkerImpl",
            "com.jogamp.common.net.PiggybackURLContext",
            "com.jogamp.common.nio.NativeBuffer",
            "com.jogamp.common.os.DynamicLookupHelper",
            "com.jogamp.common.os.DynamicLinker",
            "com.jogamp.common.os.DynamicLibraryBundle",
            "com.jogamp.common.util.cache.TempFileCache",
            "com.jogamp.common.util.CustomCompress",
            "com.jogamp.common.util.InterruptSource",
            "com.jogamp.common.net.AssetURLStreamHandler",
            "com.jogamp.common.net.GenericURLStreamHandlerFactory",
            "com.jogamp.common.nio.PointerBuffer",
            "com.jogamp.common.os.DynamicLinkerImpl",
            "jogamp.common.os.elf.Ehdr_p1",
            "jogamp.common.os.elf.SectionHeader",
            "jogamp.common.os.elf.Section",
            "jogamp.common.os.DynamicLinkerImpl",
            "com.jogamp.common.net.PiggybackURLConnection",
            "com.jogamp.common.nio.AbstractBuffer",
            "com.jogamp.common.util.HashUtil",
            "jogamp.common.os.elf.Ehdr_p2",
            "jogamp.common.os.elf.Shdr",
            "com.jogamp.common.nio.StructAccessor"
        };

        firstImports.addAll(Arrays.asList(definitelyNeededClassesHint));

        Set<String> usedImports = get_usedImports(namesOfClasses, firstImports);
        List<JavaClass> unusedClasses = get_unusedClasses(namesOfClasses, usedImports);

        return unusedClasses;
    }

    private static List<String> get_firstClassImports(List<JavaClass> allClasses, String firstClassName) {
        List<JavaClass> bundleInfos = allClasses
            .stream()
            .filter(cl -> cl.getCanonicalName().contains(firstClassName))
            .collect(Collectors.toCollection(ArrayList::new));

        assert bundleInfos.size() == 1;
        JavaClass bundleInfo = bundleInfos.get(0);

        List<String> rootClassImports = bundleInfo
            .getSource()
            .getImports()
            .stream()
            .map(s -> normalizeImport(s))
            .filter(s -> !s.startsWith("java"))
            .collect(Collectors.toCollection(ArrayList::new));

        return rootClassImports;
    }

    private static List<JavaClass> get_unusedClasses(Map<String, JavaClass> namesOfClasses, Set<String> usedImports) {
        Set<String> unusedClassesNames = new HashSet();
        unusedClassesNames.addAll(namesOfClasses.keySet());
        unusedClassesNames.removeAll(usedImports);

        List<JavaClass> unusedClasses = unusedClassesNames
            .stream()
            .sorted()
            .map(s -> namesOfClasses.get(s))
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));

        assert unusedClassesNames.size() == unusedClasses.size();

        return unusedClasses;
    }

    private static String normalizeImport(String importString) {
        if (importString.startsWith("static ")) {
            return importString.substring(7);
        }

        return importString;
    }

    private static Set<String> get_usedImports(Map<String, JavaClass> namesOfClasses, List<String> firstImports) {
        Set<String> newImports = new HashSet();
        Set<String> processedImports = new HashSet();

        newImports.addAll(firstImports);

        while (!processedImports.containsAll(newImports)) {
            processedImports.addAll(newImports); //Passt so. Ich will ja nur wissen, welche es gibt.

            List<String> currentImports = newImports
                .stream() //List<String>
                .map(importString -> namesOfClasses.get(importString)) //List<JavaClass>
                .filter(Objects::nonNull)
                .map(cl -> cl.getSource().getImports()) //List<List<String>>
                .reduce(new ArrayList(), (sumList, importsList) -> listConcat(sumList, importsList)); //List<String>

            newImports = currentImports //filtered
                .stream() //List<String>
                .map(s -> normalizeImport(s)) //List<String>
                .filter(s -> !s.startsWith("java")) //List<String>
                .collect(Collectors.toCollection(HashSet::new));
        }

        return processedImports;
    }

    private static <T> List<T> listConcat(List<T> sumList, List<T> itemList) {
        sumList.addAll(itemList);

        return sumList;
    }

    private static List<JavaClass> get_AllClasses(String javaFolderPath) {
        JavaProjectBuilder builder = new JavaProjectBuilder();
        builder.addSourceTree(new File(javaFolderPath));

        List<List<JavaClass>> classesListSquared = builder
            .getSources() //<List<JavaSource>>
            .stream()
            .map(src -> src.getClasses()) //List<List<JavaClass>>
            .collect(Collectors.toCollection(ArrayList::new));

        List<JavaClass> classesList = classesListSquared
            .stream()
            .reduce(new ArrayList(), (sumList, javaClassList) -> listConcat(sumList, javaClassList));

        return classesList;
    }

    private static Map<String, JavaClass> get_namesOfClasses(List<JavaClass> allClasses) {
        HashMap<String, JavaClass> namesOfClasses = new HashMap();

        List<String> allClassesNames = allClasses
            .stream() //List<JavaClass>
            .map(cl -> cl.getCanonicalName()) //List<String>
            .collect(Collectors.toCollection(ArrayList::new));

        assert allClasses.size() == allClassesNames.size();

        for (int i = 0; i < allClasses.size(); ++i) {
            namesOfClasses.put(allClassesNames.get(i), allClasses.get(i));
        }

        return namesOfClasses;
    }
}
