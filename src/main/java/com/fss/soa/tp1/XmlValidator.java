package com.fss.soa.tp1;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;

/**
 * FEATURE 1 - XML / XSD validation (JAXP).
 *
 * <p>The three key JAXP classes of the TP:</p>
 * <ol>
 *   <li>{@link SchemaFactory} : reads the XSD and builds a {@link Schema} (compiled, thread-safe);</li>
 *   <li>{@link Schema}        : the compiled rules;</li>
 *   <li>{@link Validator}     : checks ONE xml document against the Schema (not thread-safe,
 *       so we create a new one for each document).</li>
 * </ol>
 *
 * <p>Instead of stopping at the first problem, a custom {@link ErrorHandler} collects
 * ALL the errors with their line and column numbers.</p>
 */
public class XmlValidator {

    private final Schema schema;

    /**
     * Compiles the XSD once.
     *
     * @param xsd         stream on the .xsd file
     * @param xsdSystemId a name/URI of the schema, used only in error messages
     * @throws SAXException if the XSD itself is not a valid schema
     */
    public XmlValidator(InputStream xsd, String xsdSystemId) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        // Security best practice: forbid dangerous external resources (XXE protection).
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        this.schema = factory.newSchema(new StreamSource(xsd, xsdSystemId));
    }

    /**
     * Validates one document.
     *
     * @param xml   stream on the xml document
     * @param label name shown in the report
     * @return a {@link ValidationResult} (never throws for validation problems)
     */
    public ValidationResult validate(InputStream xml, String label) {
        ValidationResult result = new ValidationResult(label);

        Validator validator = schema.newValidator();
        try {
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        } catch (SAXException ignored) {
            // property not supported by this parser: not critical
        }
        validator.setErrorHandler(new CollectingErrorHandler(result));

        try {
            validator.validate(new StreamSource(xml));
        } catch (SAXException fatal) {
            // Already recorded by the error handler (fatalError) - nothing more to do.
        } catch (IOException io) {
            result.addFatal("I/O error while reading the document: " + io.getMessage());
        }
        return result;
    }

    /** Collects every warning / error instead of aborting on the first one. */
    private static final class CollectingErrorHandler implements ErrorHandler {

        private final ValidationResult result;

        CollectingErrorHandler(ValidationResult result) {
            this.result = result;
        }

        @Override
        public void warning(SAXParseException e) {
            result.addWarning(format(e));
        }

        @Override
        public void error(SAXParseException e) {
            result.addError(format(e));          // validation continues
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            result.addFatal(format(e));          // not well-formed: parsing must stop
            throw e;
        }

        private static String format(SAXParseException e) {
            return "line " + e.getLineNumber() + ", col " + e.getColumnNumber() + ": " + e.getMessage();
        }
    }
}
