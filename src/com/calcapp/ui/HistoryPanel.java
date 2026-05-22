package com.calcapp.ui;

import com.calcapp.model.CalcHistory;
import com.calcapp.service.SessionManager;
import com.calcapp.service.SupabaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class HistoryPanel extends JPanel {

    private final SupabaseService db;
    private final Runnable        onBack;

    private JTable          table;
    private DefaultTableModel tableModel;
    private JLabel          statusLabel;

    private static final String[] COLUMNS = {"#", "Expression", "Result", "Date"};

    public HistoryPanel(SupabaseService db, Runnable onBack) {
        this.db     = db;
        this.onBack = onBack;
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        // ── Top bar ───────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.BG_CARD);
        topBar.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel title = new JLabel("Calculation History");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(Theme.BG_CARD);

        StyledButton refreshBtn = new StyledButton("Refresh", Theme.BTN_OP);
        refreshBtn.setFont(Theme.FONT_BTN_SM);
        refreshBtn.setPreferredSize(new Dimension(80, 30));
        refreshBtn.addActionListener(e -> loadHistory());

        StyledButton clearBtn = new StyledButton("Clear All", Theme.BTN_CLEAR);
        clearBtn.setFont(Theme.FONT_BTN_SM);
        clearBtn.setPreferredSize(new Dimension(80, 30));
        clearBtn.addActionListener(e -> clearHistory());

        StyledButton backBtn = new StyledButton("← Back", Theme.ACCENT);
        backBtn.setFont(Theme.FONT_BTN_SM);
        backBtn.setPreferredSize(new Dimension(80, 30));
        backBtn.addActionListener(e -> onBack.run());

        btnPanel.add(refreshBtn);
        btnPanel.add(clearBtn);
        btnPanel.add(backBtn);

        topBar.add(title,   BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setBackground(Theme.BG_CARD);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_LABEL);
        table.setGridColor(Theme.BORDER);
        table.setRowHeight(34);
        table.setSelectionBackground(Theme.ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setBorder(null);
        table.setFillsViewportHeight(true);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.BG_INPUT);
        header.setForeground(Theme.TEXT_MUTED);
        header.setFont(Theme.FONT_LABEL);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        header.setReorderingAllowed(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);

        // Center-align #
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (sel) {
                    setBackground(Theme.ACCENT);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? Theme.BG_CARD : Theme.BG_INPUT);
                    setForeground(col == 2 ? Theme.SUCCESS : Theme.TEXT_PRIMARY);
                }
                setFont(Theme.FONT_LABEL);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (col == 0) setHorizontalAlignment(SwingConstants.CENTER);
                else setHorizontalAlignment(SwingConstants.LEFT);
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Theme.BG_DARK);
        scroll.setBorder(new EmptyBorder(10, 14, 0, 14));
        scroll.getViewport().setBackground(Theme.BG_CARD);
        add(scroll, BorderLayout.CENTER);

        // ── Status bar ────────────────────────────────────────────────────
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(Theme.BG_DARK);
        statusBar.setBorder(new EmptyBorder(6, 14, 10, 14));
        statusLabel = new JLabel("Loading…");
        statusLabel.setFont(Theme.FONT_LABEL);
        statusLabel.setForeground(Theme.TEXT_MUTED);
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);
    }

    /** Called every time this panel becomes visible. */
    public void loadHistory() {
        statusLabel.setForeground(Theme.TEXT_MUTED);
        statusLabel.setText("Loading…");
        int userId = SessionManager.getInstance().getUser().getId();

        new SwingWorker<List<CalcHistory>, Void>() {
            @Override protected List<CalcHistory> doInBackground() {
                return db.getHistory(userId);
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    List<CalcHistory> history = get();
                    for (int i = 0; i < history.size(); i++) {
                        CalcHistory h = history.get(i);
                        String date = h.getCreatedAt();
                        if (date != null && date.length() > 16) date = date.substring(0, 16).replace("T", " ");
                        tableModel.addRow(new Object[]{i + 1, h.getExpression(), h.getResult(), date});
                    }
                    statusLabel.setForeground(Theme.SUCCESS);
                    statusLabel.setText(history.size() + " record(s) loaded.");
                } catch (Exception ex) {
                    statusLabel.setForeground(Theme.ERROR);
                    statusLabel.setText("Failed to load history.");
                }
            }
        }.execute();
    }

    private void clearHistory() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete ALL your calculation history?", "Confirm Clear",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        int userId = SessionManager.getInstance().getUser().getId();
        statusLabel.setForeground(Theme.TEXT_MUTED);
        statusLabel.setText("Clearing…");

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return db.clearHistory(userId); }
            @Override protected void done() {
                try {
                    if (get()) {
                        tableModel.setRowCount(0);
                        statusLabel.setForeground(Theme.SUCCESS);
                        statusLabel.setText("History cleared.");
                    } else {
                        statusLabel.setForeground(Theme.ERROR);
                        statusLabel.setText("Failed to clear history.");
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }
}
