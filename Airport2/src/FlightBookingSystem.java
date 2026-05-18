// FlightBookingSystem.java
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FlightBookingSystem extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    private final Map<String, Flight> availableFlights = new HashMap<>();
    private final ArrayList<Booking> bookings = new ArrayList<>();
    private final Map<String, String> users = new HashMap<>();
    private String currentUser = null;

    // Panels
    private LoginPanel loginPanel;
    private CreateAccountPanel createAccountPanel;   // NEW
    private HomePanel homePanel;
    private SearchFlightsPanel searchPanel;
    private SeatSelectionPanel seatSelectionPanel;
    private BookingSummaryPanel summaryPanel;
    private MyBookingsPanel bookingsPanel;
    private FlightStatusPanel statusPanel;

    public FlightBookingSystem() {
        setTitle("SkyWing - Modern Flight Booking");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1420, 940);
        setLocationRelativeTo(null);
        setResizable(true);

        initializeData();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        initializePanels();
        addPanelsToMain();

        add(mainPanel);
        showPanel("Login");
        setVisible(true);
    }

    private void initializeData() {
        String[][] flightData = {
                {"FL101", "Manila (MNL) - Cebu (CEB)", "2026-06-01", "08:00", "09:30", "4500"},
                {"FL102", "Manila (MNL) - Davao (DVO)", "2026-06-01", "10:30", "12:15", "5200"},
                {"FL103", "Cebu (CEB) - Clark (CRK)", "2026-06-02", "07:45", "09:05", "3800"},
                {"FL204", "Davao (DVO) - Manila (MNL)", "2026-06-02", "14:00", "15:40", "4900"},
                {"FL205", "Manila (MNL) - Boracay (KLO)", "2026-06-03", "09:15", "10:45", "6200"},
                {"FL306", "Cebu (CEB) - Davao (DVO)", "2026-06-03", "11:30", "13:10", "4700"},
                {"FL307", "Clark (CRK) - Cebu (CEB)", "2026-06-04", "06:30", "07:50", "4100"},
                {"FL408", "Manila (MNL) - Iloilo (ILO)", "2026-06-04", "13:45", "15:05", "5300"},
                {"FL409", "Davao (DVO) - Cebu (CEB)", "2026-06-05", "16:20", "17:50", "4600"},
                {"FL510", "Manila (MNL) - Palawan (PPS)", "2026-06-05", "08:50", "10:40", "6800"}
        };

        for (String[] data : flightData) {
            Flight flight = new Flight(data[0], data[1], data[2], data[3], data[4],
                    Double.parseDouble(data[5]), 24);
            availableFlights.put(data[0], flight);
        }

        users.put("demo", "1234");
        users.put("heroyuki", "1234");
    }

    private void initializePanels() {
        loginPanel         = new LoginPanel(this);
        createAccountPanel = new CreateAccountPanel(this);   // NEW
        homePanel          = new HomePanel(this);
        searchPanel        = new SearchFlightsPanel(this);
        seatSelectionPanel = new SeatSelectionPanel(this);
        summaryPanel       = new BookingSummaryPanel(this);
        bookingsPanel      = new MyBookingsPanel(this);
        statusPanel        = new FlightStatusPanel(this);
    }

    private void addPanelsToMain() {
        mainPanel.add(loginPanel,         "Login");
        mainPanel.add(createAccountPanel, "CreateAccount");   // NEW
        mainPanel.add(homePanel,          "Home");
        mainPanel.add(searchPanel,        "Search");
        mainPanel.add(seatSelectionPanel, "SeatSelection");
        mainPanel.add(summaryPanel,       "Summary");
        mainPanel.add(bookingsPanel,      "Bookings");
        mainPanel.add(statusPanel,        "Status");
    }

    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        if (users.containsKey(username) && users.get(username).equals(password)) {
            currentUser = username;
            return true;
        }
        return false;
    }

    public void register(String username, String password) {
        if (username != null && password != null) {
            users.put(username.trim(), password);
        }
    }

    public void logout() {
        currentUser = null;
        showPanel("Login");
    }

    public Map<String, Flight> getAvailableFlights() {
        return Collections.unmodifiableMap(availableFlights);
    }

    public ArrayList<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }

    public void addBooking(Booking booking) {
        if (booking != null) bookings.add(booking);
    }

    public boolean cancelBooking(Booking booking) {
        return booking != null && bookings.remove(booking);
    }

    public String getCurrentUser() { return currentUser; }

    public SeatSelectionPanel  getSeatSelectionPanel() { return seatSelectionPanel; }
    public BookingSummaryPanel getSummaryPanel()       { return summaryPanel; }
    public MyBookingsPanel     getBookingsPanel()      { return bookingsPanel; }
    public FlightStatusPanel   getStatusPanel()        { return statusPanel; }

    public void showPanel(String panelName) {
        if (panelName == null) return;
        cardLayout.show(mainPanel, panelName);
        // Refresh passenger name whenever the search panel becomes visible
        if ("Search".equals(panelName)) {
            searchPanel.refreshPassengerName();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FlightBookingSystem());
    }
}