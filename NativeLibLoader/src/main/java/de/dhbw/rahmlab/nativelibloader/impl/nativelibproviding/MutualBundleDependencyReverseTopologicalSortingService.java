package de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.SequencedMap;
import java.util.SequencedSet;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

public class MutualBundleDependencyReverseTopologicalSortingService {

    /**
     * <pre>
     * Precondition: The represented graph does not contain any cyclic dependencies.
     * Precondition: All dependencies are dependents as well. ("Mutuality")
     * Order is deterministic.
     * </pre>
     */
    public static <T extends Comparable<T>> List<T> sort(final SequencedMap<T, SequencedSet<T>> dependentsToDependencies) throws IllegalArgumentException {
		DirectedAcyclicGraph<T, DefaultEdge> depsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);

        SequencedSet<Entry<T, SequencedSet<T>>> dependentsToDependenciesEntriesReversed = dependentsToDependencies.sequencedEntrySet().reversed();

        // Add all vertices.
        // Vertex insertion order defines order of dependencies which do not depend from each other.
        // Reversed because it will be reversed again at the end.
        for (Entry<T, SequencedSet<T>> dependentToDepencencySet : dependentsToDependenciesEntriesReversed) {
			final T dependent = dependentToDepencencySet.getKey();
            depsGraph.addVertex(dependent);
        }

        //DebugService.print("----");
        // Reversed because it will be reversed again at the end.
        for (Entry<T, SequencedSet<T>> dependentToDepencencySet : dependentsToDependenciesEntriesReversed) {
            final T dependent = dependentToDepencencySet.getKey();
            // Reversed because it will be reversed again at the end.
            SequencedSet<T> dependencySet = dependentToDepencencySet.getValue().reversed();

            //DebugService.print("dependent : " + dependent.toString());
            
			for (T dependency : dependencySet) {
				// Insert edge
				DefaultEdge edge;
				try {
					edge = depsGraph.addEdge(dependent, dependency);
				} catch (IllegalArgumentException ex) {
                    if (!depsGraph.containsVertex(dependency)) {
                        throw new IllegalArgumentException(
                            String.format("Dependency \"%s\" is not part of the dependents.", dependency),
                            ex);
                    } else {
                        throw new IllegalArgumentException(
                            String.format("Cyclic dependency found. Dependent: \"%s\" Dependency: \"%s\"",
                                dependent, dependency),
                            ex);
                    }
				}

				if (edge == null) {
                    throw new IllegalArgumentException(String.format("Adding edge failed. Dependent: \"%s\" Dependency: \"%s\"",
                        dependent, dependency)
                    );
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

		return sortedDeps;
	}
}
