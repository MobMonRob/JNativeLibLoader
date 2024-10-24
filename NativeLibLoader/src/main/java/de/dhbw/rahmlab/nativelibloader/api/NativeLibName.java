package de.dhbw.rahmlab.nativelibloader.api;

import de.dhbw.rahmlab.nativelibloader.impl.util.Platform;

/**
 *
 * @author fabian
 */
public class NativeLibName {

	private final String name;

	private NativeLibName(final String name) {
		this.name = name;
	}

	private static String removeDirName(String fullName) {
		final int lios = fullName.lastIndexOf('/');
		if (lios >= 0) {
			fullName = fullName.substring(lios + 1);
		}
		return fullName;
	}

	private static String normalizeLibName(String fullLibName) {
		final String lowercase = fullLibName.toLowerCase();

		int from = lowercase.indexOf(Platform.LIB_PREFIX);
		if (from == 0) {
			from = Platform.LIB_PREFIX.length();
		} else {
			from = 0;
		}

		int to = lowercase.lastIndexOf(Platform.LIB_SUFFIX);
		if (to == -1) {
			to = lowercase.length();
		}

		final String normalizedName = fullLibName.substring(from, to);
		return normalizedName;
	}

	public static NativeLibName fromPathOrName(final String pathOrName) {
		final String normalizedName = normalizeLibName(removeDirName(pathOrName));
		return new NativeLibName(normalizedName);
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
