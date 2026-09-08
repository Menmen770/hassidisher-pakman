package com.hasidicmaze;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves the game install / project folder so assets work both from
 * {@code run.bat} and from a jpackage {@code .exe}.
 */
public final class AppPaths {
    private static Path root;

    private AppPaths() {}

    public static Path root() {
        if (root != null) {
            return root;
        }
        String appHome = System.getProperty("app.home");
        if (appHome != null && !appHome.isBlank()) {
            Path p = Paths.get(appHome).toAbsolutePath().normalize();
            if (Files.isDirectory(p)) {
                root = p;
                return root;
            }
        }
        Path cwd = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        if (Files.isDirectory(cwd.resolve("assets"))) {
            root = cwd;
            return root;
        }
        // jpackage: jars live in <install>/app — assets sit next to the .exe (parent)
        Path parent = cwd.getParent();
        if (parent != null && Files.isDirectory(parent.resolve("assets"))) {
            root = parent;
            return root;
        }
        root = cwd;
        return root;
    }

    public static Path resolve(String first, String... more) {
        return root().resolve(Paths.get(first, more));
    }
}
