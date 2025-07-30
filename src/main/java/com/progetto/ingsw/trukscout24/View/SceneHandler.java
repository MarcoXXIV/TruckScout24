package com.progetto.ingsw.trukscout24.View;

import com.progetto.ingsw.trukscout24.HelloApplication;
//import com.progetto.ingsw.Message;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SceneHandler {

    private final static String RESOURCE_PATH = "/com/progetto/ingsw/trukscout24/";
    private final static String FXML_PATH = RESOURCE_PATH + "fxmls/";
    //private final static String CSS_PATH = RESOURCE_PATH + "css/";
    //private final static String FONTS_PATH = RESOURCE_PATH + "fonts/";
    private Alert alert = new Alert(Alert.AlertType.INFORMATION);
    private String theme = "light";
    private Scene scene;
    private Stage stage;


    private static SceneHandler instance = null;
    private SceneHandler() {}

    public void init(Stage primaryStage) throws Exception {
        if(this.stage != null)
            return;
        this.stage = primaryStage;
        this.stage.getIcons().add(new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream("Icone/TruckScout24.png"))));
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH + "Home.fxml"));
        this.scene = new Scene(loader.load(), null);
        this.stage.setScene(scene);
        this.stage.setTitle("Trukscout24");
        this.stage.setMaximized(true);
        this.stage.setMinWidth(1370);
        this.stage.setMinHeight(700);
        //setCSSForScene(scene);
        //loadFonts();
        this.stage.show();
    }

    public void loadFXML(String FXMLPath) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH + FXMLPath));
        if (scene == null) scene = new Scene(loader.load());
        else scene = new Scene(loader.load(), scene.getWidth(), scene.getHeight());
        //setCSSForScene(scene);
        //loadFonts();
        this.stage.setScene(scene);
        stage.show();
    }

    public static SceneHandler getInstance() {
        if(instance == null)
            instance = new SceneHandler();
        return instance;
    }

/*
    private List<String> loadCSS() {
        List<String> resources = new ArrayList<>();
        for (String style : List.of(CSS_PATH + theme + ".css", CSS_PATH + "fonts.css", CSS_PATH + "Style.css")) {
            String resource = Objects.requireNonNull(SceneHandler.class.getResource(style)).toExternalForm();
            resources.add(resource);
        }
        return resources;
    }

    private void setCSSForScene(Scene scene) {
        Objects.requireNonNull(scene, "Scene cannot be null");
        List<String> resources = loadCSS();
        scene.getStylesheets().clear();
        for(String resource : resources)
            scene.getStylesheets().add(resource);
    }
/*
    private void loadFonts() {
        for (String font : List.of(FONTS_PATH + "Roboto/Roboto-Medium.ttf")) {
            Font.loadFont(Objects.requireNonNull(SceneHandler.class.getResource(font)).toExternalForm(), 10);
        }
    }
    */


    public void showAlert(String Head, String message, int type) {
        alert = new Alert(Alert.AlertType.INFORMATION);
        stage.getIcons().add(new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream("Icone/TruckScout24.png"))));
        ImageView icon = new ImageView();
        if (type == 1){
            icon = new ImageView(new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream("Icone/information_alert.png"))));
        } else if (type == 0) {
            icon = new ImageView(new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream("Icone/denied_alert.png"))));
        }
        icon.setFitWidth(60);
        icon.setFitHeight(60);
        alert.getDialogPane().setGraphic(icon);
        alert.setTitle(Head);
        alert.setHeaderText("");
        alert.setContentText(message);
        alert.show();
    }
    /*
    public void changeTheme() {
        if("dark".equals(theme))
            theme = "light";
        else
            theme = "dark";
        changedTheme();
    }

    private void changedTheme() {
        setCSSForScene(scene);
        setCSSForAlert(alert);
    }

    private void setCSSForAlert(Alert alert) {
        Objects.requireNonNull(alert, "Alert cannot be null");
        List<String> resources = loadCSS();
        alert.getDialogPane().getStylesheets().clear();
        for(String resource : resources)
            alert.getDialogPane().getStylesheets().add(resource);
    }
    */

    public void setHomeScene() throws Exception {
        loadFXML("Home.fxml");
    }

    public void setLoginScene() throws Exception {
        loadFXML("Login.fxml");
    }

    public void setRegistrationScene() throws Exception {
        loadFXML("Registrazione.fxml");
    }

    public void setUtenteScene() throws Exception {
        loadFXML("Utente.fxml");
    }

    public void setRecoveryScene() throws Exception {
        //loadFXML("PasswordDimenticata.fxml");
    }


    public void setTruckDetailsScene() {
    }

    public void setWishlistScene() throws Exception {
        loadFXML("Wishlist.fxml");
    }
}

