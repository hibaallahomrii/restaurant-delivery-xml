package com.fss.soa.tp1;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests of FEATURE 2 : XPath querying. */
class XPathQueryServiceTest {

    private static XPathQueryService xp;

    @BeforeAll
    static void load() throws Exception {
        xp = TestSupport.defaultXpath();
    }

    @Test
    void countsRestaurantsAndDishes() throws Exception {
        assertEquals(5.0, xp.selectNumber("count(//restaurant)"), 0.0001);
        assertEquals(22.0, xp.selectNumber("count(//dish)"), 0.0001);
    }

    @Test
    void selectsOpenRestaurants() throws Exception {
        List<String> names = xp.selectValues("//restaurant[@status='open']/name");
        assertEquals(4, names.size());
        assertTrue(!names.contains("Green Garden Vegan Kitchen"));
    }

    @Test
    void selectsDishesInPromotion() throws Exception {
        List<String> names = xp.selectValues("//dish[promotion]/name");
        assertEquals(Arrays.asList("Pizza Margherita", "Tiramisu", "California Roll (8 pcs)",
                "Spicy Chicken Burger", "Green Detox Smoothie"), names);
    }

    @Test
    void selectsActiveCustomers() throws Exception {
        assertEquals(4, xp.selectValues("//customer[@active='true']").size());
    }

    @Test
    void computesDeliveredRevenue() throws Exception {
        assertEquals(218.90, xp.selectNumber("sum(//order[@status='delivered']/payment/amount)"), 0.001);
    }

    @Test
    void findsCheapestAndMostExpensiveDish() throws Exception {
        assertEquals(Arrays.asList("Iced Green Tea"), xp.selectValues("//dish[not(price > //dish/price)]/name"));
        assertEquals(Arrays.asList("Salmon Nigiri Set"), xp.selectValues("//dish[not(price < //dish/price)]/name"));
    }

    @Test
    void joinsOrdersAndRestaurantsThroughIds() throws Exception {
        assertEquals(Arrays.asList("Bella Napoli Pizzeria"),
                xp.selectValues("//restaurant[@id = //order[@id='O1001']/@restaurantRef]/name"));
    }

    @Test
    void findsDishesNeverOrdered() throws Exception {
        assertEquals(3, xp.selectValues("//dish[not(@id = //item/@dishRef)]/name").size());
    }

    @Test
    void booleanAndStringResultTypesWork() throws Exception {
        assertTrue(xp.selectBoolean("boolean(//order[@status='pending'])"));
        assertEquals("Karim Benali", xp.selectString("//driver[@id='DR001']/name"));
    }

    @Test
    void everyQueryOfTheCatalogEvaluatesWithoutError() throws Exception {
        for (QueryDefinition q : QueryCatalog.all()) {
            switch (q.getType()) {
                case NUMBER:  xp.selectNumber(q.getExpression()); break;
                case BOOLEAN: xp.selectBoolean(q.getExpression()); break;
                case STRING:  xp.selectString(q.getExpression()); break;
                default:      xp.selectValues(q.getExpression());
            }
        }
        assertTrue(QueryCatalog.all().size() >= 40);
    }
}
