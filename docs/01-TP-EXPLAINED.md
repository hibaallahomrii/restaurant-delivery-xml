# 01 - The TP explained in simple words (and solved)

This guide explains **what the TP asks**, **why**, and **how our solution answers each step**.
Read it first, then follow [`02-STEP-BY-STEP-GUIDE.md`](02-STEP-BY-STEP-GUIDE.md) to build everything by hand.

---

## Part A - The concepts (5 minutes)

### A.1 What is XML?
XML is a text format to store **structured data** using tags that you invent yourself.

```xml
<dish id="D001" available="true">
  <name>Pizza Margherita</name>
  <price currency="EUR">9.50</price>
</dish>
```

* `<dish>`, `<name>`, `<price>` are **elements**.
* `id`, `available`, `currency` are **attributes**.
* `Pizza Margherita` and `9.50` are the **text content**.

XML is the foundation of SOA: SOAP messages, WSDL files and many configuration files are XML.

### A.2 What is an XSD and why do we need it?
XML alone accepts anything: `<price>banana</price>` is perfectly *well-formed*.
An **XSD (XML Schema Definition)** is the contract that says what a *valid* document looks like:

| The XSD can say... | Example in our project |
|---|---|
| Which elements exist and in which order | a `dish` has `name`, then `price`, then optional `promotion`... |
| The data type of a value | `price` is a decimal > 0 with 2 decimals |
| The allowed values | order `status` is one of pending, confirmed, preparing, on_the_way, delivered, cancelled |
| A text pattern | a dish id looks like `D` + 3 digits |
| Required / optional | `description` is optional, `price` is mandatory |
| Keys and references | every `dishRef` in an order must point to an existing dish |

> **Well-formed vs valid**
> *Well-formed* = the syntax is correct (tags closed and nested properly).
> *Valid* = well-formed **and** it respects the XSD.
> Every valid document is well-formed, but not the opposite.

### A.3 What is XPath?
XPath is a **query language for XML**, like SQL for tables. It describes a *path* through the tree.

| Expression | Meaning |
|---|---|
| `//dish/name` | the `name` of every `dish`, anywhere |
| `//dish[price > 12]/name` | names of dishes costing more than 12 |
| `//restaurant[@status='open']` | restaurants whose `status` attribute is `open` |
| `count(//order)` | number of orders |

The JDK implements **XPath 1.0**.

### A.4 What is JAXP?
**JAXP = Java API for XML Processing**, the package `javax.xml.*`, **already included in the JDK**.
That is why our project needs *no* external dependency for the real work.

| Task | JAXP classes (the ones the TP names) |
|---|---|
| Validate XML against XSD | `SchemaFactory`, `Schema`, `Validator` |
| Parse XML into a tree | `DocumentBuilderFactory`, `DocumentBuilder`, `Document` (DOM) |
| Query with XPath | `XPathFactory`, `XPath`, `XPathExpression` |

### A.5 What is Maven?
Maven is a **build tool**. You describe the project in `pom.xml` and Maven:

1. downloads the libraries you declare (*dependencies*),
2. compiles the code (`mvn compile`),
3. runs the tests (`mvn test`),
4. packages a `.jar` (`mvn package`).

It imposes a standard layout: `src/main/java` (code), `src/main/resources` (data files), `src/test/java` (tests).

---

## Part B - The 6 steps of the TP, solved

### Step 1 - Choose a domain and model it in XML
**Asked:** pick a real domain, define the main entities, write an XML file.

**Our choice:** a *restaurant / meal-delivery platform* (like Uber Eats or Glovo).

**Entities found** (think: "which nouns does the business talk about?"):

| Entity | Role |
|---|---|
| `restaurant` | sells food; has an address, opening hours and a menu |
| `category` > `dish` | the menu is organised in categories of dishes |
| `customer` | the person who orders |
| `driver` | delivers the order |
| `order` | links a customer, a restaurant, (a driver) and dishes; has delivery, payment, review |

**Key modelling decisions**

* **Nesting vs references.** A menu *belongs to* one restaurant -> nested inside it.
  But an order *refers to* a customer who exists independently -> stored as an id (`customerRef="C001"`).
* **Attributes vs elements.** Short identifying/state values (`id`, `status`, `active`) -> attributes.
  Descriptive data (`name`, `price`, `address`) -> elements.
* **Unique readable ids** with a prefix per entity: `R001`, `D001`, `C001`, `DR001`, `O1001`.

**Result:** [`restaurant-delivery.xml`](../src/main/resources/xml/restaurant-delivery.xml)
(5 restaurants, 22 dishes, 5 customers, 4 drivers, 10 orders covering every order status).

The document tree, simplified:

```
foodDelivery
├── restaurants
│   └── restaurant [@id, @status]
│       ├── name, cuisine, address, phone, email?, rating, deliveryRadiusKm, minimumOrder
│       ├── openingHours / day [@name, @open, @close]
│       └── menu / category [@name] / dish [@id, @available, @vegetarian, @spicy]
│               └── name, description?, price, promotion?, calories?, allergens?
├── customers / customer [@id, @active]
├── drivers   / driver   [@id, @status]
└── orders    / order    [@id, @customerRef, @restaurantRef, @driverRef?, @status]
        └── orderDate, items/item [@dishRef, @quantity, @unitPrice], delivery, payment, review?
```

### Step 2 - Create the XSD schema
**Asked:** a `.xsd` that describes the expected structure.

**Our schema** ([`restaurant-delivery.xsd`](../src/main/resources/xml/restaurant-delivery.xsd)) has 4 parts:

1. **Simple types** - reusable constraints: `DishIdType` (pattern `D\d{3}`), `RatingType` (0 to 5), `OrderStatusType` (enumeration)...
2. **Complex types** - building blocks: `AddressType`, `DishType`, `OrderType`...
3. **Root element** - `foodDelivery` and the order of its children.
4. **Identity constraints** - `xs:key` (= primary key) and `xs:keyref` (= foreign key).

A taste of each part:

```xml
<!-- simple type with a pattern -->
<xs:simpleType name="DishIdType">
  <xs:restriction base="xs:string"><xs:pattern value="D\d{3}"/></xs:restriction>
</xs:simpleType>

<!-- complex type: ordered children + attributes -->
<xs:complexType name="DishType">
  <xs:sequence>
    <xs:element name="name"  type="NameType"/>
    <xs:element name="price" type="PriceElementType"/>
    <xs:element name="promotion" type="PromotionType" minOccurs="0"/>   <!-- optional -->
  </xs:sequence>
  <xs:attribute name="id" type="DishIdType" use="required"/>
</xs:complexType>

<!-- primary key and foreign key -->
<xs:key name="dishKey">
  <xs:selector xpath="restaurants/restaurant/menu/category/dish"/>
  <xs:field xpath="@id"/>
</xs:key>
<xs:keyref name="itemDishRef" refer="dishKey">
  <xs:selector xpath="orders/order/items/item"/>
  <xs:field xpath="@dishRef"/>
</xs:keyref>
```

### Step 3 - Create the Maven project
**Asked:** a Maven project in IntelliJ with the necessary dependencies in `pom.xml`.

**Our `pom.xml`** ([`pom.xml`](../pom.xml)):

* `groupId` / `artifactId` / `version` = the identity of the project;
* Java **1.8** as compiler source/target (the TP says "JDK 1.8 or higher");
* **dependency:** only `junit-jupiter` (scope `test`), for the automatic tests;
* plugins: compiler, surefire (tests), jar (executable jar), exec (`mvn exec:java`).

> **Why no XML dependency?** Because JAXP is part of the JDK. A common beginner mistake is to add
> Xerces or dom4j "just in case" - it is not needed here. The TP only says "add the necessary
> dependencies": the honest answer for this project is JUnit for the tests, and nothing else.
> Explain this choice clearly in your defence (see `06-ORAL-EXAM-QUESTIONS.md`).

### Step 4 - Implement XML/XSD validation
**Asked:** use `SchemaFactory` and `Validator`.

**Our class:** [`XmlValidator.java`](../src/main/java/com/fss/soa/tp1/XmlValidator.java).
The essential code is only five lines:

```java
SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
Schema schema = factory.newSchema(new StreamSource(xsdStream));   // compile the XSD once
Validator validator = schema.newValidator();                      // one per document
validator.setErrorHandler(collector);                             // collect ALL errors
validator.validate(new StreamSource(xmlStream));                  // throws / reports problems
```

**What we added to make it professional**

* A custom `ErrorHandler` that **collects every error** with line and column, instead of stopping at the first.
* A `ValidationResult` object that distinguishes *valid*, *invalid* (schema errors) and *not well-formed* (fatal).
* Secure processing (XXE protection).
* A test suite of **10 invalid files** that proves the schema really catches mistakes.

### Step 5 - Query with XPath
**Asked:** use `XPathFactory` and `XPathExpression`; example queries *films / active users / products on promotion*.

**Our class:** [`XPathQueryService.java`](../src/main/java/com/fss/soa/tp1/XPathQueryService.java).

```java
Document doc = documentBuilder.parse(xmlStream);                  // 1. XML -> DOM tree
XPath xpath = XPathFactory.newInstance().newXPath();              // 2. XPath engine
XPathExpression expr = xpath.compile("//dish[promotion]/name");   // 3. compile the query
NodeList result = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);   // 4. run it
```

The 4 possible result types: `NODESET`, `STRING`, `NUMBER`, `BOOLEAN`
(e.g. `count(//dish)` is a NUMBER, `boolean(//order[@status='pending'])` is a BOOLEAN).

**The three examples of the TP, translated to our domain**

| TP example | Our equivalent | XPath |
|---|---|---|
| all film titles | all dish names | `//dish/name` |
| active users | active customers | `//customer[@active='true']/email` |
| products on promotion | dishes in promotion | `//dish[promotion]/name` |

The catalog contains **51 queries** grouped in 8 families (basic, attributes, numeric, text, allergens, aggregates, joins, axes).

### Step 6 - Test and verify
**Asked:** run the main class and check the results of both features.

```bash
mvn compile exec:java
```

What you must see:

1. `[OK] ... VALID` for the sample document;
2. the XPath results (e.g. Q07 = Pizza Margherita, Tiramisu, California Roll, Spicy Chicken Burger, Green Detox Smoothie);
3. `10 / 10 invalid documents were correctly rejected`;
4. `ALL CHECKS PASSED`.

Plus `mvn test` for the 20 automatic tests.

---

## Part C - Deliverables (what to put on GitHub)

| Required by the TP | Delivered |
|---|---|
| `pom.xml` | yes |
| XML file + XSD | yes (+ 10 invalid test files) |
| Main class with validation + XPath | yes (`Main`, `XmlValidator`, `XPathQueryService`) |
| Modelling document | [`03-MODELING-DOCUMENT.md`](03-MODELING-DOCUMENT.md) |
| GitHub link on the platform | see the end of [`02-STEP-BY-STEP-GUIDE.md`](02-STEP-BY-STEP-GUIDE.md) |

---

## Part D - Common mistakes (and how this project avoids them)

| Mistake | Consequence | Fix |
|---|---|---|
| Putting `--` inside an XML comment | the file is not well-formed | never write a double hyphen in a comment |
| Wrong element order | `cvc-complex-type.2.4.a` error | follow the `xs:sequence` order of the XSD |
| Forgetting `.trim()` on text | whitespace in results | `XPathQueryService` trims every value |
| Using `distinct-values()`, `min()`, `max()` | `XPathExpressionException` - these are XPath 2.0 | see Q04, Q18, Q19 for XPath 1.0 equivalents |
| Reading the file with a relative path that breaks in the IDE | `FileNotFoundException` | `Resources.open()` tries disk, then classpath |
| Namespaces mixed up | XPath returns nothing | our document has **no namespace**, so XPath stays simple |
