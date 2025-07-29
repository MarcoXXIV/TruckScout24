// File: UtenteController.java

package com.progetto.ingsw.trukscout24.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.progetto.ingsw.trukscout24.Model.Utente;
import com.progetto.ingsw.trukscout24.Model.Prenotazione;

import java.net.URL;
import java.util.ResourceBundle;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class UtenteController implements Initializable {

    @FXML private ImageView fotoImageView;
    @FXML private Button updateProfileButton;
    @FXML private TextField telefonoField;
    @FXML private Label profileUpdateStatus;
    @FXML private TextField emailField;
    @FXML private TextField cognomeField;
    @FXML private TextField nomeField;
    @FXML private Button refreshBookingsButton;

    @FXML private TableColumn<?, ?> camionColumn;
    @FXML private Button newBookingButton;
    @FXML private TextField camionIdField;

    @FXML private ComboBox<String> categoriaBox; // aggiunto il tipo
    @FXML private Button addCamionButton;
    @FXML private Button removeCamionButton;
    @FXML private Label adminActionStatus;
    @FXML private VBox adminSection;
    @FXML private Label welcomeLabel, emailLabel, nomeLabel, cognomeLabel, telefonoLabel;
    @FXML private PasswordField passwordField, repeatPasswordField;
    @FXML private Button changePasswordButton, logoutButton;

    @FXML private TableView<Prenotazione> prenotazioniTable;
    @FXML private TableColumn<Prenotazione, String> idCamionColumn, dataColumn, statoColumn, azioniColumn;

    @FXML private Button aggiungiCamionButton, visualizzaCamionButton, salvaCamionButton, annullaButton;
    @FXML private TextField idCamionField, modelloField, prezzoField;
    @FXML private ComboBox<String> categoriaComboBox;
    @FXML private TextArea descrizioneArea;

    @FXML private TextArea reportArea;
    @FXML private Button sendReportButton;
    @FXML private Label reportSentLabel;

    @FXML private ScrollPane mainScrollPane;

    private Utente currentUser;
    private final ObservableList<Prenotazione> prenotazioni = FXCollections.observableArrayList();
    @FXML VBox aggiungiCamionForm;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initializeComponents();
            loadUserData();
            setupTable();
            loadPrenotazioni();
        } catch (Exception e) {
            showAlert("Errore di inizializzazione", "Errore durante l'inizializzazione: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void initializeComponents() {
        // Inizializza gli elementi dell'interfaccia utente
        categoriaBox.getItems().addAll("Daf", "Iveco", "Man", "Mercedes-Benz", "Renault", "Scania", "Volvo");

        // Nascondi le sezioni admin e altre sezioni finché non viene caricato l'utente
        adminSection.setVisible(false);
        adminSection.setManaged(false);
        aggiungiCamionForm.setVisible(false);
        aggiungiCamionForm.setManaged(false);
        reportSentLabel.setVisible(false);
    }

    private void loadUserData() throws SQLException, ExecutionException, InterruptedException, TimeoutException {
        // Supponiamo di fare una simulazione del login dell'utente.
        currentUser = new Utente("mario.rossi@email.com", "Mario", "Rossi", 3331234567L, false); // Impostazione manuale

        // Mostra le informazioni dell'utente nella GUI
        welcomeLabel.setText("Benvenuto, " + currentUser.nome() + " " + currentUser.cognome());
        emailLabel.setText(currentUser.email());
        nomeLabel.setText(currentUser.nome());
        cognomeLabel.setText(currentUser.cognome());
        telefonoLabel.setText(String.valueOf(currentUser.numero_di_telefono()));

        // Verifica se l'utente è un admin
        if (currentUser.isAdmin()) {
            // Se l'utente è admin, mostra le sezioni amministrative
            adminSection.setVisible(true);
            adminSection.setManaged(true);
        } else {
            // Se non è admin, mantieni le sezioni nascoste
            adminSection.setVisible(false);
            adminSection.setManaged(false);
        }
    }



    private void setupTable() {
        idCamionColumn.setCellValueFactory(new PropertyValueFactory<>("id_Camion"));
        dataColumn.setCellValueFactory(cellData -> {
            Prenotazione p = cellData.getValue();
            String dataFormatted = p.giorno() + "/" + p.mese() + "/" + p.anno();
            return new javafx.beans.property.SimpleStringProperty(dataFormatted);
        });
        statoColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Confermata"));
        azioniColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Modifica/Cancella"));

        prenotazioniTable.setItems(prenotazioni);
    }

    private void loadPrenotazioni() {
        prenotazioni.addAll(
                new Prenotazione(currentUser.email(), "TRK001", 15, 12, 2024),
                new Prenotazione(currentUser.email(), "TRK005", 20, 12, 2024),
                new Prenotazione(currentUser.email(), "TRK010", 25, 12, 2024)
        );
    }

    @FXML
    void changePasswordAction(ActionEvent event) {
        String password = passwordField.getText();
        String repeatPassword = repeatPasswordField.getText();

        if (password.isEmpty() || repeatPassword.isEmpty()) {
            showAlert("Errore", "Tutti i campi password sono obbligatori.", Alert.AlertType.ERROR);
            return;
        }

        if (!password.equals(repeatPassword)) {
            showAlert("Errore", "Le password non corrispondono.", Alert.AlertType.ERROR);
            return;
        }

        if (password.length() < 8) {
            showAlert("Errore", "La password deve essere di almeno 8 caratteri.", Alert.AlertType.ERROR);
            return;
        }

        try {
            passwordField.clear();
            repeatPasswordField.clear();
            showAlert("Successo", "Password cambiata con successo!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Errore", "Errore durante il cambio password: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void logoutAction(ActionEvent event) {
        showAlert("Logout", "Logout effettuato con successo.", Alert.AlertType.INFORMATION);
    }

    @FXML
    void aggiungiCamionAction(ActionEvent event) {
        aggiungiCamionForm.setVisible(true);
        aggiungiCamionForm.setManaged(true);
    }

    @FXML
    void visualizzaCamionAction(ActionEvent event) {
        showAlert("Info", "Apertura gestione camion...", Alert.AlertType.INFORMATION);
    }

    @FXML
    void salvaCamionAction(ActionEvent event) {
        String idCamion = idCamionField.getText().trim();
        String modello = modelloField.getText().trim();
        String categoria = categoriaComboBox.getValue();
        String prezzoStr = prezzoField.getText().trim();
        String descrizione = descrizioneArea.getText().trim();

        if (idCamion.isEmpty() || modello.isEmpty() || categoria == null || prezzoStr.isEmpty() || descrizione.isEmpty()) {
            showAlert("Errore di validazione", "Tutti i campi sono obbligatori.", Alert.AlertType.ERROR);
            return;
        }

        try {
            double prezzo = Double.parseDouble(prezzoStr);
            if (prezzo <= 0) throw new NumberFormatException();

            clearTruckForm();
            showAlert("Successo", "Camion aggiunto con successo!", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Errore di validazione", "Il prezzo deve essere un numero positivo.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Errore", "Errore durante l'aggiunta del camion: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void annullaAction(ActionEvent event) {
        clearTruckForm();
        aggiungiCamionForm.setVisible(false);
        aggiungiCamionForm.setManaged(false);
    }

    private void clearTruckForm() {
        idCamionField.clear();
        modelloField.clear();
        categoriaComboBox.getSelectionModel().clearSelection();
        prezzoField.clear();
        descrizioneArea.clear();
    }

    @FXML
    void sendReportAction(ActionEvent event) {
        String reportText = reportArea.getText().trim();

        if (reportText.isEmpty()) {
            showAlert("Errore", "Il campo segnalazione non può essere vuoto.", Alert.AlertType.ERROR);
            return;
        }

        try {
            reportArea.clear();
            showReportSentMessage();
            showAlert("Successo", "Segnalazione inviata con successo!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Errore", "Errore durante l'invio della segnalazione: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showReportSentMessage() {
        Thread thread = new Thread(() -> {
            try {
                javafx.application.Platform.runLater(() -> reportSentLabel.setVisible(true));
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> reportSentLabel.setVisible(false));
            } catch (InterruptedException e) {
                showAlert("Errore thread", "Errore durante l'esecuzione del thread.", Alert.AlertType.ERROR);
            }
        });
        thread.start();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML public void refreshBookingsAction(ActionEvent actionEvent) {}
    @FXML public void newBookingAction(ActionEvent actionEvent) {}
    @FXML public void addCamionAction(ActionEvent actionEvent) {}
    @FXML public void removeCamionAction(ActionEvent actionEvent) {}
    @FXML public void updateProfileAction(ActionEvent actionEvent) {}
}
