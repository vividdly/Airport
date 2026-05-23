// FlightStatusPanel.java
import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class FlightStatusPanel extends JPanel implements ThemeManager.ThemeListener {
    private FlightBookingSystem mainFrame;
    private DefaultListModel<String> model;
    private JList<String> statusList;

    public FlightStatusPanel(FlightBookingSystem mainFrame) {
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

        JLabel title = new JLabel("Flight Status Tracking", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);

        JPanel spacer = new JPanel(); spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(160, 0));
        header.add(spacer, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // List
        model = new DefaultListModel<>();
        statusList = new JList<>(model);
        statusList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusList.setFixedCellHeight(62);
        statusList.setCellRenderer(new StatusCellRenderer());
        statusList.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JScrollPane scroll = new JScrollPane(statusList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Bottom
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 16));
        bottom.setBackground(SkyWingUI.PANEL_BG);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SkyWingUI.CARD_BORDER));

        RoundedButton refreshBtn = new RoundedButton("Refresh Status", new Color(0, 153, 76));
        RoundedButton trackBtn   = new RoundedButton("View Details",   new Color(0, 102, 204));
        refreshBtn.setPreferredSize(new Dimension(190, 46));
        trackBtn.setPreferredSize(new Dimension(190, 46));
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        trackBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        refreshBtn.addActionListener(e -> loadFlightStatuses());
        trackBtn.addActionListener(e -> showFlightDetails());

        bottom.add(refreshBtn);
        bottom.add(trackBtn);
        add(bottom, BorderLayout.SOUTH);

        loadFlightStatuses();
    }

    private void loadFlightStatuses() {
        model.clear();
        Random rand = new Random();
        for (Flight f : mainFrame.getAvailableFlights().values()) {
            String status = getRandomStatus(rand);
            String delay  = status.contains("Delayed") ? " (+25 min)" : "";
            model.addElement(String.format("%s  |  %s  |  %s %s  |  %s",
                    f.getFlightNumber(), f.getRoute(),
                    f.getDate(), f.getDepartureTime(), status + delay));
        }
    }

    private String getRandomStatus(Random rand) {
        String[] statuses = {"On Time", "On Time", "Boarding", "Delayed", "Departed", "In Air"};
        return statuses[rand.nextInt(statuses.length)];
    }

    private void showFlightDetails() {
        if (statusList.getSelectedValue() == null) {
            SkyWingUI.showWarning(this, "No Selection", "Please select a flight first.");
            return;
        }
        String info = statusList.getSelectedValue()
                + "\n\nGate: A-" + (10 + new Random().nextInt(20))
                + "\nTerminal: 2\nStatus: Live";
        SkyWingUI.showInfo(this, "Live Flight Information", info);
    }

    @Override public void onThemeChanged() {
        setBackground(SkyWingUI.PANEL_BG);
        repaint();
    }
}

class StatusCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 235, 245)),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        if (!isSelected) {
            String text = value.toString();
            if (text.contains("Delayed")) {
                label.setForeground(new Color(200, 40, 55));
                label.setBackground(new Color(255, 245, 246));
            } else if (text.contains("Boarding") || text.contains("In Air")) {
                label.setForeground(new Color(0, 140, 60));
                label.setBackground(new Color(240, 255, 247));
            } else {
                label.setBackground(index % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                label.setForeground(SkyWingUI.DARK);
            }
        } else {
            label.setBackground(new Color(230, 242, 255));
            label.setForeground(SkyWingUI.ACCENT);
        }
        return label;
    }
}