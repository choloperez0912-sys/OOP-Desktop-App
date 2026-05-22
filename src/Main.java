import com.calcapp.ui.MainWindow;
import com.calcapp.ui.Theme;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Use FlatLaf if available, otherwise fallback
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Dark defaults for Swing components
        UIManager.put("Panel.background",           Theme.BG_DARK);
        UIManager.put("OptionPane.background",      Theme.BG_CARD);
        UIManager.put("OptionPane.messageForeground",Theme.TEXT_PRIMARY);
        UIManager.put("Button.background",          Theme.BTN_NUM);
        UIManager.put("Button.foreground",          Theme.TEXT_PRIMARY);
        UIManager.put("TabbedPane.background",      Theme.BG_CARD);
        UIManager.put("TabbedPane.foreground",      Theme.TEXT_PRIMARY);
        UIManager.put("TabbedPane.selected",        Theme.BG_INPUT);
        UIManager.put("TabbedPane.contentBorderInsets", new java.awt.Insets(0,0,0,0));
        UIManager.put("ScrollPane.background",      Theme.BG_DARK);
        UIManager.put("Viewport.background",        Theme.BG_CARD);
        UIManager.put("Table.background",           Theme.BG_CARD);
        UIManager.put("Table.foreground",           Theme.TEXT_PRIMARY);
        UIManager.put("TableHeader.background",     Theme.BG_INPUT);
        UIManager.put("TableHeader.foreground",     Theme.TEXT_MUTED);
        UIManager.put("ScrollBar.thumb",            Theme.BTN_OP);
        UIManager.put("ScrollBar.track",            Theme.BG_CARD);

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
