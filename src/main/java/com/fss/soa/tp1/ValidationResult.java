package com.fss.soa.tp1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Outcome of validating ONE xml document against the XSD.
 * Three possible situations:
 * <ul>
 *   <li>valid                 : no error at all;</li>
 *   <li>invalid (schema)      : the XML is well-formed but breaks XSD rules;</li>
 *   <li>not well-formed (fatal): the XML syntax itself is broken (cannot even be parsed).</li>
 * </ul>
 */
public class ValidationResult {

    private final String documentName;
    private final List<String> errors = new ArrayList<String>();
    private final List<String> warnings = new ArrayList<String>();
    private boolean wellFormed = true;

    public ValidationResult(String documentName) {
        this.documentName = documentName;
    }

    void addError(String message) {
        errors.add(message);
    }

    void addWarning(String message) {
        warnings.add(message);
    }

    void addFatal(String message) {
        wellFormed = false;
        errors.add(message);
    }

    public String getDocumentName() { return documentName; }

    public boolean isValid() { return errors.isEmpty(); }

    public boolean isWellFormed() { return wellFormed; }

    public List<String> getErrors() { return Collections.unmodifiableList(errors); }

    public List<String> getWarnings() { return Collections.unmodifiableList(warnings); }

    /** A short one-line verdict. */
    public String verdict() {
        if (isValid()) {
            return "VALID";
        }
        if (!wellFormed) {
            return "NOT WELL-FORMED (fatal syntax error)";
        }
        return "INVALID (" + errors.size() + " schema error" + (errors.size() > 1 ? "s" : "") + ")";
    }
}
