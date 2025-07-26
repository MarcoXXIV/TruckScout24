package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class RegistrazioneController {

    @FXML
    public Button registerButton;

    @FXML
    public Hyperlink loginLink;

    @FXML
    private TextField nomeField;

    @FXML
    private TextField cognomeField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confermaPasswordField;

    @FXML
    private void initialize() {
        registerButton.setOnAction(e -> gestisciRegistrazione());
    }


    private double mouseX, mouseY;

    @FXML
    private Node logoLabel; // Il logo cliccabile

    @FXML
    private void handleMousePressed(MouseEvent event) {
        // Prendi la posizione iniziale del mouse
        mouseX = event.getSceneX();
        mouseY = event.getSceneY();

        // Animazione quando premi il logo
        TranslateTransition transition = new TranslateTransition(Duration.millis(150), logoLabel);
        transition.setByX(10); // Spostamento orizzontale
        transition.setByY(10); // Spostamento verticale
        transition.setCycleCount(1);
        transition.setAutoReverse(true);
        transition.play();
    }

    @FXML
    private void handleMouseDragged(MouseEvent event) {
        // Spostare il logo quando il mouse viene trascinato
        double deltaX = event.getSceneX() - mouseX;
        double deltaY = event.getSceneY() - mouseY;
        logoLabel.setLayoutX(logoLabel.getLayoutX() + deltaX);
        logoLabel.setLayoutY(logoLabel.getLayoutY() + deltaY);
        mouseX = event.getSceneX();
        mouseY = event.getSceneY();
    }

    @FXML
    private void goToHome() throws Exception {
        SceneHandler.getInstance().setHomeScene();
    }

    private void gestisciRegistrazione() {
        String nome = nomeField.getText();
        String cognome = cognomeField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confermaPassword = confermaPasswordField.getText();

        if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty() || password.isEmpty() || confermaPassword.isEmpty()) {
            mostraErrore("Tutti i campi sono obbligatori.");
            return;
        }

        if (!password.equals(confermaPassword)) {
            mostraErrore("Le password non coincidono.");
            return;
        }

        if (!email.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$")) {
            mostraErrore("Email non valida.");
            return;
        }

        // Simulazione registrazione completata
        mostraInformazione();
        // Qui potresti chiamare un service per salvare l’utente
    }

    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    private void mostraInformazione() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Successo");
        alert.setHeaderText(null);
        alert.setContentText("Registrazione completata con successo!");
        alert.showAndWait();
    }

    @FXML
    private void onRegisterClick(ActionEvent event) throws Exception {
        System.out.println("registrati cliccato");

        SceneHandler.getInstance().setRegistrationScene();  // o "home.fxml", ecc.
    }

    @FXML
    private void handleMouseEntered() {
        registerButton.setStyle("-fx-background-color: #FF4500; -fx-text-fill: white;");
    }

    @FXML
    private void handleMouseExited() {
        registerButton.setStyle("-fx-background-color: #FF6B00; -fx-text-fill: white;");
    }

    @FXML
    private void handleGoToLogin() throws Exception {
        SceneHandler.getInstance().setLoginScene();

    }
}
