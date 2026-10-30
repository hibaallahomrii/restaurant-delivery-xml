package com.fss.soa.tp1;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * BONUS - Business rules that an XSD 1.0 CANNOT express.
 *
 * <p>An XSD checks the <em>shape</em> of the data (types, order, keys). It cannot compare
 * two values coming from different places of the document. These rules are therefore
 * verified in Java, using XPath to navigate the document:</p>
 * <ul>
 *   <li><b>R1</b> every ordered dish belongs to the menu of the order's restaurant;</li>
 *   <li><b>R2</b> item/@unitPrice equals the current price of the dish;</li>
 *   <li><b>R3</b> payment/amount = sum(quantity x unitPrice) + delivery fee;</li>
 *   <li><b>R4</b> a "delivered" order has a driver and a deliveredAt date;</li>
 *   <li><b>R5</b> a "cancelled" order has no driver and its payment is not "paid".</li>
 * </ul>
 */
public class BusinessRulesChecker {

    private final XPathQueryService xp;
    private final List<String> violations = new ArrayList<String>();
    private int checks = 0;

    public BusinessRulesChecker(XPathQueryService xp) {
        this.xp = xp;
    }

    public List<String> getViolations() { return violations; }

    public int getChecks() { return checks; }

    public boolean isOk() { return violations.isEmpty(); }

    public void check() throws XPathExpressionException {
        violations.clear();
        checks = 0;

        NodeList orders = xp.selectNodes("//order", xp.getDocument());
        for (int i = 0; i < orders.getLength(); i++) {
            checkOrder(orders.item(i));
        }
    }

    private void checkOrder(Node order) throws XPathExpressionException {
        String orderId = XPathQueryService.attr(order, "id");
        String restaurantId = XPathQueryService.attr(order, "restaurantRef");
        String status = XPathQueryService.attr(order, "status");
        boolean hasDriver = !XPathQueryService.attr(order, "driverRef").isEmpty();

        BigDecimal itemsTotal = BigDecimal.ZERO;
        NodeList items = xp.selectNodes("items/item", order);          // relative XPath

        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            String dishId = XPathQueryService.attr(item, "dishRef");
            int quantity = Integer.parseInt(XPathQueryService.attr(item, "quantity"));
            BigDecimal unitPrice = new BigDecimal(XPathQueryService.attr(item, "unitPrice"));

            // R1 - dish belongs to this restaurant's menu
            checks++;
            boolean belongs = xp.selectBoolean("boolean(//restaurant[@id='" + restaurantId
                    + "']/menu/category/dish[@id='" + dishId + "'])");
            if (!belongs) {
                violations.add("R1 " + orderId + ": dish " + dishId
                        + " is not on the menu of restaurant " + restaurantId);
            }

            // R2 - unit price equals the dish price
            checks++;
            String listPrice = xp.selectString("//dish[@id='" + dishId + "']/price");
            if (!listPrice.isEmpty() && unitPrice.compareTo(new BigDecimal(listPrice)) != 0) {
                violations.add("R2 " + orderId + ": unitPrice " + unitPrice + " of " + dishId
                        + " differs from the menu price " + listPrice);
            }

            itemsTotal = itemsTotal.add(unitPrice.multiply(new BigDecimal(quantity)));
        }

        // R3 - payment amount = items + delivery fee
        checks++;
        BigDecimal fee = new BigDecimal(xp.selectString("delivery/fee", order));
        BigDecimal amount = new BigDecimal(xp.selectString("payment/amount", order));
        BigDecimal expected = itemsTotal.add(fee);
        if (expected.compareTo(amount) != 0) {
            violations.add("R3 " + orderId + ": payment is " + amount + " but items (" + itemsTotal
                    + ") + fee (" + fee + ") = " + expected);
        }

        // R4 - delivered => driver + deliveredAt
        if ("delivered".equals(status)) {
            checks++;
            boolean hasDeliveredAt = xp.selectBoolean("boolean(delivery/deliveredAt)", order);
            if (!hasDriver || !hasDeliveredAt) {
                violations.add("R4 " + orderId + ": a delivered order needs a driverRef and a deliveredAt date");
            }
        }

        // R5 - cancelled => no driver, payment not "paid"
        if ("cancelled".equals(status)) {
            checks++;
            String payStatus = xp.selectString("payment/@status", order);
            if (hasDriver || "paid".equals(payStatus)) {
                violations.add("R5 " + orderId + ": a cancelled order must have no driver and an unpaid/refunded payment");
            }
        }
    }
}
