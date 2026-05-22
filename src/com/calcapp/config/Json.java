package com.calcapp.config;

import java.util.*;

/**
 * Zero-dependency JSON builder and parser.
 * Handles the simple payloads used by Supabase REST API.
 */
public final class Json {

    /** Build a JSON object from alternating key, value pairs. */
    public static String object(Object... kvPairs) {
        if (kvPairs.length % 2 != 0)
            throw new IllegalArgumentException("Provide key/value pairs");
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < kvPairs.length; i += 2) {
            if (i > 0) sb.append(",");
            sb.append(quote(kvPairs[i].toString())).append(":");
            Object v = kvPairs[i + 1];
            if (v instanceof Number || v instanceof Boolean) sb.append(v);
            else sb.append(quote(v.toString()));
        }
        return sb.append("}").toString();
    }

    /** Wrap string in JSON quotes with escaping. */
    public static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r") + "\"";
    }

    /** Get a string value by key from a JSON object string. */
    public static String getString(String json, String key) {
        if (json == null) return null;
        String search = "\"" + key + "\"";
        int ki = json.indexOf(search);
        if (ki < 0) return null;
        int colon = json.indexOf(':', ki + search.length());
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (start >= json.length()) return null;
        if (json.charAt(start) == '"') {
            StringBuilder sb = new StringBuilder();
            int i = start + 1;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) { sb.append(json.charAt(i + 1)); i += 2; }
                else if (c == '"') break;
                else { sb.append(c); i++; }
            }
            return sb.toString();
        }
        int end = start;
        while (end < json.length() && ",}]".indexOf(json.charAt(end)) < 0) end++;
        return json.substring(start, end).trim();
    }

    /** Parse all JSON objects from an array string. */
    public static List<String> parseArray(String jsonArray) {
        List<String> result = new ArrayList<>();
        if (jsonArray == null || jsonArray.isBlank()) return result;
        int i = 0;
        while (i < jsonArray.length()) {
            int start = jsonArray.indexOf('{', i);
            if (start < 0) break;
            int depth = 0, end = start;
            for (; end < jsonArray.length(); end++) {
                char c = jsonArray.charAt(end);
                if (c == '{') depth++;
                else if (c == '}') { depth--; if (depth == 0) break; }
            }
            result.add(jsonArray.substring(start, end + 1));
            i = end + 1;
        }
        return result;
    }

    /** True if JSON is null, empty, or an empty array. */
    public static boolean isEmpty(String json) {
        return json == null || json.isBlank() || json.equals("[]") || json.equals("null");
    }

    private Json() {}
}
