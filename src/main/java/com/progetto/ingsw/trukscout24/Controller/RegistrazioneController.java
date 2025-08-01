package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class RegistrazioneController {

    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;
    @FXML private AnchorPane rootPane;
    @FXML private TextField nomeField;
    @FXML private TextField cognomeField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confermaPasswordField;
    @FXML private Node logoLabel;

    private double mouseX, mouseY;

    @FXML
    private void initialize() {
        // Non necessario se nel FXML è presente onAction="#onRegisterClick"
    }

    @FXML
    private void handleMousePressed(MouseEvent event) {
        mouseX = event.getSceneX();
        mouseY = event.getSceneY();

        TranslateTransition transition = new TranslateTransition(Duration.millis(150), logoLabel);
        transition.setByX(10);
        transition.setByY(10);
        transition.setCycleCount(1);
        transition.setAutoReverse(true);
        transition.play();
    }

    @FXML
    private void handleMouseDragged(MouseEvent event) {
        double deltaX = event.getSceneX() - mouseX;
        double deltaY = event.getSceneY() - mouseY;
        logoLabel.setLayoutX(logoLabel.getLayoutX() + deltaX);
        logoLabel.setLayoutY(logoLabel.getLayoutY() + deltaY);
        mouseX = event.getSceneX();
        mouseY = event.getSceneY();
    }

    @FXML
    private void HomeClick(MouseEvent event) throws Exception {
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

        // Simulazione completata
        mostraInformazione();
        // Qui potresti inviare i dati al service
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
    private void onRegisterClick(ActionEvent event) {
        gestisciRegistrazione();
    }

    @FXML
    private void handleGoToLogin() throws Exception {
        SceneHandler.getInstance().setLoginScene();
    }
}
