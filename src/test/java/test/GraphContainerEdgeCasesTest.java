package test;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.pathfinder.GraphContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphContainerEdgeCasesTest {
	private GraphContainer container;

	@BeforeEach
	void setUp() {
		container = new GraphContainer(sampleGraph());
	}

	@Test
	void shortestPathFromAnotherSource() {
		assertEquals(List.of("B", "C", "D"), container.shortestPathBetween("B", "D"));
	}

	@Test
	void pathWeightFromAnotherSource() {
		assertEquals(3.0, container.pathWeightBetween("B", "D"), 1e-9);
	}

	@Test
	void pathIsTheSameInBothDirections() {
		assertEquals(List.of("C", "B", "A"), container.shortestPathBetween("C", "A"));
		assertEquals(3.0, container.pathWeightBetween("C", "A"), 1e-9);
	}

	@Test
	void prefersTheLighterPathOverTheShorterOne() {
		Graph<String, DefaultEdge> graph = new SimpleWeightedGraph<>(DefaultEdge.class);
		graph.addVertex("A");
		graph.addVertex("B");
		graph.addVertex("C");
		graph.addEdge("A", "B");
		graph.addEdge("B", "C");
		graph.addEdge("A", "C");
		graph.setEdgeWeight("A", "B", 1.0);
		graph.setEdgeWeight("B", "C", 1.0);
		graph.setEdgeWeight("A", "C", 5.0);
		GraphContainer weighted = new GraphContainer(graph);

		assertEquals(List.of("A", "B", "C"), weighted.shortestPathBetween("A", "C"));
		assertEquals(2.0, weighted.pathWeightBetween("A", "C"), 1e-9);
	}

	@Test
	void shortestPathFailsWhenSourceDoesNotExist() {
		var exception = assertThrows(IllegalArgumentException.class,
				() -> container.shortestPathBetween("Z", "A"));
		assertTrue(exception.getMessage().contains("does not exist"));
	}

	@Test
	void pathWeightFailsWhenSourceDoesNotExist() {
		assertThrows(IllegalArgumentException.class,
				() -> container.pathWeightBetween("Z", "A"));
	}

	@Test
	void pathWeightFailsWhenTargetDoesNotExist() {
		var exception = assertThrows(IllegalArgumentException.class,
				() -> container.pathWeightBetween("A", "Z"));
		assertTrue(exception.getMessage().contains("does not exist"));
	}

	@Test
	void shortestPathFailsWhenVerticesAreNotConnected() {
		var exception = assertThrows(IllegalArgumentException.class,
				() -> disconnectedContainer().shortestPathBetween("A", "C"));
		assertTrue(exception.getMessage().contains("No path exists"));
	}

	@Test
	void pathWeightFailsWhenVerticesAreNotConnected() {
		var exception = assertThrows(IllegalArgumentException.class,
				() -> disconnectedContainer().pathWeightBetween("A", "C"));
		assertTrue(exception.getMessage().contains("No path exists"));
	}

	private GraphContainer disconnectedContainer() {
		Graph<String, DefaultEdge> graph = new SimpleWeightedGraph<>(DefaultEdge.class);
		graph.addVertex("A");
		graph.addVertex("B");
		graph.addVertex("C");
		graph.addEdge("A", "B");
		graph.setEdgeWeight("A", "B", 1.0);
		return new GraphContainer(graph);
	}

	private Graph<String, DefaultEdge> sampleGraph() {
		Graph<String, DefaultEdge> graph = new SimpleWeightedGraph<>(DefaultEdge.class);
		graph.addVertex("A");
		graph.addVertex("B");
		graph.addVertex("C");
		graph.addVertex("D");
		graph.addEdge("A", "B");
		graph.addEdge("B", "C");
		graph.addEdge("A", "D");
		graph.addEdge("C", "D");
		graph.setEdgeWeight("A", "B", 1.0);
		graph.setEdgeWeight("B", "C", 2.0);
		graph.setEdgeWeight("A", "D", 4.0);
		graph.setEdgeWeight("C", "D", 1.0);
		return graph;
	}
}
