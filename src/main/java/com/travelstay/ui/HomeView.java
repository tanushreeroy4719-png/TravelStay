package com.travelstay.ui;

import com.travelstay.dao.HotelDAO;
import com.travelstay.dao.RestaurantDAO;
import com.travelstay.model.Hotel;
import com.travelstay.model.Restaurant;
import com.travelstay.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class HomeView {
    private final Stage stage;
    private final User user;
    private final BorderPane root = new BorderPane();
    private final TextField city = new TextField("Ahmedabad");
    private final VBox results = new VBox(12);

    public HomeView(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color:#f5f7fb;");

        Label title = new Label("TravelStay");
        title.setStyle("-fx-font-size:30px;-fx-font-weight:bold;-fx-text-fill:#16324f;");
        Label welcome = new Label("Welcome, " + user.fullName() + "  •  " + user.role());
        Button myBookings = new Button("My Bookings");
        myBookings.setOnAction(e -> stage.getScene().setRoot(new MyBookingsView(stage, user).getView()));
        Button logout = new Button("Logout");
        logout.setOnAction(e -> stage.getScene().setRoot(new LoginView(stage).getView()));
        HBox top = new HBox(20, title, welcome, new Region(), myBookings, logout);
        HBox.setHgrow(top.getChildren().get(2), Priority.ALWAYS);
        top.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup group = new ToggleGroup();
        RadioButton hotels = new RadioButton("🏨 Hotels");
        hotels.setToggleGroup(group);
        hotels.setSelected(true);
        RadioButton restaurants = new RadioButton("🍽 Restaurants");
        restaurants.setToggleGroup(group);
        Button search = new Button("Search");
        search.setOnAction(e -> {
            if (hotels.isSelected()) searchHotels();
            else searchRestaurants();
        });
        city.setPromptText("City");
        city.setPrefWidth(260);
        HBox searchBar = new HBox(12, new Label("Location:"), city, hotels, restaurants, search);
        searchBar.setPadding(new Insets(20, 0, 20, 0));

        ScrollPane scroll = new ScrollPane(results);
        scroll.setFitToWidth(true);
        results.setPadding(new Insets(5));

        root.setTop(new VBox(top, searchBar));
        root.setCenter(scroll);
        searchHotels();
    }

    private void searchHotels() {
        results.getChildren().clear();
        try {
            var hotelList = new HotelDAO().findByCity(city.getText());
            if (hotelList.isEmpty()) {
                results.getChildren().add(new Label("No hotels found for \"" + city.getText() + "\"."));
                return;
            }
            for (Hotel h : hotelList) {
                VBox card = new VBox(6);
                card.setPadding(new Insets(18));
                card.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#e1e6ed;");
                Label name = new Label("🏨 " + h.name() + "   ⭐ " + h.rating());
                name.setStyle("-fx-font-size:20px;-fx-font-weight:bold;");
                Label info = new Label(h.city() + " • " + h.address() + "\n" + h.description());
                HBox actions = new HBox(10);
                Button view = new Button("View Hotel");
                view.setOnAction(e -> showHotel(h));
                Button book = new Button("Book a Room");
                book.setOnAction(e -> BookingDialog.open(stage, h, user, this::searchHotels));
                actions.getChildren().addAll(view, book);
                card.getChildren().addAll(name, info, actions);
                results.getChildren().add(card);
            }
        } catch (Exception ex) {
            showError(ex, "Couldn't load hotels right now.");
        }
    }

    private void searchRestaurants() {
        results.getChildren().clear();
        try {
            var restaurantList = new RestaurantDAO().findByCity(city.getText());
            if (restaurantList.isEmpty()) {
                results.getChildren().add(new Label("No restaurants found for \"" + city.getText() + "\"."));
                return;
            }
            for (Restaurant r : restaurantList) {
                VBox card = new VBox(6);
                card.setPadding(new Insets(18));
                card.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#e1e6ed;");
                Label name = new Label("🍽 " + r.name() + "   ⭐ " + r.rating());
                name.setStyle("-fx-font-size:20px;-fx-font-weight:bold;");
                Label info = new Label(r.city() + " • " + r.cuisine() + "\n" + r.address());
                Button view = new Button("Reserve Table");
                view.setOnAction(e -> ReservationDialog.open(stage, r, user, this::searchRestaurants));
                card.getChildren().addAll(name, info, view);
                results.getChildren().add(card);
            }
        } catch (Exception ex) {
            showError(ex, "Couldn't load restaurants right now.");
        }
    }

    private void showHotel(Hotel h) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Hotel Details");
        a.setHeaderText(h.name());
        a.setContentText("Location: " + h.city() + "\nAddress: " + h.address() + "\nRating: " + h.rating() +
                "\n\n" + h.description());
        a.showAndWait();
    }

    private void showError(Exception ex, String userMessage) {
        // Log the real cause for developers; never show raw DB/exception details to the user.
        System.err.println(userMessage + " Cause: " + ex);
        new Alert(Alert.AlertType.ERROR, userMessage + "\nPlease try again in a moment.").showAndWait();
    }

    public Parent getView() { return root; }
}
