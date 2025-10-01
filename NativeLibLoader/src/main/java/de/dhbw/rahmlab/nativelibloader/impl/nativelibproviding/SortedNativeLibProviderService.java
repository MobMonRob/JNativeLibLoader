package de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding;

import de.dhbw.rahmlab.nativelibloader.api.NativeLib;
import de.dhbw.rahmlab.nativelibloader.api.NativeLibName;
import de.dhbw.rahmlab.nativelibloader.impl.nativeparsing.NativeLibsDependenciesGetterService;
import de.dhbw.rahmlab.nativelibloader.impl.util.DebugService;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author fabian
 */
public class SortedNativeLibProviderService {

	public static List<NativeLib> getSortedNativeLibs(Class markerClass, String nativesFolderName, Optional<Set<String>> loadOnlyThisLibsAndTheirDepsOpt) throws Exception {
		Objects.requireNonNull(markerClass);

		// Get bundled libs.
		final Set<Path> nativeLibsPaths = NativeLibsPathsFinderService.findNativeLibsPaths(markerClass, nativesFolderName);
        final LinkedHashSet<NativeLib> nativeLibsSorted = nativeLibsFromSelectedPaths(nativeLibsPaths).stream()
            .sorted(Comparator.comparing(NativeLib::getName))
            .collect(Collectors.toCollection(LinkedHashSet<NativeLib>::new));
        final SequencedMap<NativeLibName, NativeLib> nativeLibNameLookupSorted = generateNativeLibNameLookup(nativeLibsSorted);

        // Better include in API if needed. Comment only to find the right place in the future.
        // Please note: If the lib should be loadable by other ones, it still needs to be extracted.
        // // This is the default behaviour and is done at this point.
        // // But the code should model this more clearly.
        // nativeLibNameLookup.remove(NativeLibName.fromPathOrName("libc.so.6"));

		// Get their mutual dependencies.
		SequencedMap<NativeLibName, SequencedSet<NativeLibName>> nativeLibsToDeps = getMutualDeps(nativeLibNameLookupSorted);

		// filter for loadOnlyThisLibsAndTheirDeps
		if (loadOnlyThisLibsAndTheirDepsOpt.isPresent()) {
			Set<NativeLibName> loadOnlyThisLibsAndTheirDeps = loadOnlyThisLibsAndTheirDepsOpt.get().stream()
				.map(NativeLibName::fromPathOrName)
				.collect(Collectors.toSet());

			// ensure existence of all loadOnlyThisLibsAndTheirDeps
			Set<NativeLibName> allNativeLibNames = nativeLibNameLookupSorted.keySet();
			for (NativeLibName libName : loadOnlyThisLibsAndTheirDeps) {
				if (!allNativeLibNames.contains(libName)) {
					throw new RuntimeException(String.format("NativeLibLoader: loadOnlyThisLibsAndTheirDeps \"%s\" not found!", libName));
				}
			}

			nativeLibsToDeps = filterNativeLibsToDeps(nativeLibsToDeps, loadOnlyThisLibsAndTheirDeps);
		}

		// Sort libs of the bundle reverse-topological by their mutual dependency relation.
		final List<NativeLibName> sortedLibNames = MutualBundleDependencyReverseTopologicalSortingService.sort(nativeLibsToDeps);

        DebugService.print("Sorted dependencies:");
        sortedLibNames.forEach(dep -> DebugService.print(dep.toString()));
        DebugService.print("----");

		final List<NativeLib> sortedLibs = sortedLibNames.stream()
            .map(libName -> nativeLibNameLookupSorted.get(libName))
            .collect(Collectors.toCollection(ArrayList<NativeLib>::new));

		return sortedLibs;
	}

    private static SequencedMap<NativeLibName, SequencedSet<NativeLibName>> filterNativeLibsToDeps(SequencedMap<NativeLibName, SequencedSet<NativeLibName>> nativeLibsToDeps, Set<NativeLibName> loadOnlyThisLibsAndTheirDeps) {

        LinkedHashMap<NativeLibName, SequencedSet<NativeLibName>> filteredNativeLibsToDeps = LinkedHashMap.newLinkedHashMap(nativeLibsToDeps.size());
		List<NativeLibName> currentLibs = new ArrayList<>(nativeLibsToDeps.size());
		currentLibs.addAll(loadOnlyThisLibsAndTheirDeps);
		while (!currentLibs.isEmpty()) {
			NativeLibName currentLib = currentLibs.removeLast();
			if (filteredNativeLibsToDeps.containsKey(currentLib)) {
				continue;
			}
            SequencedSet<NativeLibName> currentLibDeps = nativeLibsToDeps.get(currentLib);
			filteredNativeLibsToDeps.put(currentLib, currentLibDeps);
			currentLibs.addAll(currentLibDeps);
		}
		return filteredNativeLibsToDeps;
	}

    private static Set<NativeLib> nativeLibsFromSelectedPaths(final Set<Path> nativeLibsPaths) {
        // Unsure, if silent failure is correct here.
        // Unsure, if failure is possible at all with the way it is used.
        // Maybe exception is better for NativeLib::fromPath.
		final Set<NativeLib> nativeLibs = nativeLibsPaths.stream()
            .map(NativeLib::fromPath)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toCollection(HashSet<NativeLib>::new));
		return nativeLibs;
	}

    private static SequencedMap<NativeLibName, NativeLib> generateNativeLibNameLookup(SequencedSet<NativeLib> nativeLibsSorted) {
        LinkedHashMap<NativeLibName, NativeLib> nameToLib = LinkedHashMap.newLinkedHashMap(nativeLibsSorted.size());
        for (NativeLib nativeLib : nativeLibsSorted) {
            NativeLib oldValue = nameToLib.putIfAbsent(nativeLib.getName(), nativeLib);
            if (oldValue != null) {
                DebugService.print(String.format("NativeLibLoader: Found two libs with same name: \"%s\": \"%s\" and \"%s\"", nativeLib.getName(), nativeLib.getPath(), oldValue.getPath()));
            }
        }
		return nameToLib;
	}

    private static SequencedSet<NativeLibName> bundleDepsFromDeps(final Set<String> deps, final Map<NativeLibName, NativeLib> nativeLibNameLookup) {
        final Set<NativeLibName> allDeps = deps.stream()
            .map(NativeLibName::fromPathOrName)
            .collect(Collectors.toSet());

        Set<NativeLibName> bundleDeps = HashSet.<NativeLibName>newHashSet(allDeps.size());
        List<NativeLibName> externalDeps = new ArrayList<>(0);
        for (NativeLibName dep : allDeps) {
            if (nativeLibNameLookup.containsKey(dep)) {
                bundleDeps.add(dep);
            } else if (DebugService.isDebug()) {
                externalDeps.add(dep);
            }
        }

        LinkedHashSet bundleDepsSorted = bundleDeps.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));

        if (DebugService.isDebug()) {
            bundleDepsSorted.forEach(dep -> DebugService.print("dependency bundled: " + dep.toString()));
            externalDeps = externalDeps.stream().sorted().toList();
            externalDeps.forEach(dep -> DebugService.print("dependency external: " + dep.toString()));
        }

        return bundleDepsSorted;
	}

    private static SequencedMap<NativeLibName, SequencedSet<NativeLibName>> getMutualDeps(
        final SequencedMap<NativeLibName, NativeLib> nativeLibNameLookupSorted) throws Exception {

        SequencedSet<NativeLibName> nativeLibNamesSorted = nativeLibNameLookupSorted.sequencedKeySet();
        LinkedHashMap<NativeLibName, SequencedSet<NativeLibName>> nativeLibsToDeps = LinkedHashMap.newLinkedHashMap(nativeLibNamesSorted.size());
        for (NativeLibName dependentName : nativeLibNamesSorted) {
            NativeLib dependent = nativeLibNameLookupSorted.get(dependentName);
            DebugService.print("----");
			Set<String> deps;
			try {
                deps = NativeLibsDependenciesGetterService.getDeps(dependent.getPath().toString());
			} catch (Exception e) {
				// Invalid file
				DebugService.print(String.format("Invalid file: \"%s\", error: \"%s\"", dependent.getPath(), e.getMessage()));
				continue;
            }

            DebugService.print("dependent : " + dependent.getName().toString());

            final SequencedSet<NativeLibName> bundleDeps = bundleDepsFromDeps(deps, nativeLibNameLookupSorted);

            nativeLibsToDeps.put(dependent.getName(), bundleDeps);
        }
        DebugService.print("----");

		return nativeLibsToDeps;
	}
}
