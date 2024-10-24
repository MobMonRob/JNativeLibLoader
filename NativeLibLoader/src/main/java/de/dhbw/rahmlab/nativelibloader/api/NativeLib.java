package de.dhbw.rahmlab.nativelibloader.api;

import de.dhbw.rahmlab.nativelibloader.impl.util.DebugService;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author fabian
 */
public class NativeLib {

	private final NativeLibName name;
	private final Path path;

	private NativeLib(final NativeLibName name, final Path path) {
		this.name = name;
		this.path = path;
	}

	public static Optional<NativeLib> fromPath(final Path fromPath) {
		if (Objects.isNull(fromPath)) {
			DebugService.print("fromPath is null.");
			return Optional.empty();
		}

		Path path;
		try {
			path = fromPath.toRealPath();
		} catch (IOException ex) {
			DebugService.print("Can't access path: " + fromPath.toString());
			return Optional.empty();
		}

		final NativeLibName nativeLibName = NativeLibName.fromPathOrName(path.toString());

		NativeLib nativeLib = new NativeLib(nativeLibName, path);
		return Optional.of(nativeLib);
	}

	public Path getPath() {
		return path;
	}

	public NativeLibName getName() {
		return name;
	}
}
