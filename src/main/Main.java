package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(Main.class.getResource("/styles/auth.css").toExternalForm());
        scene.getStylesheets().add(Main.class.getResource("/styles/dashboard.css").toExternalForm());
        scene.getStylesheets().add(Main.class.getResource("/styles/base.css").toExternalForm());
        scene.getStylesheets().add(Main.class.getResource("/styles/components.css").toExternalForm());
        stage.setTitle("POS System");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/views/" + fxml));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(Main.class.getResource("/styles/auth.css").toExternalForm());
        scene.getStylesheets().add(Main.class.getResource("/styles/dashboard.css").toExternalForm());
        scene.getStylesheets().add(Main.class.getResource("/styles/base.css").toExternalForm());
        scene.getStylesheets().add(Main.class.getResource("/styles/components.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}
