package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.ulpgc.pathfinder.FileGraphLoader;
import software.ulpgc.pathfinder.GraphContainer;
import software.ulpgc.pathfinder.GraphLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileGraphLoaderTest {
	@TempDir
	Path tempDir;

	@Test
	void loadsAGraphFromAValidFile() throws IOException {
		GraphContainer container = loaderFor("A,B,1.0", "B,C,2.0", "A,D,4.0", "C,D,1.0").load();

		assertEquals(List.of("A", "B", "C"), container.shortestPathBetween("A", "C"));
		assertEquals(3.0, container.pathWeightBetween("A", "C"), 1e-9);
	}

	@Test
	void skipsInvalidLinesAndReportsThem() throws IOException {
		Path file = write("A,B,1.0", "only-one-field", "B,C,abc", "B,C,2.0", "", "C,D,1.0", "A,B");
		ByteArrayOutputStream captured = new ByteArrayOutputStream();
		PrintStream original = System.err;
		GraphContainer container;

		System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
		try {
			container = new FileGraphLoader(file.toFile()).load();
		}
		finally {
			System.setErr(original);
		}

		String errors = captured.toString(StandardCharsets.UTF_8);
		assertTrue(errors.contains("Invalid line at 1 only-one-field"));
		assertTrue(errors.contains("Invalid line at 2 B,C,abc"));
		assertTrue(errors.contains("Invalid line at 4"));
		assertTrue(errors.contains("Invalid line at 6 A,B"));
		assertEquals(List.of("A", "B", "C", "D"), container.shortestPathBetween("A", "D"));
		assertEquals(4.0, container.pathWeightBetween("A", "D"), 1e-9);
	}

	@Test
	void anEmptyFileProducesAnEmptyGraph() throws IOException {
		GraphContainer container = loaderFor().load();

		assertThrows(IllegalArgumentException.class, () -> container.shortestPathBetween("A", "B"));
	}

	@Test
	void failsWhenTheFileDoesNotExist() {
		GraphLoader loader = new FileGraphLoader(tempDir.resolve("missing.csv").toFile());

		assertThrows(IOException.class, loader::load);
	}

	private GraphLoader loaderFor(String... lines) throws IOException {
		return new FileGraphLoader(write(lines).toFile());
	}

	private Path write(String... lines) throws IOException {
		Path file = tempDir.resolve("graph.csv");
		Files.writeString(file, String.join("\n", lines));
		return file;
	}
}
