package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.Database.DBConnessione;
import com.progetto.ingsw.trukscout24.Messaggi;
import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;

public class PasswordDimenticataController {

    private final SceneHandler sceneHandler = SceneHandler.getInstance();
    private final DBConnessione db = DBConnessione.getInstance();

    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField, confirmPasswordField;
    @FXML private Button resetPasswordButton;
    @FXML private Label statusMessage;

    @FXML
    void resetPasswordAction(ActionEvent event) throws Exception {
        String email = emailField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Errore", Messaggi.registration_field_empty_error, Alert.AlertType.ERROR);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert("Errore", Messaggi.registration_password_error, Alert.AlertType.ERROR);
            return;
        }

        if (newPassword.length() < 8) {
            showAlert("Errore", Messaggi.recovery_password_min_length_error, Alert.AlertType.ERROR);
            return;
        }

        String currentUserEmail = sceneHandler.getCurrentUserEmail();

        db.checkExistEmail(email).thenAccept(exists -> {
            if (!exists) {
                showAlert("Errore", Messaggi.recovery_password_email_not_found, Alert.AlertType.ERROR);
                return;
            }

            if (currentUserEmail != null && !currentUserEmail.equals(email)) {
                showAlert("Errore", Messaggi.recovery_password_other_user_error, Alert.AlertType.ERROR);
                return;
            }

            String encryptedPassword = db.encryptedPassword(newPassword);
            db.updatePassword(email, encryptedPassword);

            Platform.runLater(() -> {
                newPasswordField.clear();
                confirmPasswordField.clear();
                emailField.clear();
                showAlert("Successo", Messaggi.update_password_success, Alert.AlertType.INFORMATION);
                try {
                    sceneHandler.setLoginScene();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }).exceptionally(ex -> {
            showAlert("Errore", Messaggi.recovery_password_exception + ex.getMessage(), Alert.AlertType.ERROR);
            return null;
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    private void HomeClick(MouseEvent event) throws Exception {
        sceneHandler.setHomeScene();
    }
}
