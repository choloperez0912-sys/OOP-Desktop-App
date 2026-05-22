package com.calcapp.ui;

import com.calcapp.config.AppConfig;
import com.calcapp.service.SessionManager;
import com.calcapp.service.SupabaseService;

import javax.swing.*;
import java.awt.*;

/**
 * Main JFrame — hosts a CardLayout to switch between:
 *   LOGIN  → CALCULATOR  ↔  HISTORY
 */
public class MainWindow extends JFrame {

    private static final String VIEW_LOGIN      = "LOGIN";
    private static final String VIEW_CALCULATOR = "CALCULATOR";
    private static final String VIEW_HISTORY    = "HISTORY";

    private final CardLayout      cardLayout = new CardLayout();
    private final JPanel          cardPanel  = new JPanel(cardLayout);
    private final SupabaseService db         = new SupabaseService();

    private CalculatorPanel calcPanel;
    private HistoryPanel    histPanel;

    public MainWindow() {
        super(AppConfig.getAppName() + " [" + AppConfig.getAppEnv() + "]");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 600);
        setMinimumSize(new Dimension(380, 560));
        setLocationRelativeTo(null);
        setResizable(false);

        // ── Dark title bar (Windows 11 hint) ─────────────────────────────
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        setBackground(Theme.BG_DARK);
        getContentPane().setBackground(Theme.BG_DARK);

        // ── Build views ───────────────────────────────────────────────────
        LoginPanel loginPanel = new LoginPanel(db, this::showCalculator);
        cardPanel.add(loginPanel, VIEW_LOGIN);
        cardPanel.setBackground(Theme.BG_DARK);

        add(cardPanel, BorderLayout.CENTER);
        cardLayout.show(cardPanel, VIEW_LOGIN);
    }

    // ── Navigation ────────────────────────────────────────────────────────

    private void showCalculator() {
        if (calcPanel == null) {
            calcPanel = new CalculatorPanel(db, this::showHistory, this::logout);
            histPanel = new HistoryPanel(db, this::showCalculator);
            cardPanel.add(calcPanel, VIEW_CALCULATOR);
            cardPanel.add(histPanel, VIEW_HISTORY);
        }
        cardLayout.show(cardPanel, VIEW_CALCULATOR);
        calcPanel.requestFocusInWindow();
    }

    private void showHistory() {
        histPanel.loadHistory();
        cardLayout.show(cardPanel, VIEW_HISTORY);
    }

    private void logout() {
        SessionManager.getInstance().logout();
        // Reset — recreate calc/history panels next login
        if (calcPanel != null) {
            cardPanel.remove(calcPanel);
            cardPanel.remove(histPanel);
            calcPanel = null;
            histPanel = null;
        }
        cardLayout.show(cardPanel, VIEW_LOGIN);
    }
}
