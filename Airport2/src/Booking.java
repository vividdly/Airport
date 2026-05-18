// Booking.java
public class Booking {

    private final String bookingId;
    private final String passengerName;
    private final Flight flight;
    private final String seatNumber;

    public Booking(String bookingId, String passengerName, Flight flight, String seatNumber) {
        this.bookingId = bookingId;
        this.passengerName = passengerName;
        this.flight = flight;
        this.seatNumber = seatNumber;
    }

    public String getBookingId() { return bookingId; }
    public String getPassengerName() { return passengerName; }
    public Flight getFlight() { return flight; }
    public String getSeatNumber() { return seatNumber; }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s | Seat %s | PHP %,.0f",
                bookingId, passengerName, flight.getRoute(), seatNumber, flight.getPrice());
    }
}