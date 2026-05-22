package com.calcapp.config;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Loads configuration from a .env file (never committed to Git).
 * Falls back to environment variables for CI/production.
 */
public final class AppConfig {

    private static final Map<String, String> CONFIG = new HashMap<>();

    static {
        // Try to load .env from project root
        Path envFile = Paths.get(".env");
        if (Files.exists(envFile)) {
            try (BufferedReader reader = Files.newBufferedReader(envFile)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eq = line.indexOf('=');
                    if (eq > 0) {
                        String key = line.substring(0, eq).trim();
                        String val = line.substring(eq + 1).trim()
                                        .replaceAll("^\"|\"$", ""); // strip quotes
                        CONFIG.put(key, val);
                    }
                }
            } catch (IOException e) {
                System.err.println("[AppConfig] Could not read .env: " + e.getMessage());
            }
        } else {
            System.out.println("[AppConfig] No .env file found, using environment variables.");
        }
    }

    private static String get(String key, String fallback) {
        // Priority: .env file > system environment variable > fallback
        String val = CONFIG.getOrDefault(key, System.getenv(key));
        return (val != null && !val.isBlank()) ? val : fallback;
    }

    // ── Supabase ──────────────────────────────────────────────────────────
    public static String getSupabaseUrl() {
        return get("SUPABASE_URL", "https://wzammohhuzquhqtfrlox.supabase.co/rest/v1");
    }

    public static String getSupabaseKey() {
        return get("SUPABASE_KEY",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind6YW1tb2hodXpxdWhxdGZybG94Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzk0NjcwMjYsImV4cCI6MjA5NTA0MzAyNn0." +
            "Egew-lh0KUn0_TzdMAr4jGGIBT7NCk-E1GEDst5iiOM");
    }

    // ── App ───────────────────────────────────────────────────────────────
    public static String getAppEnv() {
        return get("APP_ENV", "development");
    }

    public static String getAppName() { return "CalcApp"; }

    private AppConfig() {}
}
