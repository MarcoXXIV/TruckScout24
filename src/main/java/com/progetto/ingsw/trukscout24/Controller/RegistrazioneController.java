package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.Database.DBConnessione;
import com.progetto.ingsw.trukscout24.Database.Validazione;
import com.progetto.ingsw.trukscout24.Messaggi;
import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    @FXML private TextField telefonoField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confermaPasswordField;
    @FXML private TextField passwordTextField;
    @FXML private TextField confermaPasswordTextField;
    @FXML private Button togglePasswordButton;
    @FXML private Button toggleConfirmPasswordButton;
    @FXML private Node logoLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private CheckBox privacyCheckBox;

    private double mouseX, mouseY;
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

    private final SceneHandler sceneHandler = SceneHandler.getInstance();

    @FXML
    private void initialize() {
        setupUI();
        setupValidation();
        setupKeyHandling();
    }

    private void setupUI() {
        if (loadingIndicator != null) loadingIndicator.setVisible(false);
        if (passwordTextField != null) passwordTextField.setVisible(false);
        if (confermaPasswordTextField != null) confermaPasswordTextField.setVisible(false);
    }

    @FXML
    private void handleMousePressed(MouseEvent event) {
        mouseX = event.getSceneX();
        mouseY = event.getSceneY();

        TranslateTransition transition = new TranslateTransition(Duration.millis(150), logoLabel);
        transition.setByX(5);
        transition.setByY(5);
        transition.setCycleCount(2);
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
        try{
            sceneHandler.setHomeScene();
        }catch (Exception e){
            sceneHandler.showAlert("Errore", Messaggi.errore_generico, 0);
            sceneHandler.setHomeScene();
        }
    }

    @FXML
    private void onRegisterClick(ActionEvent event) {
        performRegistration();
    }

    @FXML
    private void handleGoToLogin() throws Exception {
        try{
            sceneHandler.setLoginScene();
        }catch (Exception e){
            sceneHandler.showAlert("Errore", Messaggi.errore_login, 0);
            sceneHandler.setHomeScene();
        }
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        passwordVisible = !passwordVisible;
        togglePasswordField(passwordField, passwordTextField, passwordVisible, togglePasswordButton);
    }

    @FXML
    private void toggleConfirmPasswordVisibility(ActionEvent event) {
        confirmPasswordVisible = !confirmPasswordVisible;
        togglePasswordField(confermaPasswordField, confermaPasswordTextField, confirmPasswordVisible, toggleConfirmPasswordButton);
    }

    private void togglePasswordField(PasswordField passwordField, TextField textField, boolean visible, Button toggleButton) {
        if (visible) {
            String password = passwordField.getText();
            passwordField.setVisible(false);
            if (textField != null) {
                textField.setText(password);
                textField.setVisible(true);
                textField.requestFocus();
                textField.positionCaret(password.length());
            }
            toggleButton.setText("🙈");
        } else {
            String password = textField != null ? textField.getText() : "";
            if (textField != null) {
                textField.setVisible(false);
            }
            passwordField.setText(password);
            passwordField.setVisible(true);
            passwordField.requestFocus();
            passwordField.positionCaret(password.length());
            toggleButton.setText("👁");
        }
    }

    private void performRegistration() {
        String nome = nomeField.getText().trim();
        String cognome = cognomeField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String telefono = telefonoField != null ? telefonoField.getText().trim() : "";
        String password = passwordVisible ? passwordTextField.getText() : passwordField.getText();
        String confermaPassword = confirmPasswordVisible ? confermaPasswordTextField.getText() : confermaPasswordField.getText();

        resetFieldStyles();

        if (!validateInput(nome, cognome, email, password, confermaPassword, telefono)) {
            return;
        }

        setUIEnabled(false);
        showLoading(true);

        insertNewUser(nome, cognome, email, telefono, password);
    }

    private boolean validateInput(String nome, String cognome, String email, String password, String confermaPassword, String telefono) {
        if (!Validazione.getInstance().isValidName(nome)) {
            highlightErrorField(nomeField);
            sceneHandler.showAlert("Errore Validazione", Messaggi.registratione_nome_error, 0);
            return false;
        }

        if (!Validazione.getInstance().isValidSurname(cognome)) {
            highlightErrorField(cognomeField);
            sceneHandler.showAlert("Errore Validazione", Messaggi.registrazione_cognome_error, 0);
            return false;
        }

        if (telefono != null && !telefono.isEmpty() && !Validazione.getInstance().isValidPhoneNumber(telefono)) {
            highlightErrorField(telefonoField);
            sceneHandler.showAlert("Errore Validazione", Messaggi.registrazione_telefono_error, 0);
            return false;
        }

        if (!Validazione.getInstance().isValidEmailFormat(email)) {
            highlightErrorField(emailField);
            sceneHandler.showAlert("Errore Validazione", Messaggi.registrazione_email_error, 0);
            return false;
        }

        if (!Validazione.getInstance().isValidPassword(password)) {
            highlightErrorField(passwordField);
            sceneHandler.showAlert("Errore Validazione", Messaggi.registratione_password_length_error, 0);
            return false;
        }

        if (!password.equals(confermaPassword)) {
            highlightErrorField(confermaPasswordField);
            sceneHandler.showAlert("Errore Validazione", Messaggi.registratione_password_error, 0);
            return false;
        }

        return true;
    }

    private void insertNewUser(String nome, String cognome, String email, String telefono, String password) {
        Task<Void> insertTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Long numeroTelefono = telefono.isEmpty() ? null : Long.parseLong(telefono.replaceAll("[^0-9]", ""));
                    DBConnessione.getInstance().insertUsers(nome, cognome, email, numeroTelefono, password, false);
                } catch (Exception e) {
                    sceneHandler.showAlert("Errore",Messaggi.errore_generico,0);
                    throw new RuntimeException("Errore nel salvataggio utente: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    setUIEnabled(true);
                    showLoading(false);
                    sceneHandler.showAlert("Successo", Messaggi.registrazione_completata, 1);
                    clearFields();
                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2.5), e -> {
                        try {
                            sceneHandler.setLoginScene();
                        } catch (Exception ex) {
                            sceneHandler.showAlert("Errore", Messaggi.errore_login,0);
                        }
                    }));
                    timeline.play();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setUIEnabled(true);
                    showLoading(false);
                    sceneHandler.showAlert("Errore Registrazione", Messaggi.errore_registrazione, 0);
                });
            }
        };

        Thread insertThread = new Thread(insertTask);
        insertThread.setDaemon(true);
        insertThread.start();
    }

    private void setupValidation() {
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                resetFieldStyles();
            }
        });

        if (confermaPasswordField != null) {
            confermaPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
                validatePasswordMatch();
            });
        }

        if (confermaPasswordTextField != null) {
            confermaPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                validatePasswordMatch();
            });
        }

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePasswordMatch();
        });

        if (passwordTextField != null) {
            passwordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                validatePasswordMatch();
            });
        }
    }

    private void validatePasswordMatch() {
        String password = passwordVisible ? (passwordTextField != null ? passwordTextField.getText() : "") : passwordField.getText();
        String confirmPassword = confirmPasswordVisible ? (confermaPasswordTextField != null ? confermaPasswordTextField.getText() : "") : (confermaPasswordField != null ? confermaPasswordField.getText() : "");

        if (!password.isEmpty() && !confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
            confermaPasswordField.setStyle("-fx-border-color: #ff6b6b;");
        } else if (password.equals(confirmPassword) && !confirmPassword.isEmpty()) {
            confermaPasswordField.setStyle("");
        }
    }

    private void setupKeyHandling() {
        nomeField.setOnKeyPressed(this::handleEnterKey);
        cognomeField.setOnKeyPressed(this::handleEnterKey);
        emailField.setOnKeyPressed(this::handleEnterKey);
        if (telefonoField != null) telefonoField.setOnKeyPressed(this::handleEnterKey);
        passwordField.setOnKeyPressed(this::handleEnterKey);
        if (passwordTextField != null) passwordTextField.setOnKeyPressed(this::handleEnterKey);
        if (confermaPasswordField != null) confermaPasswordField.setOnKeyPressed(this::handleEnterKey);
        if (confermaPasswordTextField != null) confermaPasswordTextField.setOnKeyPressed(this::handleEnterKey);

        setupTabOrder();
    }

    private void setupTabOrder() {
        nomeField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                cognomeField.requestFocus();
            }
        });

        cognomeField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                emailField.requestFocus();
            }
        });
    }

    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (areRequiredFieldsFilled()) {
                performRegistration();
            } else {
                focusFirstEmptyField();
            }
        }
    }

    private boolean areRequiredFieldsFilled() {
        return !nomeField.getText().trim().isEmpty() &&
                !cognomeField.getText().trim().isEmpty() &&
                !emailField.getText().trim().isEmpty() &&
                !passwordField.getText().isEmpty() &&
                (confermaPasswordField == null || !confermaPasswordField.getText().isEmpty());
    }

    private void focusFirstEmptyField() {
        if (nomeField.getText().trim().isEmpty()) {
            nomeField.requestFocus();
        } else if (cognomeField.getText().trim().isEmpty()) {
            cognomeField.requestFocus();
        } else if (emailField.getText().trim().isEmpty()) {
            emailField.requestFocus();
        } else if (passwordField.getText().isEmpty()) {
            passwordField.requestFocus();
        } else if (confermaPasswordField != null && confermaPasswordField.getText().isEmpty()) {
            confermaPasswordField.requestFocus();
        }
    }

    private void highlightErrorField(Node field) {
        resetFieldStyles();
        if (field instanceof TextField) {
            ((TextField) field).setStyle("-fx-border-color: #ff6b6b;");
        } else if (field instanceof PasswordField) {
            ((PasswordField) field).setStyle("-fx-border-color: #ff6b6b;");
        }
    }

    private void resetFieldStyles() {
        nomeField.setStyle("");
        cognomeField.setStyle("");
        emailField.setStyle("");
        if (telefonoField != null) telefonoField.setStyle("");
        passwordField.setStyle("");
        if (confermaPasswordField != null) confermaPasswordField.setStyle("");
    }

    private void setUIEnabled(boolean enabled) {
        nomeField.setDisable(!enabled);
        cognomeField.setDisable(!enabled);
        emailField.setDisable(!enabled);
        if (telefonoField != null) telefonoField.setDisable(!enabled);
        passwordField.setDisable(!enabled);
        if (passwordTextField != null) passwordTextField.setDisable(!enabled);
        if (confermaPasswordField != null) confermaPasswordField.setDisable(!enabled);
        if (confermaPasswordTextField != null) confermaPasswordTextField.setDisable(!enabled);
        registerButton.setDisable(!enabled);
        if (togglePasswordButton != null) togglePasswordButton.setDisable(!enabled);
        if (toggleConfirmPasswordButton != null) toggleConfirmPasswordButton.setDisable(!enabled);
        if (privacyCheckBox != null) privacyCheckBox.setDisable(!enabled);
        if (loginLink != null) loginLink.setDisable(!enabled);
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
    }

    private void clearFields() {
        nomeField.clear();
        cognomeField.clear();
        emailField.clear();
        if (telefonoField != null) telefonoField.clear();
        passwordField.clear();
        if (passwordTextField != null) passwordTextField.clear();
        if (confermaPasswordField != null) confermaPasswordField.clear();
        if (confermaPasswordTextField != null) confermaPasswordTextField.clear();
        if (privacyCheckBox != null) privacyCheckBox.setSelected(false);

        passwordVisible = false;
        confirmPasswordVisible = false;
        if (passwordTextField != null) passwordTextField.setVisible(false);
        if (confermaPasswordTextField != null) confermaPasswordTextField.setVisible(false);
        passwordField.setVisible(true);
        if (confermaPasswordField != null) confermaPasswordField.setVisible(true);
        if (togglePasswordButton != null) togglePasswordButton.setText("👁");
        if (toggleConfirmPasswordButton != null) toggleConfirmPasswordButton.setText("👁");
    }
}