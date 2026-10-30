package com.fss.soa.tp1;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests of FEATURE 1 : XML / XSD validation. */
class XmlValidatorTest {

    private static XmlValidator validator;

    @BeforeAll
    static void compileSchemaOnce() throws Exception {
        try (InputStream xsd = Resources.open(Resources.DEFAULT_XSD)) {
            validator = new XmlValidator(xsd, Resources.DEFAULT_XSD);
        }
    }

    private ValidationResult validate(String location) throws Exception {
        try (InputStream in = Resources.open(location)) {
            return validator.validate(in, location);
        }
    }

    @Test
    void validDocumentIsAccepted() throws Exception {
        ValidationResult r = validate(Resources.DEFAULT_XML);
        assertTrue(r.isValid(), "The sample document must be valid but got: " + r.getErrors());
    }

    @Test
    void everyInvalidSampleIsRejected() throws Exception {
        String[] files = {
                "invalid-01-missing-required-element.xml", "invalid-02-bad-enumeration.xml",
                "invalid-03-bad-id-pattern.xml", "invalid-04-broken-reference.xml",
                "invalid-05-duplicate-id.xml", "invalid-06-negative-price.xml",
                "invalid-07-rating-out-of-range.xml", "invalid-08-not-well-formed.xml",
                "invalid-09-wrong-element-order.xml", "invalid-10-bad-email-and-quantity.xml"
        };
        for (String file : files) {
            ValidationResult r = validate(Resources.INVALID_DIR + file);
            assertFalse(r.isValid(), file + " should have been rejected");
        }
    }

    @Test
    void brokenForeignKeyIsDetected() throws Exception {
        ValidationResult r = validate(Resources.INVALID_DIR + "invalid-04-broken-reference.xml");
        assertTrue(r.getErrors().toString().contains("R999"), "error must mention the unknown id R999");
    }

    @Test
    void duplicatePrimaryKeyIsDetected() throws Exception {
        ValidationResult r = validate(Resources.INVALID_DIR + "invalid-05-duplicate-id.xml");
        assertTrue(r.getErrors().toString().contains("Duplicate key"), "error must report a duplicate key");
    }

    @Test
    void notWellFormedDocumentIsReportedAsFatal() throws Exception {
        ValidationResult r = validate(Resources.INVALID_DIR + "invalid-08-not-well-formed.xml");
        assertFalse(r.isWellFormed(), "a syntax error must set wellFormed=false");
    }

    @Test
    void schemaErrorsKeepTheDocumentWellFormed() throws Exception {
        ValidationResult r = validate(Resources.INVALID_DIR + "invalid-06-negative-price.xml");
        assertTrue(r.isWellFormed(), "a schema violation is not a syntax error");
        assertFalse(r.isValid());
    }

    @Test
    void severalErrorsAreCollectedInOneRun() throws Exception {
        ValidationResult r = validate(Resources.INVALID_DIR + "invalid-10-bad-email-and-quantity.xml");
        assertTrue(r.getErrors().size() >= 2, "both defects must be reported, got " + r.getErrors());
    }
}
