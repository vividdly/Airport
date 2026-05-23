// SkyWingUI.java - Shared UI utilities for SkyWing
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Centralized UI factory for SkyWing.
 * Provides styled modals, text fields, combo boxes, and sizing constants.
 */
public class SkyWingUI {

    // ── Global Palette ─────────────────────────────────────────────────────
    public static final Color ACCENT      = new Color(0, 119, 200);
    public static final Color SUCCESS     = new Color(0, 168, 84);
    public static final Color DANGER      = new Color(220, 53, 69);
    public static final Color WARN        = new Color(255, 140, 0);
    public static final Color PANEL_BG    = new Color(245, 248, 252);
    public static final Color FIELD_BG    = Color.WHITE;
    public static final Color BORDER_CLR  = new Color(210, 218, 230);
    public static final Color BORDER_FOCUS= ACCENT;
    public static final Color MUTED       = new Color(100, 110, 130);
    public static final Color DARK        = new Color(18, 24, 40);
    public static final Color CARD_BG     = Color.WHITE;
    public static final Color CARD_BORDER = new Color(218, 225, 235);

    // ── Global Sizing ──────────────────────────────────────────────────────
    public static final int FIELD_HEIGHT    = 46;   // text fields
    public static final int COMBO_HEIGHT    = 46;   // combo boxes
    public static final int HEADER_HEIGHT   = 100;  // panel headers
    public static final int CARD_PADDING    = 24;   // card inner padding
    public static final int SIDEBAR_WIDTH   = 320;

    // ── Styled JTextField ──────────────────────────────────────────────────
    public static JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                // Rounded fill
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D ph = (Graphics2D) g.create();
                    ph.setColor(new Color(180, 190, 210));
                    ph.setFont(getFont().deriveFont(Font.ITALIC));
                    FontMetrics fm = ph.getFontMetrics();
                    ph.drawString(placeholder, 14, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    ph.dispose();
                }
            }
        };
        applyFieldStyle(f);
        return f;
    }

    public static JPasswordField styledPassword() {
        JPasswordField f = new JPasswordField();
        applyFieldStyle(f);
        return f;
    }

    private static void applyFieldStyle(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setBackground(FIELD_BG);
        f.setForeground(DARK);
        f.setCaretColor(ACCENT);
        f.setOpaque(false);
        f.setBorder(new RoundedFieldBorder(10, BORDER_CLR));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));
        f.setPreferredSize(new Dimension(340, FIELD_HEIGHT));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(new RoundedFieldBorder(10, BORDER_FOCUS));
                f.repaint();
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(new RoundedFieldBorder(10, BORDER_CLR));
                f.repaint();
            }
        });
    }

    // ── Styled JComboBox ───────────────────────────────────────────────────
    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setBackground(FIELD_BG);
        c.setForeground(DARK);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, COMBO_HEIGHT));
        c.setPreferredSize(new Dimension(280, COMBO_HEIGHT));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 4)));
        c.setRenderer(new ComboRenderer());
        // Make popup items taller
        c.setMaximumRowCount(8);
        return c;
    }

    // ── Modal Dialogs (styled replacement for JOptionPane) ────────────────

    /** Info / success dialog */
    public static void showInfo(Component parent, String title, String message) {
        showModal(parent, title, message, SUCCESS, "✓");
    }

    /** Warning dialog */
    public static void showWarning(Component parent, String title, String message) {
        showModal(parent, title, message, WARN, "⚠");
    }

    /** Error dialog */
    public static void showError(Component parent, String title, String message) {
        showModal(parent, title, message, DANGER, "✕");
    }

    /** Yes/No confirmation — returns true if user clicked Yes */
    public static boolean showConfirm(Component parent, String title, String message) {
        JDialog dialog = createBaseDialog(parent, title);

        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(30, 36, 24, 36));

        // Icon + message
        JPanel top = new JPanel(new BorderLayout(18, 0));
        top.setOpaque(false);

        JLabel icon = new JLabel("?");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 32));
        icon.setForeground(ACCENT);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(52, 52));
        top.add(icon, BorderLayout.WEST);

        JLabel msg = new JLabel("<html><div style='width:280px;font-family:Segoe UI;font-size:13pt;color:#121828;'>"
                + message.replace("\n", "<br>") + "</div></html>");
        top.add(msg, BorderLayout.CENTER);
        content.add(top, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btns.setOpaque(false);

        boolean[] result = {false};
        RoundedButton noBtn  = new RoundedButton("No",  new Color(150, 158, 170));
        RoundedButton yesBtn = new RoundedButton("Yes", DANGER);
        noBtn.setPreferredSize(new Dimension(100, 42));
        yesBtn.setPreferredSize(new Dimension(100, 42));
        noBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        yesBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        noBtn.addActionListener(e  -> dialog.dispose());
        yesBtn.addActionListener(e -> { result[0] = true; dialog.dispose(); });

        btns.add(noBtn);
        btns.add(yesBtn);
        content.add(btns, BorderLayout.SOUTH);

        dialog.add(content);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, 200));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true); // blocks
        return result[0];
    }

    private static void showModal(Component parent, String title, String message, Color accent, String iconChar) {
        JDialog dialog = createBaseDialog(parent, title);

        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(30, 36, 28, 36));

        // Accent strip at top
        JPanel strip = new JPanel();
        strip.setBackground(accent);
        strip.setPreferredSize(new Dimension(0, 5));
        dialog.add(strip, BorderLayout.NORTH);

        // Icon + message
        JPanel top = new JPanel(new BorderLayout(18, 0));
        top.setOpaque(false);

        JLabel icon = new JLabel(iconChar);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 28));
        icon.setForeground(accent);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(48, 48));
        top.add(icon, BorderLayout.WEST);

        JLabel msg = new JLabel("<html><div style='width:280px;font-family:Segoe UI;font-size:13pt;color:#121828;'>"
                + message.replace("\n", "<br>") + "</div></html>");
        msg.setVerticalAlignment(SwingConstants.CENTER);
        top.add(msg, BorderLayout.CENTER);
        content.add(top, BorderLayout.CENTER);

        // OK button
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btns.setOpaque(false);
        RoundedButton ok = new RoundedButton("OK", accent);
        ok.setPreferredSize(new Dimension(100, 42));
        ok.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ok.addActionListener(e -> dialog.dispose());
        btns.add(ok);
        content.add(btns, BorderLayout.SOUTH);

        dialog.add(content);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, 180));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static JDialog createBaseDialog(Component parent, String title) {
        Window owner = parent == null ? null
                : (parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent));
        JDialog d = owner instanceof Frame
                ? new JDialog((Frame) owner, title, true)
                : new JDialog((Dialog) owner, title, true);
        d.setLayout(new BorderLayout());
        d.setUndecorated(false);
        d.getRootPane().setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));
        return d;
    }

    // ── Inner: Rounded border for text fields ─────────────────────────────
    public static class RoundedFieldBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        RoundedFieldBorder(int radius, Color color) { this.radius = radius; this.color = color; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, radius, radius));
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) { return new Insets(10, 14, 10, 14); }
        @Override public Insets getBorderInsets(Component c, Insets i) {
            i.set(10, 14, 10, 14); return i;
        }
    }

    // ── Inner: Custom ComboBox renderer ───────────────────────────────────
    private static class ComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            lbl.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            if (isSelected) {
                lbl.setBackground(new Color(230, 242, 255));
                lbl.setForeground(ACCENT);
            } else {
                lbl.setBackground(FIELD_BG);
                lbl.setForeground(DARK);
            }
            return lbl;
        }
    }
}