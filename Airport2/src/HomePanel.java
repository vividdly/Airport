// HomePanel.java
import javax.swing.*;
import java.awt.*;

public class HomePanel extends JPanel implements ThemeManager.ThemeListener {
    private FlightBookingSystem mainFrame;

    public HomePanel(FlightBookingSystem mainFrame) {
        this.mainFrame = mainFrame;
        ThemeManager.getInstance().addListener(this);
        setLayout(new BorderLayout(20, 20));
        buildUI();
    }

    private void buildUI() {
        removeAll();
        setBackground(ThemeManager.getInstance().getBackground());

        // Hero Header with Logo
        JPanel hero = new JPanel(new BorderLayout());
        hero.setBackground(ThemeManager.getInstance().getAccentColor());
        hero.setPreferredSize(new Dimension(0, 180));

        // Logo on the left
        JLabel logoLabel = createLogoLabel(180, 80);
        hero.add(logoLabel, BorderLayout.WEST);

        JLabel welcome = new JLabel("Welcome back, " +
                (mainFrame.getCurrentUser() != null ? mainFrame.getCurrentUser() : "Traveler") + "!",
                SwingConstants.CENTER);
        welcome.setFont(new Font("Arial", Font.BOLD, 36));
        welcome.setForeground(Color.WHITE);
        hero.add(welcome, BorderLayout.CENTER);

        add(hero, BorderLayout.NORTH);

        // Main Content - Beautiful Cards
        JPanel content = new JPanel(new GridLayout(1, 3, 30, 30));
        content.setBorder(BorderFactory.createEmptyBorder(60, 70, 60, 70));
        content.setBackground(ThemeManager.getInstance().getBackground());

        content.add(createOptionCard("Search Flights", "Find and book amazing flights", "Search"));
        content.add(createOptionCard("My Bookings", "View and manage your reservations", "Bookings"));
        content.add(createOptionCard("Flight Status", "Track live flight updates", "Status"));

        add(content, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(ThemeManager.getInstance().getBackground());
        RoundedButton logoutBtn = new RoundedButton("Logout", new Color(220, 53, 69));
        logoutBtn.addActionListener(e -> mainFrame.logout());
        footer.add(logoutBtn);
        add(footer, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private JPanel createOptionCard(String title, String desc, String targetPanel) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(35, 25, 35, 25)));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 21));

        JLabel descLabel = new JLabel("<html><center>" + desc + "</center></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        descLabel.setForeground(new Color(80, 80, 80));

        RoundedButton btn = new RoundedButton("Open", new Color(0, 153, 76));
        btn.addActionListener(e -> mainFrame.showPanel(targetPanel));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);

        return card;
    }

    private JLabel createLogoLabel(int width, int height) {
        JLabel label = new JLabel("SKYWING");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 42));
        label.setForeground(Color.WHITE);

        // FIX: Load remote image on a background thread so we never block the EDT.
        // Previously, new ImageIcon(url) fetched the image synchronously on the EDT,
        // which freezes the UI until the download completes (or times out).
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(
                        "https://png.pngtree.com/png-vector/20221028/ourmid/pngtree-plane-travel-logo-vector-icon-paper-world-vector-png-image_39818710.png");
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                SwingUtilities.invokeLater(() -> {
                    label.setIcon(new ImageIcon(img));
                    label.setText(null);
                });
            } catch (Exception ignored) {
                // fallback text is already set; nothing more to do
            }
        }, "logo-loader-home").start();

        return label;
    }

    @Override
    public void onThemeChanged() {
        buildUI();
    }
}