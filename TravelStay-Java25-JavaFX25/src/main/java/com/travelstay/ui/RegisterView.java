package com.travelstay.ui;

import com.travelstay.dao.AuthDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterView {
    private final Stage stage;
    private final VBox root = new VBox(15);
    private final TextField fullName = new TextField();
    private final TextField email = new TextField();
    private final TextField phone = new TextField();
    private final PasswordField password = new PasswordField();
    private final PasswordField confirmPassword = new PasswordField();
    private final Label errorLabel = new Label();

    public RegisterView(Stage stage) {
        this.stage = stage;
        root.setPadding(new Insets(50));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right,#102a43,#1f6f8b);");

        Label title = new Label("✈ Create your account");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox card = new VBox(12);
        card.setMaxWidth(430);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color:white; -fx-background-radius:18;");

        fullName.setPromptText("Full name");
        email.setPromptText("Email");
        phone.setPromptText("Phone (optional)");
        password.setPromptText("Password (min 6 characters)");
        confirmPassword.setPromptText("Confirm password");

        errorLabel.setStyle("-fx-text-fill:#c0392b; -fx-wrap-text:true;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button register = new Button("Create account");
        register.setMaxWidth(Double.MAX_VALUE);
        register.setOnAction(e -> doRegister());

        Button backToLogin = new Button("Back to login");
        backToLogin.setMaxWidth(Double.MAX_VALUE);
        backToLogin.setStyle("-fx-background-color: transparent; -fx-text-fill: #1f6f8b; -fx-underline: true;");
        backToLogin.setOnAction(e -> stage.getScene().setRoot(new LoginView(stage).getView()));

        card.getChildren().addAll(title, fullName, email, phone, password, confirmPassword,
                errorLabel, register, backToLogin);
        root.getChildren().add(card);
    }

    private void doRegister() {
        String name = fullName.getText().trim();
        String mail = email.getText().trim();
        String pw = password.getText();
        String confirmPw = confirmPassword.getText();

        if (name.isEmpty() || mail.isEmpty() || pw.isEmpty()) {
            showFormError("Full name, email and password are required.");
            return;
        }
        if (!mail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            showFormError("Please enter a valid email address.");
            return;
        }
        if (pw.length() < 6) {
            showFormError("Password must be at least 6 characters.");
            return;
        }
        if (!pw.equals(confirmPw)) {
            showFormError("Passwords do not match.");
            return;
        }

        try {
            new AuthDAO().register(name, mail, pw, phone.getText().trim());
            new Alert(Alert.AlertType.INFORMATION, "Account created! You can now log in.").showAndWait();
            stage.getScene().setRoot(new LoginView(stage).getView());
        } catch (IllegalStateException ex) {
            // Expected, user-facing validation message (e.g. duplicate email).
            showFormError(ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Registration failed: " + ex);
            showFormError("Something went wrong while creating your account. Please try again.");
        }
    }

    private void showFormError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    public Parent getView() { return root; }
}
