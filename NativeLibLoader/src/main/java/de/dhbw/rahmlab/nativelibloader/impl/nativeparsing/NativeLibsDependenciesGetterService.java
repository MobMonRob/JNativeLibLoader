package de.dhbw.rahmlab.nativelibloader.impl.nativeparsing;

import de.dhbw.rahmlab.nativelibloader.impl.util.Platform;
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
		return switch (Platform.OS) {
			case WINDOWS ->
				getDllDeps(path);
			case LINUX ->
				getSoDeps(path);
		};
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
