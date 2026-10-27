package com.studentanalytics;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Main application class extending JavaFX Application.
 * Configures the primary window stage, scene, and stylesheet.
 */
public class Main extends Application {

    public static final String APP_TITLE = "Student Analytics Dashboard - Faculty Academic Record System";
    public static final int DEFAULT_WIDTH = 1120;
    public static final int DEFAULT_HEIGHT = 720;
    public static final int MIN_WIDTH = 880;
    public static final int MIN_HEIGHT = 580;

    @Override
    public void start(Stage primaryStage) {
        DashboardController controller = new DashboardController();
        Scene scene = new Scene(controller.getRoot(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Attach custom JavaFX CSS stylesheet
        URL cssResource = getClass().getResource("/styles.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.err.println("Warning: styles.css could not be located in resources.");
        }

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

        // Center on screen and display
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Standard entry point.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
