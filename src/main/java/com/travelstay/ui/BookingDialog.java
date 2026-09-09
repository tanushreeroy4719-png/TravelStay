package com.travelstay.ui;

import com.travelstay.dao.RoomDAO;
import com.travelstay.model.Hotel;
import com.travelstay.model.Room;
import com.travelstay.model.User;
import com.travelstay.service.BookingService;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Modal dialog: pick a room + dates + guests, then create a hotel booking. */
public final class BookingDialog {

    private BookingDialog() {}

    public static void open(Window owner, Hotel hotel, User user, Runnable onBooked) {
        List<Room> rooms;
        try {
            rooms = new RoomDAO().findByHotel(hotel.hotelId());
        } catch (Exception ex) {
            System.err.println("Failed to load rooms: " + ex);
            new Alert(Alert.AlertType.ERROR, "Couldn't load rooms for this hotel. Please try again.").showAndWait();
            return;
        }
        if (rooms.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No rooms are configured for this hotel yet.").showAndWait();
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Book a room — " + hotel.name());
        dialog.setHeaderText(hotel.name() + ", " + hotel.city());

        ButtonType bookButtonType = new ButtonType("Book", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(bookButtonType, ButtonType.CANCEL);

        ComboBox<Room> roomBox = new ComboBox<>();
        roomBox.getItems().addAll(rooms);
        roomBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Room r) {
                return r == null ? "" : r.roomType() + " — ₹" + r.pricePerNight() + "/night (up to " + r.capacity() + " guests)";
            }
            @Override public Room fromString(String s) { return null; }
        });
        roomBox.getSelectionModel().selectFirst();

        DatePicker checkIn = new DatePicker(LocalDate.now().plusDays(1));
        DatePicker checkOut = new DatePicker(LocalDate.now().plusDays(2));
        Spinner<Integer> guests = new Spinner<>(1, 10, 1);
        guests.setEditable(true);

        Label totalLabel = new Label();
        totalLabel.setStyle("-fx-font-weight:bold;");

        Runnable updateTotal = () -> {
            Room r = roomBox.getValue();
            LocalDate in = checkIn.getValue();
            LocalDate out = checkOut.getValue();
            if (r == null || in == null || out == null || !out.isAfter(in)) {
                totalLabel.setText("Select valid dates to see total.");
                return;
            }
            long nights = ChronoUnit.DAYS.between(in, out);
            totalLabel.setText("Total: ₹" + (nights * r.pricePerNight()) + " for " + nights + " night(s)");
        };
        roomBox.valueProperty().addListener((o, a, b) -> updateTotal.run());
        checkIn.valueProperty().addListener((o, a, b) -> updateTotal.run());
        checkOut.valueProperty().addListener((o, a, b) -> updateTotal.run());
        updateTotal.run();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Room:"), roomBox);
        grid.addRow(1, new Label("Check-in:"), checkIn);
        grid.addRow(2, new Label("Check-out:"), checkOut);
        grid.addRow(3, new Label("Guests:"), guests);
        grid.add(totalLabel, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Use an event filter (rather than the result converter) so that on invalid
        // input we can consume the event and keep the dialog open for correction,
        // instead of it silently closing.
        Button bookButton = (Button) dialog.getDialogPane().lookupButton(bookButtonType);
        bookButton.addEventFilter(ActionEvent.ACTION, event -> {
            Room room = roomBox.getValue();
            LocalDate in = checkIn.getValue();
            LocalDate out = checkOut.getValue();
            int guestCount = guests.getValue();

            if (room == null || in == null || out == null) {
                new Alert(Alert.AlertType.WARNING, "Please choose a room and both dates.").showAndWait();
                event.consume();
                return;
            }
            if (!out.isAfter(in)) {
                new Alert(Alert.AlertType.WARNING, "Check-out must be after check-in.").showAndWait();
                event.consume();
                return;
            }
            if (guestCount > room.capacity()) {
                new Alert(Alert.AlertType.WARNING, "This room fits up to " + room.capacity() + " guests.").showAndWait();
                event.consume();
                return;
            }
            long nights = ChronoUnit.DAYS.between(in, out);
            double total = nights * room.pricePerNight();
            try {
                int bookingId = new BookingService().bookRoom(user.userId(), room.roomId(), in, out, guestCount, total);
                new Alert(Alert.AlertType.INFORMATION,
                        "Booking confirmed! Reference #" + bookingId + "\nTotal: ₹" + total).showAndWait();
                if (onBooked != null) onBooked.run();
                // No event.consume() here: let the dialog close normally.
            } catch (IllegalArgumentException | IllegalStateException ex) {
                // Friendly, user-facing validation/availability message. Keep the dialog open.
                new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
                event.consume();
            } catch (Exception ex) {
                System.err.println("Booking failed: " + ex);
                new Alert(Alert.AlertType.ERROR, "Something went wrong while booking. Please try again.").showAndWait();
                event.consume();
            }
        });

        dialog.showAndWait();
    }
}
