/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dhbw.rahmlab.nativelibloader.api;

import de.dhbw.rahmlab.nativelibloader.impl.jogamp.os.NativeLibrary;
import de.dhbw.rahmlab.nativelibloader.impl.util.DebugService;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author fabian
 */
public class NativeLibName {

    private final String name;

    private NativeLibName(final String name) {
        this.name = name;
    }

    public static Optional<NativeLibName> fromPathOrName(final String pathOrName) {
        final String normalizedName = NativeLibrary.isValidNativeLibraryName(pathOrName, false);
        if (Objects.isNull(normalizedName)) {
            DebugService.print("Does not contain a valid library name: " + pathOrName);
            return Optional.empty();
        }

        NativeLibName nativeLibName = new NativeLibName(normalizedName);
        return Optional.of(nativeLibName);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        NativeLibName rhs = (NativeLibName) obj;

        return this.name.equals(rhs.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
