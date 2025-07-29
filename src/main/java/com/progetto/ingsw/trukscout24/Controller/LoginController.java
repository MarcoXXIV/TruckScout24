package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class LoginController {

    public VBox formBox;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button loginButton;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink registerLink;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button togglePasswordButton;

    private boolean passwordVisible = false;
    private SceneHandler sceneHandler;

    @FXML
    public void initialize() {
        sceneHandler = SceneHandler.getInstance();

        errorLabel.setVisible(false);
        if (successLabel != null) {
            successLabel.setVisible(false);
        }
        setupFieldValidation();
    }

    private double mouseX, mouseY;

    @FXML
    private Label logoLabel; // Il logo cliccabile

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
    private void handleLogin(ActionEvent event) throws Exception {
        String email = emailField.getText();
        String password = passwordField.getText();

        hideMessages();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Inserisci email e password");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Formato email non valido");
            return;
        }

        if (authenticateUser(email, password)) {
            sceneHandler.setHomeScene();
        } else {
            showError("Email o password non corrette");
        }
    }


    @FXML
    private void handleForgotPassword(ActionEvent event) throws Exception {
        sceneHandler.setRecoveryScene();
    }

    @FXML
    private void goToHome() throws Exception {
        SceneHandler.getInstance().setHomeScene();
    }

    @FXML
    private void handleRegister(ActionEvent event) throws Exception {
        sceneHandler.setRegistrationScene();
    }

    @FXML
    private void onEnterPressed(KeyEvent event) throws Exception {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin(new ActionEvent());
        }
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            String password = passwordField.getText();
            passwordField.setVisible(false);
            if (passwordTextField != null) {
                passwordTextField.setText(password);
                passwordTextField.setVisible(true);
            }
            togglePasswordButton.setText("🙈");
        } else {
            String password = passwordTextField != null ? passwordTextField.getText() : "";
            if (passwordTextField != null) {
                passwordTextField.setVisible(false);
            }
            passwordField.setText(password);
            passwordField.setVisible(true);
            togglePasswordButton.setText("👁");
        }
    }

    private void setupFieldValidation() {
        // Implementazione validazione campi...
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    private boolean authenticateUser(String email, String password) {
        return email.equals("admin@trukscout24.com") && password.equals("admin123");
    }

    private void showError(String message) {
        //sceneHandler.showAlert("Errore", message, SceneHandler.AlertType.ERROR);
    }

    private void hideMessages() {
        errorLabel.setVisible(false);
        if (successLabel != null) {
            successLabel.setVisible(false);
        }
    }
}
