// CreateAccountPanel.java
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class CreateAccountPanel extends JPanel implements ThemeManager.ThemeListener {

    private static final Color ACCENT     = new Color(0, 119, 200);
    private static final Color SUCCESS    = new Color(0, 153, 76);
    private static final Color DANGER     = new Color(220, 53, 69);
    private static final Color WARN       = new Color(255, 140, 0);
    private static final Color PANEL_BG   = new Color(245, 248, 252);
    private static final Color FIELD_BG   = Color.WHITE;
    private static final Color BORDER_CLR = new Color(210, 218, 230);
    private static final Color MUTED      = new Color(100, 110, 130);
    private static final Color DARK_TEXT  = new Color(20, 25, 40);

    private final FlightBookingSystem mainFrame;

    private JTextField     usernameField;
    private JTextField     emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;

    private JLabel usernameError;
    private JLabel emailError;
    private JLabel passwordError;
    private JLabel confirmError;

    private JPanel[] strengthSegments;
    private JLabel   strengthLabel;

    public CreateAccountPanel(FlightBookingSystem mainFrame) {
        this.mainFrame = mainFrame;
        ThemeManager.getInstance().addListener(this);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        removeAll();
        setBackground(PANEL_BG);

        // ── Left blue branding panel ──────────────────────────────────────
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
        brand.add(Box.createVerticalStrut(30), bc);

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

        // ── Right form panel — BorderLayout so it fills the space ─────────
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(PANEL_BG);

        // Centering wrapper inside the right panel
        JPanel centerBox = new JPanel(new GridBagLayout());
        centerBox.setBackground(PANEL_BG);

        JScrollPane scroll = new JScrollPane(buildForm(),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        scroll.setPreferredSize(new Dimension(480, 700));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;
        gc.fill = GridBagConstraints.NONE;
        centerBox.add(scroll, gc);

        rightPanel.add(centerBox, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel buildForm() {
        // Fixed-width form, GridBagLayout so every row aligns perfectly
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(420, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        int row = 0;

        // Heading
        JLabel heading = makeLabel("Create your account", 30, Font.BOLD, DARK_TEXT);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = row++; c.insets = new Insets(40, 0, 6, 0);
        form.add(heading, c);

        JLabel sub = makeLabel("Join SkyWing and start booking today", 14, Font.PLAIN, MUTED);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = row++; c.insets = new Insets(0, 0, 28, 0);
        form.add(sub, c);

        // Username
        c.gridy = row++; c.insets = new Insets(0, 0, 6, 0);
        form.add(fieldLabel("Username"), c);
        usernameField = createTextField("e.g. juan_dela_cruz");
        c.gridy = row++; c.insets = new Insets(0, 0, 2, 0);
        form.add(usernameField, c);
        usernameError = errorLabel();
        c.gridy = row++; c.insets = new Insets(0, 0, 12, 0);
        form.add(usernameError, c);
        usernameField.getDocument().addDocumentListener(new QuickDocListener(() -> validateUsername()));

        // Email
        c.gridy = row++; c.insets = new Insets(0, 0, 6, 0);
        form.add(fieldLabel("Email address"), c);
        emailField = createTextField("e.g. juan@email.com");
        c.gridy = row++; c.insets = new Insets(0, 0, 2, 0);
        form.add(emailField, c);
        emailError = errorLabel();
        c.gridy = row++; c.insets = new Insets(0, 0, 12, 0);
        form.add(emailError, c);
        emailField.getDocument().addDocumentListener(new QuickDocListener(() -> validateEmail()));

        // Password
        c.gridy = row++; c.insets = new Insets(0, 0, 6, 0);
        form.add(fieldLabel("Password"), c);
        passwordField = createPasswordField();
        c.gridy = row++; c.insets = new Insets(0, 0, 2, 0);
        form.add(wrapWithToggle(passwordField), c);
        passwordError = errorLabel();
        c.gridy = row++; c.insets = new Insets(0, 0, 4, 0);
        form.add(passwordError, c);
        c.gridy = row++; c.insets = new Insets(0, 0, 2, 0);
        form.add(buildStrengthBar(), c);
        strengthLabel = makeLabel("", 12, Font.PLAIN, MUTED);
        c.gridy = row++; c.insets = new Insets(0, 0, 12, 0);
        form.add(strengthLabel, c);
        passwordField.getDocument().addDocumentListener(new QuickDocListener(() -> {
            validatePassword();
            updateStrengthBar(new String(passwordField.getPassword()));
        }));

        // Confirm password
        c.gridy = row++; c.insets = new Insets(0, 0, 6, 0);
        form.add(fieldLabel("Confirm password"), c);
        confirmField = createPasswordField();
        c.gridy = row++; c.insets = new Insets(0, 0, 2, 0);
        form.add(wrapWithToggle(confirmField), c);
        confirmError = errorLabel();
        c.gridy = row++; c.insets = new Insets(0, 0, 24, 0);
        form.add(confirmError, c);
        confirmField.getDocument().addDocumentListener(new QuickDocListener(() -> validateConfirm()));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setOpaque(false);
        RoundedButton createBtn = new RoundedButton("CREATE ACCOUNT", SUCCESS);
        RoundedButton backBtn   = new RoundedButton("← Back to Login", new Color(100, 110, 130));
        createBtn.setPreferredSize(new Dimension(210, 48));
        backBtn.setPreferredSize(new Dimension(190, 48));
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        createBtn.addActionListener(e -> attemptRegister());
        backBtn.addActionListener(e -> mainFrame.showPanel("Login"));
        btnRow.add(backBtn);
        btnRow.add(createBtn);
        c.gridy = row++; c.insets = new Insets(0, 0, 40, 0);
        form.add(btnRow, c);

        return form;
    }

    // ── Field builders ────────────────────────────────────────────────────

    private JLabel fieldLabel(String text) {
        return makeLabel(text, 13, Font.BOLD, DARK_TEXT);
    }

    private JTextField createTextField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(180, 190, 205));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, 12, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
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
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setBackground(FIELD_BG);
        f.setForeground(DARK_TEXT);
        f.setCaretColor(ACCENT);
        f.setPreferredSize(new Dimension(0, 46));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT, 2, true),
                        BorderFactory.createEmptyBorder(9, 11, 9, 11)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            }
        });
    }

    private JPanel wrapWithToggle(JPasswordField pf) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setPreferredSize(new Dimension(0, 46));
        JButton toggle = new JButton("Show");
        toggle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toggle.setForeground(ACCENT);
        toggle.setBorderPainted(false);
        toggle.setContentAreaFilled(false);
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.setFocusPainted(false);
        toggle.addActionListener(e -> {
            if (pf.getEchoChar() == 0) { pf.setEchoChar('•'); toggle.setText("Show"); }
            else                       { pf.setEchoChar((char) 0); toggle.setText("Hide"); }
        });
        wrap.add(pf, BorderLayout.CENTER);
        wrap.add(toggle, BorderLayout.EAST);
        return wrap;
    }

    private JLabel errorLabel() {
        JLabel lbl = makeLabel("", 12, Font.PLAIN, DANGER);
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));
        return lbl;
    }

    // ── Strength bar ──────────────────────────────────────────────────────

    private JPanel buildStrengthBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 5, 0));
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 8));
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
        if (pwd.length() >= 8)              score++;
        if (pwd.matches(".*[A-Z].*"))        score++;
        if (pwd.matches(".*[0-9].*"))        score++;
        if (pwd.matches(".*[^a-zA-Z0-9].*")) score++;
        Color[] colors = {DANGER, WARN, new Color(0, 180, 100), SUCCESS};
        String[] labels = {"", "Weak", "Fair", "Good", "Strong"};
        for (int i = 0; i < 4; i++)
            strengthSegments[i].setBackground(i < score ? colors[score - 1] : new Color(220, 225, 235));
        strengthLabel.setText(pwd.isEmpty() ? "" : labels[score]);
        strengthLabel.setForeground(score > 0 ? colors[score - 1] : MUTED);
    }

    // ── Validation ────────────────────────────────────────────────────────

    private boolean validateUsername() {
        String u = usernameField.getText().trim();
        if (u.isEmpty())               { setError(usernameError, "Username is required."); return false; }
        if (u.length() < 3)            { setError(usernameError, "At least 3 characters required."); return false; }
        if (!u.matches("[a-zA-Z0-9_]+")){ setError(usernameError, "Only letters, numbers, underscores."); return false; }
        clearError(usernameError); return true;
    }

    private boolean validateEmail() {
        String e = emailField.getText().trim();
        if (e.isEmpty()) { setError(emailError, "Email is required."); return false; }
        if (!e.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            setError(emailError, "Enter a valid email address."); return false;
        }
        clearError(emailError); return true;
    }

    private boolean validatePassword() {
        String p = new String(passwordField.getPassword());
        if (p.isEmpty())    { setError(passwordError, "Password is required."); return false; }
        if (p.length() < 6) { setError(passwordError, "Minimum 6 characters."); return false; }
        clearError(passwordError); return true;
    }

    private boolean validateConfirm() {
        String p = new String(passwordField.getPassword());
        String cc = new String(confirmField.getPassword());
        if (cc.isEmpty())  { setError(confirmError, "Please confirm your password."); return false; }
        if (!p.equals(cc)) { setError(confirmError, "Passwords do not match."); return false; }
        clearError(confirmError); return true;
    }

    private void setError(JLabel lbl, String msg) { lbl.setText("⚠ " + msg); }
    private void clearError(JLabel lbl)           { lbl.setText(""); }

    // ── Registration ──────────────────────────────────────────────────────

    private void attemptRegister() {
        boolean ok = validateUsername() & validateEmail() & validatePassword() & validateConfirm();
        if (!ok) return;
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        mainFrame.register(username, password);
        SkyWingUI.showInfo(this, "Welcome to SkyWing",
                "Account created successfully!\nYou can now log in as \"" + username + "\".");
        clearForm();
        mainFrame.showPanel("Login");
    }

    private void clearForm() {
        usernameField.setText(""); emailField.setText("");
        passwordField.setText(""); confirmField.setText("");
        clearError(usernameError); clearError(emailError);
        clearError(passwordError); clearError(confirmError);
        updateStrengthBar("");
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private JLabel makeLabel(String text, int size, int style, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", style, size));
        lbl.setForeground(color);
        return lbl;
    }

    @Override public void onThemeChanged() { buildUI(); }

    private static class QuickDocListener implements javax.swing.event.DocumentListener {
        private final Runnable task;
        QuickDocListener(Runnable task) { this.task = task; }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { task.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { task.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { task.run(); }
    }
}