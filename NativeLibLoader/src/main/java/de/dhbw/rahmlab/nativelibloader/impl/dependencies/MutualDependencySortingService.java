/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dhbw.rahmlab.nativelibloader.impl.dependencies;

import com.kichik.pecoff4j.ImportDirectory;
import com.kichik.pecoff4j.ImportDirectoryEntry;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.util.cache.TempJarCache;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import com.kichik.pecoff4j.PE;
import com.kichik.pecoff4j.RVAConverter;
import com.kichik.pecoff4j.ResourceDirectory;
import com.kichik.pecoff4j.ResourceEntry;
import com.kichik.pecoff4j.SectionData;
import com.kichik.pecoff4j.SectionTable;
import com.kichik.pecoff4j.constant.ResourceType;
import com.kichik.pecoff4j.io.PEParser;
import com.kichik.pecoff4j.io.ResourceParser;
import com.kichik.pecoff4j.resources.StringFileInfo;
import com.kichik.pecoff4j.resources.StringTable;
import com.kichik.pecoff4j.resources.VersionInfo;
import com.kichik.pecoff4j.util.ResourceHelper;
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
        MicrosoftPe.DataDir importTable = peFile.pe().optionalHdr().dataDirs().importTable();
        //peFile._io()

        if (importTable.size() == 0) {
            throw new Exception("Import Table is empty!");
        }

        //Rva2Offset
        //Virtual address to .idata Section
        long idataAddress = peFile.pe().optionalHdr().dataDirs().importTable().virtualAddress();
        String formatString = String.format("0x%08X", idataAddress);
        System.out.println(formatString); //Korrekt

        String sectionName = peFile.pe().sectionHeaderTable().get(7).name();
        System.out.println(sectionName); //Korrekt
        long iDataRawAddress = peFile.pe().sectionHeaderTable().get(7).pointerToRawData();
        String formatString2 = String.format("0x%08X", iDataRawAddress);
        System.out.println(formatString2); //Korrekt

        System.out.println("----");

        MicrosoftPe.ImportSection importSection = peFile.pe().sectionHeaderTable().stream()
            .map(MicrosoftPe.SectionHeader::importSection)
            .filter(Objects::nonNull)
            .findAny()
            .orElseThrow();

        ArrayList<MicrosoftPe.ImageImportDescriptor> imageImportDescriptors = importSection.importTable();
        imageImportDescriptors.remove(imageImportDescriptors.size() - 1); //Last entry is just a parsing artefakt
        List<Long> names = imageImportDescriptors.stream()
            .map(MicrosoftPe.ImageImportDescriptor::nameRva)
            .collect(Collectors.toList());

        names.stream()
            .map(nameRVA -> String.format("0x%08X", nameRVA))
            .forEach(s -> System.out.println(s));

        /*
        imageImportDescriptors.stream()
            .map(iid -> iid.name())
            .filter(Objects::nonNull)
            .forEach(s -> System.out.println(s));
         */
        System.out.println("----");
        imageImportDescriptors.stream()
            .map(iid -> iid.nameOffset())
            .forEach(no -> System.out.println(String.format("0x%08X", no)));

        System.out.println("----");
        imageImportDescriptors.stream()
            .map(iid -> iid.name())
            .forEach(name -> System.out.println(name));

        /*
        PE pe = PEParser.parse(path);
        ImportDirectory id = pe.getImageData().getImportTable();
        int idNameRVA = id.getEntry(0).getNameRVA();
        int rawName = pe.getSectionTable().getRVAConverter().convertVirtualAddressToRawDataPointer(idNameRVA);
         */
        return new ArrayList<String>();
    }
}
