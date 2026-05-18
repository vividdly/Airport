// CreateAccountPanel.java
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * A polished, full-screen Create Account panel for SkyWing.
 *
 * Design improvements over the old JOptionPane approach:
 *  - Dedicated panel in CardLayout (no modal dialogs)
 *  - Real-time password strength indicator
 *  - Password confirmation field with match validation
 *  - Inline error labels instead of popup messages
 *  - Show/hide password toggle
 *  - Email field (stored in user profile)
 *  - Clean two-column layout: branding left, form right
 */
public class CreateAccountPanel extends JPanel implements ThemeManager.ThemeListener {

    // ── Colors (consistent with SkyWing palette) ──────────────────────────
    private static final Color ACCENT      = new Color(0, 119, 200);
    private static final Color SUCCESS     = new Color(0, 153, 76);
    private static final Color DANGER      = new Color(220, 53, 69);
    private static final Color WARN        = new Color(255, 140, 0);
    private static final Color PANEL_BG    = new Color(245, 248, 252);
    private static final Color FIELD_BG    = Color.WHITE;
    private static final Color BORDER_CLR  = new Color(210, 218, 230);
    private static final Color MUTED       = new Color(100, 110, 130);
    private static final Color DARK_TEXT   = new Color(20, 25, 40);

    // ── State ───────────────────────────────────────────────────────────────
    private final FlightBookingSystem mainFrame;

    // Form fields
    private JTextField     usernameField;
    private JTextField     emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;

    // Feedback labels (inline)
    private JLabel usernameError;
    private JLabel emailError;
    private JLabel passwordError;
    private JLabel confirmError;

    // Strength bar segments
    private JPanel[] strengthSegments;
    private JLabel   strengthLabel;

    // ── Constructor ─────────────────────────────────────────────────────────
    public CreateAccountPanel(FlightBookingSystem mainFrame) {
        this.mainFrame = mainFrame;
        ThemeManager.getInstance().addListener(this);
        setLayout(new BorderLayout());
        buildUI();
    }

    // ── UI Construction ─────────────────────────────────────────────────────
    private void buildUI() {
        removeAll();
        setBackground(PANEL_BG);

        // ── Left branding panel ────────────────────────────────────────────
        JPanel brand = new JPanel(new GridBagLayout());
        brand.setBackground(ACCENT);
        brand.setPreferredSize(new Dimension(460, 0));

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0; bc.gridy = GridBagConstraints.RELATIVE;
        bc.insets = new Insets(12, 40, 12, 40);
        bc.anchor = GridBagConstraints.CENTER;

        JLabel logoText = new JLabel("SKYWING");
        logoText.setFont(new Font("Georgia", Font.BOLD, 52));
        logoText.setForeground(Color.WHITE);
        brand.add(logoText, bc);

        JLabel tagline = makeLabel("Your Sky. Your Journey.", 18, Font.ITALIC, new Color(190, 220, 255));
        brand.add(tagline, bc);

        brand.add(Box.createVerticalStrut(40), bc);

        String[] perks = {
                "✈  Book 10+ domestic routes",
                "🪑  Choose your perfect seat",
                "📋  Manage all your trips in one place",
                "🔔  Get live flight status updates"
        };
        for (String p : perks) {
            JLabel pl = makeLabel(p, 15, Font.PLAIN, new Color(220, 235, 255));
            pl.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            brand.add(pl, bc);
        }

        add(brand, BorderLayout.WEST);

        // ── Right form panel ───────────────────────────────────────────────
        JPanel formWrapper = new JPanel(new GridBagLayout());
        formWrapper.setBackground(PANEL_BG);

        GridBagConstraints fw = new GridBagConstraints();
        fw.gridx = 0; fw.gridy = 0; fw.fill = GridBagConstraints.BOTH;
        fw.weightx = 1; fw.weighty = 1;
        fw.insets = new Insets(0, 60, 0, 60);

        JPanel form = buildFormPanel();
        formWrapper.add(form, fw);

        add(formWrapper, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Header
        form.add(Box.createVerticalStrut(50));
        JLabel heading = makeLabel("Create your account", 30, Font.BOLD, DARK_TEXT);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(heading);

        JLabel sub = makeLabel("Join SkyWing and start booking today", 14, Font.PLAIN, MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(sub);
        form.add(Box.createVerticalStrut(32));

        // ── Username ──
        form.add(fieldLabel("Username"));
        usernameField = createTextField("e.g. juan_dela_cruz");
        form.add(usernameField);
        usernameError = errorLabel();
        form.add(usernameError);
        form.add(Box.createVerticalStrut(14));

        // Live username validation
        usernameField.getDocument().addDocumentListener(new QuickDocListener(() -> validateUsername()));

        // ── Email ──
        form.add(fieldLabel("Email address"));
        emailField = createTextField("e.g. juan@email.com");
        form.add(emailField);
        emailError = errorLabel();
        form.add(emailError);
        form.add(Box.createVerticalStrut(14));

        emailField.getDocument().addDocumentListener(new QuickDocListener(() -> validateEmail()));

        // ── Password ──
        form.add(fieldLabel("Password"));
        passwordField = createPasswordField();
        form.add(wrapWithToggle(passwordField));
        passwordError = errorLabel();
        form.add(passwordError);
        form.add(Box.createVerticalStrut(6));

        // Strength bar
        form.add(buildStrengthBar());
        strengthLabel = makeLabel("", 12, Font.PLAIN, MUTED);
        form.add(strengthLabel);
        form.add(Box.createVerticalStrut(14));

        passwordField.getDocument().addDocumentListener(new QuickDocListener(() -> {
            validatePassword();
            updateStrengthBar(new String(passwordField.getPassword()));
        }));

        // ── Confirm ──
        form.add(fieldLabel("Confirm password"));
        confirmField = createPasswordField();
        form.add(wrapWithToggle(confirmField));
        confirmError = errorLabel();
        form.add(confirmError);
        form.add(Box.createVerticalStrut(28));

        confirmField.getDocument().addDocumentListener(new QuickDocListener(() -> validateConfirm()));

        // ── Buttons ──
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setOpaque(false);

        RoundedButton createBtn = new RoundedButton("CREATE ACCOUNT", SUCCESS);
        RoundedButton backBtn   = new RoundedButton("← Back to Login", new Color(100, 110, 130));

        createBtn.addActionListener(e -> attemptRegister());
        backBtn.addActionListener(e -> mainFrame.showPanel("Login"));

        btnRow.add(backBtn);
        btnRow.add(createBtn);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(btnRow);
        form.add(Box.createVerticalStrut(50));

        return form;
    }

    // ── Field Builders ───────────────────────────────────────────────────────

    private JLabel fieldLabel(String text) {
        JLabel lbl = makeLabel(text, 13, Font.BOLD, DARK_TEXT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 3, 4, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField createTextField(String placeholder) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(new Color(180, 190, 205));
                    g.setFont(getFont().deriveFont(Font.ITALIC));
                    g.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        styleField(f);
        return f;
    }

    private JPasswordField createPasswordField() {
        JPasswordField f = new JPasswordField();
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Arial", Font.PLAIN, 15));
        f.setBackground(FIELD_BG);
        f.setForeground(DARK_TEXT);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, BORDER_CLR),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Focus highlight
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(10, ACCENT),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(10, BORDER_CLR),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            }
        });
    }

    /**
     * Wraps a password field with a show/hide toggle button on the right.
     */
    private JPanel wrapWithToggle(JPasswordField pf) {
        JPanel wrap = new JPanel(new BorderLayout(0, 0));
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton toggle = new JButton("Show");
        toggle.setFont(new Font("Arial", Font.PLAIN, 12));
        toggle.setForeground(ACCENT);
        toggle.setBorderPainted(false);
        toggle.setContentAreaFilled(false);
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.setFocusPainted(false);
        toggle.addActionListener(e -> {
            if (pf.getEchoChar() == 0) {
                pf.setEchoChar('•');
                toggle.setText("Show");
            } else {
                pf.setEchoChar((char) 0);
                toggle.setText("Hide");
            }
        });

        wrap.add(pf, BorderLayout.CENTER);
        wrap.add(toggle, BorderLayout.EAST);
        return wrap;
    }

    private JLabel errorLabel() {
        JLabel lbl = makeLabel("", 12, Font.PLAIN, DANGER);
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // ── Password Strength Bar ────────────────────────────────────────────────

    private JPanel buildStrengthBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 5, 0));
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        strengthSegments = new JPanel[4];
        for (int i = 0; i < 4; i++) {
            strengthSegments[i] = new JPanel();
            strengthSegments[i].setBackground(new Color(220, 225, 235));
            strengthSegments[i].setOpaque(true);
            bar.add(strengthSegments[i]);
        }
        return bar;
    }

    private void updateStrengthBar(String pwd) {
        int score = 0;
        if (pwd.length() >= 8)                           score++;
        if (pwd.matches(".*[A-Z].*"))                    score++;
        if (pwd.matches(".*[0-9].*"))                    score++;
        if (pwd.matches(".*[^a-zA-Z0-9].*"))             score++;

        Color[] barColors = {DANGER, WARN, new Color(0, 180, 100), SUCCESS};
        String[] labels   = {"", "Weak", "Fair", "Good", "Strong"};

        for (int i = 0; i < 4; i++) {
            strengthSegments[i].setBackground(
                    i < score ? barColors[score - 1] : new Color(220, 225, 235));
        }
        strengthLabel.setText(pwd.isEmpty() ? "" : labels[score]);
        strengthLabel.setForeground(score > 0 ? barColors[score - 1] : MUTED);
    }

    // ── Validation ───────────────────────────────────────────────────────────

    private boolean validateUsername() {
        String u = usernameField.getText().trim();
        if (u.isEmpty()) {
            setError(usernameError, "Username is required.");
            return false;
        }
        if (u.length() < 3) {
            setError(usernameError, "At least 3 characters required.");
            return false;
        }
        if (!u.matches("[a-zA-Z0-9_]+")) {
            setError(usernameError, "Only letters, numbers, underscores.");
            return false;
        }
        clearError(usernameError);
        return true;
    }

    private boolean validateEmail() {
        String e = emailField.getText().trim();
        if (e.isEmpty()) {
            setError(emailError, "Email is required.");
            return false;
        }
        if (!e.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            setError(emailError, "Enter a valid email address.");
            return false;
        }
        clearError(emailError);
        return true;
    }

    private boolean validatePassword() {
        String p = new String(passwordField.getPassword());
        if (p.isEmpty()) {
            setError(passwordError, "Password is required.");
            return false;
        }
        if (p.length() < 6) {
            setError(passwordError, "Minimum 6 characters.");
            return false;
        }
        clearError(passwordError);
        return true;
    }

    private boolean validateConfirm() {
        String p = new String(passwordField.getPassword());
        String c = new String(confirmField.getPassword());
        if (c.isEmpty()) {
            setError(confirmError, "Please confirm your password.");
            return false;
        }
        if (!p.equals(c)) {
            setError(confirmError, "Passwords do not match.");
            return false;
        }
        clearError(confirmError);
        return true;
    }

    private void setError(JLabel lbl, String msg) {
        lbl.setText("⚠ " + msg);
    }

    private void clearError(JLabel lbl) {
        lbl.setText("");
    }

    // ── Registration ─────────────────────────────────────────────────────────

    private void attemptRegister() {
        boolean ok = validateUsername() & validateEmail() & validatePassword() & validateConfirm();
        if (!ok) return;

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        mainFrame.register(username, password);

        // Success feedback then redirect to login
        JOptionPane.showMessageDialog(this,
                "Account created! You can now log in as \"" + username + "\".",
                "Welcome to SkyWing", JOptionPane.INFORMATION_MESSAGE);

        clearForm();
        mainFrame.showPanel("Login");
    }

    private void clearForm() {
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmField.setText("");
        clearError(usernameError);
        clearError(emailError);
        clearError(passwordError);
        clearError(confirmError);
        updateStrengthBar("");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JLabel makeLabel(String text, int size, int style, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", style, size));
        lbl.setForeground(color);
        return lbl;
    }

    @Override
    public void onThemeChanged() {
        buildUI();
    }

    // ── Inner: Rounded Border ─────────────────────────────────────────────────

    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color  = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(radius, radius, radius, radius); }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(radius, radius, radius, radius);
            return insets;
        }
    }

    // ── Inner: Quick DocumentListener helper ─────────────────────────────────

    private static class QuickDocListener implements javax.swing.event.DocumentListener {
        private final Runnable task;

        QuickDocListener(Runnable task) { this.task = task; }

        @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { task.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { task.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { task.run(); }
    }
}