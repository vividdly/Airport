// Flight.java
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Flight {

    private final String flightNumber;
    private final String route;
    private final String date;
    private final String departureTime;
    private final String arrivalTime;
    private final double price;
    private final int totalSeats;
    private final Set<String> bookedSeats = new HashSet<>();

    public Flight(String flightNumber, String route, String date,
                  String departureTime, String arrivalTime,
                  double price, int totalSeats) {

        if (flightNumber == null || route == null || date == null ||
                departureTime == null || arrivalTime == null) {
            throw new IllegalArgumentException("Flight fields cannot be null");
        }
        if (totalSeats <= 0) {
            throw new IllegalArgumentException("Total seats must be positive");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        this.flightNumber = flightNumber;
        this.route = route;
        this.date = date;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.totalSeats = totalSeats;
    }

    // Getters
    public String getFlightNumber() { return flightNumber; }
    public String getRoute() { return route; }
    public String getDate() { return date; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public double getPrice() { return price; }
    public int getTotalSeats() { return totalSeats; }
    public int getAvailableSeats() {
        return totalSeats - bookedSeats.size();
    }

    public boolean bookSeat(String seat) {
        if (seat == null || seat.isBlank()) {
            return false;
        }

        String normalizedSeat = seat.trim().toUpperCase();

        if (!normalizedSeat.matches("[1-9][0-9]?[A-F]")) {
            return false;
        }

        if (bookedSeats.contains(normalizedSeat)) {
            return false;
        }

        return bookedSeats.add(normalizedSeat);
    }

    /**
     * FIX: normalize the seat string before removal so that cancelSeat("3a")
     * correctly removes a seat that was booked as "3A".
     */
    public boolean cancelSeat(String seat) {
        if (seat == null || seat.isBlank()) return false;
        return bookedSeats.remove(seat.trim().toUpperCase());
    }

    public Set<String> getBookedSeats() {
        return Collections.unmodifiableSet(new HashSet<>(bookedSeats)); // defensive copy
    }

    @Override
    public String toString() {
        return flightNumber + " | " + route + " | " + date + " " + departureTime;
    }
}