package com.fss.soa.tp1;

/** One named XPath query of the catalog: what it asks, the expression, and the expected result type. */
public class QueryDefinition {

    /** The four result types of XPath 1.0. */
    public enum Type { NODES, STRING, NUMBER, BOOLEAN }

    private final String id;
    private final String section;
    private final String title;
    private final String expression;
    private final Type type;

    public QueryDefinition(String id, String section, String title, String expression, Type type) {
        this.id = id;
        this.section = section;
        this.title = title;
        this.expression = expression;
        this.type = type;
    }

    public String getId() { return id; }
    public String getSection() { return section; }
    public String getTitle() { return title; }
    public String getExpression() { return expression; }
    public Type getType() { return type; }
}
