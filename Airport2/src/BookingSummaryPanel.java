// BookingSummaryPanel.java
import javax.swing.*;
import java.awt.*;

public class BookingSummaryPanel extends JPanel implements ThemeManager.ThemeListener {
    private FlightBookingSystem mainFrame;
    private JLabel summaryLabel;

    public BookingSummaryPanel(FlightBookingSystem mainFrame) {
        this.mainFrame = mainFrame;
        ThemeManager.getInstance().addListener(this);
        setLayout(new BorderLayout(20, 20));
        buildUI();
    }

    private void buildUI() {
        removeAll();
        setBackground(ThemeManager.getInstance().getBackground());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeManager.getInstance().getAccentColor());
        header.setPreferredSize(new Dimension(0, 120));

        JLabel title = new JLabel("Booking Confirmed!", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        summaryLabel = new JLabel("", SwingConstants.CENTER);
        summaryLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(summaryLabel, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        btnPanel.setOpaque(false);

        RoundedButton homeBtn = new RoundedButton("Back to Home", new Color(0, 153, 76));
        RoundedButton bookingsBtn = new RoundedButton("My Bookings", new Color(0, 102, 204));

        homeBtn.addActionListener(e -> mainFrame.showPanel("Home"));
        bookingsBtn.addActionListener(e -> mainFrame.showPanel("Bookings"));

        btnPanel.add(homeBtn);
        btnPanel.add(bookingsBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    public void showSummary(Booking booking) {
        String summary = "<html><center>" +
                "<h2>Booking ID: " + booking.getBookingId() + "</h2>" +
                "<p><b>Passenger:</b> " + booking.getPassengerName() + "</p>" +
                "<p><b>Flight:</b> " + booking.getFlight().getRoute() + "</p>" +
                "<p><b>Date:</b> " + booking.getFlight().getDate() + "</p>" +
                "<p><b>Time:</b> " + booking.getFlight().getDepartureTime() + "</p>" +
                "<p><b>Seat:</b> " + booking.getSeatNumber() + "</p>" +
                "<p><b>Price:</b> PHP " + String.format("%,.0f", booking.getFlight().getPrice()) + "</p>" +
                "</center></html>";

        summaryLabel.setText(summary);
    }

    @Override
    public void onThemeChanged() {
        // FIX: Save current summary text before rebuilding, then restore it.
        // Previously, buildUI() recreated summaryLabel (empty), discarding the
        // booking details that showSummary() had set.
        String savedText = summaryLabel != null ? summaryLabel.getText() : "";
        buildUI();
        if (summaryLabel != null) summaryLabel.setText(savedText);
    }
}