package com.fss.soa.tp1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.fss.soa.tp1.QueryDefinition.Type.BOOLEAN;
import static com.fss.soa.tp1.QueryDefinition.Type.NODES;
import static com.fss.soa.tp1.QueryDefinition.Type.NUMBER;

/**
 * The catalog of ready-made XPath queries for the Food Delivery document.
 * It goes from very simple (select all names) to advanced (joins between elements
 * using ids, axes, union, "never ordered" dishes ...).
 * It also contains the three examples of the TP statement (films -> dishes,
 * active users -> active customers, products on promotion -> dishes with promotion).
 */
public final class QueryCatalog {

    private static final String A = "1. Basic selection";
    private static final String B = "2. Attributes and predicates";
    private static final String C = "3. Numeric comparisons";
    private static final String D = "4. Text functions";
    private static final String E = "5. Allergens and diet";
    private static final String F = "6. Aggregates (count / sum)";
    private static final String G = "7. Orders and cross-references (joins by id)";
    private static final String H = "8. Axes, position and union";

    private static final String LOWER = "translate(name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')";

    private QueryCatalog() { }

    public static List<QueryDefinition> all() {
        List<QueryDefinition> q = new ArrayList<QueryDefinition>();

        // ---- 1. Basic selection ------------------------------------------------
        q.add(new QueryDefinition("Q01", A, "All restaurant names (TP example: 'all film titles')",
                "//restaurants/restaurant/name", NODES));
        q.add(new QueryDefinition("Q02", A, "All dish names, in every menu",
                "//dish/name", NODES));
        q.add(new QueryDefinition("Q03", A, "How many dishes are there?",
                "count(//dish)", NUMBER));
        q.add(new QueryDefinition("Q04", A, "Distinct cities (XPath 1.0 trick for distinct-values)",
                "//restaurant[not(address/city = preceding-sibling::restaurant/address/city)]/address/city", NODES));

        // ---- 2. Attributes and predicates --------------------------------------
        q.add(new QueryDefinition("Q05", B, "Restaurants that are currently open",
                "//restaurant[@status='open']/name", NODES));
        q.add(new QueryDefinition("Q06", B, "Restaurants that are NOT open",
                "//restaurant[@status!='open']/name", NODES));
        q.add(new QueryDefinition("Q07", B, "Dishes in promotion (TP example: 'products on sale')",
                "//dish[promotion]/name", NODES));
        q.add(new QueryDefinition("Q08", B, "Dishes with a discount of 15% or more",
                "//dish[promotion/@discountPercent >= 15]/name", NODES));
        q.add(new QueryDefinition("Q09", B, "Vegetarian dishes that can be ordered now",
                "//dish[@vegetarian='true' and @available='true']/name", NODES));
        q.add(new QueryDefinition("Q10", B, "Spicy dishes",
                "//dish[@spicy='true']/name", NODES));
        q.add(new QueryDefinition("Q11", B, "Dishes that are out of stock",
                "//dish[@available='false']/name", NODES));
        q.add(new QueryDefinition("Q12", B, "E-mails of ACTIVE customers (TP example: 'active users')",
                "//customer[@active='true']/email", NODES));
        q.add(new QueryDefinition("Q13", B, "Last names of INACTIVE customers",
                "//customer[@active='false']/lastName", NODES));
        q.add(new QueryDefinition("Q14", B, "Customers having at least 200 loyalty points",
                "//customer[loyaltyPoints >= 200]/lastName", NODES));
        q.add(new QueryDefinition("Q15", B, "Available delivery drivers",
                "//driver[@status='available']/name", NODES));

        // ---- 3. Numeric comparisons --------------------------------------------
        q.add(new QueryDefinition("Q16", C, "Dishes more expensive than 12 EUR",
                "//dish[price > 12]/name", NODES));
        q.add(new QueryDefinition("Q17", C, "Dishes priced between 5 and 10 EUR (inclusive)",
                "//dish[price >= 5 and price <= 10]/name", NODES));
        q.add(new QueryDefinition("Q18", C, "The cheapest dish (no dish is cheaper)",
                "//dish[not(price > //dish/price)]/name", NODES));
        q.add(new QueryDefinition("Q19", C, "The most expensive dish (no dish is dearer)",
                "//dish[not(price < //dish/price)]/name", NODES));
        q.add(new QueryDefinition("Q20", C, "Restaurants rated 4.5 or more",
                "//restaurant[rating >= 4.5]/name", NODES));

        // ---- 4. Text functions -------------------------------------------------
        q.add(new QueryDefinition("Q21", D, "Dishes whose name contains 'Pizza'",
                "//dish[contains(name,'Pizza')]/name", NODES));
        q.add(new QueryDefinition("Q22", D, "Restaurants whose name starts with 'B'",
                "//restaurant[starts-with(name,'B')]/name", NODES));
        q.add(new QueryDefinition("Q23", D, "Case-insensitive search of 'burger' (translate trick)",
                "//dish[contains(" + LOWER + ",'burger')]/name", NODES));
        q.add(new QueryDefinition("Q24", D, "Restaurants located in Lyon",
                "//restaurant[address/city='Lyon']/name", NODES));

        // ---- 5. Allergens and diet ---------------------------------------------
        q.add(new QueryDefinition("Q25", E, "Dishes containing gluten",
                "//dish[allergens/allergen='gluten']/name", NODES));
        q.add(new QueryDefinition("Q26", E, "Dishes declaring NO allergen at all",
                "//dish[not(allergens/allergen)]/name", NODES));
        q.add(new QueryDefinition("Q27", E, "Vegetarian dishes WITHOUT gluten",
                "//dish[@vegetarian='true' and not(allergens/allergen='gluten')]/name", NODES));

        // ---- 6. Aggregates -----------------------------------------------------
        q.add(new QueryDefinition("Q28", F, "Total number of orders",
                "count(//order)", NUMBER));
        q.add(new QueryDefinition("Q29", F, "Revenue of delivered orders (EUR)",
                "sum(//order[@status='delivered']/payment/amount)", NUMBER));
        q.add(new QueryDefinition("Q30", F, "Average dish price (EUR)",
                "sum(//dish/price) div count(//dish)", NUMBER));
        q.add(new QueryDefinition("Q31", F, "Total delivery fees (EUR)",
                "sum(//order/delivery/fee)", NUMBER));
        q.add(new QueryDefinition("Q32", F, "Average restaurant rating",
                "sum(//restaurant/rating) div count(//restaurant)", NUMBER));
        q.add(new QueryDefinition("Q33", F, "Is there at least one pending order? (boolean)",
                "boolean(//order[@status='pending'])", BOOLEAN));

        // ---- 7. Orders and cross-references ------------------------------------
        q.add(new QueryDefinition("Q34", G, "Ids of delivered orders",
                "//order[@status='delivered']/@id", NODES));
        q.add(new QueryDefinition("Q35", G, "Orders placed by customer C001",
                "//order[@customerRef='C001']/@id", NODES));
        q.add(new QueryDefinition("Q36", G, "Name of the restaurant of order O1001 (join order -> restaurant)",
                "//restaurant[@id = //order[@id='O1001']/@restaurantRef]/name", NODES));
        q.add(new QueryDefinition("Q37", G, "Names of the dishes ordered in O1001 (join item -> dish)",
                "//dish[@id = //order[@id='O1001']/items/item/@dishRef]/name", NODES));
        q.add(new QueryDefinition("Q38", G, "Last names of customers who left a 5-star review",
                "//customer[@id = //order[review/@stars=5]/@customerRef]/lastName", NODES));
        q.add(new QueryDefinition("Q39", G, "Orders with at least one item ordered in quantity 2 or more",
                "//order[items/item/@quantity >= 2]/@id", NODES));
        q.add(new QueryDefinition("Q40", G, "Orders without an assigned driver",
                "//order[not(@driverRef)]/@id", NODES));
        q.add(new QueryDefinition("Q41", G, "Orders whose total is above 40 EUR",
                "//order[payment/amount > 40]/@id", NODES));
        q.add(new QueryDefinition("Q42", G, "Dishes that were NEVER ordered",
                "//dish[not(@id = //item/@dishRef)]/name", NODES));
        q.add(new QueryDefinition("Q43", G, "Names of drivers who delivered at least one order",
                "//driver[@id = //order[@status='delivered']/@driverRef]/name", NODES));

        // ---- 8. Axes, position and union ---------------------------------------
        q.add(new QueryDefinition("Q44", H, "Restaurant that owns dish D007 (ancestor axis)",
                "//dish[@id='D007']/ancestor::restaurant/name", NODES));
        q.add(new QueryDefinition("Q45", H, "Dish that follows D001 in the menu (following-sibling axis)",
                "//dish[@id='D001']/following-sibling::dish[1]/name", NODES));
        q.add(new QueryDefinition("Q46", H, "First dish of every category",
                "//category/dish[1]/name", NODES));
        q.add(new QueryDefinition("Q47", H, "Last dish of the whole document",
                "(//dish)[last()]/name", NODES));
        q.add(new QueryDefinition("Q48", H, "Menu categories of restaurant R002",
                "//restaurant[@id='R002']/menu/category/@name", NODES));
        q.add(new QueryDefinition("Q49", H, "Opening time of R001 on Monday",
                "//restaurant[@id='R001']/openingHours/day[@name='Monday']/@open", NODES));
        q.add(new QueryDefinition("Q50", H, "Restaurants closed on Sunday (no <day name='Sunday'>)",
                "//restaurant[not(openingHours/day[@name='Sunday'])]/name", NODES));
        q.add(new QueryDefinition("Q51", H, "Union (|): spicy dishes OR dishes in promotion",
                "//dish[@spicy='true']/name | //dish[promotion]/name", NODES));

        return Collections.unmodifiableList(q);
    }
}
