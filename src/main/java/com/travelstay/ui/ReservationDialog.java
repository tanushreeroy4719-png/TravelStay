package com.travelstay.ui;

import com.travelstay.dao.TableDAO;
import com.travelstay.model.Restaurant;
import com.travelstay.model.RestaurantTable;
import com.travelstay.model.User;
import com.travelstay.service.BookingService;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Modal dialog: pick a table + date/time + guests, then create a restaurant reservation. */
public final class ReservationDialog {

    private static final LocalTime[] TIME_SLOTS = buildTimeSlots();

    private ReservationDialog() {}

    public static void open(Window owner, Restaurant restaurant, User user, Runnable onReserved) {
        List<RestaurantTable> tables;
        try {
            tables = new TableDAO().findByRestaurant(restaurant.restaurantId());
        } catch (Exception ex) {
            System.err.println("Failed to load tables: " + ex);
            new Alert(Alert.AlertType.ERROR, "Couldn't load tables for this restaurant. Please try again.").showAndWait();
            return;
        }
        if (tables.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No tables are configured for this restaurant yet.").showAndWait();
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Reserve a table — " + restaurant.name());
        dialog.setHeaderText(restaurant.name() + ", " + restaurant.city());

        ButtonType reserveButtonType = new ButtonType("Reserve", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reserveButtonType, ButtonType.CANCEL);

        ComboBox<RestaurantTable> tableBox = new ComboBox<>();
        tableBox.getItems().addAll(tables);
        tableBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(RestaurantTable t) {
                return t == null ? "" : "Table " + t.tableNumber() + " (seats " + t.seats() + ")";
            }
            @Override public RestaurantTable fromString(String s) { return null; }
        });
        tableBox.getSelectionModel().selectFirst();

        DatePicker date = new DatePicker(LocalDate.now());
        ComboBox<LocalTime> timeBox = new ComboBox<>();
        timeBox.getItems().addAll(TIME_SLOTS);
        timeBox.getSelectionModel().select(0);
        Spinner<Integer> guests = new Spinner<>(1, 12, 2);
        guests.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Table:"), tableBox);
        grid.addRow(1, new Label("Date:"), date);
        grid.addRow(2, new Label("Time:"), timeBox);
        grid.addRow(3, new Label("Guests:"), guests);

        dialog.getDialogPane().setContent(grid);

        Button reserveButton = (Button) dialog.getDialogPane().lookupButton(reserveButtonType);
        reserveButton.addEventFilter(ActionEvent.ACTION, event -> {
            RestaurantTable table = tableBox.getValue();
            LocalDate d = date.getValue();
            LocalTime t = timeBox.getValue();
            int guestCount = guests.getValue();

            if (table == null || d == null || t == null) {
                new Alert(Alert.AlertType.WARNING, "Please choose a table, date and time.").showAndWait();
                event.consume();
                return;
            }
            if (d.isBefore(LocalDate.now())) {
                new Alert(Alert.AlertType.WARNING, "Please choose today or a future date.").showAndWait();
                event.consume();
                return;
            }
            if (guestCount > table.seats()) {
                new Alert(Alert.AlertType.WARNING, "This table seats up to " + table.seats() + " guests.").showAndWait();
                event.consume();
                return;
            }
            try {
                int reservationId = new BookingService().reserveTable(
                        user.userId(), table.tableId(), d, Time.valueOf(t), guestCount);
                new Alert(Alert.AlertType.INFORMATION,
                        "Reservation confirmed! Reference #" + reservationId).showAndWait();
                if (onReserved != null) onReserved.run();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
                event.consume();
            } catch (Exception ex) {
                System.err.println("Reservation failed: " + ex);
                new Alert(Alert.AlertType.ERROR, "Something went wrong while reserving. Please try again.").showAndWait();
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private static LocalTime[] buildTimeSlots() {
        LocalTime[] slots = new LocalTime[24]; // 11:00 to 22:30 in 30-min steps
        LocalTime t = LocalTime.of(11, 0);
        for (int i = 0; i < slots.length; i++) {
            slots[i] = t;
            t = t.plusMinutes(30);
        }
        return slots;
    }
}
