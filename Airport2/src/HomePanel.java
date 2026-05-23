// HomePanel.java
import javax.swing.*;
import java.awt.*;

public class HomePanel extends JPanel implements ThemeManager.ThemeListener {
    private FlightBookingSystem mainFrame;

    public HomePanel(FlightBookingSystem mainFrame) {
        this.mainFrame = mainFrame;
        ThemeManager.getInstance().addListener(this);
        setLayout(new BorderLayout(0, 0));
        buildUI();
    }

    private void buildUI() {
        removeAll();
        setBackground(SkyWingUI.PANEL_BG);

        // ── Header ──
        JPanel hero = new JPanel(new BorderLayout());
        hero.setBackground(SkyWingUI.ACCENT);
        hero.setPreferredSize(new Dimension(0, 140));
        hero.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        JLabel logoLabel = createLogoLabel(120, 60);
        hero.add(logoLabel, BorderLayout.WEST);

        JPanel heroCenter = new JPanel(new GridBagLayout());
        heroCenter.setOpaque(false);
        JLabel welcome = new JLabel("Welcome back, "
                + (mainFrame.getCurrentUser() != null ? mainFrame.getCurrentUser() : "Traveler") + "!");
        welcome.setFont(new Font("Georgia", Font.BOLD, 34));
        welcome.setForeground(Color.WHITE);
        heroCenter.add(welcome);
        hero.add(heroCenter, BorderLayout.CENTER);

        add(hero, BorderLayout.NORTH);

        // ── Subtitle strip ──
        JPanel subStrip = new JPanel(new FlowLayout(FlowLayout.CENTER));
        subStrip.setBackground(new Color(0, 95, 162));
        subStrip.setPreferredSize(new Dimension(0, 40));
        JLabel sub = new JLabel("Where would you like to fly today?");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        sub.setForeground(new Color(190, 220, 255));
        subStrip.add(sub);
        add(subStrip, BorderLayout.AFTER_LINE_ENDS);

        // ── Cards ──
        JPanel content = new JPanel(new GridLayout(1, 3, 28, 0));
        content.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));
        content.setBackground(SkyWingUI.PANEL_BG);

        content.add(createOptionCard("\u2708\uFE0F  Search Flights",
                "Browse available routes, filter by date and price, and book your seat.",
                "Search", SkyWingUI.ACCENT));
        content.add(createOptionCard("\uD83D\uDCCB  My Bookings",
                "View your upcoming trips, check booking IDs, or cancel reservations.",
                "Bookings", new Color(0, 153, 100)));
        content.add(createOptionCard("\uD83D\uDCE1  Flight Status",
                "Track real-time departure and arrival updates for any flight.",
                "Status", new Color(160, 80, 0)));

        add(content, BorderLayout.CENTER);

        // ── Footer ──
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 14));
        footer.setBackground(SkyWingUI.PANEL_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SkyWingUI.CARD_BORDER));
        RoundedButton logoutBtn = new RoundedButton("Logout", new Color(220, 53, 69));
        logoutBtn.setPreferredSize(new Dimension(140, 44));
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.addActionListener(e -> mainFrame.logout());
        footer.add(logoutBtn);
        add(footer, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private JPanel createOptionCard(String title, String desc, String targetPanel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(SkyWingUI.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SkyWingUI.CARD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(SkyWingUI.CARD_PADDING + 8, SkyWingUI.CARD_PADDING,
                        SkyWingUI.CARD_PADDING, SkyWingUI.CARD_PADDING)));

        // Top accent line
        JPanel topBar = new JPanel();
        topBar.setBackground(accent);
        topBar.setPreferredSize(new Dimension(0, 4));
        card.add(topBar, BorderLayout.NORTH);

        JPanel mid = new JPanel();
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.setOpaque(false);
        mid.add(Box.createVerticalStrut(12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 20));
        titleLabel.setForeground(SkyWingUI.DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mid.add(titleLabel);
        mid.add(Box.createVerticalStrut(12));

        JLabel descLabel = new JLabel("<html><div style='width:200px;font-family:Segoe UI;font-size:12pt;color:#646e82;line-height:1.5;'>"
                + desc + "</div></html>");
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mid.add(descLabel);

        card.add(mid, BorderLayout.CENTER);

        RoundedButton btn = new RoundedButton("Open →", accent);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.addActionListener(e -> mainFrame.showPanel(targetPanel));
        card.add(btn, BorderLayout.SOUTH);

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(247, 251, 255));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(accent, 1, true),
                        BorderFactory.createEmptyBorder(SkyWingUI.CARD_PADDING + 8,
                                SkyWingUI.CARD_PADDING, SkyWingUI.CARD_PADDING, SkyWingUI.CARD_PADDING)));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(SkyWingUI.CARD_BG);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(SkyWingUI.CARD_BORDER, 1, true),
                        BorderFactory.createEmptyBorder(SkyWingUI.CARD_PADDING + 8,
                                SkyWingUI.CARD_PADDING, SkyWingUI.CARD_PADDING, SkyWingUI.CARD_PADDING)));
            }
        });

        return card;
    }

    private JLabel createLogoLabel(int width, int height) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(width + 24, height));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(
                        "https://png.pngtree.com/png-vector/20221028/ourmid/pngtree-plane-travel-logo-vector-icon-paper-world-vector-png-image_39818710.png");
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                SwingUtilities.invokeLater(() -> label.setIcon(new ImageIcon(img)));
            } catch (Exception ignored) {}
        }, "logo-loader-home").start();
        return label;
    }

    @Override
    public void onThemeChanged() { buildUI(); }
}