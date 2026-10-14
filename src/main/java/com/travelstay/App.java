package com.travelstay;

import com.travelstay.ui.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("TravelStay - Hotel & Restaurant Booking");
        stage.setScene(new Scene(new LoginView(stage).getView(), 1000, 650));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
