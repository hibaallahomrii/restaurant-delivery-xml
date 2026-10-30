#!/usr/bin/env bash
# OPTIONAL: independent cross-check of the XSD with libxml2's xmllint
# (Linux/macOS: "sudo apt install libxml2-utils" or "brew install libxml2").
# The Java program does the same job - this script is only a second opinion.
cd "$(dirname "$0")/../src/main/resources/xml"

echo "== valid document =="
xmllint --noout --schema restaurant-delivery.xsd restaurant-delivery.xml

for f in invalid/*.xml; do
  echo; echo "== $f (must FAIL) =="
  xmllint --noout --schema restaurant-delivery.xsd "$f" 2>&1 | head -3
done
