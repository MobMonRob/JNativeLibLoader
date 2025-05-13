package de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding;

import de.dhbw.rahmlab.nativelibloader.api.NativeLib;
import de.dhbw.rahmlab.nativelibloader.api.NativeLibName;
import de.dhbw.rahmlab.nativelibloader.impl.nativeparsing.NativeLibsDependenciesGetterService;
import de.dhbw.rahmlab.nativelibloader.impl.util.DebugService;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
		final Set<NativeLib> nativeLibs = nativeLibsFromSelectedPaths(nativeLibsPaths);
		final Map<NativeLibName, NativeLib> nativeLibNameLookup = generateNativeLibNameLookup(nativeLibs);

		// Get their mutual dependencies.
		Map<NativeLibName, Set<NativeLibName>> nativeLibsToDeps = getMutualDeps(nativeLibNameLookup);

		// filter for loadOnlyThisLibsAndTheirDeps
		if (loadOnlyThisLibsAndTheirDepsOpt.isPresent()) {
			Set<NativeLibName> loadOnlyThisLibsAndTheirDeps = loadOnlyThisLibsAndTheirDepsOpt.get().stream()
				.map(NativeLibName::fromPathOrName)
				.collect(Collectors.toSet());

			// ensure existence of all loadOnlyThisLibsAndTheirDeps
			Set<NativeLibName> allNativeLibNames = nativeLibNameLookup.keySet();
			for (NativeLibName libName : loadOnlyThisLibsAndTheirDeps) {
				if (!allNativeLibNames.contains(libName)) {
					throw new RuntimeException(String.format("NativeLibLoader: loadOnlyThisLibsAndTheirDeps \"%s\" not found!", libName));
				}
			}

			nativeLibsToDeps = filterNativeLibsToDeps(nativeLibsToDeps, loadOnlyThisLibsAndTheirDeps);
		}

		// Sort libs of the bundle reverse-topological by their mutual dependency relation.
		final List<NativeLibName> sortedLibNames = MutualBundleDependencyReverseTopologicalSortingService.sort(nativeLibsToDeps);

		final List<NativeLib> sortedLibs = sortedLibNames.stream()
			.map(libName -> nativeLibNameLookup.get(libName))
			.collect(Collectors.toCollection(ArrayList<NativeLib>::new));

		return sortedLibs;
	}

	private static Map<NativeLibName, Set<NativeLibName>> filterNativeLibsToDeps(Map<NativeLibName, Set<NativeLibName>> nativeLibsToDeps, Set<NativeLibName> loadOnlyThisLibsAndTheirDeps) {
		Map<NativeLibName, Set<NativeLibName>> filteredNativeLibsToDeps = HashMap.newHashMap(nativeLibsToDeps.size());
		List<NativeLibName> currentLibs = new ArrayList<>(nativeLibsToDeps.size());
		currentLibs.addAll(loadOnlyThisLibsAndTheirDeps);
		while (!currentLibs.isEmpty()) {
			NativeLibName currentLib = currentLibs.removeLast();
			if (filteredNativeLibsToDeps.containsKey(currentLib)) {
				continue;
			}
			Set<NativeLibName> currentLibDeps = nativeLibsToDeps.get(currentLib);
			filteredNativeLibsToDeps.put(currentLib, currentLibDeps);
			currentLibs.addAll(currentLibDeps);
		}
		return filteredNativeLibsToDeps;
	}

	private static Set<NativeLib> nativeLibsFromSelectedPaths(final Set<Path> nativeLibsPaths) {
		final Set<NativeLib> nativeLibs = nativeLibsPaths.stream()
			.map(path -> NativeLib.fromPath(path))
			.filter(optional -> optional.isPresent())
			.map(optional -> optional.get())
			.collect(Collectors.toCollection(HashSet<NativeLib>::new));
		return nativeLibs;
	}

    private static Map<NativeLibName, NativeLib> generateNativeLibNameLookup(Set<NativeLib> nativeLibs) {
        if (DebugService.isDebug()) {
            nativeLibs = nativeLibs.stream()
                .sorted(Comparator.comparing(NativeLib::getName))
                .collect(Collectors.toCollection(LinkedHashSet<NativeLib>::new));
        }
        Map<NativeLibName, NativeLib> nameToLib = new HashMap(nativeLibs.size());
        for (NativeLib nativeLib : nativeLibs) {
            NativeLib oldValue = nameToLib.putIfAbsent(nativeLib.getName(), nativeLib);
            if (DebugService.isDebug()) {
                if (oldValue != null) {
                    DebugService.print(String.format("NativeLibLoader: Found two libs with same name: \"%s\": \"%s\" and \"%s\"", nativeLib.getName(), nativeLib.getPath(), oldValue.getPath()));
                }
            }
        }
		return nameToLib;
	}

    private static Set<NativeLibName> bundleDepsFromDeps(final Set<String> deps, final Map<NativeLibName, NativeLib> nativeLibNameLookup) {
        final Set<NativeLibName> allDeps = deps.stream()
            .map(dep -> NativeLibName.fromPathOrName(dep))
            .collect(Collectors.toCollection(LinkedHashSet<NativeLibName>::new));

        Set<NativeLibName> bundleDeps = HashSet.<NativeLibName>newHashSet(allDeps.size());
        List<NativeLibName> externalDeps = new ArrayList<>(0);
        for (NativeLibName dep : allDeps) {
            if (nativeLibNameLookup.containsKey(dep)) {
                bundleDeps.add(dep);
            } else {
                if (DebugService.isDebug()) {
                    externalDeps.add(dep);
                }
            }
        }

        if (DebugService.isDebug()) {
            bundleDeps = bundleDeps.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
            externalDeps = externalDeps.stream().sorted().toList();

            bundleDeps.forEach(dep -> DebugService.print("dependency bundled: " + dep.toString()));
            externalDeps.forEach(dep -> DebugService.print("dependency external: " + dep.toString()));
        }

		return bundleDeps;
	}

    private static Map<NativeLibName, Set<NativeLibName>> getMutualDeps(
        final Map<NativeLibName, NativeLib> nativeLibNameLookup) throws Exception {
        Set<NativeLibName> nativeLibNames = nativeLibNameLookup.keySet();
        Map<NativeLibName, Set<NativeLibName>> nativeLibsToDeps = new HashMap(nativeLibNames.size());
        if (DebugService.isDebug()) {
            nativeLibNames = nativeLibNames.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
        }
        for (NativeLibName dependentName : nativeLibNames) {
            NativeLib dependent = nativeLibNameLookup.get(dependentName);
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

            final Set<NativeLibName> bundleDeps = bundleDepsFromDeps(deps, nativeLibNameLookup);

            nativeLibsToDeps.put(dependent.getName(), bundleDeps);
        }
        DebugService.print("----");

		return nativeLibsToDeps;
	}
}
