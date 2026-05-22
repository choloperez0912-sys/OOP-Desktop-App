package com.calcapp.service;

import com.calcapp.config.AppConfig;
import com.calcapp.config.Json;
import com.calcapp.model.CalcHistory;
import com.calcapp.model.User;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * All Supabase PostgREST calls.
 *
 * Required Supabase tables (run this SQL in Supabase SQL Editor):
 *
 *   create table if not exists users (
 *     id            serial primary key,
 *     username      text unique not null,
 *     password_hash text not null,
 *     role          text not null default 'user',
 *     created_at    timestamptz default now()
 *   );
 *
 *   create table if not exists calc_history (
 *     id         serial primary key,
 *     user_id    int references users(id) on delete cascade,
 *     expression text not null,
 *     result     text not null,
 *     created_at timestamptz default now()
 *   );
 *
 *   -- Allow anon to read/write (for demo). In production use RLS properly.
 *   alter table users       enable row level security;
 *   alter table calc_history enable row level security;
 *   create policy "anon_all" on users       for all using (true) with check (true);
 *   create policy "anon_all" on calc_history for all using (true) with check (true);
 */
public class SupabaseService {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    // ── Login ─────────────────────────────────────────────────────────────

    /** Returns User on success, null on failure. */
    public User login(String username, String rawPassword) {
        try {
            String hash = sha256(rawPassword);
            String url  = AppConfig.getSupabaseUrl()
                    + "/users?username=eq." + encode(username)
                    + "&password_hash=eq." + encode(hash)
                    + "&select=id,username,role&limit=1";

            HttpResponse<String> res = HTTP.send(get(url), BodyHandlers.ofString());
            if (res.statusCode() != 200 || Json.isEmpty(res.body())) return null;

            List<String> objs = Json.parseArray(res.body());
            if (objs.isEmpty()) return null;
            String obj = objs.get(0);

            return new User(
                Integer.parseInt(Json.getString(obj, "id")),
                Json.getString(obj, "username"),
                Json.getString(obj, "role")
            );
        } catch (Exception e) {
            System.err.println("[login] " + e.getMessage());
            return null;
        }
    }

    // ── Register ──────────────────────────────────────────────────────────

    /** Returns true if registration succeeded. */
    public boolean register(String username, String rawPassword) {
        try {
            String body = Json.object(
                "username",      username,
                "password_hash", sha256(rawPassword),
                "role",          "user"
            );
            HttpRequest req = baseBuilder(AppConfig.getSupabaseUrl() + "/users")
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .POST(BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> res = HTTP.send(req, BodyHandlers.ofString());
            return res.statusCode() == 201;
        } catch (Exception e) {
            System.err.println("[register] " + e.getMessage());
            return false;
        }
    }

    // ── Calc History ──────────────────────────────────────────────────────

    public boolean saveHistory(int userId, String expression, String result) {
        try {
            String body = Json.object(
                "user_id",    userId,
                "expression", expression,
                "result",     result
            );
            HttpRequest req = baseBuilder(AppConfig.getSupabaseUrl() + "/calc_history")
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .POST(BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> res = HTTP.send(req, BodyHandlers.ofString());
            return res.statusCode() == 201;
        } catch (Exception e) {
            System.err.println("[saveHistory] " + e.getMessage());
            return false;
        }
    }

    public List<CalcHistory> getHistory(int userId) {
        List<CalcHistory> list = new ArrayList<>();
        try {
            String url = AppConfig.getSupabaseUrl()
                    + "/calc_history?user_id=eq." + userId
                    + "&select=id,user_id,expression,result,created_at"
                    + "&order=id.desc&limit=100";
            HttpResponse<String> res = HTTP.send(get(url), BodyHandlers.ofString());
            if (Json.isEmpty(res.body())) return list;

            for (String obj : Json.parseArray(res.body())) {
                try {
                    list.add(new CalcHistory(
                        safeInt(Json.getString(obj, "id")),
                        safeInt(Json.getString(obj, "user_id")),
                        Json.getString(obj, "expression"),
                        Json.getString(obj, "result"),
                        Json.getString(obj, "created_at")
                    ));
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[getHistory] " + e.getMessage());
        }
        return list;
    }

    public boolean deleteHistory(int historyId) {
        try {
            HttpRequest req = baseBuilder(AppConfig.getSupabaseUrl()
                    + "/calc_history?id=eq." + historyId)
                    .DELETE()
                    .build();
            HttpResponse<String> res = HTTP.send(req, BodyHandlers.ofString());
            return res.statusCode() == 204 || res.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("[deleteHistory] " + e.getMessage());
            return false;
        }
    }

    public boolean clearHistory(int userId) {
        try {
            HttpRequest req = baseBuilder(AppConfig.getSupabaseUrl()
                    + "/calc_history?user_id=eq." + userId)
                    .DELETE()
                    .build();
            HttpResponse<String> res = HTTP.send(req, BodyHandlers.ofString());
            return res.statusCode() == 204 || res.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("[clearHistory] " + e.getMessage());
            return false;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private HttpRequest get(String url) throws Exception {
        return baseBuilder(url).GET().build();
    }

    private HttpRequest.Builder baseBuilder(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey",        AppConfig.getSupabaseKey())
                .header("Authorization", "Bearer " + AppConfig.getSupabaseKey())
                .header("Accept",        "application/json");
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private int safeInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) hex.append(String.format("%02x", b));
        return hex.toString();
    }
}
