package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Smoothing module");
        Scene scene = new Scene(root, 1160, 670);
        primaryStage.setScene(scene);
        scene.getStylesheets().add("sample/mainWindow.css");
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
