# 05 - The validation files

Folder: [`src/main/resources/xml/invalid/`](../src/main/resources/xml/invalid/)

These 10 documents are copies of the valid file with **exactly one deliberate defect** (the last one has two).
They prove that the XSD is *strict enough*. A schema that accepts everything is useless: a validator must also be
tested with **bad** data.

Run them all:
```bash
java -jar target/restaurant-delivery-xml.jar suite       # or: ./scripts/run-without-maven.sh suite
```
Run one of them on its own:
```bash
java -jar target/restaurant-delivery-xml.jar validate --xml src/main/resources/xml/invalid/invalid-04-broken-reference.xml
```
Expected final line of the suite: `Result: 10 / 10 invalid documents were correctly rejected.`

Each file begins with a comment giving the defect and the expected error.

---

## 1. The three levels of failure

| Level | Meaning | Example file | Java verdict |
|---|---|---|---|
| **Not well-formed** | broken XML syntax: the parser cannot even read it | 08 | `NOT WELL-FORMED (fatal syntax error)` |
| **Invalid** | readable XML but breaks an XSD rule | 01-07, 09, 10 | `INVALID (n schema errors)` |
| **Valid** | respects the XSD | `restaurant-delivery.xml` | `VALID` |

Business-rule problems (wrong totals) are a **fourth** level, invisible to the XSD - see the `rules` mode.

---

## 2. The ten files, the exact errors and the fix

| # | File | Defect | Real error message (shortened) | Fix |
|---|---|---|---|---|
| 01 | `invalid-01-missing-required-element` | `<name>` removed from restaurant R001 | `cvc-complex-type.2.4.a: Invalid content was found starting with element 'cuisine'. One of '{name}' is expected.` | restore `<name>` |
| 02 | `invalid-02-bad-enumeration` | order `status="shipped"` | `cvc-enumeration-valid: Value 'shipped' is not facet-valid with respect to enumeration '[pending, confirmed, ...]'` | use one of the listed statuses |
| 03 | `invalid-03-bad-id-pattern` | customer `id="CUST-4"` | `cvc-pattern-valid: Value 'CUST-4' is not facet-valid with respect to pattern 'C\d{3}'` | `C004` |
| 04 | `invalid-04-broken-reference` | order O1006 has `restaurantRef="R999"` | `cvc-identity-constraint.4.3: Key 'orderRestaurantRef' with value 'R999' not found` | reference an existing restaurant |
| 05 | `invalid-05-duplicate-id` | dish D018 re-uses id `D017` | `cvc-identity-constraint.4.2.2: Duplicate key value [D017] declared for identity constraint "dishKey"` | give each dish a unique id |
| 06 | `invalid-06-negative-price` | price `-11.00` | `cvc-minExclusive-valid: Value '-11.00' is not facet-valid with respect to minExclusive '0.0'` | positive price |
| 07 | `invalid-07-rating-out-of-range` | rating `7.5` | `cvc-maxInclusive-valid: Value '7.5' is not facet-valid with respect to maxInclusive '5.0'` | 0 to 5 |
| 08 | `invalid-08-not-well-formed` | `</nam>` instead of `</name>` | `The element type "name" must be terminated by the matching end-tag "</name>".` | fix the tag |
| 09 | `invalid-09-wrong-element-order` | `<phone>` before `<address>` | `cvc-complex-type.2.4.a: Invalid content was found starting with element 'phone'. One of '{address}' is expected.` | follow the `xs:sequence` order |
| 10 | `invalid-10-bad-email-and-quantity` | bad e-mail **and** quantity `0` | 4 messages: pattern + type for the e-mail, minInclusive + attribute type for `quantity` | fix both (shows that **all** errors are reported in one run) |

### Reading an error message
```
line 449, col 98: cvc-enumeration-valid: Value 'shipped' is not facet-valid ...
 |        |         |                      |
 |        |         |                      +-- human explanation
 |        |         +-- rule code (cvc = "constraint validity check")
 |        +-- column in the file
 +-- line in the file  (open the file at this line!)
```

Frequent codes:

| Code | Meaning |
|---|---|
| `cvc-complex-type.2.4.a` | wrong, unexpected or misplaced child element |
| `cvc-complex-type.2.4.b` | a required child element is missing at the end |
| `cvc-complex-type.4` | a required attribute is missing |
| `cvc-datatype-valid` | value does not match the data type (e.g. text instead of number) |
| `cvc-enumeration-valid` | value not in the allowed list |
| `cvc-pattern-valid` | value does not match the regular expression |
| `cvc-minInclusive-valid`, `cvc-maxInclusive-valid`, `cvc-minExclusive-valid` | number outside the allowed range |
| `cvc-identity-constraint.4.2.2` | duplicate primary key (`xs:key`/`xs:unique`) |
| `cvc-identity-constraint.4.3` | foreign key (`xs:keyref`) points to nothing |
| `cvc-elt.1.a` | root element is not declared in the schema |

### Why several messages for one defect?
When a value is invalid, Xerces reports the facet error (`cvc-pattern-valid`) **and** a second error on the
enclosing type (`cvc-attribute.3`, `cvc-type.3.1.3`...). That is normal: count *defects*, not messages.

---

## 3. Two different tools, same verdict (cross-check)

`scripts/validate-with-xmllint.sh` validates the same files with libxml2 (a completely different implementation).
Both tools accept the valid file and reject the ten invalid ones - a good sign that the schema is
correct and not an accident of one parser. The messages differ: after a structural error (files 01 and 09)
libxml2 also lists many follow-up `keyref` errors, because the broken restaurant could not be indexed, while
Xerces (Java) stops at the structural error and reports it once.

---

## 4. Business-rule violations (valid for the XSD, wrong for the business)

The XSD cannot detect these. Try on a copy of the valid file:

| Change | XSD | `rules` mode |
|---|---|---|
| `<amount currency="EUR">41.50</amount>` -> `40.00` in order O1001 | VALID | `R3 O1001: payment is 40.00 but items (39.00) + fee (2.50) = 41.50` |
| in order O1003 change `dishRef="D011"` to `D013` (price 12.00 on the menu, order says 11.50) | VALID | `R2 O1003: unitPrice 11.50 of D013 differs from the menu price 12.00` |
| in order O1006 replace dish `D002` by `D006` (a sushi on a pizza order) | VALID | `R1 O1006: dish D006 is not on the menu of restaurant R001` |

---

## 5. How to add your own invalid test

1. Copy `restaurant-delivery.xml` to `invalid/invalid-11-my-case.xml`.
2. Introduce ONE defect and add a comment at the top.
3. Change the schema location to `../restaurant-delivery.xsd`.
4. Add a line to `INVALID_SAMPLES` in `Main.java` (and to the list in `XmlValidatorTest`).
5. Run `suite`: your file must be rejected.
