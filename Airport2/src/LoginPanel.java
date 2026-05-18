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

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;

        JLabel logo = createLogoLabel(320, 140);
        gbc.gridy = 0;
        content.add(logo, gbc);

        JLabel title = new JLabel("Welcome to SkyWing", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(ThemeManager.getInstance().getAccentColor());
        gbc.gridy = 1;
        content.add(title, gbc);

        JLabel subtitle = new JLabel("Sign in to book your next adventure", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridy = 2;
        content.add(subtitle, gbc);

        gbc.gridy = 3;
        content.add(new JLabel("Username"), gbc);

        JTextField userField = new JTextField("heroyuki", 22);
        styleField(userField);
        gbc.gridy = 4;
        content.add(userField, gbc);

        gbc.gridy = 5;
        content.add(new JLabel("Password"), gbc);

        JPasswordField passField = new JPasswordField("1234", 22);
        styleField(passField);
        gbc.gridy = 6;
        content.add(passField, gbc);

        gbc.gridy = 7;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        btnPanel.setOpaque(false);

        RoundedButton loginBtn  = new RoundedButton("LOGIN", new Color(0, 153, 76));
        RoundedButton signupBtn = new RoundedButton("CREATE ACCOUNT", new Color(0, 119, 200));

        loginBtn.addActionListener(e -> {
            if (mainFrame.login(userField.getText().trim(), new String(passField.getPassword()))) {
                mainFrame.showPanel("Home");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Navigate to the dedicated CreateAccount panel
        signupBtn.addActionListener(e -> mainFrame.showPanel("CreateAccount"));

        btnPanel.add(loginBtn);
        btnPanel.add(signupBtn);
        content.add(btnPanel, gbc);

        add(content, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 17));
        field.setPreferredSize(new Dimension(340, 48));
    }

    private JLabel createLogoLabel(int w, int h) {
        JLabel label = new JLabel("SKYWING");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 48));
        label.setForeground(ThemeManager.getInstance().getAccentColor());

        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(
                        "https://png.pngtree.com/png-vector/20221028/ourmid/pngtree-plane-travel-logo-vector-icon-paper-world-vector-png-image_39818710.png");
                ImageIcon icon = new ImageIcon(url);
                Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                SwingUtilities.invokeLater(() -> {
                    label.setIcon(new ImageIcon(scaled));
                    label.setText(null);
                });
            } catch (Exception ignored) {}
        }, "logo-loader-login").start();

        return label;
    }

    @Override
    public void onThemeChanged() {
        buildUI();
    }
}