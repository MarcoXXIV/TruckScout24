package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.Database.DBConnessione;
import com.progetto.ingsw.trukscout24.Database.Validazione;
import com.progetto.ingsw.trukscout24.Messaggi;
import com.progetto.ingsw.trukscout24.Model.Utente;
import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.concurrent.CompletableFuture;

public class LoginController {

    public VBox formBox;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button loginButton;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink registerLink;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button togglePasswordButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label logoLabel;

    private boolean passwordVisible = false;
    private SceneHandler sceneHandler;
    private double mouseX, mouseY;

    @FXML
    public void initialize() {
        sceneHandler = SceneHandler.getInstance();

        errorLabel.setVisible(false);
        if (successLabel != null) {
            successLabel.setVisible(false);
        }
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }

        setupFieldValidation();
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
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();

        hideMessages();

        if (email.isEmpty() || password.isEmpty()) {
            showError(Messaggi.login_campi_vuoti);
            return;
        }

        if (!Validazione.getInstance().isValidEmailFormat(email)) {
            showError(Messaggi.login_email_non_valida);
            return;
        }

        setUIEnabled(false);
        showLoading(true);

        performLoginAsync(email, password);
    }

    private void performLoginAsync(String email, String password) {
        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    CompletableFuture<Boolean> loginResult = DBConnessione.getInstance().checkLoginCredentials(email, password);
                    return loginResult.get();
                } catch (Exception e) {
                    throw new RuntimeException(Messaggi.login_error + ": " + e.getMessage());
                }
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    setUIEnabled(true);
                    showLoading(false);

                    if (getValue()) {
                        loadUserAndProceed(email);
                    } else {
                        showError(Messaggi.login_errore_credenziali);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setUIEnabled(true);
                    showLoading(false);
                    showError(Messaggi.login_connessione_error);
                });
            }
        };

        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();
    }

    private void loadUserAndProceed(String email) {
        Task<Utente> userTask = new Task<>() {
            @Override
            protected Utente call() throws Exception {
                CompletableFuture<Utente> userFuture = DBConnessione.getInstance().setUser(email);
                return userFuture.get();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    Utente user = getValue();
                    if (user != null) {
                        System.out.println("Login successo per l'email: " + email);

                        sceneHandler.loginUser(user);

                        try {
                            sceneHandler.setHomeScene();
                        } catch (Exception e) {
                            showError(Messaggi.returnHome_error);
                        }
                    } else {
                        showError(Messaggi.login_user_data_error);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showError(Messaggi.login_user_data_error);
                });
            }
        };

        Thread userThread = new Thread(userTask);
        userThread.setDaemon(true);
        userThread.start();
    }

    @FXML
    private void PasswordDimenticataClick(MouseEvent event) throws Exception {
        sceneHandler.setRecoveryScene();
    }

    @FXML
    private void HomeClick(MouseEvent event) throws Exception {
        sceneHandler.setHomeScene();
    }

    @FXML
    private void RegistrazioneClick(MouseEvent event) throws Exception {
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
                passwordTextField.requestFocus();
                passwordTextField.positionCaret(password.length());
            }
            togglePasswordButton.setText("🙈");
        } else {
            String password = passwordTextField != null ? passwordTextField.getText() : "";
            if (passwordTextField != null) {
                passwordTextField.setVisible(false);
            }
            passwordField.setText(password);
            passwordField.setVisible(true);
            passwordField.requestFocus();
            passwordField.positionCaret(password.length());
            togglePasswordButton.setText("👁");
        }
    }

    private void setupFieldValidation() {
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && errorLabel.isVisible()) {
                hideMessages();
            }
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && errorLabel.isVisible()) {
                hideMessages();
            }
        });

        if (passwordTextField != null) {
            passwordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty() && errorLabel.isVisible()) {
                    hideMessages();
                }
            });
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        if (successLabel != null) {
            successLabel.setVisible(false);
        }
    }

    private void showSuccess(String message) {
        if (successLabel != null) {
            successLabel.setText(message);
            successLabel.setVisible(true);
        }
        errorLabel.setVisible(false);
    }

    private void hideMessages() {
        errorLabel.setVisible(false);
        if (successLabel != null) {
            successLabel.setVisible(false);
        }
    }

    private void setUIEnabled(boolean enabled) {
        emailField.setDisable(!enabled);
        passwordField.setDisable(!enabled);
        if (passwordTextField != null) {
            passwordTextField.setDisable(!enabled);
        }
        loginButton.setDisable(!enabled);
        togglePasswordButton.setDisable(!enabled);
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
    }

    private void saveCredentials(String email) {
        System.out.println("Credenziali salvate per: " + email);
    }
}
