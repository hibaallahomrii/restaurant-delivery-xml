package com.fss.soa.tp1;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.util.Locale;

/**
 * Builds a readable table of all the orders by combining several XPath queries:
 * for each order we follow customerRef / restaurantRef / driverRef to the other entities.
 */
public class OrderReport {

    private final XPathQueryService xp;

    public OrderReport(XPathQueryService xp) {
        this.xp = xp;
    }

    public void print(Reporter out) throws XPathExpressionException {
        String header = String.format(Locale.ROOT, "%-6s %-18s %-26s %-13s %-14s %11s %-6s",
                "Order", "Customer", "Restaurant", "Status", "Driver", "Total", "Stars");
        out.println(header);
        out.println(Reporter.repeat('-', header.length()));

        NodeList orders = xp.selectNodes("//order", xp.getDocument());
        for (int i = 0; i < orders.getLength(); i++) {
            Node o = orders.item(i);

            String customer = xp.selectString("//customer[@id='" + XPathQueryService.attr(o, "customerRef")
                    + "']/firstName") + " " + xp.selectString("//customer[@id='"
                    + XPathQueryService.attr(o, "customerRef") + "']/lastName");
            String restaurant = xp.selectString("//restaurant[@id='"
                    + XPathQueryService.attr(o, "restaurantRef") + "']/name");

            String driverRef = XPathQueryService.attr(o, "driverRef");
            String driver = driverRef.isEmpty() ? "-" : xp.selectString("//driver[@id='" + driverRef + "']/name");

            String total = xp.selectString("payment/amount", o);
            String stars = xp.selectString("review/@stars", o);

            out.println(String.format(Locale.ROOT, "%-6s %-18s %-26s %-13s %-14s %11s %-6s",
                    XPathQueryService.attr(o, "id"), cut(customer, 18), cut(restaurant, 26),
                    XPathQueryService.attr(o, "status"), cut(driver, 14),
                    total + " EUR", stars.isEmpty() ? "-" : stars));
        }
    }

    private static String cut(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }
}
