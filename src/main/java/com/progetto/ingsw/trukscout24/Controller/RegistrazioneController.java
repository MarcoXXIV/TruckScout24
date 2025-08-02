package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.Database.DBConnessione;
import com.progetto.ingsw.trukscout24.Database.Validazione;
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
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private CheckBox privacyCheckBox;

    private double mouseX, mouseY;
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

    @FXML
    private void initialize() {
        setupUI();
        setupValidation();
        setupKeyHandling();
    }

    private void setupUI() {
        if (errorLabel != null) errorLabel.setVisible(false);
        if (successLabel != null) successLabel.setVisible(false);
        if (loadingIndicator != null) loadingIndicator.setVisible(false);

        // Inizializza campi password testuali come nascosti
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
        SceneHandler.getInstance().setHomeScene();
    }

    @FXML
    private void onRegisterClick(ActionEvent event) {
        performRegistration();
    }

    @FXML
    private void handleGoToLogin() throws Exception {
        SceneHandler.getInstance().setLoginScene();
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
        // Raccogli dati
        String nome = nomeField.getText().trim();
        String cognome = cognomeField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String telefono = telefonoField != null ? telefonoField.getText().trim() : "";
        String password = passwordVisible ? passwordTextField.getText() : passwordField.getText();
        String confermaPassword = confirmPasswordVisible ? confermaPasswordTextField.getText() : confermaPasswordField.getText();

        hideMessages();

        // Validazione basic
        if (!validateBasicInput(nome, cognome, email, password, confermaPassword, telefono)) {
            return;
        }

        // Controllo privacy
        if (privacyCheckBox != null && !privacyCheckBox.isSelected()) {
            showError("Devi accettare i termini e condizioni");
            return;
        }

        // Disabilita UI
        setUIEnabled(false);
        showLoading(true);

        // Esegui registrazione asincrona
        performRegistrationAsync(nome, cognome, email, telefono, password, confermaPassword);
    }

    private boolean validateBasicInput(String nome, String cognome, String email, String password, String confermaPassword, String telefono) {
        if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty() || password.isEmpty() || confermaPassword.isEmpty()) {
            showError("Tutti i campi sono obbligatori");
            return false;
        }

        if (!Validazione.getInstance().isValidEmail(email)) {
            showError("Formato email non valido");
            return false;
        }

        if (!password.equals(confermaPassword)) {
            showError("Le password non coincidono");
            return false;
        }

        if (password.length() < 6) {
            showError("La password deve contenere almeno 6 caratteri");
            return false;
        }

        if (!telefono.isEmpty() && !Validazione.getInstance().isValidPhoneNumber(telefono)) {
            showError("Numero di telefono non valido");
            return false;
        }

        return true;
    }

    private void performRegistrationAsync(String nome, String cognome, String email, String telefono, String password, String confermaPassword) {
        Task<Boolean> registrationTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    return Validazione.getInstance().checkRegistration(nome, cognome, email, telefono, password, confermaPassword).get();
                } catch (Exception e) {
                    throw new RuntimeException("Errore durante la registrazione: " + e.getMessage());
                }
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    setUIEnabled(true);
                    showLoading(false);

                    if (getValue()) {
                        SceneHandler.getInstance().setCurrentUserEmail(email);
                        insertNewUser(nome, cognome, email, telefono, password);
                    }
                    // Gli errori sono già gestiti dalla validazione
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setUIEnabled(true);
                    showLoading(false);
                    showError("Errore di connessione. Riprova.");
                });
            }
        };

        Thread registrationThread = new Thread(registrationTask);
        registrationThread.setDaemon(true);
        registrationThread.start();
    }

    private void insertNewUser(String nome, String cognome, String email, String telefono, String password) {
        Task<Void> insertTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Long numeroTelefono = telefono.isEmpty() ? null : Long.parseLong(telefono.replaceAll("[^0-9]", ""));
                DBConnessione.getInstance().insertUsers(nome, cognome, email, numeroTelefono, password, false);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    showSuccess("Registrazione completata con successo!");
                    clearFields();

                    // Ritorna al login dopo 2 secondi
                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                        try {
                            SceneHandler.getInstance().setLoginScene();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }));
                    timeline.play();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showError("Errore nel salvataggio. Riprova.");
                });
            }
        };

        Thread insertThread = new Thread(insertTask);
        insertThread.setDaemon(true);
        insertThread.start();
    }

    private void setupValidation() {
        // Validazione real-time per email
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && errorLabel != null && errorLabel.isVisible()) {
                hideMessages();
            }
        });

        // Validazione password match
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
    }

    private void validatePasswordMatch() {
        String password = passwordVisible ? (passwordTextField != null ? passwordTextField.getText() : "") : passwordField.getText();
        String confirmPassword = confirmPasswordVisible ? (confermaPasswordTextField != null ? confermaPasswordTextField.getText() : "") :
                (confermaPasswordField != null ? confermaPasswordField.getText() : "");

        if (!password.isEmpty() && !confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
            // Potresti mostrare un feedback visivo qui
        }
    }

    private void setupKeyHandling() {
        // Gestione Enter su tutti i campi
        nomeField.setOnKeyPressed(this::handleEnterKey);
        cognomeField.setOnKeyPressed(this::handleEnterKey);
        emailField.setOnKeyPressed(this::handleEnterKey);
        if (telefonoField != null) telefonoField.setOnKeyPressed(this::handleEnterKey);
        passwordField.setOnKeyPressed(this::handleEnterKey);
        if (passwordTextField != null) passwordTextField.setOnKeyPressed(this::handleEnterKey);
        if (confermaPasswordField != null) confermaPasswordField.setOnKeyPressed(this::handleEnterKey);
        if (confermaPasswordTextField != null) confermaPasswordTextField.setOnKeyPressed(this::handleEnterKey);
    }

    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            performRegistration();
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
        if (successLabel != null) {
            successLabel.setVisible(false);
        }
    }

    private void showSuccess(String message) {
        if (successLabel != null) {
            successLabel.setText(message);
            successLabel.setVisible(true);
        }
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    private void hideMessages() {
        if (errorLabel != null) errorLabel.setVisible(false);
        if (successLabel != null) successLabel.setVisible(false);
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
    }
}
