// MyBookingsPanel.java
import javax.swing.*;
import java.awt.*;

public class MyBookingsPanel extends JPanel implements ThemeManager.ThemeListener {
    private FlightBookingSystem mainFrame;
    private DefaultListModel<String> model;
    private JList<String> list;

    public MyBookingsPanel(FlightBookingSystem mainFrame) {
        this.mainFrame = mainFrame;
        ThemeManager.getInstance().addListener(this);
        setLayout(new BorderLayout(15, 15));
        buildUI();
    }

    private void buildUI() {
        removeAll();

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeManager.getInstance().getAccentColor());
        header.setPreferredSize(new Dimension(0, 100));

        JLabel title = new JLabel("My Bookings", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        model = new DefaultListModel<>();
        list = new JList<>(model);
        list.setFont(new Font("Arial", Font.PLAIN, 16));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottom.setBackground(ThemeManager.getInstance().getBackground());

        RoundedButton refreshBtn = new RoundedButton("Refresh", new Color(0, 153, 76));
        RoundedButton cancelBtn = new RoundedButton("Cancel Selected", new Color(220, 53, 69));
        RoundedButton homeBtn = new RoundedButton("Home", new Color(0, 102, 204));
        RoundedButton logoutBtn = new RoundedButton("Logout", new Color(255, 140, 0));

        refreshBtn.addActionListener(e -> refreshList());
        cancelBtn.addActionListener(e -> cancelSelectedBooking());
        homeBtn.addActionListener(e -> mainFrame.showPanel("Home"));
        logoutBtn.addActionListener(e -> mainFrame.logout());

        bottom.add(refreshBtn);
        bottom.add(cancelBtn);
        bottom.add(homeBtn);
        bottom.add(logoutBtn);

        add(bottom, BorderLayout.SOUTH);

        refreshList();
    }

    public void refreshList() {
        model.clear();
        for (Booking b : mainFrame.getBookings()) {
            model.addElement(b.toString());
        }
        if (model.isEmpty()) {
            model.addElement("No bookings yet. Your future trips will appear here.");
        }
    }

    private void cancelSelectedBooking() {
        int index = list.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selected = model.getElementAt(index);

        // Guard: nothing to cancel when the placeholder message is shown
        if (selected.startsWith("No bookings")) return;

        int choice = JOptionPane.showConfirmDialog(this, "Cancel this booking?\n\n" + selected,
                "Confirm Cancel", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            Booking toCancel = null;
            for (Booking b : mainFrame.getBookings()) {
                if (b.toString().equals(selected)) {
                    toCancel = b;
                    break;
                }
            }
            if (toCancel != null) {
                // FIX: cancelSeat on the flight AND remove from the real booking list
                // via mainFrame.cancelBooking() instead of the defensive-copy list.
                toCancel.getFlight().cancelSeat(toCancel.getSeatNumber());
                mainFrame.cancelBooking(toCancel);
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshList();
            }
        }
    }

    @Override
    public void onThemeChanged() {
        buildUI();
    }
}