package com.fss.soa.tp1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Small helper that opens a file either from the file system or from the classpath.
 *
 * <p>Why? When the project runs from IntelliJ / Maven, the XML and XSD files are
 * packaged as <em>classpath resources</em> (src/main/resources). But a student may also
 * want to validate their own file given as a normal path on the command line.
 * This class supports both.</p>
 */
public final class Resources {

    /** Default locations (classpath-relative) of the sample files. */
    public static final String DEFAULT_XML = "xml/restaurant-delivery.xml";
    public static final String DEFAULT_XSD = "xml/restaurant-delivery.xsd";
    public static final String INVALID_DIR = "xml/invalid/";

    private Resources() { }

    /**
     * Opens a location. The file system is tried first, then the classpath.
     *
     * @param location a file path (e.g. "C:/data/file.xml") or a classpath resource ("xml/file.xml")
     * @return an opened stream - the caller must close it
     * @throws IOException if the location cannot be found
     */
    public static InputStream open(String location) throws IOException {
        Path path = Paths.get(location);
        if (Files.isRegularFile(path)) {
            return Files.newInputStream(path);
        }
        String cp = location.startsWith("/") ? location.substring(1) : location;
        InputStream in = Resources.class.getClassLoader().getResourceAsStream(cp);
        if (in == null) {
            throw new FileNotFoundException(
                    "Not found (neither on disk nor on the classpath): " + location);
        }
        return in;
    }
}
