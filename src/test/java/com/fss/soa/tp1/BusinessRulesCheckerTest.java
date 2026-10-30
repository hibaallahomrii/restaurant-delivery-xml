package com.fss.soa.tp1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests of the bonus business rules (things the XSD cannot check). */
class BusinessRulesCheckerTest {

    @Test
    void sampleDataRespectsAllRules() throws Exception {
        BusinessRulesChecker checker = new BusinessRulesChecker(TestSupport.defaultXpath());
        checker.check();
        assertTrue(checker.isOk(), "violations: " + checker.getViolations());
        assertTrue(checker.getChecks() > 50);
    }

    @Test
    void wrongPaymentAmountIsDetected() throws Exception {
        String xml = TestSupport.readText(Resources.DEFAULT_XML)
                .replace("<amount currency=\"EUR\">41.50</amount>", "<amount currency=\"EUR\">40.00</amount>");
        BusinessRulesChecker checker = new BusinessRulesChecker(TestSupport.xpathOn(xml));
        checker.check();
        assertEquals(1, checker.getViolations().size());
        assertTrue(checker.getViolations().get(0).startsWith("R3 O1001"));
    }

    @Test
    void dishFromAnotherRestaurantIsDetected() throws Exception {
        // order O1006 belongs to R001 (pizzeria) - make it contain a sushi dish (D006 belongs to R002)
        String xml = TestSupport.readText(Resources.DEFAULT_XML)
                .replace("<item dishRef=\"D002\" quantity=\"1\" unitPrice=\"12.00\"/>",
                         "<item dishRef=\"D006\" quantity=\"1\" unitPrice=\"14.90\"/>");
        BusinessRulesChecker checker = new BusinessRulesChecker(TestSupport.xpathOn(xml));
        checker.check();
        assertTrue(checker.getViolations().toString().contains("R1 O1006"), checker.getViolations().toString());
    }
}
