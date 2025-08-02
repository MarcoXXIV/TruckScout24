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
import com.progetto.ingsw.trukscout24.Model.Prenotazione;
import com.progetto.ingsw.trukscout24.Database.DBConnessione;

import java.net.URL;
import java.util.ResourceBundle;

public class UtenteController implements Initializable {

    private final SceneHandler sceneHandler = SceneHandler.getInstance();
    private final String currentUserEmail = sceneHandler.getCurrentUserEmail();
    private final DBConnessione db = DBConnessione.getInstance();

    @FXML private ImageView homeLogoImageView;
    @FXML private Button updateProfileButton;
    @FXML private TextField telefono, email,cognome, nome;
    @FXML private Label profileUpdateStatus;

    @FXML private TableColumn<?, ?> camionColumn;
    @FXML private PasswordField passwordField, repeatPasswordField;
    @FXML private Button changePasswordButton, logoutButton;

    @FXML private TableView<Prenotazione> prenotazioniTable;
    @FXML private TableColumn<Prenotazione, String> idCamionColumn, dataColumn, statoColumn, azioniColumn;

    @FXML private Button aggiungiCamionButton, visualizzaCamionButton, salvaCamionButton, annullaButton;
    @FXML private TextField idCamionField, modelloField, prezzoField;
    @FXML private ComboBox<String> categoriaComboBox;
    @FXML private TextArea descrizioneArea;

    @FXML private ScrollPane mainScrollPane;

    private final ObservableList<Prenotazione> prenotazioni = FXCollections.observableArrayList();
    @FXML VBox aggiungiCamionForm;

    @FXML
    private void homeAction(MouseEvent event) throws Exception {
        sceneHandler.setHomeScene();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initializeComponents();
            setupTable();
            loadUserData();
        } catch (Exception e) {
            showAlert("Errore di inizializzazione", "Errore durante l'inizializzazione: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void initializeComponents() {
        // Inizializza altri componenti se necessario
    }

    private void loadUserData() {
        db.setUser(currentUserEmail).thenAccept(user -> {
            if (currentUserEmail == null) {
                showAlert("Errore", "Utente non trovato", Alert.AlertType.ERROR);
                return;
            }else{
                javafx.application.Platform.runLater(() -> {
                    email.setText(user.email());
                    nome.setText(user.nome());
                    cognome.setText(user.cognome());
                    telefono.setText(String.valueOf(user.numero_di_telefono()));
                });}
            loadPrenotazioni(); // carica le prenotazioni solo dopo aver caricato l'utente
        }).exceptionally(ex -> {
            showAlert("Errore", "Errore nel caricamento utente: " + ex.getMessage(), Alert.AlertType.ERROR);
            return null;
        });
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
        if (currentUserEmail== null) return;

        db.getPrenotazione(currentUserEmail).thenAccept(lista -> {
            javafx.application.Platform.runLater(() -> {
                prenotazioni.clear();
                prenotazioni.addAll(lista);
            });
        }).exceptionally(ex -> {
            showAlert("Errore", "Errore nel caricamento prenotazioni: " + ex.getMessage(), Alert.AlertType.ERROR);
            return null;
        });
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

        // Cripta la password prima di aggiornarla nel DB
        String encryptedPassword = db.encryptedPassword(password);

        // Aggiorna la password criptata nel database
        db.updatePassword(currentUserEmail, encryptedPassword);

        // Pulisci i campi delle password
        passwordField.clear();
        repeatPasswordField.clear();

        showAlert("Successo", "Password cambiata con successo!", Alert.AlertType.INFORMATION);
    }


    @FXML
    void logoutAction(ActionEvent event) throws Exception {
        sceneHandler.setCurrentUserEmail(null);
        showAlert("Logout", "Logout effettuato con successo.", Alert.AlertType.INFORMATION);
        sceneHandler.setHomeScene();
    }

    @FXML
    void aggiungiCamionAction(ActionEvent event) {
        aggiungiCamionForm.setVisible(true);
        aggiungiCamionForm.setManaged(true);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML public void refreshBookingsAction(ActionEvent actionEvent) {
        loadPrenotazioni();
    }

    @FXML public void newBookingAction(ActionEvent actionEvent) {
        // Da implementare in seguito
    }

    @FXML public void updateProfileAction(ActionEvent actionEvent) {
        // Da implementare in seguito
    }
}
