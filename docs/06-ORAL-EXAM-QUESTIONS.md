# 06 - Questions and answers to prepare your defence

**1. What is the difference between well-formed and valid?**
Well-formed = correct XML syntax (closed and nested tags, one root, quoted attributes). Valid = well-formed AND
conforms to a schema. Demonstrated by `invalid-08` (not well-formed) versus `invalid-06` (well-formed but invalid).

**2. Why an XSD rather than a DTD?**
XSD is itself XML, supports real data types (decimal, date, boolean), patterns, ranges, namespaces and
key/keyref constraints. A DTD only has ID/IDREF and almost no typing.

**3. What is the role of `SchemaFactory`, `Schema` and `Validator`?**
`SchemaFactory` compiles the XSD into a `Schema` (immutable and thread-safe). A `Validator` created from the
schema checks one document; it is not thread-safe so we create one per document.

**4. Why do you collect errors with an `ErrorHandler`?**
By default the validator throws at the first error. With a custom `ErrorHandler`, `error()` lets validation
continue, so the user sees **all** problems with line and column in a single run. `fatalError()` (syntax) must stop.

**5. What are `xs:key` and `xs:keyref`?**
`xs:key` = primary key (exists and unique). `xs:keyref` = foreign key (must match an existing key).
We use them for restaurant, dish, customer, driver and order ids, and for `customerRef`, `restaurantRef`,
`driverRef`, `dishRef`. Demonstrated by `invalid-04` and `invalid-05`.

**6. What is the difference between `xs:key` and `xs:unique`?**
Both force uniqueness; `xs:key` also requires the field to be present, and only keys can be referenced by `keyref`.
We use `xs:unique` for category names and day names inside one restaurant.

**7. Attribute or element: how did you choose?**
Attributes for ids, states and flags (atomic, short, easy to filter); elements for descriptive or structured data
that may grow or needs order (address, price, allergens).

**8. Why store `unitPrice` in each item if the dish already has a price?**
It is a snapshot: the menu price may change later, but an order's history must remain correct.

**9. Which dependencies did you add to the `pom.xml` and why?**
None for the application: JAXP (`javax.xml.*`) is in the JDK. Only `junit-jupiter` with scope `test`.
Plugins: compiler (Java 1.8), surefire (tests), jar (executable manifest), exec (`mvn exec:java`).

**10. Which XPath version does Java support? Consequences?**
XPath 1.0. No `distinct-values`, `min`, `max`, `avg`, `lower-case`. We use equivalent XPath 1.0 patterns
(Q04, Q18, Q19, Q30, Q23) - or Saxon-HE if XPath 2.0+ were required.

**11. Explain `//dish[not(price > //dish/price)]/name`.**
`price > //dish/price` is true when this dish is more expensive than at least one dish (general comparison on a
node-set is existential). `not(...)` keeps the dish that is not dearer than any other: the cheapest.

**12. Explain a "join" in XPath.**
`//restaurant[@id = //order[@id='O1001']/@restaurantRef]/name` compares an attribute with a set of attribute values:
it navigates from an order to its restaurant through the id reference - the XML equivalent of a SQL JOIN.

**13. What can the XSD NOT check?**
Rules involving several places of the document: payment = items + fee, a dish belongs to the restaurant of the
order, delivered orders have a driver... XSD 1.0 has no cross-field expressions (XSD 1.1 has `xs:assert`,
unsupported by the JDK). We check them in `BusinessRulesChecker` with XPath.

**14. DOM versus SAX versus StAX?**
DOM loads the whole tree in memory (easy random access, needed for XPath); SAX is an event stream (low memory,
read-only, one pass); StAX is a pull parser (you control the reading). For a small file like ours DOM is fine.

**15. How do you protect against XXE (XML External Entity attacks)?**
`FEATURE_SECURE_PROCESSING`, `ACCESS_EXTERNAL_DTD=""` on the validator, and `disallow-doctype-decl` on the
DOM parser.

**16. Why did you not use a namespace?**
To keep XPath simple for the TP. A production message would declare a `targetNamespace`, and the XPath code
would need a `NamespaceContext`.

**17. How do you prove that your validator really works?**
With the 10 invalid files (10/10 rejected), the valid file (accepted), the JUnit tests, and a cross-check with `xmllint`.

**18. How would you evolve this project towards a web service?**
Expose `validate` and the queries as a REST (or SOAP) endpoint; the XSD becomes the contract of the messages,
and the XML orders become the payload exchanged between restaurant, order and delivery services.
