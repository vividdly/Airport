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
        setLayout(new BorderLayout(0, 0));
        buildUI();
    }

    private void buildUI() {
        removeAll();

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SkyWingUI.ACCENT);
        header.setPreferredSize(new Dimension(0, SkyWingUI.HEADER_HEIGHT));
        header.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        RoundedButton homeBtn = new RoundedButton("← Home", SkyWingUI.ACCENT);
        homeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        homeBtn.addActionListener(e -> mainFrame.showPanel("Home"));
        header.add(homeBtn, BorderLayout.WEST);

        JLabel title = new JLabel("My Bookings", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);

        JPanel spacer = new JPanel(); spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(160, 0));
        header.add(spacer, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // List
        model = new DefaultListModel<>();
        list = new JList<>(model);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(64);
        list.setCellRenderer(new BookingCellRenderer());
        list.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Bottom bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 16));
        bottom.setBackground(SkyWingUI.PANEL_BG);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SkyWingUI.CARD_BORDER));

        RoundedButton refreshBtn = new RoundedButton("Refresh", new Color(0, 153, 76));
        RoundedButton cancelBtn  = new RoundedButton("Cancel Booking", new Color(220, 53, 69));
        RoundedButton logoutBtn  = new RoundedButton("Logout", new Color(255, 140, 0));

        for (RoundedButton b : new RoundedButton[]{refreshBtn, cancelBtn, logoutBtn}) {
            b.setPreferredSize(new Dimension(180, 46));
            b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        }

        refreshBtn.addActionListener(e -> refreshList());
        cancelBtn.addActionListener(e -> cancelSelectedBooking());
        logoutBtn.addActionListener(e -> mainFrame.logout());

        bottom.add(refreshBtn);
        bottom.add(cancelBtn);
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
            model.addElement("✈  No bookings yet — your future trips will appear here.");
        }
    }

    private void cancelSelectedBooking() {
        int index = list.getSelectedIndex();
        if (index == -1) {
            SkyWingUI.showWarning(this, "No Selection", "Please select a booking to cancel.");
            return;
        }
        String selected = model.getElementAt(index);
        if (selected.startsWith("✈")) return;

        boolean confirmed = SkyWingUI.showConfirm(this, "Cancel Booking",
                "Are you sure you want to cancel this booking?\n\n" + selected);

        if (confirmed) {
            Booking toCancel = null;
            for (Booking b : mainFrame.getBookings()) {
                if (b.toString().equals(selected)) { toCancel = b; break; }
            }
            if (toCancel != null) {
                toCancel.getFlight().cancelSeat(toCancel.getSeatNumber());
                mainFrame.cancelBooking(toCancel);
                SkyWingUI.showInfo(this, "Cancelled", "Booking cancelled successfully.");
                refreshList();
            }
        }
    }

    @Override public void onThemeChanged() { buildUI(); }

    // Custom cell renderer
    private static class BookingCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 235, 245)),
                    BorderFactory.createEmptyBorder(10, 16, 10, 16)));
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            if (isSelected) {
                lbl.setBackground(new Color(230, 242, 255));
                lbl.setForeground(SkyWingUI.ACCENT);
            } else {
                lbl.setBackground(index % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                lbl.setForeground(SkyWingUI.DARK);
            }
            return lbl;
        }
    }
}