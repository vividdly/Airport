// SeatSelectionPanel.java
import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class SeatSelectionPanel extends JPanel implements ThemeManager.ThemeListener {
    private FlightBookingSystem mainFrame;
    private Flight selectedFlight;
    private String passengerName;
    private String selectedSeat = null;

    private JButton confirmBtn;
    private Set<JButton> seatButtons = new HashSet<>();

    public SeatSelectionPanel(FlightBookingSystem mainFrame) {
        this.mainFrame = mainFrame;
        ThemeManager.getInstance().addListener(this);
        setLayout(new BorderLayout(10, 10));
    }

    public void setBookingDetails(Flight flight, String passenger) {
        this.selectedFlight = flight;
        this.passengerName = passenger;
        this.selectedSeat = null;
        removeAll();
        createUI();
        revalidate();
        repaint();
    }

    private void createUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeManager.getInstance().getAccentColor());
        header.setPreferredSize(new Dimension(0, 100));
        JLabel title = new JLabel("   \u2708\uFE0F Select Your Seat - " + selectedFlight.getFlightNumber(), JLabel.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Seat Map wrapper panel
        JPanel seatPanel = new JPanel(new BorderLayout());
        seatPanel.setBorder(BorderFactory.createTitledBorder("Aircraft Seat Map"));
        seatPanel.setBackground(ThemeManager.getInstance().getPanelBackground());

        // Legend
        JPanel legend = new JPanel();
        legend.setOpaque(false);
        legend.add(createLegend("Available", new Color(0, 153, 76)));
        legend.add(createLegend("Selected",  new Color(255, 215, 0)));
        legend.add(createLegend("Booked",    new Color(220, 53, 69)));
        seatPanel.add(legend, BorderLayout.NORTH);

        // FIX 1: GridLayout(6, 7) -- 7 columns = 6 seats + 1 aisle spacer per row.
        //         Was GridLayout(6,6) which overflowed and made all seats invisible.
        JPanel grid = new JPanel(new GridLayout(6, 7, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        grid.setBackground(ThemeManager.getInstance().getPanelBackground());

        Set<String> booked = selectedFlight.getBookedSeats();

        // FIX 2: Reset seatButtons each time so old-flight buttons don't linger.
        seatButtons = new HashSet<>();

        char[] seatLetters = {'A', 'B', 'C', 'D', 'E', 'F'};

        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col <= 6; col++) {
                if (col == 3) {
                    grid.add(new JLabel(" "));
                    continue;
                }
                // FIX 3: correct letter mapping col4->D, col5->E, col6->F
                int letterIndex = col < 3 ? col : col - 1;
                char letter = seatLetters[letterIndex];
                String seatNum = row + "" + letter;

                JButton btn = new JButton(seatNum);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
                btn.setPreferredSize(new Dimension(72, 72));

                if (booked.contains(seatNum)) {
                    btn.setBackground(new Color(220, 53, 69));
                    btn.setEnabled(false);
                } else {
                    btn.setBackground(new Color(0, 153, 76));
                    btn.setForeground(Color.WHITE);
                }

                btn.addActionListener(e -> selectSeat(btn, seatNum));
                seatButtons.add(btn);
                grid.add(btn);
            }
        }

        // FIX 4: Actually add grid to seatPanel, and seatPanel to main panel.
        //         Both add() calls were missing, so the seat map was never shown.
        seatPanel.add(grid, BorderLayout.CENTER);
        add(seatPanel, BorderLayout.CENTER);

        // Bottom bar
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(ThemeManager.getInstance().getPanelBackground());

        JLabel flightInfo = new JLabel(
                " " + selectedFlight.getRoute() + " | " +
                        selectedFlight.getDate() + " " + selectedFlight.getDepartureTime(),
                JLabel.CENTER);
        flightInfo.setFont(new Font("Arial", Font.PLAIN, 16));

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);

        RoundedButton backBtn = new RoundedButton("\u2190 Back", new Color(100, 100, 100));
        confirmBtn = new RoundedButton("CONFIRM BOOKING", new Color(0, 153, 76));
        confirmBtn.setEnabled(false);

        backBtn.addActionListener(e -> mainFrame.showPanel("Search"));
        confirmBtn.addActionListener(e -> confirmBooking());

        btnPanel.add(backBtn);
        btnPanel.add(confirmBtn);

        bottom.add(flightInfo, BorderLayout.NORTH);
        bottom.add(btnPanel,   BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private JLabel createLegend(String text, Color color) {
        JLabel lbl = new JLabel(" \u25a0 " + text);
        lbl.setForeground(color);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return lbl;
    }

    private void selectSeat(JButton btn, String seat) {
        for (JButton b : seatButtons) {
            if (b.isEnabled()) b.setBackground(new Color(0, 153, 76));
        }
        btn.setBackground(new Color(255, 215, 0));
        selectedSeat = seat;
        if (confirmBtn != null) confirmBtn.setEnabled(true);
    }

    private void confirmBooking() {
        if (selectedSeat == null) return;

        if (selectedFlight.bookSeat(selectedSeat)) {
            String bookingId = "BK" + (100000 + (int)(Math.random() * 900000));
            Booking booking = new Booking(bookingId, passengerName, selectedFlight, selectedSeat);
            mainFrame.addBooking(booking);
            mainFrame.getSummaryPanel().showSummary(booking);
            mainFrame.showPanel("Summary");
        }
    }

    @Override
    public void onThemeChanged() {
        setBackground(ThemeManager.getInstance().getBackground());
        repaint();
    }
}