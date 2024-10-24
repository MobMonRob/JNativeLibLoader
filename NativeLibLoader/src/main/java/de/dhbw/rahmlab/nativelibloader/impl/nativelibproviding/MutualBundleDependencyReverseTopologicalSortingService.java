package de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding;

import de.dhbw.rahmlab.nativelibloader.impl.util.DebugService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

/**
 *
 * checked precondition: the represented graph does not contain any cyclic dependencies.
 *
 * @author fabian
 */
public class MutualBundleDependencyReverseTopologicalSortingService {

	public static <T> List<T> sort(final Map<T, Set<T>> dependentsToDependecySet) throws IllegalArgumentException {
		DirectedAcyclicGraph<T, DefaultEdge> depsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);

		DebugService.print("----");

		// Recursion is not needed because the full vertex set is already known
		for (Entry<T, Set<T>> dependentToDepencencySet : dependentsToDependecySet.entrySet()) {
			final T dependent = dependentToDepencencySet.getKey();
			final Set<T> dependencySet = dependentToDepencencySet.getValue();

			// Ensures vertices are present
			depsGraph.addVertex(dependent);

			for (T dependency : dependencySet) {

				// Ensures vertices are present
				depsGraph.addVertex(dependency);

				// Insert edge
				DefaultEdge edge;
				try {
					edge = depsGraph.addEdge(dependent, dependency);
				} catch (IllegalArgumentException ex) {
					DebugService.print("Cyclic dependency found. " + "Dependent: " + dependent.toString() + " Dependency: " + dependency.toString());
					throw ex;
				}

				if (edge == null) {
					DebugService.print("Adding edge failed. " + "Dependent: " + dependent.toString() + " Dependency: " + dependency.toString());
				}

				DebugService.print("dependent : " + dependent.toString());
				DebugService.print("dependency: " + dependency.toString());
				DebugService.print("----");
			}
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
