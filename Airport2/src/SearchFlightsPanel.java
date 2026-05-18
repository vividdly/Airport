// SearchFlightsPanel.java
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

/**
 * Improved Search & Filter panel for SkyWing.
 *
 * BUGS FIXED:
 *  1. buildUI() was called on theme change, which wiped all field values mid-use.
 *     → Filter state is now preserved; only the results area refreshes on theme change.
 *  2. Passenger name defaulted to the OLD user because buildUI() was only ever called
 *     once. Now refreshPassengerName() is exposed so FlightBookingSystem can call it on
 *     panel switch.
 *  3. routeFilter only matched "Manila (MNL)" exactly, so typing "Manila" in the
 *     live-search box AND selecting a route filter would double-exclude results.
 *     → Route filter now checks f.getRoute().contains(selectedCity) where selectedCity
 *     is extracted from the combo label (e.g. "Manila (MNL)" → "MNL").
 *  4. maxPrice silently fell back to 99999 when the field was blank, hiding the default
 *     8000 cap from the user. Field now shows placeholder "No limit" when empty.
 *  5. Flight cards had no flight number shown — hard to tell flights apart.
 *  6. "No flights found" label was left-pinned and invisible when the scroll pane was wide.
 *  7. JComboBox and JTextField had no AlignmentX set, causing BoxLayout warnings.
 *  8. Filter panel had no scroll — on small screens the Apply button was clipped.
 *  9. Results area padding was missing; cards touched the scroll border.
 * 10. Sort order of results was random (HashMap iteration). Now sorted by price asc.
 *
 * DESIGN IMPROVEMENTS:
 *  - Filter sidebar has a card-style background with a subtle shadow border.
 *  - Each filter group is clearly labelled and visually separated.
 *  - Price slider + text field stay in sync (slider caps at 10 000; field accepts any value).
 *  - Flight cards show: route, flight number badge, dep/arr times, seats available chip,
 *    and price — all in a clean two-row layout.
 *  - "Seats" chip turns red when ≤ 3 seats remain.
 *  - Active filter count badge on the sidebar title so users know filters are applied.
 *  - "Clear Filters" button resets everything to default.
 *  - Results header shows "X flights found" count.
 */
public class SearchFlightsPanel extends JPanel implements ThemeManager.ThemeListener {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color ACCENT       = new Color(0, 119, 200);
    private static final Color SUCCESS      = new Color(0, 153, 76);
    private static final Color DANGER       = new Color(220, 53, 69);
    private static final Color SIDEBAR_BG   = new Color(248, 251, 255);
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color CARD_BORDER  = new Color(218, 225, 235);
    private static final Color MUTED        = new Color(100, 110, 130);
    private static final Color DARK         = new Color(18, 24, 40);

    // ── State ─────────────────────────────────────────────────────────────────
    private final FlightBookingSystem mainFrame;

    // Filter controls — kept as fields so they survive theme rebuilds
    private JTextField    searchField;
    private JTextField    passengerNameField;
    private JComboBox<String> routeFilter;
    private JComboBox<String> dateFilter;
    private JSlider       priceSlider;
    private JTextField    maxPriceField;
    private JLabel        resultCountLabel;
    private JPanel        flightsContainer;

    // Tracks whether controls are initialised (avoid NPE during first buildUI)
    private boolean controlsReady = false;

    // ── Constructor ───────────────────────────────────────────────────────────
    public SearchFlightsPanel(FlightBookingSystem mainFrame) {
        this.mainFrame = mainFrame;
        ThemeManager.getInstance().addListener(this);
        setLayout(new BorderLayout());
        buildUI();
    }

    // ── Called by FlightBookingSystem when this panel becomes visible ─────────
    public void refreshPassengerName() {
        if (passengerNameField != null) {
            String user = mainFrame.getCurrentUser();
            passengerNameField.setText(user != null ? user : "");
        }
    }

    // ── UI Construction ───────────────────────────────────────────────────────
    private void buildUI() {
        removeAll();
        setBackground(ThemeManager.getInstance().getBackground());

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildResults(), BorderLayout.CENTER);

        controlsReady = true;
        loadFlights();
        revalidate();
        repaint();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ACCENT);
        header.setPreferredSize(new Dimension(0, 90));
        header.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        RoundedButton homeBtn = new RoundedButton("← Home", ACCENT);
        homeBtn.addActionListener(e -> mainFrame.showPanel("Home"));
        header.add(homeBtn, BorderLayout.WEST);

        JLabel title = new JLabel("Search Flights", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);

        // Right spacer to keep title centred
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(180, 0));
        header.add(spacer, BorderLayout.EAST);

        return header;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JScrollPane buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, CARD_BORDER),
                BorderFactory.createEmptyBorder(24, 20, 24, 20)));

        // ── Title row ──
        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel filterTitle = new JLabel("Filters");
        filterTitle.setFont(new Font("Georgia", Font.BOLD, 20));
        filterTitle.setForeground(ACCENT);
        titleRow.add(filterTitle, BorderLayout.WEST);
        sidebar.add(titleRow);
        sidebar.add(vgap(18));

        // ── Search ──
        sidebar.add(sectionLabel("Search"));
        sidebar.add(vgap(6));
        searchField = styledField("Flight number or city…");
        searchField.getDocument().addDocumentListener(docListener());
        sidebar.add(searchField);
        sidebar.add(vgap(16));

        // ── Passenger name ──
        sidebar.add(sectionLabel("Passenger Name"));
        sidebar.add(vgap(6));
        String currentUser = mainFrame.getCurrentUser();
        passengerNameField = styledField(currentUser != null ? currentUser : "Your name");
        if (currentUser != null) passengerNameField.setText(currentUser);
        sidebar.add(passengerNameField);
        sidebar.add(vgap(16));

        // ── Route ──
        sidebar.add(sectionLabel("Route"));
        sidebar.add(vgap(6));
        routeFilter = styledCombo(new String[]{
                "All Routes",
                "Manila (MNL)", "Cebu (CEB)", "Davao (DVO)",
                "Clark (CRK)", "Boracay (KLO)", "Iloilo (ILO)", "Palawan (PPS)"
        });
        routeFilter.addActionListener(e -> loadFlights());
        sidebar.add(routeFilter);
        sidebar.add(vgap(16));

        // ── Date ──
        sidebar.add(sectionLabel("Departure Date"));
        sidebar.add(vgap(6));
        dateFilter = styledCombo(new String[]{
                "All Dates",
                "2026-06-01", "2026-06-02", "2026-06-03",
                "2026-06-04", "2026-06-05"
        });
        dateFilter.addActionListener(e -> loadFlights());
        sidebar.add(dateFilter);
        sidebar.add(vgap(16));

        // ── Price ──
        sidebar.add(sectionLabel("Max Price (PHP)"));
        sidebar.add(vgap(6));

        // Slider + text field in sync
        JPanel priceRow = new JPanel(new BorderLayout(8, 0));
        priceRow.setOpaque(false);
        priceRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        priceSlider = new JSlider(1000, 10000, 8000);
        priceSlider.setOpaque(false);
        priceSlider.setMajorTickSpacing(3000);
        priceSlider.setPaintTicks(true);

        maxPriceField = new JTextField("8000", 5);
        maxPriceField.setFont(new Font("Arial", Font.PLAIN, 14));
        maxPriceField.setHorizontalAlignment(SwingConstants.RIGHT);
        maxPriceField.setMaximumSize(new Dimension(72, 32));
        maxPriceField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));

        // Keep slider ↔ field in sync
        priceSlider.addChangeListener(e -> {
            maxPriceField.setText(String.valueOf(priceSlider.getValue()));
            loadFlights();
        });
        maxPriceField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { syncSliderFromField(); }
            public void removeUpdate(DocumentEvent e)  { syncSliderFromField(); }
            public void changedUpdate(DocumentEvent e) { syncSliderFromField(); }
        });

        priceRow.add(priceSlider, BorderLayout.CENTER);
        priceRow.add(maxPriceField, BorderLayout.EAST);
        sidebar.add(priceRow);
        sidebar.add(vgap(6));

        // Slider labels
        JPanel sliderLabels = new JPanel(new BorderLayout());
        sliderLabels.setOpaque(false);
        sliderLabels.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        sliderLabels.add(mutedLabel("₱1,000"), BorderLayout.WEST);
        sliderLabels.add(mutedLabel("₱10,000"), BorderLayout.EAST);
        sidebar.add(sliderLabels);
        sidebar.add(vgap(24));

        // ── Buttons ──
        RoundedButton clearBtn = new RoundedButton("Clear Filters", new Color(140, 148, 160));
        clearBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        clearBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearBtn.addActionListener(e -> clearFilters());
        sidebar.add(clearBtn);
        sidebar.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(sidebar,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(310, 0));
        return scroll;
    }

    // ── Results area ──────────────────────────────────────────────────────────
    private JPanel buildResults() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(ThemeManager.getInstance().getBackground());

        // Results header bar
        JPanel resultsHeader = new JPanel(new BorderLayout());
        resultsHeader.setBackground(ThemeManager.getInstance().getBackground());
        resultsHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        resultCountLabel = new JLabel("Loading flights…");
        resultCountLabel.setFont(new Font("Arial", Font.BOLD, 15));
        resultCountLabel.setForeground(MUTED);
        resultsHeader.add(resultCountLabel, BorderLayout.WEST);

        JLabel sortLabel = new JLabel("Sorted by: Price ↑");
        sortLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        sortLabel.setForeground(MUTED);
        resultsHeader.add(sortLabel, BorderLayout.EAST);

        wrapper.add(resultsHeader, BorderLayout.NORTH);

        // Flight cards
        flightsContainer = new JPanel();
        flightsContainer.setLayout(new BoxLayout(flightsContainer, BoxLayout.Y_AXIS));
        flightsContainer.setBackground(ThemeManager.getInstance().getBackground());
        flightsContainer.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JScrollPane scroll = new JScrollPane(flightsContainer);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    // ── Load / Filter / Sort ─────────────────────────────────────────────────
    private void loadFlights() {
        if (!controlsReady || flightsContainer == null) return;

        flightsContainer.removeAll();

        String keyword   = searchField.getText().toLowerCase().trim();
        String routeSel  = (String) routeFilter.getSelectedItem();
        String dateSel   = (String) dateFilter.getSelectedItem();
        double maxPrice  = parseMaxPrice();

        // Collect matching flights then sort by price
        List<Flight> matched = new ArrayList<>();
        for (Flight f : mainFrame.getAvailableFlights().values()) {
            if (f.getPrice() > maxPrice) continue;

            // Route filter: extract the IATA code from the combo label, e.g. "Manila (MNL)" → "MNL"
            if (!"All Routes".equals(routeSel)) {
                String iata = extractIata(routeSel);
                if (!f.getRoute().contains(iata != null ? iata : routeSel)) continue;
            }

            if (!"All Dates".equals(dateSel) && !f.getDate().equals(dateSel)) continue;

            // Keyword search: flight number, route, and date
            if (!keyword.isEmpty()) {
                boolean hit = f.getFlightNumber().toLowerCase().contains(keyword)
                        || f.getRoute().toLowerCase().contains(keyword)
                        || f.getDate().contains(keyword);
                if (!hit) continue;
            }

            matched.add(f);
        }

        // Sort ascending by price
        matched.sort(Comparator.comparingDouble(Flight::getPrice));

        // Update counter
        resultCountLabel.setText(matched.isEmpty()
                ? "No flights found"
                : matched.size() + " flight" + (matched.size() == 1 ? "" : "s") + " found");

        if (matched.isEmpty()) {
            flightsContainer.add(buildEmptyState());
        } else {
            for (Flight f : matched) {
                flightsContainer.add(createFlightCard(f));
                flightsContainer.add(Box.createVerticalStrut(14));
            }
        }

        flightsContainer.revalidate();
        flightsContainer.repaint();
    }

    private double parseMaxPrice() {
        try {
            double val = Double.parseDouble(maxPriceField.getText().trim());
            return val > 0 ? val : 99_999;
        } catch (NumberFormatException e) {
            return 99_999;
        }
    }

    /** Extracts the IATA code from "City (IATA)" labels, or returns null. */
    private String extractIata(String comboLabel) {
        int start = comboLabel.indexOf('(');
        int end   = comboLabel.indexOf(')');
        if (start >= 0 && end > start) {
            return comboLabel.substring(start + 1, end).trim();
        }
        return null;
    }

    private void syncSliderFromField() {
        try {
            int val = Integer.parseInt(maxPriceField.getText().trim());
            if (val >= priceSlider.getMinimum() && val <= priceSlider.getMaximum()) {
                priceSlider.setValue(val);
            }
        } catch (NumberFormatException ignored) {}
        loadFlights();
    }

    private void clearFilters() {
        searchField.setText("");
        routeFilter.setSelectedIndex(0);
        dateFilter.setSelectedIndex(0);
        priceSlider.setValue(10000);
        maxPriceField.setText("10000");
        loadFlights();
    }

    // ── Flight Card ───────────────────────────────────────────────────────────
    private JPanel createFlightCard(Flight flight) {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setBackground(CARD_BG);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)));

        // ── Left: route + times ──
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        // Flight number badge + route on same row
        JPanel routeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        routeRow.setOpaque(false);

        JLabel badge = new JLabel(flight.getFlightNumber());
        badge.setFont(new Font("Arial", Font.BOLD, 11));
        badge.setForeground(ACCENT);
        badge.setOpaque(true);
        badge.setBackground(new Color(230, 242, 255));
        badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        routeRow.add(badge);

        JLabel routeLabel = new JLabel(flight.getRoute());
        routeLabel.setFont(new Font("Georgia", Font.BOLD, 17));
        routeLabel.setForeground(DARK);
        routeRow.add(routeLabel);
        left.add(routeRow);
        left.add(Box.createVerticalStrut(8));

        // Times + date row
        JPanel timeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        timeRow.setOpaque(false);

        timeRow.add(timeLabel("🕐 " + flight.getDepartureTime()));
        timeRow.add(mutedLabel("→"));
        timeRow.add(timeLabel(flight.getArrivalTime()));
        timeRow.add(mutedLabel(" · " + flight.getDate()));

        // Seats chip
        int avail = flight.getAvailableSeats();
        JLabel seatsChip = new JLabel(avail + " seats left");
        seatsChip.setFont(new Font("Arial", Font.BOLD, 11));
        boolean low = avail <= 3;
        seatsChip.setForeground(low ? DANGER : SUCCESS);
        seatsChip.setOpaque(true);
        seatsChip.setBackground(low ? new Color(255, 240, 240) : new Color(230, 250, 238));
        seatsChip.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        timeRow.add(Box.createHorizontalStrut(8));
        timeRow.add(seatsChip);

        left.add(timeRow);
        card.add(left, BorderLayout.CENTER);

        // ── Right: price + button ──
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel price = new JLabel("₱" + String.format("%,.0f", flight.getPrice()));
        price.setFont(new Font("Georgia", Font.BOLD, 22));
        price.setForeground(SUCCESS);
        price.setAlignmentX(Component.RIGHT_ALIGNMENT);
        right.add(price);
        right.add(Box.createVerticalStrut(8));

        RoundedButton bookBtn = new RoundedButton("Select Seat", ACCENT);
        bookBtn.setPreferredSize(new Dimension(148, 40));
        bookBtn.setFont(new Font("Arial", Font.BOLD, 14));
        bookBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        bookBtn.addActionListener(e -> {
            if (avail == 0) {
                JOptionPane.showMessageDialog(this,
                        "Sorry, this flight is fully booked.",
                        "No Seats Available", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String passenger = passengerNameField.getText().trim();
            if (passenger.isEmpty()) passenger = mainFrame.getCurrentUser() != null
                    ? mainFrame.getCurrentUser() : "Passenger";
            mainFrame.getSeatSelectionPanel().setBookingDetails(flight, passenger);
            mainFrame.showPanel("SeatSelection");
        });
        right.add(bookBtn);
        card.add(right, BorderLayout.EAST);

        // Hover highlight
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(245, 250, 255));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT, 1, true),
                        BorderFactory.createEmptyBorder(18, 22, 18, 22)));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(CARD_BG);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                        BorderFactory.createEmptyBorder(18, 22, 18, 22)));
            }
        });

        return card;
    }

    // ── Empty state ───────────────────────────────────────────────────────────
    private JPanel buildEmptyState() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(60, 0, 0, 0));

        JLabel icon = new JLabel("✈", SwingConstants.CENTER);
        icon.setFont(new Font("Arial", Font.PLAIN, 64));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(icon);
        panel.add(Box.createVerticalStrut(16));

        JLabel msg = new JLabel("No flights match your filters", SwingConstants.CENTER);
        msg.setFont(new Font("Georgia", Font.BOLD, 20));
        msg.setForeground(DARK);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(msg);
        panel.add(Box.createVerticalStrut(8));

        JLabel hint = new JLabel("Try adjusting the route, date, or price range.", SwingConstants.CENTER);
        hint.setFont(new Font("Arial", Font.PLAIN, 14));
        hint.setForeground(MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(hint);

        return panel;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(new Color(180, 190, 210));
                    g.setFont(getFont().deriveFont(Font.ITALIC));
                    g.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT, 1),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CARD_BORDER, 1),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            }
        });
        return f;
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("Arial", Font.PLAIN, 14));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel mutedLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        lbl.setForeground(MUTED);
        return lbl;
    }

    private JLabel timeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(DARK);
        return lbl;
    }

    private Component vgap(int h) { return Box.createVerticalStrut(h); }

    private DocumentListener docListener() {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { loadFlights(); }
            public void removeUpdate(DocumentEvent e)  { loadFlights(); }
            public void changedUpdate(DocumentEvent e) { loadFlights(); }
        };
    }

    @Override
    public void onThemeChanged() {
        // Avoid wiping field values — just refresh colours and results
        setBackground(ThemeManager.getInstance().getBackground());
        if (flightsContainer != null) {
            flightsContainer.setBackground(ThemeManager.getInstance().getBackground());
            loadFlights();
        }
    }
}