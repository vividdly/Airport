// BookingSummaryPanel.java
import javax.swing.*;
import java.awt.*;

public class BookingSummaryPanel extends JPanel implements ThemeManager.ThemeListener {
    private FlightBookingSystem mainFrame;
    private JLabel summaryLabel;

    public BookingSummaryPanel(FlightBookingSystem mainFrame) {
        this.mainFrame = mainFrame;
        ThemeManager.getInstance().addListener(this);
        setLayout(new BorderLayout(0, 0));
        buildUI();
    }

    private void buildUI() {
        String savedText = summaryLabel != null ? summaryLabel.getText() : "";
        removeAll();
        setBackground(SkyWingUI.PANEL_BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SkyWingUI.SUCCESS);
        header.setPreferredSize(new Dimension(0, SkyWingUI.HEADER_HEIGHT + 30));
        header.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        JLabel checkIcon = new JLabel("✓", SwingConstants.LEFT);
        checkIcon.setFont(new Font("Segoe UI", Font.BOLD, 48));
        checkIcon.setForeground(new Color(200, 255, 220));
        checkIcon.setPreferredSize(new Dimension(70, 0));
        header.add(checkIcon, BorderLayout.WEST);

        JPanel titleBox = new JPanel(new GridBagLayout());
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Booking Confirmed!");
        title.setFont(new Font("Georgia", Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        titleBox.add(title);
        header.add(titleBox, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // Summary card
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setBackground(SkyWingUI.PANEL_BG);

        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SkyWingUI.CARD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(32, 40, 32, 40)));
        card.setPreferredSize(new Dimension(560, 340));

        summaryLabel = new JLabel(savedText, SwingConstants.CENTER);
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        card.add(summaryLabel, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;
        cardWrapper.add(card, gc);
        add(cardWrapper, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnPanel.setBackground(SkyWingUI.PANEL_BG);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SkyWingUI.CARD_BORDER));

        RoundedButton homeBtn     = new RoundedButton("Back to Home", SkyWingUI.SUCCESS);
        RoundedButton bookingsBtn = new RoundedButton("My Bookings",  SkyWingUI.ACCENT);
        homeBtn.setPreferredSize(new Dimension(190, 48));
        bookingsBtn.setPreferredSize(new Dimension(190, 48));
        homeBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bookingsBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));

        homeBtn.addActionListener(e -> mainFrame.showPanel("Home"));
        bookingsBtn.addActionListener(e -> {
            mainFrame.getBookingsPanel().refreshList();
            mainFrame.showPanel("Bookings");
        });

        btnPanel.add(homeBtn);
        btnPanel.add(bookingsBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    public void showSummary(Booking booking) {
        summaryLabel.setText("<html><center>" +
                "<h2 style='color:#007728;font-family:Georgia;'>Booking ID: " + booking.getBookingId() + "</h2>" +
                "<table style='font-family:Segoe UI;font-size:13pt;border-spacing:8px;'>" +
                "<tr><td><b>Passenger</b></td><td>" + booking.getPassengerName() + "</td></tr>" +
                "<tr><td><b>Route</b></td><td>" + booking.getFlight().getRoute() + "</td></tr>" +
                "<tr><td><b>Date</b></td><td>" + booking.getFlight().getDate() + "</td></tr>" +
                "<tr><td><b>Departure</b></td><td>" + booking.getFlight().getDepartureTime() + "</td></tr>" +
                "<tr><td><b>Seat</b></td><td>" + booking.getSeatNumber() + "</td></tr>" +
                "<tr><td><b>Price</b></td><td style='color:#007728;'><b>PHP " +
                String.format("%,.0f", booking.getFlight().getPrice()) + "</b></td></tr>" +
                "</table></center></html>");
    }

    @Override public void onThemeChanged() { buildUI(); }
}