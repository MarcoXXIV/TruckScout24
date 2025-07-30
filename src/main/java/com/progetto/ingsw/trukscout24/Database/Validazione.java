package com.progetto.ingsw.trukscout24.Database;

import com.progetto.ingsw.trukscout24.Messaggi;
import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.application.Platform;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Validazione {
    private static Validazione instance = null;

    private Validazione() {}

    public static Validazione getInstance(){
        if (instance == null){
            instance = new Validazione();
        }
        return instance;
    }

    public CompletableFuture<Boolean> checkRegistration(String name, String surname, String email, String password, String repeatedPassword) throws SQLException, ExecutionException, InterruptedException, TimeoutException {
        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || repeatedPassword.isEmpty()){
            SceneHandler.getInstance().showAlert("Errore nella compilazione dei campi", Messaggi.registration_field_empty_error, 0);
            return CompletableFuture.completedFuture(false);
        }
        if (!email.contains("@")){
            SceneHandler.getInstance().showAlert("Email non valida", Messaggi.registration_email_error, 0);
            return CompletableFuture.completedFuture(false);
        }
        if (!password.equals(repeatedPassword)){
            SceneHandler.getInstance().showAlert("Password non valida", Messaggi.registration_password_error, 0);
            return CompletableFuture.completedFuture(false);
        }
        if (password.length() < 6 ){
            SceneHandler.getInstance().showAlert("Password non valida", Messaggi.registration_password_length_error, 0);
            return CompletableFuture.completedFuture(false);
        }
        CompletableFuture<Boolean> future = DBConnessione.getInstance().checkExistEmail(email);
        Boolean valid = future.get(10, TimeUnit.SECONDS);
        if (valid){
            Platform.runLater(() -> SceneHandler.getInstance().showAlert("Email non valida", Messaggi.registration_email_exist_error, 0));
            CompletableFuture.completedFuture(false);
            return CompletableFuture.completedFuture(true);
        }
        SceneHandler.getInstance().showAlert("Registrazione completata", Messaggi.registrazione_completata, 1);
        return CompletableFuture.completedFuture(true);
    }

    public boolean checkPassword(String password, String repeatedPassword){
        if (!password.equals(repeatedPassword)){
            SceneHandler.getInstance().showAlert("Password non valida", Messaggi.registration_password_error, 0);
            return false;
        }
        if (password.length() < 6 ){
            SceneHandler.getInstance().showAlert("Password non valida", Messaggi.registration_password_length_error, 0);
            return false;
        }
        return true;
    }
}

