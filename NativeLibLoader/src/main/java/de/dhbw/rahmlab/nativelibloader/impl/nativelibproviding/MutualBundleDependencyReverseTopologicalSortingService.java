package de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding;

import de.dhbw.rahmlab.nativelibloader.impl.util.DebugService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

public class MutualBundleDependencyReverseTopologicalSortingService {

    /**
     * <pre>
     * Precondition: The represented graph does not contain any cyclic dependencies.
     * Precondition: All dependencies are dependents as well. ("Mutuality")
     * </pre>
     */
	public static <T extends Comparable<T>> List<T> sort(final Map<T, Set<T>> dependentsToDependecySet) throws IllegalArgumentException {
		DirectedAcyclicGraph<T, DefaultEdge> depsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        
        // (Debugging only) Make order deterministic only for debugging. This improves the debugging experience.
        List<Entry<T, Set<T>>> sortedDependentsToDependecySet;
        List<Entry<T, Set<T>>> reversedSortedDependentsToDependecySet;
        if (DebugService.isDebug()) {
            sortedDependentsToDependecySet = dependentsToDependecySet.entrySet().stream().sorted(Entry.comparingByKey()).toList();
            reversedSortedDependentsToDependecySet = sortedDependentsToDependecySet.reversed();
        } else {
            sortedDependentsToDependecySet = List.copyOf(dependentsToDependecySet.entrySet());
            reversedSortedDependentsToDependecySet = sortedDependentsToDependecySet;
        }

        //DebugService.print("----");

        // Add all vertices.
        // (Debugging only) Reversed because it will be reversed again at the end.
        // (Debugging only) Vertex insertion order defines order of dependencies which do not depend from each other.
        for (Entry<T, Set<T>> dependentToDepencencySet : reversedSortedDependentsToDependecySet) {
			final T dependent = dependentToDepencencySet.getKey();
            depsGraph.addVertex(dependent);
        }

        // (Debugging only) sortedDependentsToDependecySet for sorted printing only.
		for (Entry<T, Set<T>> dependentToDepencencySet : sortedDependentsToDependecySet) {
			final T dependent = dependentToDepencencySet.getKey();
			Set<T> dependencySet = dependentToDepencencySet.getValue();
            
            // (Debugging only) Sorted dependencies printing.
            if (DebugService.isDebug()) {
                dependencySet = dependencySet.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
            }

            //DebugService.print("dependent : " + dependent.toString());
            
			for (T dependency : dependencySet) {
				// Insert edge
				DefaultEdge edge;
				try {
					edge = depsGraph.addEdge(dependent, dependency);
				} catch (IllegalArgumentException ex) {
                    if (!depsGraph.containsVertex(dependency)) {
                        throw new IllegalArgumentException(String.format("Dependency \"%s\" is not part of the dependents.", dependency), ex);
                    } else {
                        throw new IllegalArgumentException(String.format("Cyclic dependency found. Dependent: \"%s\" Dependency: \"%s\"", dependency), ex);
                    }
				}

				if (edge == null) {
                    throw new IllegalArgumentException(String.format("Adding edge failed. Dependent: \"%s\" Dependency: \"%s\"", dependency));
				}

                //DebugService.print("dependency: " + dependency.toString());
			}
            
            //DebugService.print("----");
		}

		ArrayList<T> sortedDeps = new ArrayList();

		// Already in topological order
		depsGraph.iterator().forEachRemaining(sortedDeps::add);

		// Actually we need reverse topological sorting.
		// Shallowest dependent / deepest dependency / least dependend lib at the beginning.
		// Deepest dependent / shallowest dependency / most dependend lib at the end.
		Collections.reverse(sortedDeps);

		DebugService.print("Sorted dependencies:");
		sortedDeps.forEach(dep -> DebugService.print(dep.toString()));
		DebugService.print("----");

		return sortedDeps;
	}
}
