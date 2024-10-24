package de.dhbw.rahmlab.nativelibloader.impl.util;

import java.util.Locale;

public class Platform {

	public static enum OS {
		LINUX, WINDOWS
	}

	public static enum Arch {
		AMD64
	}

	public static final OS OS = getOS();

	private static OS getOS() {
		final String name = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
		if (name.contains("nix") || name.contains("nux")) {
			return OS.LINUX;
		} else if (name.contains("win")) {
			return OS.WINDOWS;
		}
		throw new RuntimeException(String.format("Unsupported OS: \"%s\"", name));
	}

	public static final Arch ARCH = getProcessor();

	private static Arch getProcessor() {
		final String arch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
		if (arch.contains("86") || arch.contains("amd")) {
			if (arch.contains("64")) {
				return Arch.AMD64;
			}
		}
		throw new RuntimeException(String.format("Unsupported Arch: \"%s\"", arch));
	}

	public static final String PLATFORM_DIR_NAME = getPlatformDirName();

	private static String getPlatformDirName() {
		return String.format("%s-%s", OS.toString().toLowerCase(), ARCH.toString().toLowerCase());
	}

	public static final String LIB_PREFIX = getLibPrefix();

	private static String getLibPrefix() {
		return "lib";
	}

	public static final String LIB_SUFFIX = getLibSuffix();

	private static String getLibSuffix() {
		return switch (OS) {
			case LINUX ->
				".so";
			case WINDOWS ->
				".dll";
		};
	}
}
