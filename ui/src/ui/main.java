package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

import static ui.CommonResourcesPaths.MAIN_FXML_INCLUDE_RESOURCE;


public class main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // load RitzpaMainSceneController
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(MAIN_FXML_INCLUDE_RESOURCE);
        fxmlLoader.setLocation(url);
        Parent mainSceneComponent = fxmlLoader.load(url.openStream());
        fxmlLoader.getController();

        // Create Scene
        Scene primaryScene = new Scene(mainSceneComponent, 600, 600);

        //Get a URL to the CSS file and add it to the scene
        primaryScene.getStylesheets().add(getClass().getResource("resources/Style1.css").toExternalForm()); // using by class loader

        primaryStage.setTitle("Ritzpa Stock Exchange");
        primaryStage.getIcons().add(new Image("/ui/resources/RSE_Icon.jpg"));
        primaryStage.setScene(primaryScene);
        primaryStage.setMinWidth(350);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }
}
