/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dhbw.rahmlab.nativelibloader.impl.dependencies;

import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.Platform;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.util.cache.TempJarCache;
import de.dhbw.rahmlab.nativelibloader.impl.dependencies.Elf.EndianElf.SectionHeader;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.NativeLibrary;
import de.dhbw.rahmlab.nativelibloader.impl.dependencies.MicrosoftPe.ImageImportDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * @author fabian
 */
public class MutualBundleDependencySortingService {

    static MutualBundleDependencySortingService instance;

    static {
        instance = new MutualBundleDependencySortingService();
    }

    private MutualBundleDependencySortingService() {
    }

    public static MutualBundleDependencySortingService getInstance() {
        return instance;
    }

    /**
     *
     * @param libNames must be already added into
     * {@link de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.util.cache.TempJarCache TempJarCache}
     * via
     * {@link de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.jvm.JNILibLoaderBase#addNativeJarLibs(Class, String) addNativeJarLibs}
     * @return 
     * @throws java.lang.Exception
     *
     */
    public List<String> mutualBundleDependencyTopologicalSorting(Set<String> libNames) throws Exception {
        Map<String, String> libNameToPaths = new HashMap<>(libNames.size());

        // Precondition: libNames must be already added into TempJarCache
        for (String lib : libNames) {
            String path = TempJarCache.findLibrary(lib);
            if (path == null) {
                throw new Exception(lib + " not found in TempJarCache");
            }
            libNameToPaths.put(lib, path);
        }

        DirectedAcyclicGraph<String, DefaultEdge> depsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);

        DebugService.print("----");

        // Recursion is not needed because the full vertex set is known
        for (Entry<String, String> libNameToPath : libNameToPaths.entrySet()) {
            Set<String> deps = getDeps(libNameToPath.getValue());

            for (String dep : deps) {
                String libBaseName = NativeLibrary.isValidNativeLibraryName(dep, false);
                String libPath = TempJarCache.findLibrary(libBaseName);

                // Dependency not loaded into TempJarCache (implies not in Bundle)
                if (libPath == null) {
                    continue;
                }

                // Dependency not within Bundle (although loaded into TempJarCache)
                if (!libNameToPaths.containsKey(libBaseName)) {
                    continue;
                }

                // Ensures vertices are present
                depsGraph.addVertex(libNameToPath.getKey());
                depsGraph.addVertex(libBaseName);

                // Dependency within Bundle -> insert edge between LibNames
                DefaultEdge edge;
                try {
                    edge = depsGraph.addEdge(libNameToPath.getKey(), libBaseName);
                } catch (java.lang.IllegalArgumentException ex) {
                    DebugService.print("Cyclic dependencies are not allowed!");
                    throw ex;
                }

                if (edge == null) {
                    DebugService.print("Adding edge failed between '" + libNameToPath.getKey() + "' and '" + libBaseName + "'.");
                }

                DebugService.print("dependent : " + libNameToPath.getKey());
                DebugService.print("dependency: " + libBaseName);
                DebugService.print("----");
            }
        }

        ArrayList<String> topologicalSortedBundleLibNames = new ArrayList<>();

        // Already in topological order
        depsGraph.iterator().forEachRemaining(topologicalSortedBundleLibNames::add);

        // Actually we need reverse topological sorting.
        // Shallowest dependent / deepest dependency / least dependend lib at the beginning.
        // Deepest dependent / shallowest dependency / most dependend lib at the end.
        Collections.reverse(topologicalSortedBundleLibNames);

        topologicalSortedBundleLibNames.forEach(libName -> DebugService.print(libName));
        DebugService.print("----");

        return topologicalSortedBundleLibNames;
    }

    private static Set<String> getDeps(String path) throws Exception {
        switch (Platform.OS_TYPE) {
            case WINDOWS:
                return getWindowsDeps(path);
            case LINUX:
                return getLinuxDeps(path);
            default:
                throw new Exception("OS not supported.");
        }
    }

    private static Set<String> getWindowsDeps(String path) throws Exception {
        MicrosoftPe peFile = MicrosoftPe.fromFile(path);

        MicrosoftPe.ImportSection importSection = peFile.pe().sectionHeaderTable().stream()
            .map(MicrosoftPe.SectionHeader::importSection)
            .filter(Objects::nonNull)
            .findAny()
            .orElseThrow();

        ArrayList<MicrosoftPe.ImageImportDescriptor> imageImportDescriptors = importSection.importTable();
        imageImportDescriptors.remove(imageImportDescriptors.size() - 1); //Last entry is just a parsing artefakt

        HashSet<String> depsNames = imageImportDescriptors.stream()
            .map(iid -> iid.name())
            .collect(Collectors.toCollection(HashSet<String>::new));

        return depsNames;
    }

    private static Set<String> getLinuxDeps(String path) throws Exception {
        Elf elfFile = Elf.fromFile(path);

        SectionHeader dynamicSectionHeader = elfFile.header().sectionHeaders().stream()
            .filter(Objects::nonNull)
            .filter(sh -> sh.type() == Elf.ShType.DYNAMIC)
            .findAny()
            .orElseThrow();

        Elf.EndianElf.DynamicSection dynamicSection = (Elf.EndianElf.DynamicSection) dynamicSectionHeader.body();

        if (!dynamicSection.isStringTableLinked()) {
            throw new Exception("String table is not linked!");
        }

        ArrayList<Elf.EndianElf.DynamicSectionEntry> dynamicSectionEntries = dynamicSection.entries().stream()
            .filter(Objects::nonNull)
            .filter(se -> se.tagEnum() == Elf.DynamicArrayTags.NEEDED)
            .collect(Collectors.toCollection(ArrayList<Elf.EndianElf.DynamicSectionEntry>::new));

        HashSet<String> depsNames = dynamicSectionEntries.stream()
            .map(e -> e.name())
            .collect(Collectors.toCollection(HashSet<String>::new));

        return depsNames;
    }
}
