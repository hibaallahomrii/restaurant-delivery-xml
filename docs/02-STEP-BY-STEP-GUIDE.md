# 02 - Step-by-step guide: build, run and publish the project

Follow these steps in order. Each step ends with a **Checkpoint** so you know it worked.
If you only want to run the finished project, jump to **Step 8**.

---

## Step 0 - Install the tools

| Tool | Check command | Expected |
|---|---|---|
| JDK 8 or newer | `java -version` | `openjdk version "17..."` (or 8, 11, 21...) |
| Maven (optional but recommended) | `mvn -version` | `Apache Maven 3.x` and a Java version |
| Git | `git --version` | `git version 2.x` |
| IntelliJ IDEA Community | - | opens normally |

**Checkpoint:** `java -version` works. If `mvn` is "not recognized" on Windows, IntelliJ has its own
bundled Maven, so you can still follow the IntelliJ route.

---

## Step 1 - Create the Maven project in IntelliJ IDEA

1. **File > New > Project**.
2. Left panel: **New Project** (or "Java"). Fill in:
   * **Name:** `restaurant-delivery-xml`
   * **Language:** Java
   * **Build system:** **Maven**
   * **JDK:** 8 or newer
   * **Advanced Settings > GroupId:** `com.fss.soa`, **ArtifactId:** `restaurant-delivery-xml`
3. Click **Create**.

**Checkpoint:** you see `pom.xml` and the folders `src/main/java`, `src/main/resources`, `src/test/java`.

> Shortcut: instead of creating from scratch you can **File > Open** the delivered folder and
> IntelliJ imports it as a Maven project automatically ("Load Maven Changes" if asked).

---

## Step 2 - Configure `pom.xml`

Replace the generated content with the file of this repository. The important parts:

```xml
<properties>
  <maven.compiler.source>1.8</maven.compiler.source>
  <maven.compiler.target>1.8</maven.compiler.target>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<dependencies>
  <dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```

Click the small **Maven refresh icon** (or press `Ctrl+Shift+O`) so IntelliJ downloads JUnit.

**Checkpoint:** the Maven tool window (right side) shows *Dependencies > junit-jupiter*, no red errors.

> **Why is there no XML library?** `javax.xml.*` (JAXP) is part of the JDK.

---

## Step 3 - Choose the domain and write the XML

Create `src/main/resources/xml/restaurant-delivery.xml`.

Method to model any domain in 4 questions:

1. **Which nouns?** restaurant, dish, customer, driver, order.
2. **Who contains whom?** a restaurant *contains* its menu -> nest it.
3. **Who merely points to whom?** an order *points to* a customer -> use an id reference.
4. **Which values are identifiers/states?** -> attributes (`id`, `status`); everything else -> elements.

Start small, then grow:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<foodDelivery>
  <restaurants>
    <restaurant id="R001" status="open">
      <name>Bella Napoli Pizzeria</name>
      ...
```

Then copy the full file of the project (5 restaurants, 22 dishes, 5 customers, 4 drivers, 10 orders).

**Checkpoint:** open the file in IntelliJ - no red underline. (Well-formed: every tag closed.)

---

## Step 4 - Write the XSD

Create `src/main/resources/xml/restaurant-delivery.xsd` in this order (bottom-up is easiest):

1. **Simple types** (patterns, enumerations, number ranges).
2. **Complex types** - small ones first (`AddressType`, `PriceElementType`), then bigger ones (`DishType`, `OrderType`).
3. **Root element** `foodDelivery`.
4. **Keys and keyrefs**.

Link the XML to the XSD so IntelliJ validates while you type - add to the root element of the XML:

```xml
<foodDelivery xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:noNamespaceSchemaLocation="restaurant-delivery.xsd">
```

(`noNamespaceSchemaLocation` because our schema has no `targetNamespace`.)

**Checkpoint:** introduce a typo on purpose, e.g. `status="opne"`. IntelliJ underlines it in red.
Undo the typo.

### Validate online (the link given in the TP statement)
Paste the XML and the XSD in an online XML-schema validator and click validate. You should get "valid".
This is a good second opinion before you write any Java.

### Validate on the command line (optional)
```bash
./scripts/validate-with-xmllint.sh
```

---

## Step 5 - Write the Java classes

Package: `com.fss.soa.tp1` in `src/main/java`.

| Order | Class | Purpose |
|---|---|---|
| 1 | `Resources` | open a file from disk or from the classpath |
| 2 | `ValidationResult` | holds the verdict + the list of errors |
| 3 | `XmlValidator` | **Feature 1** - SchemaFactory, Schema, Validator |
| 4 | `XPathQueryService` | **Feature 2** - DocumentBuilder, XPathFactory, XPathExpression |
| 5 | `QueryDefinition` + `QueryCatalog` | the list of 51 named XPath queries |
| 6 | `BusinessRulesChecker`, `OrderReport` | bonus |
| 7 | `Reporter` | prints on the console and saves reports |
| 8 | `Main` | wires everything together, with several modes |

### The heart of Feature 1 (validation)
```java
SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
Schema schema = factory.newSchema(new StreamSource(xsdStream));
Validator validator = schema.newValidator();
validator.validate(new StreamSource(xmlStream));   // SAXException if invalid
```

### The heart of Feature 2 (XPath)
```java
DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
Document doc = dbf.newDocumentBuilder().parse(xmlStream);
XPath xpath = XPathFactory.newInstance().newXPath();
XPathExpression expr = xpath.compile("//dish[promotion]/name");
NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
for (int i = 0; i < nodes.getLength(); i++) {
    System.out.println(nodes.item(i).getTextContent());
}
```

**Checkpoint:** no red code in IntelliJ; **Build > Build Project** succeeds.

---

## Step 6 - Add the invalid files (validation test files)

Folder `src/main/resources/xml/invalid/` contains 10 copies of the valid file, each with ONE defect:

| File | Defect |
|---|---|
| `invalid-01-missing-required-element` | restaurant without `<name>` |
| `invalid-02-bad-enumeration` | order `status="shipped"` |
| `invalid-03-bad-id-pattern` | customer id `CUST-4` |
| `invalid-04-broken-reference` | `restaurantRef="R999"` |
| `invalid-05-duplicate-id` | two dishes with id `D017` |
| `invalid-06-negative-price` | price `-11.00` |
| `invalid-07-rating-out-of-range` | rating `7.5` |
| `invalid-08-not-well-formed` | `</nam>` instead of `</name>` |
| `invalid-09-wrong-element-order` | `<phone>` before `<address>` |
| `invalid-10-bad-email-and-quantity` | invalid e-mail and quantity `0` |

Each file starts with a comment describing the defect and the expected error.
Details and how to read the errors: [`05-VALIDATION-FILES.md`](05-VALIDATION-FILES.md).

---

## Step 7 - Write the tests

In `src/test/java/com/fss/soa/tp1/`: `XmlValidatorTest`, `XPathQueryServiceTest`, `BusinessRulesCheckerTest`.

Run them: right-click the folder `src/test/java` > **Run 'All Tests'**, or `mvn test`.

**Checkpoint:** all tests green (20 tests).

---

## Step 8 - Run the project

### In IntelliJ
1. Open `Main.java`.
2. Click the green triangle next to `public static void main` > **Run 'Main.main()'**.

To run a specific mode: **Run > Edit Configurations > Main > Program arguments** and type `xpath`
(or `validate`, `rules`, `suite`, `interactive`).

### With Maven
```bash
mvn compile exec:java                          # everything
mvn compile exec:java -Dexec.args="xpath"      # only the XPath feature
mvn package && java -jar target/restaurant-delivery-xml.jar
```

### Without Maven
```bash
./scripts/run-without-maven.sh          # Linux / macOS / Git Bash
scripts\run-without-maven.bat           # Windows
```

**Checkpoint (Step 6 of the TP):**

* `[OK] ... VALID`
* `Q07 ... Pizza Margherita, Tiramisu, California Roll (8 pcs), Spicy Chicken Burger, Green Detox Smoothie`
* `67 checks executed. [OK] no business rule violation.`
* `Result: 10 / 10 invalid documents were correctly rejected.`
* `FINAL RESULT : ALL CHECKS PASSED`
* the folder `output/` contains 3 report files.

---

## Step 9 - Experiments (to really understand)

Do these on a **copy** of the XML and run `validate --xml your-copy.xml`:

| Experiment | What you will learn |
|---|---|
| Change a price to `abc` | type checking (`cvc-datatype-valid`) |
| Swap two elements | `xs:sequence` is order-sensitive |
| Delete an `id` attribute | `use="required"` |
| Change `dishRef="D001"` to `D777` | `xs:keyref` = foreign key check |
| Change a payment amount (keep it a valid number) | the XSD still says VALID, but `rules` finds the error: **the XSD checks shape, not logic** |
| In interactive mode type `//dish[price < 6]/name` | writing your own XPath |

Interactive console:
```bash
java -jar target/restaurant-delivery-xml.jar interactive
xpath> //dish[price < 6]/name
xpath> count(//order[@status='delivered'])
xpath> :quit
```

---

## Step 10 - Write the modelling document

The TP asks for "a document explaining your modelling (structure, choices, examples of XPath queries)".
It is ready in [`03-MODELING-DOCUMENT.md`](03-MODELING-DOCUMENT.md). Read it, understand it, and
adapt the wording to your own style.

---

## Step 11 - Publish on GitHub

### 11.1 Create the repository
1. Go to github.com > **New repository**.
2. Name: `restaurant-delivery-xml`. Keep it **Public** (or give your teachers access if Private).
3. Do **not** tick "Add a README" (we already have one). Click **Create repository**.

### 11.2 Push the code
In a terminal inside the project folder:

```bash
git init
git add .
git commit -m "TP1 SOA: XML/XSD/XPath restaurant delivery"
git branch -M main
git remote add origin https://github.com/<your-username>/restaurant-delivery-xml.git
git push -u origin main
```

(`.gitignore` already excludes `target/` and IDE files.)

If Git asks for a password, use a **Personal Access Token** (GitHub > Settings > Developer settings)
or sign in through IntelliJ: **Git > GitHub > Share Project on GitHub**.

### 11.3 Verify
Open the repository page in your browser and check that these are visible:
`pom.xml`, `src/main/resources/xml/*.xml` + `.xsd`, `src/main/java/...Main.java`, `docs/03-MODELING-DOCUMENT.md`, `README.md`.

### 11.4 Submit
Copy the URL `https://github.com/<your-username>/restaurant-delivery-xml` and paste it into the
submission area of the platform.

---

## Troubleshooting

| Symptom | Cause | Solution |
|---|---|---|
| `FileNotFoundException: Not found (neither on disk nor on the classpath)` | resources not copied | in IntelliJ: **Build > Rebuild Project**; with Maven run `mvn compile` first |
| `cvc-elt.1.a: Cannot find the declaration of element 'foodDelivery'` | wrong XSD loaded or wrong root name | check `--xsd` and the root element |
| `'mvn' is not recognized` | Maven not on PATH | use IntelliJ's Maven tool window, or `scripts\run-without-maven.bat` |
| `source option 5 is no longer supported` | wrong compiler level | keep `1.8` (or higher) in `pom.xml` properties |
| `XPathExpressionException: Could not find function: distinct-values` | XPath 2.0 function | JDK = XPath 1.0; see the XPath 1.0 tricks in the catalog (Q04, Q18, Q19) |
| XPath returns no result | wrong case (`Dish` vs `dish`) or a namespace | XPath is case-sensitive; our document has no namespace |
| Accents are garbled on Windows console | console code page | run `chcp 65001` first; reports in `output/` are UTF-8 |
| `Premature end of file` | empty file | the XML file was not saved / is empty |
