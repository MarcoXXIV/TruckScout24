package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.View.SceneHandler;
import com.progetto.ingsw.trukscout24.Messaggi;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.progetto.ingsw.trukscout24.Model.Prenotazione;
import com.progetto.ingsw.trukscout24.Model.Utente;
import com.progetto.ingsw.trukscout24.Database.DBConnessione;

import java.net.URL;
import java.util.ResourceBundle;

public class UtenteController implements Initializable {

    private final SceneHandler sceneHandler = SceneHandler.getInstance();
    private final DBConnessione db = DBConnessione.getInstance();

    private String currentUserEmail;
    private Utente currentUser;

    @FXML private ImageView homeLogoImageView;
    @FXML private Button updateProfileButton;
    @FXML private TextField telefono, email, cognome, nome;
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
        try{
            sceneHandler.setHomeScene();
        }catch (Exception e){
            sceneHandler.showAlert("Errore", Messaggi.errore_generico, 0);
            sceneHandler.setHomeScene();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = sceneHandler.getCurrentUser();

        if (currentUser == null) {
            sceneHandler.showAlert("Errore", Messaggi.UTENTE_NON_AUTENTICATO, 0);
            try {
                sceneHandler.setHomeScene();
                return;
            } catch (Exception e){
                sceneHandler.showAlert("Errore", Messaggi.errore_generico, 0);
                try {
                    sceneHandler.setHomeScene();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        currentUserEmail = currentUser.email();

        try {
            setupTable();
            loadUserData();
        } catch (Exception e) {
            sceneHandler.showAlert("Errore", Messaggi.ERRORE_GENERICO, 0);
        }
    }

    private void loadUserData() {
        if (currentUserEmail == null) {
            sceneHandler.showAlert("Errore", Messaggi.EMAIL_NON_DISPONIBILE, 0);
            return;
        }

        db.setUser(currentUserEmail).thenAccept(user -> {
            if (user == null) {
                sceneHandler.showAlert("Errore", Messaggi.UTENTE_NON_TROVATO, 0);
                return;
            } else {
                javafx.application.Platform.runLater(() -> {
                    email.setText(user.email());
                    nome.setText(user.nome());
                    cognome.setText(user.cognome());
                    String telefonoText = user.numero_di_telefono() != null ? String.valueOf(user.numero_di_telefono()) : "";
                    telefono.setText(telefonoText);
                });
            }
            loadPrenotazioni();
        }).exceptionally(ex -> {
            sceneHandler.showAlert("Errore", Messaggi.CARICAMENTO_UTENTE_FALLITO + ex.getMessage(), 0);
            return null;
        });
    }

    private void setupTable() {
        idCamionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().nome_camion()));
        dataColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().giorno() + "/" + cellData.getValue().mese() + "/" + cellData.getValue().anno()));
        statoColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Confermata"));
        azioniColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Cancella"));

        azioniColumn.setCellFactory(column -> new TableCell<Prenotazione, String>() {
            private final Button btn = new Button("Cancella");

            {
                btn.getStyleClass().add("danger-btn");
                btn.setOnAction(event -> {
                    Prenotazione prenotazione = getTableRow().getItem();
                    if (prenotazione != null) {
                        deletePrenotazione(prenotazione);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    setGraphic(btn);
                } else {
                    setGraphic(null);
                }
            }
        });

        prenotazioniTable.setItems(prenotazioni);
    }

    private void deletePrenotazione(Prenotazione prenotazione) {
        db.removeSelectedPrenotazioniItem(prenotazione.nome_camion(), prenotazione.id_utente());
        sceneHandler.showAlert("Errore", Messaggi.PRENOTAZIONE_CANCELLATA, 0);
        loadPrenotazioni();
    }

    private void loadPrenotazioni() {
        if (currentUserEmail == null) return;

        db.getPrenotazione(currentUserEmail).thenAccept(lista -> {
            javafx.application.Platform.runLater(() -> {
                prenotazioni.clear();
                if (lista != null) {
                    prenotazioni.addAll(lista);
                }
            });
        }).exceptionally(ex -> {
            sceneHandler.showAlert("Errore", Messaggi.PRENOTAZIONI_ERROR + ex.getMessage(), 0);
            return null;
        });
    }

    @FXML
    void changePasswordAction(ActionEvent event) {
        String password = passwordField.getText();
        String repeatPassword = repeatPasswordField.getText();

        if (password.isEmpty() || repeatPassword.isEmpty()) {
            sceneHandler.showAlert("Errore", Messaggi.CAMPI_PASSWORD_OBBLIGATORI,0);
            return;
        }

        if (!password.equals(repeatPassword)) {
            sceneHandler.showAlert("Errore", Messaggi.PASSWORD_NON_COINCIDONO, 0);
            return;
        }

        if (password.length() < 6) {
            sceneHandler.showAlert("Errore", Messaggi.PASSWORD_TROPPO_CORTA, 0);
            return;
        }

        if (currentUserEmail == null) {
            sceneHandler.showAlert("Errore", Messaggi.UTENTE_NON_IDENTIFICATO, 0);
            return;
        }

        String encryptedPassword = db.encryptedPassword(password);

        db.updatePassword(currentUserEmail, encryptedPassword);

        passwordField.clear();
        repeatPasswordField.clear();

        sceneHandler.showAlert("Info", Messaggi.PASSWORD_CAMBIATA, 1);
    }

    @FXML
    void logoutAction(ActionEvent event) throws Exception {
        sceneHandler.logoutUser();
        sceneHandler.showAlert("Info", Messaggi.LOGOUT, 1);
        sceneHandler.setHomeScene();
    }

    @FXML
    void aggiungiCamionAction(ActionEvent event) {
        aggiungiCamionForm.setVisible(true);
        aggiungiCamionForm.setManaged(true);
    }

    @FXML public void refreshBookingsAction(ActionEvent actionEvent) {
        loadPrenotazioni();
    }

    @FXML public void newBookingAction(ActionEvent actionEvent) {
    }

    @FXML public void updateProfileAction(ActionEvent actionEvent) {
    }
}
