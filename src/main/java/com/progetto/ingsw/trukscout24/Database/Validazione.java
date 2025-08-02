package com.progetto.ingsw.trukscout24.Database;

import com.progetto.ingsw.trukscout24.Messaggi;
import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.application.Platform;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class Validazione {
    private static Validazione instance = null;

    // Pattern per validazione
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9]{10,15}$");

    private Validazione() {}

    public static Validazione getInstance(){
        if (instance == null){
            instance = new Validazione();
        }
        return instance;
    }

    public CompletableFuture<Boolean> checkRegistration(String name, String surname, String email, String phone, String password, String repeatedPassword) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validazione campi vuoti
                if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || repeatedPassword.isEmpty()) {
                    Platform.runLater(() -> SceneHandler.getInstance().showAlert("Errore nella compilazione dei campi", Messaggi.registration_field_empty_error, 0));
                    return false;
                }

                // Validazione email
                if (!isValidEmail(email)) {
                    Platform.runLater(() -> SceneHandler.getInstance().showAlert("Email non valida", Messaggi.registration_email_error, 0));
                    return false;
                }

                // Validazione password
                if (!password.equals(repeatedPassword)) {
                    Platform.runLater(() -> SceneHandler.getInstance().showAlert("Password non valida", Messaggi.registration_password_error, 0));
                    return false;
                }

                if (password.length() < 6) {
                    Platform.runLater(() -> SceneHandler.getInstance().showAlert("Password non valida", Messaggi.registration_password_length_error, 0));
                    return false;
                }

                // Validazione telefono (se fornito)
                if (!phone.isEmpty() && !isValidPhoneNumber(phone)) {
                    Platform.runLater(() -> SceneHandler.getInstance().showAlert("Numero di telefono non valido", "Il formato del numero di telefono non è corretto", 0));
                    return false;
                }

                // Controllo email esistente
                CompletableFuture<Boolean> emailExists = DBConnessione.getInstance().checkExistEmail(email);
                Boolean exists = emailExists.get(10, TimeUnit.SECONDS);

                if (exists) {
                    Platform.runLater(() -> SceneHandler.getInstance().showAlert("Email non valida", Messaggi.registration_email_exist_error, 0));
                    return false;
                }

                // Successo
                Platform.runLater(() -> SceneHandler.getInstance().showAlert("Registrazione completata", Messaggi.registrazione_completata, 1));
                return true;

            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Platform.runLater(() -> SceneHandler.getInstance().showAlert("Errore", "Errore durante la validazione: " + e.getMessage(), 0));
                return false;
            }
        });
    }

    public boolean checkPassword(String password, String repeatedPassword) {
        if (!password.equals(repeatedPassword)) {
            SceneHandler.getInstance().showAlert("Password non valida", Messaggi.registration_password_error, 0);
            return false;
        }
        if (password.length() < 6) {
            SceneHandler.getInstance().showAlert("Password non valida", Messaggi.registration_password_length_error, 0);
            return false;
        }
        return true;
    }

    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Telefono opzionale
        }

        // Rimuovi spazi e caratteri speciali eccetto + iniziale
        String cleanPhone = phone.replaceAll("[\\s-()]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    public boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2 && name.matches("^[a-zA-ZÀ-ÿ\\s'.-]+$");
    }

    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
