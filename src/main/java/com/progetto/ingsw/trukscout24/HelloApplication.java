package com.progetto.ingsw.trukscout24;

import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static javafx.application.Application.launch;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        SceneHandler sceneHandler = SceneHandler.getInstance();
        sceneHandler.init(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}