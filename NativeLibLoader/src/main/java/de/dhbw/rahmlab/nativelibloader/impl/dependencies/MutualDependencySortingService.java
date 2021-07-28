/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dhbw.rahmlab.nativelibloader.impl.dependencies;

import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.Platform;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.util.cache.TempJarCache;
import de.dhbw.rahmlab.nativelibloader.impl.dependencies.Elf.EndianElf.SectionHeader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author fabian
 */
public class MutualDependencySortingService {

    static MutualDependencySortingService instance;

    static {
        instance = new MutualDependencySortingService();
    }

    private MutualDependencySortingService() {
    }

    public static MutualDependencySortingService getInstance() {
        return instance;
    }

    /**
     *
     * @param libNames must be already added into
     * {@link de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.util.cache.TempJarCache TempJarCache}
     * via
     * {@link de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.jvm.JNILibLoaderBase#addNativeJarLibs(Class, String) addNativeJarLibs}
     *
     */
    public List<String> windowsMutualDependencyTopologicalSorting(Set<String> libNames) throws Exception {
        if (Platform.OS_TYPE != Platform.OSType.WINDOWS) {
            throw new Exception("Sorting currently only for windows implemented!");
        }

        Map<String, String> libNameToPaths = new HashMap<String, String>(libNames.size());

        // Precondition: libNames must be already added into TempJarCache
        for (String lib : libNames) {
            String path = TempJarCache.findLibrary(lib);
            if (path == null) {
                throw new Exception(lib + " not found in TempJarCache");
            }
            libNameToPaths.put(lib, path);
        }

        // Debug
        libNameToPaths.keySet().forEach(name -> System.out.println(name));

        //Deps holen
        //Deps Namen anpassen
        //Deps filtern
        //Deps in den Graphen
        //Graph sortieren
        return new ArrayList<String>();
    }

    // TODO: private machen
    public List<String> getWindowsDeps(String path) throws Exception {
        MicrosoftPe peFile = MicrosoftPe.fromFile(path);

        MicrosoftPe.ImportSection importSection = peFile.pe().sectionHeaderTable().stream()
            .map(MicrosoftPe.SectionHeader::importSection)
            .filter(Objects::nonNull)
            .findAny()
            .orElseThrow();

        ArrayList<MicrosoftPe.ImageImportDescriptor> imageImportDescriptors = importSection.importTable();
        imageImportDescriptors.remove(imageImportDescriptors.size() - 1); //Last entry is just a parsing artefakt

        ArrayList<String> depsNames = imageImportDescriptors.stream()
            .map(iid -> iid.name())
            .collect(Collectors.toCollection(ArrayList<String>::new));

        return depsNames;
    }

    // TODO: private machen
    public List<String> getLinuxDeps(String path) throws Exception {
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

        ArrayList<String> depsNames = dynamicSectionEntries.stream()
            .map(e -> e.name())
            .collect(Collectors.toCollection(ArrayList<String>::new));

        return depsNames;
    }
}
