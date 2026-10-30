package com.fss.soa.tp1;

import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * TP1 - SOA &amp; Web Services : XML modelling, XSD validation and XPath querying.
 * Domain : Restaurant / Meal-delivery platform.
 *
 * <pre>
 * Usage:  java -jar restaurant-delivery-xml.jar [mode] [--xml file.xml] [--xsd file.xsd]
 *
 *   (no mode)    run everything (validation + XPath + business rules + invalid suite)
 *   validate     FEATURE 1 : validate the XML against the XSD
 *   xpath        FEATURE 2 : run the XPath query catalog + order report
 *   rules        BONUS     : business rules that the XSD cannot express
 *   suite        BONUS     : validate the 10 deliberately invalid files (all must be rejected)
 *   interactive  type your own XPath expressions
 *   help         show this help
 * </pre>
 */
public class Main {

    /** The invalid test files and the defect each one contains. */
    private static final String[][] INVALID_SAMPLES = {
            {"invalid-01-missing-required-element.xml", "required <name> element missing"},
            {"invalid-02-bad-enumeration.xml", "order status 'shipped' not allowed"},
            {"invalid-03-bad-id-pattern.xml", "customer id 'CUST-4' breaks the id pattern"},
            {"invalid-04-broken-reference.xml", "order references a restaurant R999 that does not exist"},
            {"invalid-05-duplicate-id.xml", "two dishes share the id D017"},
            {"invalid-06-negative-price.xml", "dish with a negative price"},
            {"invalid-07-rating-out-of-range.xml", "restaurant rating 7.5 (max 5)"},
            {"invalid-08-not-well-formed.xml", "mismatched end tag </nam> (not well-formed)"},
            {"invalid-09-wrong-element-order.xml", "<phone> placed before <address>"},
            {"invalid-10-bad-email-and-quantity.xml", "bad e-mail + item quantity 0"},
    };

    private String xmlPath = Resources.DEFAULT_XML;
    private String xsdPath = Resources.DEFAULT_XSD;

    public static void main(String[] args) {
        int exit = new Main().run(args);
        System.exit(exit);
    }

    // =====================================================================
    //  Argument handling
    // =====================================================================
    int run(String[] args) {
        String mode = "all";
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if ("--xml".equals(a) && i + 1 < args.length) {
                xmlPath = args[++i];
            } else if ("--xsd".equals(a) && i + 1 < args.length) {
                xsdPath = args[++i];
            } else if (!a.startsWith("--")) {
                mode = a.toLowerCase(Locale.ROOT);
            }
        }

        try {
            switch (mode) {
                case "all":         return runAll();
                case "validate":    return runValidation(new Reporter());
                case "xpath":       return runXPath(new Reporter());
                case "rules":       return runRules(new Reporter());
                case "suite":       return runInvalidSuite(new Reporter());
                case "interactive": return runInteractive();
                case "help":
                default:
                    printHelp();
                    return "help".equals(mode) ? 0 : 2;
            }
        } catch (Exception e) {
            System.err.println("FATAL ERROR: " + e);
            return 1;
        }
    }

    private void printHelp() {
        System.out.println("TP1 SOA - Food Delivery : XML validation and XPath querying");
        System.out.println();
        System.out.println("Usage: java -jar restaurant-delivery-xml.jar [mode] [--xml file] [--xsd file]");
        System.out.println();
        System.out.println("  (no mode)    run everything");
        System.out.println("  validate     validate the XML against the XSD            (feature 1)");
        System.out.println("  xpath        run the XPath query catalog                  (feature 2)");
        System.out.println("  rules        check business rules the XSD cannot express  (bonus)");
        System.out.println("  suite        validate the 10 invalid sample files         (bonus)");
        System.out.println("  interactive  type your own XPath expressions              (bonus)");
        System.out.println("  help         this help");
    }

    // =====================================================================
    //  Run everything + write reports in ./output
    // =====================================================================
    private int runAll() throws Exception {
        Reporter validation = new Reporter();
        Reporter xpath = new Reporter();
        Reporter rules = new Reporter();

        int a = runValidation(validation);
        int b = a == 0 ? runXPath(xpath) : 1;
        int c = a == 0 ? runRules(rules) : 1;
        int d = runInvalidSuite(validation);

        validation.save(Paths.get("output", "validation-report.txt"));
        xpath.save(Paths.get("output", "xpath-report.txt"));
        rules.save(Paths.get("output", "business-rules-report.txt"));

        int exit = (a + b + c + d) == 0 ? 0 : 1;
        System.out.println();
        System.out.println("==============================================================================");
        System.out.println("  FINAL RESULT : " + (exit == 0 ? "ALL CHECKS PASSED" : "SOME CHECKS FAILED"));
        System.out.println("  Reports saved in the folder: " + Paths.get("output").toAbsolutePath());
        System.out.println("==============================================================================");
        return exit;
    }

    // =====================================================================
    //  FEATURE 1 - XML / XSD validation
    // =====================================================================
    private int runValidation(Reporter out) throws Exception {
        out.title("FEATURE 1 - XML / XSD VALIDATION");
        out.println("XML file : " + xmlPath);
        out.println("XSD file : " + xsdPath);

        ValidationResult result = validateOne(xmlPath, xsdPath);
        printResult(out, result);
        return result.isValid() ? 0 : 1;
    }

    private ValidationResult validateOne(String xml, String xsd) throws Exception {
        XmlValidator validator;
        try (InputStream xsdStream = Resources.open(xsd)) {
            validator = new XmlValidator(xsdStream, xsd);
        }
        try (InputStream xmlStream = Resources.open(xml)) {
            return validator.validate(xmlStream, xml);
        }
    }

    private void printResult(Reporter out, ValidationResult r) {
        out.println();
        out.println((r.isValid() ? "[OK]   " : "[FAIL] ") + r.getDocumentName() + "  ->  " + r.verdict());
        int n = 1;
        for (String e : r.getErrors()) {
            out.println("         " + (n++) + ") " + e);
        }
        for (String w : r.getWarnings()) {
            out.println("         warning: " + w);
        }
    }

    // =====================================================================
    //  BONUS - the invalid test files: every one MUST be rejected
    // =====================================================================
    private int runInvalidSuite(Reporter out) throws Exception {
        out.title("VALIDATION TEST SUITE - 10 invalid documents (all must be rejected)");

        XmlValidator validator;
        try (InputStream xsdStream = Resources.open(xsdPath)) {
            validator = new XmlValidator(xsdStream, xsdPath);
        }

        int correctlyRejected = 0;
        for (String[] sample : INVALID_SAMPLES) {
            String location = Resources.INVALID_DIR + sample[0];
            ValidationResult r;
            try (InputStream in = Resources.open(location)) {
                r = validator.validate(in, sample[0]);
            }
            boolean rejected = !r.isValid();
            if (rejected) {
                correctlyRejected++;
            }
            out.println();
            out.println((rejected ? "[REJECTED as expected] " : "[ACCEPTED - PROBLEM!]  ") + sample[0]);
            out.println("   defect  : " + sample[1]);
            out.println("   verdict : " + r.verdict());
            // show only the first error of each file to keep the report readable
            if (!r.getErrors().isEmpty()) {
                out.println("   1st err : " + shorten(r.getErrors().get(0), 150));
            }
        }

        out.println();
        out.println("Result: " + correctlyRejected + " / " + INVALID_SAMPLES.length
                + " invalid documents were correctly rejected.");
        return correctlyRejected == INVALID_SAMPLES.length ? 0 : 1;
    }

    // =====================================================================
    //  FEATURE 2 - XPath
    // =====================================================================
    private int runXPath(Reporter out) throws Exception {
        out.title("FEATURE 2 - XPATH QUERIES");
        XPathQueryService xp = loadXPath();

        String currentSection = "";
        for (QueryDefinition q : QueryCatalog.all()) {
            if (!q.getSection().equals(currentSection)) {
                currentSection = q.getSection();
                out.section(currentSection);
            }
            out.println();
            out.println(q.getId() + "  " + q.getTitle());
            out.println("      XPath : " + q.getExpression());
            out.println("      Result: " + evaluate(xp, q));
        }

        out.section("9. Order report (several XPath queries combined)");
        out.println();
        new OrderReport(xp).print(out);
        return 0;
    }

    private String evaluate(XPathQueryService xp, QueryDefinition q) throws XPathExpressionException {
        switch (q.getType()) {
            case NUMBER:
                return formatNumber(xp.selectNumber(q.getExpression()));
            case BOOLEAN:
                return String.valueOf(xp.selectBoolean(q.getExpression()));
            case STRING:
                return xp.selectString(q.getExpression());
            case NODES:
            default:
                List<String> values = xp.selectValues(q.getExpression());
                if (values.isEmpty()) {
                    return "(no result)";
                }
                return "[" + values.size() + "] " + join(values);
        }
    }

    private XPathQueryService loadXPath() throws Exception {
        try (InputStream in = Resources.open(xmlPath)) {
            return new XPathQueryService(in);
        }
    }

    // =====================================================================
    //  BONUS - business rules
    // =====================================================================
    private int runRules(Reporter out) throws Exception {
        out.title("BONUS - BUSINESS RULES (what an XSD cannot check)");
        XPathQueryService xp = loadXPath();
        BusinessRulesChecker checker = new BusinessRulesChecker(xp);
        checker.check();

        out.println("R1 dish belongs to the order's restaurant");
        out.println("R2 unit price equals the menu price");
        out.println("R3 payment = items + delivery fee");
        out.println("R4 delivered order has a driver and a delivery date");
        out.println("R5 cancelled order has no driver and is not 'paid'");
        out.println();
        out.println(checker.getChecks() + " checks executed.");
        if (checker.isOk()) {
            out.println("[OK]   no business rule violation.");
            return 0;
        }
        for (String v : checker.getViolations()) {
            out.println("[FAIL] " + v);
        }
        return 1;
    }

    // =====================================================================
    //  BONUS - interactive XPath console
    // =====================================================================
    private int runInteractive() throws Exception {
        XPathQueryService xp = loadXPath();
        System.out.println("Interactive XPath console on: " + xmlPath);
        System.out.println("Type an XPath 1.0 expression, ':list' for examples, ':quit' to leave.");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("xpath> ");
            String line = reader.readLine();
            if (line == null || ":quit".equals(line.trim()) || ":q".equals(line.trim())) {
                break;
            }
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (":list".equals(line)) {
                for (QueryDefinition q : Arrays.asList(QueryCatalog.all().get(4), QueryCatalog.all().get(6),
                        QueryCatalog.all().get(17), QueryCatalog.all().get(28), QueryCatalog.all().get(35))) {
                    System.out.println("  " + q.getExpression() + "      # " + q.getTitle());
                }
                continue;
            }
            try {
                List<String> values = xp.selectValues(line);
                System.out.println(values.isEmpty() ? "(no result)" : "[" + values.size() + "] " + join(values));
            } catch (XPathExpressionException notANodeSet) {
                // e.g. count(//dish) returns a number, not a node-set: evaluate it as a string
                try {
                    System.out.println(xp.selectString(line));
                } catch (XPathExpressionException bad) {
                    System.out.println("Invalid XPath: " + bad.getMessage());
                }
            }
        }
        return 0;
    }

    // =====================================================================
    //  Small helpers
    // =====================================================================
    private static String join(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(values.get(i));
        }
        return sb.toString();
    }

    private static String formatNumber(double d) {
        if (d == Math.rint(d)) {
            return String.valueOf((long) d);
        }
        return String.format(Locale.ROOT, "%.2f", d);
    }

    private static String shorten(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
