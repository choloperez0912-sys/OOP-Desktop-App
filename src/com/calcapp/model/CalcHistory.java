package com.calcapp.model;

public class CalcHistory {
    private final int    id;
    private final int    userId;
    private final String expression;
    private final String result;
    private final String createdAt;

    public CalcHistory(int id, int userId, String expression, String result, String createdAt) {
        this.id         = id;
        this.userId     = userId;
        this.expression = expression;
        this.result     = result;
        this.createdAt  = createdAt != null ? createdAt : "";
    }

    public int    getId()         { return id; }
    public int    getUserId()     { return userId; }
    public String getExpression() { return expression; }
    public String getResult()     { return result; }
    public String getCreatedAt()  { return createdAt; }

    @Override
    public String toString() {
        return expression + " = " + result;
    }
}
