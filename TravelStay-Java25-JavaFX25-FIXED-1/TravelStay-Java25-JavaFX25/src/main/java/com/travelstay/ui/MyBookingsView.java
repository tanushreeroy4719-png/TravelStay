package com.travelstay.ui;

import com.travelstay.dao.BookingDAO;
import com.travelstay.model.HotelBooking;
import com.travelstay.model.RestaurantBooking;
import com.travelstay.model.User;
import com.travelstay.service.BookingService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class MyBookingsView {
    private final Stage stage;
    private final User user;
    private final BorderPane root = new BorderPane();
    private final VBox results = new VBox(12);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    public MyBookingsView(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color:#f5f7fb;");

        Label title = new Label("My Bookings");
        title.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:#16324f;");
        Button back = new Button("← Back");
        back.setOnAction(e -> stage.getScene().setRoot(new HomeView(stage, user).getView()));
        HBox top = new HBox(20, title, new Region(), back);
        HBox.setHgrow(top.getChildren().get(1), Priority.ALWAYS);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(0, 0, 20, 0));

        ScrollPane scroll = new ScrollPane(results);
        scroll.setFitToWidth(true);
        results.setPadding(new Insets(5));

        root.setTop(top);
        root.setCenter(scroll);
        loadBookings();
    }

    private void loadBookings() {
        results.getChildren().clear();
        try {
            var hotelBookings = new BookingDAO().findHotelBookingsByUser(user.userId());
            var restaurantBookings = new BookingDAO().findRestaurantBookingsByUser(user.userId());

            if (hotelBookings.isEmpty() && restaurantBookings.isEmpty()) {
                results.getChildren().add(new Label("You don't have any bookings yet."));
                return;
            }

            if (!hotelBookings.isEmpty()) {
                Label section = new Label("🏨 Hotel Bookings");
                section.setStyle("-fx-font-size:18px;-fx-font-weight:bold;");
                results.getChildren().add(section);
                for (HotelBooking b : hotelBookings) results.getChildren().add(hotelCard(b));
            }
            if (!restaurantBookings.isEmpty()) {
                Label section = new Label("🍽 Restaurant Reservations");
                section.setStyle("-fx-font-size:18px;-fx-font-weight:bold;");
                results.getChildren().add(section);
                for (RestaurantBooking b : restaurantBookings) results.getChildren().add(restaurantCard(b));
            }
        } catch (Exception ex) {
            System.err.println("Failed to load bookings: " + ex);
            results.getChildren().add(new Label("Couldn't load your bookings. Please try again."));
        }
    }

    private VBox hotelCard(HotelBooking b) {
        VBox card = card();
        Label name = new Label("🏨 " + b.hotelName() + " — " + b.roomType());
        name.setStyle("-fx-font-size:18px;-fx-font-weight:bold;");
        Label details = new Label(b.checkIn().format(DATE_FMT) + " → " + b.checkOut().format(DATE_FMT) +
                "  •  " + b.guests() + " guest(s)  •  ₹" + b.totalAmount());
        Label status = statusLabel(b.status());
        HBox row = new HBox(15, details, status);
        row.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().addAll(name, row);
        if (b.status().equals("CONFIRMED")) {
            Button cancel = new Button("Cancel booking");
            cancel.setStyle("-fx-text-fill:#c0392b;");
            cancel.setOnAction(e -> cancelHotel(b));
            card.getChildren().add(cancel);
        }
        return card;
    }

    private VBox restaurantCard(RestaurantBooking b) {
        VBox card = card();
        Label name = new Label("🍽 " + b.restaurantName() + " — Table " + b.tableNumber());
        name.setStyle("-fx-font-size:18px;-fx-font-weight:bold;");
        Label details = new Label(b.date().format(DATE_FMT) + " at " + b.time().format(TIME_FMT) +
                "  •  " + b.guests() + " guest(s)");
        Label status = statusLabel(b.status());
        HBox row = new HBox(15, details, status);
        row.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().addAll(name, row);
        if (b.status().equals("CONFIRMED")) {
            Button cancel = new Button("Cancel reservation");
            cancel.setStyle("-fx-text-fill:#c0392b;");
            cancel.setOnAction(e -> cancelRestaurant(b));
            card.getChildren().add(cancel);
        }
        return card;
    }

    private void cancelHotel(HotelBooking b) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Cancel this hotel booking?");
        confirm.showAndWait().filter(bt -> bt == ButtonType.OK).ifPresent(bt -> {
            try {
                boolean cancelled = new BookingService().cancelHotelBooking(b.bookingId(), user.userId());
                if (!cancelled) new Alert(Alert.AlertType.WARNING, "This booking could not be cancelled.").showAndWait();
                loadBookings();
            } catch (Exception ex) {
                System.err.println("Cancel failed: " + ex);
                new Alert(Alert.AlertType.ERROR, "Something went wrong. Please try again.").showAndWait();
            }
        });
    }

    private void cancelRestaurant(RestaurantBooking b) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Cancel this reservation?");
        confirm.showAndWait().filter(bt -> bt == ButtonType.OK).ifPresent(bt -> {
            try {
                boolean cancelled = new BookingService().cancelRestaurantBooking(b.reservationId(), user.userId());
                if (!cancelled) new Alert(Alert.AlertType.WARNING, "This reservation could not be cancelled.").showAndWait();
                loadBookings();
            } catch (Exception ex) {
                System.err.println("Cancel failed: " + ex);
                new Alert(Alert.AlertType.ERROR, "Something went wrong. Please try again.").showAndWait();
            }
        });
    }

    private Label statusLabel(String status) {
        Label l = new Label(status);
        String color = switch (status) {
            case "CONFIRMED" -> "#1e8449";
            case "CANCELLED" -> "#c0392b";
            default -> "#7f8c8d";
        };
        l.setStyle("-fx-text-fill:" + color + "; -fx-font-weight:bold;");
        return l;
    }

    private VBox card() {
        VBox card = new VBox(6);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#e1e6ed;");
        return card;
    }

    public Parent getView() { return root; }
}
