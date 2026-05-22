package com.calcapp.service;

/**
 * Simple recursive-descent expression evaluator.
 * Supports: + - * / % parentheses, unary minus, and decimal numbers.
 * No external dependencies, no use of eval/scripting engines.
 */
public class CalculatorEngine {

    private String expr;
    private int    pos;

    public String evaluate(String expression) {
        try {
            expr = expression.replaceAll("\\s+", "");
            pos  = 0;
            double result = parseExpr();
            if (pos != expr.length()) throw new RuntimeException("Unexpected: " + expr.charAt(pos));
            // Return clean number
            if (result == (long) result) return String.valueOf((long) result);
            return String.valueOf(result);
        } catch (ArithmeticException ae) {
            return "Error: Division by zero";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // expr = term (('+' | '-') term)*
    private double parseExpr() {
        double val = parseTerm();
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if      (c == '+') { pos++; val += parseTerm(); }
            else if (c == '-') { pos++; val -= parseTerm(); }
            else break;
        }
        return val;
    }

    // term = factor (('*' | '/' | '%') factor)*
    private double parseTerm() {
        double val = parseFactor();
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if      (c == '*') { pos++; val *= parseFactor(); }
            else if (c == '/') { pos++; double d = parseFactor(); if (d == 0) throw new ArithmeticException("div0"); val /= d; }
            else if (c == '%') { pos++; double d = parseFactor(); if (d == 0) throw new ArithmeticException("div0"); val %= d; }
            else break;
        }
        return val;
    }

    // factor = number | '(' expr ')' | '-' factor
    private double parseFactor() {
        if (pos >= expr.length()) throw new RuntimeException("Unexpected end");
        char c = expr.charAt(pos);
        if (c == '(') {
            pos++; // skip '('
            double val = parseExpr();
            if (pos >= expr.length() || expr.charAt(pos) != ')')
                throw new RuntimeException("Missing ')'");
            pos++; // skip ')'
            return val;
        }
        if (c == '-') { pos++; return -parseFactor(); }
        if (c == '+') { pos++; return parseFactor(); }
        // number
        int start = pos;
        while (pos < expr.length() && (Character.isDigit(expr.charAt(pos)) || expr.charAt(pos) == '.')) pos++;
        if (pos == start) throw new RuntimeException("Expected number at pos " + pos);
        return Double.parseDouble(expr.substring(start, pos));
    }
}
