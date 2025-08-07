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
            sceneHandler.showAlert("Errore", Messaggi.registration_field_empty_error, 0);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            sceneHandler.showAlert("Errore", Messaggi.registration_password_error, 0);
            return;
        }

        if (newPassword.length() < 8) {
            sceneHandler.showAlert("Errore", Messaggi.recovery_password_min_length_error, 0);
            return;
        }

        String currentUserEmail = sceneHandler.getCurrentUserEmail();

        db.checkExistEmail(email).thenAccept(exists -> {
            if (!exists) {
                sceneHandler.showAlert("Errore", Messaggi.recovery_password_email_not_found,0);
                return;
            }

            if (currentUserEmail != null && !currentUserEmail.equals(email)) {
                sceneHandler.showAlert("Errore", Messaggi.recovery_password_other_user_error, 0);
                return;
            }

            String encryptedPassword = db.encryptedPassword(newPassword);
            db.updatePassword(email, encryptedPassword);

            Platform.runLater(() -> {
                newPasswordField.clear();
                confirmPasswordField.clear();
                emailField.clear();
                sceneHandler.showAlert("Successo", Messaggi.update_password_success, 0);
                try {
                    sceneHandler.setLoginScene();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }).exceptionally(ex -> {
            sceneHandler.showAlert("Errore", Messaggi.recovery_password_exception + ex.getMessage(), 0);
            return null;
        });
    }

    @FXML
    private void HomeClick(MouseEvent event) throws Exception {
        try{
            sceneHandler.setHomeScene();
        }catch (Exception e){
            sceneHandler.showAlert("Errore",Messaggi.errore_generico,0);
            sceneHandler.setHomeScene();
        }
    }
}
