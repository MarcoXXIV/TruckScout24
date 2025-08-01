// File: UtenteController.java

package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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

    private final SceneHandler sceneHandler = SceneHandler.getInstance();

    @FXML private ImageView homeLogoImageView;
    @FXML private Button updateProfileButton;
    @FXML private TextField telefonoField;
    @FXML private Label profileUpdateStatus;
    @FXML private TextField emailField;
    @FXML private TextField cognomeField;
    @FXML private TextField nomeField;

    @FXML private TableColumn<?, ?> camionColumn;
    @FXML private Label welcomeLabel, emailLabel, nomeLabel, cognomeLabel, telefonoLabel;
    @FXML private PasswordField passwordField, repeatPasswordField;
    @FXML private Button changePasswordButton, logoutButton;

    @FXML private TableView<Prenotazione> prenotazioniTable;
    @FXML private TableColumn<Prenotazione, String> idCamionColumn, dataColumn, statoColumn, azioniColumn;

    @FXML private Button aggiungiCamionButton, visualizzaCamionButton, salvaCamionButton, annullaButton;
    @FXML private TextField idCamionField, modelloField, prezzoField;
    @FXML private ComboBox<String> categoriaComboBox;
    @FXML private TextArea descrizioneArea;


    @FXML private ScrollPane mainScrollPane;

    private Utente currentUser;
    private final ObservableList<Prenotazione> prenotazioni = FXCollections.observableArrayList();
    @FXML VBox aggiungiCamionForm;

    // Aggiungi questo metodo per gestire il click sul logo
    @FXML
    private void homeAction(MouseEvent event) throws Exception {
        sceneHandler.setHomeScene();
    }

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


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML public void refreshBookingsAction(ActionEvent actionEvent) {}
    @FXML public void newBookingAction(ActionEvent actionEvent) {}
    @FXML public void updateProfileAction(ActionEvent actionEvent) {}
}
