package com.fss.soa.tp1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Prints text on the console AND keeps a copy in memory so that it can be saved
 * to a report file at the end of the run (folder "output/").
 */
public class Reporter {

    private static final String NL = System.lineSeparator();
    private final StringBuilder buffer = new StringBuilder();

    public void println(String text) {
        System.out.println(text);
        buffer.append(text).append(NL);
    }

    public void println() {
        println("");
    }

    /** Prints a big section title. */
    public void title(String text) {
        String line = repeat('=', 78);
        println();
        println(line);
        println("  " + text);
        println(line);
    }

    /** Prints a smaller section title. */
    public void section(String text) {
        println();
        println(">> " + text);
        println(repeat('-', Math.min(78, text.length() + 3)));
    }

    /** Saves everything printed so far into a UTF-8 text file. */
    public void save(Path file) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        Files.write(file, buffer.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** Java 8 compatible replacement for String.repeat (Java 11+). */
    public static String repeat(char c, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
