/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dhbw.rahmlab.nativelibloader.impl.nativelibproviding;

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

        final Optional<NativeLibName> nativeLibName = NativeLibName.fromPathOrName(path.toString());
        if (nativeLibName.isEmpty()) {
            return Optional.empty();
        }

        NativeLib nativeLib = new NativeLib(nativeLibName.get(), path);
        return Optional.of(nativeLib);
    }

    public Path getPath() {
        return path;
    }

    public NativeLibName getName() {
        return name;
    }
}
