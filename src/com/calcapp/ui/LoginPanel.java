package com.calcapp.ui;

import com.calcapp.model.User;
import com.calcapp.service.SessionManager;
import com.calcapp.service.SupabaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JPanel {

    private final SupabaseService db;
    private final Runnable        onLoginSuccess;

    // Login fields
    private JTextField     loginUserField;
    private JPasswordField loginPassField;
    private JLabel         loginStatus;

    // Register fields
    private JTextField     regUserField;
    private JPasswordField regPassField;
    private JPasswordField regPass2Field;
    private JLabel         regStatus;

    private JTabbedPane tabs;

    public LoginPanel(SupabaseService db, Runnable onLoginSuccess) {
        this.db             = db;
        this.onLoginSuccess = onLoginSuccess;
        setBackground(Theme.BG_DARK);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(340, 420));
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Title
        JLabel title = new JLabel("CalcApp", SwingConstants.CENTER);
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT);
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        card.add(title, BorderLayout.NORTH);

        // Tabs
        tabs = new JTabbedPane();
        tabs.setBackground(Theme.BG_CARD);
        tabs.setForeground(Theme.TEXT_PRIMARY);
        tabs.setFont(Theme.FONT_LABEL);
        tabs.addTab("Login",    buildLoginTab());
        tabs.addTab("Register", buildRegisterTab());

        // Style tabs
        UIManager.put("TabbedPane.selected",    Theme.BG_INPUT);
        UIManager.put("TabbedPane.background",  Theme.BG_CARD);
        UIManager.put("TabbedPane.foreground",  Theme.TEXT_PRIMARY);

        card.add(tabs, BorderLayout.CENTER);
        add(card);
    }

    // ── Login Tab ─────────────────────────────────────────────────────────

    private JPanel buildLoginTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.BG_CARD);
        p.setBorder(new EmptyBorder(20, 8, 8, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 2, 5, 2);
        c.gridx = 0; c.weightx = 1;

        c.gridy = 0; p.add(label("Username"), c);
        c.gridy = 1;
        loginUserField = field();
        p.add(loginUserField, c);

        c.gridy = 2; p.add(label("Password"), c);
        c.gridy = 3;
        loginPassField = passField();
        p.add(loginPassField, c);

        c.gridy = 4; c.insets = new Insets(14, 2, 5, 2);
        StyledButton btn = new StyledButton("Login", Theme.ACCENT);
        btn.setPreferredSize(new Dimension(260, 42));
        btn.addActionListener(e -> doLogin());
        loginUserField.addActionListener(e -> doLogin());
        loginPassField.addActionListener(e -> doLogin());
        p.add(btn, c);

        c.gridy = 5; c.insets = new Insets(4, 2, 2, 2);
        loginStatus = statusLabel();
        p.add(loginStatus, c);

        return p;
    }

    private void doLogin() {
        String user = loginUserField.getText().trim();
        String pass = new String(loginPassField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            loginStatus.setForeground(Theme.ERROR);
            loginStatus.setText("Please enter username and password.");
            return;
        }
        loginStatus.setForeground(Theme.TEXT_MUTED);
        loginStatus.setText("Connecting…");

        new SwingWorker<User, Void>() {
            @Override protected User doInBackground() { return db.login(user, pass); }
            @Override protected void done() {
                try {
                    User u = get();
                    if (u != null) {
                        SessionManager.getInstance().setUser(u);
                        loginStatus.setForeground(Theme.SUCCESS);
                        loginStatus.setText("Welcome, " + u.getUsername() + "!");
                        Timer t = new Timer(600, ev -> onLoginSuccess.run());
                        t.setRepeats(false); t.start();
                    } else {
                        loginStatus.setForeground(Theme.ERROR);
                        loginStatus.setText("Invalid username or password.");
                        loginPassField.setText("");
                    }
                } catch (Exception ex) {
                    loginStatus.setForeground(Theme.ERROR);
                    loginStatus.setText("Connection error. Check internet.");
                }
            }
        }.execute();
    }

    // ── Register Tab ──────────────────────────────────────────────────────

    private JPanel buildRegisterTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.BG_CARD);
        p.setBorder(new EmptyBorder(20, 8, 8, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 2, 4, 2);
        c.gridx = 0; c.weightx = 1;

        c.gridy = 0; p.add(label("Username"), c);
        c.gridy = 1; regUserField = field(); p.add(regUserField, c);
        c.gridy = 2; p.add(label("Password"), c);
        c.gridy = 3; regPassField = passField(); p.add(regPassField, c);
        c.gridy = 4; p.add(label("Confirm Password"), c);
        c.gridy = 5; regPass2Field = passField(); p.add(regPass2Field, c);

        c.gridy = 6; c.insets = new Insets(14, 2, 5, 2);
        StyledButton btn = new StyledButton("Create Account", Theme.SUCCESS);
        btn.setFont(Theme.FONT_BTN_SM);
        btn.setPreferredSize(new Dimension(260, 42));
        btn.addActionListener(e -> doRegister());
        p.add(btn, c);

        c.gridy = 7; c.insets = new Insets(4, 2, 2, 2);
        regStatus = statusLabel();
        p.add(regStatus, c);

        return p;
    }

    private void doRegister() {
        String user  = regUserField.getText().trim();
        String pass  = new String(regPassField.getPassword());
        String pass2 = new String(regPass2Field.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            regStatus.setForeground(Theme.ERROR);
            regStatus.setText("All fields are required.");
            return;
        }
        if (!pass.equals(pass2)) {
            regStatus.setForeground(Theme.ERROR);
            regStatus.setText("Passwords do not match.");
            return;
        }
        if (pass.length() < 6) {
            regStatus.setForeground(Theme.ERROR);
            regStatus.setText("Password must be at least 6 characters.");
            return;
        }
        regStatus.setForeground(Theme.TEXT_MUTED);
        regStatus.setText("Creating account…");

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return db.register(user, pass); }
            @Override protected void done() {
                try {
                    if (get()) {
                        regStatus.setForeground(Theme.SUCCESS);
                        regStatus.setText("Account created! You can now log in.");
                        regUserField.setText(""); regPassField.setText(""); regPass2Field.setText("");
                        Timer t = new Timer(1200, ev -> tabs.setSelectedIndex(0));
                        t.setRepeats(false); t.start();
                    } else {
                        regStatus.setForeground(Theme.ERROR);
                        regStatus.setText("Username taken or server error.");
                    }
                } catch (Exception ex) {
                    regStatus.setForeground(Theme.ERROR);
                    regStatus.setText("Connection error.");
                }
            }
        }.execute();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.TEXT_MUTED);
        l.setFont(Theme.FONT_LABEL);
        return l;
    }

    private JTextField field() {
        JTextField f = new JTextField();
        f.setBackground(Theme.BG_INPUT);
        f.setForeground(Theme.TEXT_PRIMARY);
        f.setCaretColor(Theme.TEXT_PRIMARY);
        f.setFont(Theme.FONT_FIELD);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        f.setPreferredSize(new Dimension(260, 38));
        return f;
    }

    private JPasswordField passField() {
        JPasswordField f = new JPasswordField();
        f.setBackground(Theme.BG_INPUT);
        f.setForeground(Theme.TEXT_PRIMARY);
        f.setCaretColor(Theme.TEXT_PRIMARY);
        f.setFont(Theme.FONT_FIELD);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        f.setPreferredSize(new Dimension(260, 38));
        return f;
    }

    private JLabel statusLabel() {
        JLabel l = new JLabel(" ", SwingConstants.CENTER);
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(Theme.TEXT_MUTED);
        return l;
    }
}
