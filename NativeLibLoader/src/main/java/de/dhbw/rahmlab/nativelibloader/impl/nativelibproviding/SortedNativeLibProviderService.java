/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding;

import de.dhbw.rahmlab.nativelibloader.impl.nativeparsing.NativeLibsDependenciesGetterService;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author fabian
 */
public class SortedNativeLibProviderService {

    public static List<NativeLib> getSortedNativeLibs(Class markerClass) throws Exception {
        Objects.requireNonNull(markerClass);

        // Get bundled libs.
        final Set<Path> nativeLibsPaths = NativeLibsPathsFinderService.findNativeLibsPaths(markerClass);
        final Set<NativeLib> nativeLibs = nativeLibsFromSelectedPaths(nativeLibsPaths);
        final Map<NativeLibName, NativeLib> nativeLibNameLookup = generateNativeLibNameLookup(nativeLibs);

        // Get their mutual dependencies.
        final Map<NativeLibName, Set<NativeLibName>> nativeLibsToDeps = getMutualDeps(nativeLibs, nativeLibNameLookup);

        // Sort libs of the bundle reverse-topological by their mutual dependency relation.
        final List<NativeLibName> sortedLibNames = MutualBundleDependencyReverseTopologicalSortingService.sort(nativeLibsToDeps);

        final List<NativeLib> sortedLibs = sortedLibNames.stream()
            .map(libName -> nativeLibNameLookup.get(libName))
            .collect(Collectors.toCollection(ArrayList<NativeLib>::new));

        return sortedLibs;
    }

    private static Set<NativeLib> nativeLibsFromSelectedPaths(final Set<Path> nativeLibsPaths) {
        final Set<NativeLib> nativeLibs = nativeLibsPaths.stream()
            .map(path -> NativeLib.fromPath(path))
            .filter(optional -> optional.isPresent())
            .map(optional -> optional.get())
            .collect(Collectors.toCollection(HashSet<NativeLib>::new));
        return nativeLibs;
    }

    private static Map<NativeLibName, NativeLib> generateNativeLibNameLookup(final Set<NativeLib> nativeLibs) {
        Map<NativeLibName, NativeLib> nameToLib = new HashMap(nativeLibs.size());
        nativeLibs.forEach(nativeLib -> nameToLib.put(nativeLib.getName(), nativeLib));
        return nameToLib;
    }

    private static Set<NativeLibName> bundleDepsFromDeps(final Set<String> deps, final Map<NativeLibName, NativeLib> nativeLibNameLookup) {
        // Keep only those dependencies which are part of the bundle
        final Set<NativeLibName> bundleDeps = deps.stream()
            .map(dep -> NativeLibName.fromPathOrName(dep))
            .filter(optional -> optional.isPresent())
            .map(optional -> optional.get())
            .filter(nativeLibName -> nativeLibNameLookup.containsKey(nativeLibName))
            .collect(Collectors.toCollection(HashSet<NativeLibName>::new));
        return bundleDeps;
    }

    private static Map<NativeLibName, Set<NativeLibName>> getMutualDeps(
        final Set<NativeLib> nativeLibs,
        final Map<NativeLibName, NativeLib> nativeLibNameLookup) throws Exception {
        Map<NativeLibName, Set<NativeLibName>> nativeLibsToDeps = new HashMap(nativeLibs.size());
        for (NativeLib nativeLib : nativeLibs) {
            final Set<String> deps = NativeLibsDependenciesGetterService.getDeps(nativeLib.getPath().toString());
            final Set<NativeLibName> bundleDeps = bundleDepsFromDeps(deps, nativeLibNameLookup);

            nativeLibsToDeps.put(nativeLib.getName(), bundleDeps);
        }
        return nativeLibsToDeps;
    }
}
