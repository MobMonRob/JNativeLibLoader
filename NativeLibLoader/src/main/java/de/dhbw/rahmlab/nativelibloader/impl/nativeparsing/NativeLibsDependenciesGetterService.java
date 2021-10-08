/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dhbw.rahmlab.nativelibloader.impl.nativeparsing;

import de.dhbw.rahmlab.nativelibloader.impl.jogamp.os.Platform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author fabian
 */
public class NativeLibsDependenciesGetterService {

    public static Set<String> getDeps(String path) throws IOException, NoSuchElementException, Exception {
        switch (Platform.OS_TYPE) {
            case WINDOWS:
                return getDllDeps(path);
            case LINUX:
                return getSoDeps(path);
            default:
                throw new Exception("OS not supported: " + Platform.OS_TYPE.toString());
        }
    }

    private static Set<String> getDllDeps(String path) throws IOException, NoSuchElementException {
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

    private static Set<String> getSoDeps(String path) throws IOException, NoSuchElementException, Exception {
        Elf elfFile = Elf.fromFile(path);

        Elf.EndianElf.SectionHeader dynamicSectionHeader = elfFile.header().sectionHeaders().stream()
            .filter(Objects::nonNull)
            .filter(sh -> sh.type() == Elf.ShType.DYNAMIC)
            .findAny()
            .orElseThrow();

        Elf.EndianElf.DynamicSection dynamicSection = (Elf.EndianElf.DynamicSection) dynamicSectionHeader.body();

        if (!dynamicSection.isStringTableLinked()) {
            throw new Exception("ELF string table is not linked! (" + path + ")");
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
