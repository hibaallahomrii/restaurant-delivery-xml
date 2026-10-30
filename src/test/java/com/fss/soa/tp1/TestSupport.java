package com.fss.soa.tp1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Shared helpers for the tests. */
final class TestSupport {

    private TestSupport() { }

    static String readText(String location) throws IOException {
        try (InputStream in = Resources.open(location)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    static InputStream stream(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    static XPathQueryService xpathOn(String xmlText) throws Exception {
        return new XPathQueryService(stream(xmlText));
    }

    static XPathQueryService defaultXpath() throws Exception {
        try (InputStream in = Resources.open(Resources.DEFAULT_XML)) {
            return new XPathQueryService(in);
        }
    }
}
