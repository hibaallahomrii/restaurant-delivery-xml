# 04 - XPath 1.0 cheat sheet (with our Food Delivery document)

The JDK evaluates **XPath 1.0**. All examples run on `restaurant-delivery.xml`
(try them live with `java -jar target/restaurant-delivery-xml.jar interactive`).

## 1. Paths

| Syntax | Meaning | Example |
|---|---|---|
| `/` | from the root | `/foodDelivery/restaurants/restaurant` |
| `//` | anywhere in the document | `//dish` |
| `.` | current node | `./name` |
| `..` | parent node | `//price/..` (the dish) |
| `*` | any element | `//dish/*` (all children of a dish) |
| `@attr` | an attribute | `//restaurant/@id` |
| `@*` | any attribute | `//dish[1]/@*` |
| `text()` | the text node | `//dish/name/text()` |

## 2. Predicates `[ ... ]` (filters)

| Syntax | Example | Meaning |
|---|---|---|
| position number | `//category/dish[1]` | first dish of each category |
| `last()` | `(//dish)[last()]` | last dish of the document |
| `position()` | `//dish[position() <= 3]` | first three dishes of each parent |
| attribute test | `//order[@status='delivered']` | by attribute value |
| child value | `//dish[price > 12]` | by child element value |
| child exists | `//dish[promotion]` | has a `promotion` child |
| attribute exists | `//order[@driverRef]` | has a `driverRef` attribute |
| negation | `//order[not(@driverRef)]` | has NO `driverRef` |
| and / or | `//dish[@spicy='true' or promotion]` | combine conditions |
| comparison | `=  !=  <  <=  >  >=` | numbers or strings |

> In an XML file (and in Java strings) `<` is fine inside an XPath string passed to Java.
> Inside an XML/XSD attribute you would have to write `&lt;`.

## 3. Axes (directions in the tree)

| Axis | Example | Meaning |
|---|---|---|
| `child::` (default) | `restaurant/name` | direct children |
| `descendant::` | `restaurant/descendant::dish` | all nested dishes |
| `parent::` | `//dish/parent::category` | the parent |
| `ancestor::` | `//dish[@id='D007']/ancestor::restaurant` | all ancestors |
| `following-sibling::` | `//dish[@id='D001']/following-sibling::dish[1]` | next brother |
| `preceding-sibling::` | `//restaurant[2]/preceding-sibling::restaurant` | previous brothers |
| `self::` | `//*[self::dish or self::category]` | the node itself |

## 4. Functions

| Kind | Function | Example |
|---|---|---|
| Node-set | `count(x)` | `count(//order)` -> 10 |
| | `last()`, `position()` | see predicates |
| Number | `sum(x)` | `sum(//order/delivery/fee)` -> 27 |
| | `round() floor() ceiling()` | `round(sum(//dish/price) div count(//dish))` |
| | arithmetic `+ - * div mod` | `sum(//dish/price) div count(//dish)` (`div`, not `/`!) |
| String | `contains(s, t)` | `//dish[contains(name,'Pizza')]` |
| | `starts-with(s, t)` | `//restaurant[starts-with(name,'B')]` |
| | `string-length(s)` | `//dish[string-length(name) > 20]` |
| | `substring(s, from, len)` | `substring(//dish[1]/name, 1, 5)` -> `Pizza` |
| | `normalize-space(s)` | removes extra spaces |
| | `translate(s, from, to)` | lower-case trick, see Q23 |
| | `concat(a, b, ...)` | `concat('Dish: ', //dish[1]/name)` |
| Boolean | `not(x)`, `boolean(x)`, `true()`, `false()` | `boolean(//order[@status='pending'])` |

## 5. Union

`//dish[@spicy='true']/name | //dish[promotion]/name`  -> nodes of both queries, no duplicates, document order.

## 6. Result types in Java (`XPathConstants`)

| Constant | Java type | Typical expression |
|---|---|---|
| `NODESET` | `NodeList` | `//dish/name` |
| `STRING` | `String` | `//driver[@id='DR001']/name` |
| `NUMBER` | `Double` | `count(//dish)` |
| `BOOLEAN` | `Boolean` | `boolean(//order[@status='pending'])` |
| `NODE` | `Node` | first node only |

## 7. XPath 2.0 functions you may know - and their XPath 1.0 replacement

| Not available in the JDK | XPath 1.0 replacement (see catalog) |
|---|---|
| `distinct-values(//city)` | `//restaurant[not(address/city = preceding-sibling::restaurant/address/city)]/address/city` (Q04) |
| `min(//price)` | `//dish[not(price > //dish/price)]/name` (Q18) |
| `max(//price)` | `//dish[not(price < //dish/price)]/name` (Q19) |
| `avg(//price)` | `sum(//price) div count(//price)` (Q30) |
| `string-join(...)` | join in Java (`Main.join`) |
| `lower-case(s)` | `translate(s, 'ABC...Z', 'abc...z')` (Q23) |

## 8. Join pattern (two lists compared)
`//restaurant[@id = //order[@id='O1001']/@restaurantRef]/name`
reads: "restaurants whose id equals ANY of the restaurantRef values of order O1001".
When one side of `=` is a node-set, the comparison is true if **at least one** node matches.

## 9. Frequent pitfalls

* XPath is **case-sensitive**: `//Dish` finds nothing.
* Attribute values are text: write `@available='true'` (with quotes).
* `//dish[1]` means "the first dish of **each parent**"; for the first of the whole document write `(//dish)[1]`.
* Use `div` for division (`/` is the path separator).
* With a namespace in the document, plain names stop matching (our document has none on purpose).
