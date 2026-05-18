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
        setLayout(new BorderLayout(15, 15));
        buildUI();
    }

    private void buildUI() {
        removeAll();

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeManager.getInstance().getAccentColor());
        header.setPreferredSize(new Dimension(0, 100));

        JLabel title = new JLabel("Flight Status Tracking", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);

        JButton homeBtn = new RoundedButton("Home", ThemeManager.getInstance().getAccentColor());
        homeBtn.addActionListener(e -> mainFrame.showPanel("Home"));
        header.add(homeBtn, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        model = new DefaultListModel<>();
        statusList = new JList<>(model);
        statusList.setFont(new Font("Arial", Font.PLAIN, 16));
        statusList.setCellRenderer(new StatusCellRenderer());

        JScrollPane scroll = new JScrollPane(statusList);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottom.setBackground(ThemeManager.getInstance().getBackground());

        RoundedButton refreshBtn = new RoundedButton("Refresh Status", new Color(0, 153, 76));
        RoundedButton trackBtn = new RoundedButton("View Details", new Color(0, 102, 204));

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
            String delay = status.contains("Delayed") ? " (+25 min)" : "";

            String text = String.format("%s | %s | %s %s | %s",
                    f.getFlightNumber(), f.getRoute(),
                    f.getDate(), f.getDepartureTime(), status + delay);

            model.addElement(text);
        }
    }

    private String getRandomStatus(Random rand) {
        String[] statuses = {"On Time", "On Time", "Boarding", "Delayed", "Departed", "In Air"};
        return statuses[rand.nextInt(statuses.length)];
    }

    private void showFlightDetails() {
        if (statusList.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(this, "Please select a flight first!",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Flight Status Details\n\n" + statusList.getSelectedValue() +
                        "\n\nGate: A-" + (10 + new Random().nextInt(20)) +
                        "\nTerminal: 2\nStatus: Live",
                "Live Flight Information",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void onThemeChanged() {
        setBackground(ThemeManager.getInstance().getBackground());
        repaint();
    }
}

// Custom Renderer - Non-public class (this is fine)
class StatusCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        String text = value.toString();
        if (text.contains("Delayed")) {
            label.setForeground(new Color(220, 53, 69));
        } else if (text.contains("Boarding") || text.contains("In Air")) {
            label.setForeground(new Color(0, 153, 76));
        }
        return label;
    }
}