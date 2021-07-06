/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dhbw.rahmlab.nativelibloader.impl.dependencies;

import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.util.cache.TempJarCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author fabian
 */
public class MutualDependencySortingService {

    public List<String> sortLibs(List<String> libNames) throws Exception {
        //Vorbedingung: Im TempJarCache liegen nur Dateien der aktuellen Plattform

        //SortLibs nur für Windows aufrufen? ->Check einbauen
        //Oder hier drin umschalten? ->Not implemented für Linux einbauen
        //Unschön. Wäre besser als Klasse "NativeLib{Name, Path, ...}"
        Map<String, String> libPaths = new HashMap<String, String>(libNames.size());

        for (String lib : libNames) {
            String path = TempJarCache.findLibrary(lib);
            if (path == null) {
                throw new Exception(lib + " not found in TempJarCache");
            }
            libPaths.put(lib, path);
        }

        //Deps holen
        //Deps Namen anpassen
        //Deps filtern
        //Deps in den Graphen
        //Graph sortieren
        return new ArrayList<String>();
    }

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
}
