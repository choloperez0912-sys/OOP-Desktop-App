package com.calcapp.ui;

import com.calcapp.service.CalculatorEngine;
import com.calcapp.service.SessionManager;
import com.calcapp.service.SupabaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CalculatorPanel extends JPanel {

    private final SupabaseService   db;
    private final CalculatorEngine  engine = new CalculatorEngine();
    private final Runnable          onHistory;
    private final Runnable          onLogout;

    private JLabel  exprLabel;    // shows what's being typed
    private JLabel  resultLabel;  // shows the current result

    private String  expression = "";
    private boolean justEvaled = false; // if true, next digit starts fresh

    public CalculatorPanel(SupabaseService db, Runnable onHistory, Runnable onLogout) {
        this.db        = db;
        this.onHistory = onHistory;
        this.onLogout  = onLogout;
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
        setupKeyboard();
    }

    private void buildUI() {
        // ── Top bar ───────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.BG_CARD);
        topBar.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel appName = new JLabel("CalcApp");
        appName.setFont(Theme.FONT_LABEL);
        appName.setForeground(Theme.ACCENT);

        String username = SessionManager.getInstance().getUser().getUsername();
        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBar.setBackground(Theme.BG_CARD);

        StyledButton histBtn = new StyledButton("History", Theme.BTN_OP);
        histBtn.setFont(Theme.FONT_BTN_SM);
        histBtn.setPreferredSize(new Dimension(80, 30));
        histBtn.addActionListener(e -> onHistory.run());

        StyledButton logoutBtn = new StyledButton("Logout", Theme.BTN_CLEAR);
        logoutBtn.setFont(Theme.FONT_BTN_SM);
        logoutBtn.setPreferredSize(new Dimension(70, 30));
        logoutBtn.addActionListener(e -> onLogout.run());

        JLabel userLabel = new JLabel(username);
        userLabel.setForeground(Theme.TEXT_MUTED);
        userLabel.setFont(Theme.FONT_LABEL);

        rightBar.add(userLabel);
        rightBar.add(histBtn);
        rightBar.add(logoutBtn);

        topBar.add(appName,  BorderLayout.WEST);
        topBar.add(rightBar, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Display ───────────────────────────────────────────────────────
        JPanel display = new JPanel(new GridLayout(2, 1));
        display.setBackground(Theme.BG_INPUT);
        display.setBorder(new EmptyBorder(16, 20, 16, 20));
        display.setPreferredSize(new Dimension(400, 110));

        exprLabel = new JLabel("", SwingConstants.RIGHT);
        exprLabel.setFont(Theme.FONT_EXPR);
        exprLabel.setForeground(Theme.TEXT_MUTED);

        resultLabel = new JLabel("0", SwingConstants.RIGHT);
        resultLabel.setFont(Theme.FONT_DISPLAY);
        resultLabel.setForeground(Theme.TEXT_PRIMARY);

        display.add(exprLabel);
        display.add(resultLabel);
        add(display, BorderLayout.CENTER);

        // ── Button Grid ───────────────────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(5, 4, 8, 8));
        grid.setBackground(Theme.BG_DARK);
        grid.setBorder(new EmptyBorder(14, 14, 14, 14));

        String[][] keys = {
            {"C",   "(",   ")",   "%"},
            {"7",   "8",   "9",   "/"},
            {"4",   "5",   "6",   "*"},
            {"1",   "2",   "3",   "-"},
            {"0",   ".",   "⌫",  "+"},
        };

        for (String[] row : keys) {
            for (String k : row) {
                StyledButton btn = makeButton(k);
                grid.add(btn);
            }
        }

        // ── Equals button (full width) ────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setBackground(Theme.BG_DARK);
        bottom.setBorder(new EmptyBorder(0, 14, 14, 14));

        StyledButton eqBtn = new StyledButton("=", Theme.BTN_EQUALS);
        eqBtn.setPreferredSize(new Dimension(400, 56));
        eqBtn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        eqBtn.addActionListener(e -> evaluate());

        bottom.add(grid,  BorderLayout.CENTER);
        bottom.add(eqBtn, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);
    }

    private StyledButton makeButton(String label) {
        Color bg = switch (label) {
            case "C"  -> Theme.BTN_CLEAR;
            case "/"  -> Theme.BTN_OP;
            case "*"  -> Theme.BTN_OP;
            case "-"  -> Theme.BTN_OP;
            case "+"  -> Theme.BTN_OP;
            case "%"  -> Theme.BTN_OP;
            case "("  -> Theme.BTN_OP;
            case ")"  -> Theme.BTN_OP;
            default   -> Theme.BTN_NUM;
        };
        StyledButton btn = new StyledButton(label, bg);
        btn.setPreferredSize(new Dimension(70, 56));
        btn.addActionListener(e -> handleInput(label));
        return btn;
    }

    // ── Input logic ───────────────────────────────────────────────────────

    private void handleInput(String key) {
        switch (key) {
            case "C"  -> { expression = ""; justEvaled = false; updateDisplay("0"); }
            case "⌫"  -> {
                if (!expression.isEmpty()) {
                    expression = expression.substring(0, expression.length() - 1);
                }
                updateDisplay(expression.isEmpty() ? "0" : expression);
            }
            default -> {
                if (justEvaled && Character.isDigit(key.charAt(0))) {
                    expression = key; justEvaled = false;
                } else {
                    if (justEvaled) justEvaled = false; // continue after operator
                    expression += key;
                }
                String preview = engine.evaluate(expression);
                exprLabel.setText(expression);
                if (!preview.startsWith("Error")) resultLabel.setText(preview);
                else resultLabel.setText(expression.isEmpty() ? "0" : resultLabel.getText());
            }
        }
    }

    private void evaluate() {
        if (expression.isEmpty()) return;
        String result = engine.evaluate(expression);
        exprLabel.setText(expression + " =");
        resultLabel.setText(result);

        // Save to DB asynchronously
        String expr = expression;
        int userId   = SessionManager.getInstance().getUser().getId();
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                db.saveHistory(userId, expr, result);
                return null;
            }
        }.execute();

        if (!result.startsWith("Error")) {
            expression = result;
            justEvaled = true;
        } else {
            expression = "";
        }
    }

    private void updateDisplay(String val) {
        exprLabel.setText(expression.isEmpty() ? "" : expression);
        resultLabel.setText(val);
    }

    // ── Keyboard support ──────────────────────────────────────────────────

    private void setupKeyboard() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if ("0123456789.+-*/%()".indexOf(c) >= 0) handleInput(String.valueOf(c));
                else if (c == '\n' || c == '=') evaluate();
                else if (c == '\b') handleInput("⌫");
                else if (c == 'c' || c == 'C') handleInput("C");
            }
        });
    }
}
