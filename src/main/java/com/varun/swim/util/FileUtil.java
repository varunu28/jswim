package com.varun.swim.util;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class FileUtil {

    private static final String CONFIG_FILE = "config.txt";

    /**
     * Persist the port associated with a server to the CONFIG_FILE. Also creates the config file if it already
     * doesn't exist.
     */
    public static void persistServerConfig(int port) throws IOException {
        File file = new File(CONFIG_FILE);
        file.createNewFile();
        try (FileOutputStream fileOutputStream = new FileOutputStream(file, true)) {
            String configEntry = String.format("%d\n", port);
            fileOutputStream.write(configEntry.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Retrieves port associated with all peer nodes except the port of server reading the config.
     */
    public static ImmutableSet<Integer> readPeerServerConfig(int currentServerPort) throws IOException {
        return Files.readAllLines(Paths.get(CONFIG_FILE))
                .stream()
                .mapToInt(Integer::parseInt)
                .filter(p -> p != currentServerPort)
                .boxed()
                .collect(toImmutableSet());
    }
}
