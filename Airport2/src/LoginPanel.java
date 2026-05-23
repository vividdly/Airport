// LoginPanel.java
import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel implements ThemeManager.ThemeListener {

    private FlightBookingSystem mainFrame;

    public LoginPanel(FlightBookingSystem mainFrame) {
        this.mainFrame = mainFrame;
        ThemeManager.getInstance().addListener(this);
        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 252));
        buildUI();
    }

    private void buildUI() {
        removeAll();

        // ── Left blue branding panel ──────────────────────────────────────
        JPanel brand = new JPanel(new GridBagLayout());
        brand.setBackground(new Color(0, 119, 200));
        brand.setPreferredSize(new Dimension(460, 0));

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0; bc.gridy = GridBagConstraints.RELATIVE;
        bc.insets = new Insets(10, 40, 10, 40);
        bc.anchor = GridBagConstraints.CENTER;

        JLabel logo = createLogoLabel(200, 90);
        brand.add(logo, bc);

        JLabel name = new JLabel("SKYWING");
        name.setFont(new Font("Georgia", Font.BOLD, 48));
        name.setForeground(Color.WHITE);
        brand.add(name, bc);

        JLabel slogan = new JLabel("Your Sky. Your Journey.");
        slogan.setFont(new Font("Georgia", Font.ITALIC, 16));
        slogan.setForeground(new Color(190, 220, 255));
        brand.add(slogan, bc);

        add(brand, BorderLayout.WEST);

        // ── Right panel: uses BorderLayout so it fills the space properly ─
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(245, 248, 252));

        // Inner centering wrapper
        JPanel centerBox = new JPanel(new GridBagLayout());
        centerBox.setBackground(new Color(245, 248, 252));

        // The actual form — fixed width via GridBagConstraints ipadx
        JPanel form = buildForm();

        form.setPreferredSize(new Dimension(420, 380));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;
        gc.fill = GridBagConstraints.NONE;
        centerBox.add(form, gc);

        rightPanel.add(centerBox, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        // Title
        JLabel title = new JLabel("Welcome back", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 30));
        title.setForeground(new Color(18, 24, 40));
        c.gridy = 0; c.insets = new Insets(0, 0, 6, 0);
        form.add(title, c);

        JLabel sub = new JLabel("Sign in to your SkyWing account", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(new Color(100, 110, 130));
        c.gridy = 1; c.insets = new Insets(0, 0, 36, 0);
        form.add(sub, c);

        // Username label
        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLbl.setForeground(new Color(18, 24, 40));
        c.gridy = 2; c.insets = new Insets(0, 0, 6, 0);
        form.add(userLbl, c);

        // Username field
        JTextField userField = SkyWingUI.styledField("Enter username");
        userField.setText("heroyuki");
        c.gridy = 3; c.insets = new Insets(0, 0, 18, 0);
        form.add(userField, c);

        // Password label
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLbl.setForeground(new Color(18, 24, 40));
        c.gridy = 4; c.insets = new Insets(0, 0, 6, 0);
        form.add(passLbl, c);

        // Password field
        JPasswordField passField = SkyWingUI.styledPassword();
        passField.setText("1234");
        c.gridy = 5; c.insets = new Insets(0, 0, 36, 0);
        form.add(passField, c);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnPanel.setOpaque(false);
        RoundedButton loginBtn  = new RoundedButton("LOGIN", new Color(0, 153, 76));
        RoundedButton signupBtn = new RoundedButton("CREATE ACCOUNT", new Color(0, 119, 200));
        loginBtn.setPreferredSize(new Dimension(160, 48));
        signupBtn.setPreferredSize(new Dimension(210, 48));
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        signupBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));

        loginBtn.addActionListener(e -> {
            if (mainFrame.login(userField.getText().trim(), new String(passField.getPassword()))) {
                mainFrame.showPanel("Home");
            } else {
                SkyWingUI.showError(this, "Login Failed", "Invalid username or password.\nPlease try again.");
            }
        });
        signupBtn.addActionListener(e -> mainFrame.showPanel("CreateAccount"));
        btnPanel.add(loginBtn);
        btnPanel.add(signupBtn);
        c.gridy = 6; c.insets = new Insets(0, 0, 0, 0);
        form.add(btnPanel, c);

        return form;
    }

    private JLabel createLogoLabel(int w, int h) {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(
                        "https://png.pngtree.com/png-vector/20221028/ourmid/pngtree-plane-travel-logo-vector-icon-paper-world-vector-png-image_39818710.png");
                ImageIcon icon = new ImageIcon(url);
                Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                SwingUtilities.invokeLater(() -> label.setIcon(new ImageIcon(scaled)));
            } catch (Exception ignored) {}
        }, "logo-loader-login").start();
        return label;
    }

    @Override
    public void onThemeChanged() { buildUI(); }
}