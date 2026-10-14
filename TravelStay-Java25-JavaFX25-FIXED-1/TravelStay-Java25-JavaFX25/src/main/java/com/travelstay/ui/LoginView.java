package com.travelstay.ui;

import com.travelstay.dao.AuthDAO;
import com.travelstay.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginView {
    private final Stage stage;
    private final VBox root = new VBox(15);
    private final TextField email = new TextField();
    private final PasswordField password = new PasswordField();

    public LoginView(Stage stage) {
        this.stage = stage;
        root.setPadding(new Insets(50));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right,#102a43,#1f6f8b);");

        Label title = new Label("✈ TravelStay");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label sub = new Label("Hotel & Restaurant Booking");
        sub.setStyle("-fx-font-size: 18px; -fx-text-fill: #d9f0ff;");

        VBox card = new VBox(12);
        card.setMaxWidth(430);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color:white; -fx-background-radius:18;");

        email.setPromptText("Email");
        password.setPromptText("Password");
        Button login = new Button("Login");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(e -> doLogin());

        Label demo = new Label("Demo: admin@travelstay.com / admin123");
        demo.setStyle("-fx-text-fill:#666;");

        Button createAccount = new Button("Create an account");
        createAccount.setMaxWidth(Double.MAX_VALUE);
        createAccount.setStyle("-fx-background-color: transparent; -fx-text-fill: #1f6f8b; -fx-underline: true;");
        createAccount.setOnAction(e -> stage.getScene().setRoot(new RegisterView(stage).getView()));

        card.getChildren().addAll(title, sub, email, password, login, demo, createAccount);
        root.getChildren().add(card);
    }

    private void doLogin() {
        if (email.getText().isBlank() || password.getText().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Please enter both email and password.").showAndWait();
            return;
        }
        try {
            User user = new AuthDAO().login(email.getText().trim(), password.getText());
            if (user == null) {
                new Alert(Alert.AlertType.ERROR, "Invalid email or password.").showAndWait();
                return;
            }
            stage.getScene().setRoot(new HomeView(stage, user).getView());
        } catch (Exception ex) {
            System.err.println("Login failed: " + ex);
            new Alert(Alert.AlertType.ERROR, "Something went wrong. Please try again.").showAndWait();
        }
    }

    public Parent getView() { return root; }
}
