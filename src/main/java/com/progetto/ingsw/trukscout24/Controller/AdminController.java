package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.Database.DBConnessione;
import com.progetto.ingsw.trukscout24.Model.Prenotazione;
import com.progetto.ingsw.trukscout24.Messaggi;
import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class AdminController implements Initializable {

    private final DBConnessione dbconnessione = DBConnessione.getInstance();
    private final SceneHandler sceneHandler = SceneHandler.getInstance();

    @FXML private Rectangle footerBackground;
    @FXML private ImageView logoImageView;

    @FXML private TextField idCamionField;
    @FXML private TextField nomeCamionField;
    @FXML private TextField modelloCamionField;
    @FXML private TextField annoCamionField;
    @FXML private TextField potenzaCamionField;
    @FXML private TextField kilometriCamionField;
    @FXML private ComboBox<String> carburanteCamionCombo;
    @FXML private ComboBox<String> cambioCamionCombo;
    @FXML private ComboBox<Integer> classeEmissioniCombo;
    @FXML private ComboBox<String> categoriaCamionCombo;
    @FXML private TextField prezzoCamionField;
    @FXML private TextField chiaviCamionField;
    @FXML private TextArea descrizioneCamionArea;
    @FXML private Button aggiungiCamionBtn;
    @FXML private Button clearFormBtn;

    @FXML private ImageView previewImageView;
    @FXML private Button selezionaImmagineBtn;
    @FXML private Label nomeImmagineLabel;

    private File selectedImageFile;

    @FXML private TextField idCamionRimuoviField;
    @FXML private Button rimuoviCamionBtn;

    @FXML private TableView<Prenotazione> prenotazioniTable;
    @FXML private TableColumn<Prenotazione, String> idUtenteColumn;
    @FXML private TableColumn<Prenotazione, String> nomeCamionColumn;
    @FXML private TableColumn<Prenotazione, String> dataColumn;
    @FXML private TableColumn<Prenotazione, String> statoColumn;
    @FXML private Button refreshPrenotazioniBtn;

    private final ObservableList<Prenotazione> prenotazioniData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupTable();
        loadPrenotazioni();
        setupImagePreview();
    }

    private void setupImagePreview() {
        previewImageView.setFitHeight(150);
        previewImageView.setFitWidth(200);
        previewImageView.setPreserveRatio(true);
        previewImageView.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 2; -fx-border-style: dashed;");

        nomeImmagineLabel.setText("Nessuna immagine selezionata");
    }

    @FXML
    private void selezionaImmagine() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Immagine Camion");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(selezionaImmagineBtn.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            nomeImmagineLabel.setText(file.getName());
            nomeImmagineLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

            try {
                Image image = new Image(file.toURI().toString());
                previewImageView.setImage(image);
            } catch (Exception e) {
                showAlert("Errore", Messaggi.admin_immagine_error, Alert.AlertType.ERROR);
            }
        }
    }

    private boolean salvaImmagine(String idCamion) {
        if (selectedImageFile == null) {
            return true; // Nessuna immagine da salvare, ma non è un errore
        }

        try {
            Path resourcesDir = Paths.get("src/main/resources/com/progetto/ingsw/trukscout24/immagini/");
            if (!Files.exists(resourcesDir)) {
                Files.createDirectories(resourcesDir);
            }

            String fileName = selectedImageFile.getName();
            String extension = "";
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot > 0) {
                extension = fileName.substring(lastDot).toLowerCase();
            } else {
                extension = ".jpg"; // Default se non c'è estensione
            }

            String[] supportedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
            String finalExtension = extension;
            boolean isSupported = Arrays.stream(supportedExtensions)
                    .anyMatch(ext -> ext.equals(finalExtension));

            if (!isSupported) {
                extension = ".jpg"; // Forza JPG se l'estensione non è supportata
            }

            String destinationFileName = idCamion + extension;
            Path destinationPath = resourcesDir.resolve(destinationFileName);

            rimuoviImmagineEsistente(idCamion);

            Files.copy(selectedImageFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Immagine salvata: " + destinationPath.toString());
            return true;

        } catch (IOException e) {
            System.err.println("Errore nel salvataggio dell'immagine: " + e.getMessage());
            Platform.runLater(() ->
                    showAlert("Errore Immagine", "Impossibile salvare l'immagine: " + e.getMessage(), Alert.AlertType.ERROR)
            );
            return false;
        }
    }

    private void rimuoviImmagineEsistente(String idCamion) {
        try {
            Path resourcesDir = Paths.get("src/main/resources/com/progetto/ingsw/trukscout24/immagini/");
            if (Files.exists(resourcesDir)) {
                String[] extensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
                for (String ext : extensions) {
                    Path imagePath = resourcesDir.resolve(idCamion + ext);
                    if (Files.exists(imagePath)) {
                        Files.delete(imagePath);
                        System.out.println("Immagine precedente rimossa: " + imagePath);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nella rimozione dell'immagine esistente: " + e.getMessage());
        }
    }

    private void setupComboBoxes() {
        carburanteCamionCombo.setItems(FXCollections.observableArrayList(
                "DESEL", "GPL", "Metano", "Ibrido"
        ));

        cambioCamionCombo.setItems(FXCollections.observableArrayList(
                "MANUALE", "AUTOMATICO", "SEMI-AUTOMATICO"
        ));

        classeEmissioniCombo.setItems(FXCollections.observableArrayList(
                1, 2, 3, 4, 5, 6
        ));

        categoriaCamionCombo.setItems(FXCollections.observableArrayList(
                "DAF", "IVECO", "MERCEDES BENZ", "MAN", "RENAULT", "SCANIA", "VOLVO"
        ));
    }

    private void setupTable() {
        nomeCamionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().nome_camion())
        );

        idUtenteColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().id_utente())
        );

        dataColumn.setCellValueFactory(cellData -> {
            Prenotazione p = cellData.getValue();
            String dataFormatted = String.format("%02d/%02d/%04d", p.giorno(), p.mese(), p.anno());
            return new SimpleStringProperty(dataFormatted);
        });

        statoColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("Confermata")
        );
        prenotazioniTable.setItems(prenotazioniData);
    }

    @FXML
    private void goToHome() {
        try {
            sceneHandler.setHomeScene();
        } catch (Exception e) {
            showAlert("Errore", Messaggi.admin_home_error + ": " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void aggiungiCamion() {
        if (!validateForm()) {
            return;
        }

        String idCamion = idCamionField.getText().trim();

        aggiungiCamionBtn.setDisable(true);
        aggiungiCamionBtn.setText("Aggiungendo...");

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    String nomeCamion = nomeCamionField.getText().trim();
                    String modelloCamion = modelloCamionField.getText().trim();
                    Integer potenzaCamion = Integer.parseInt(potenzaCamionField.getText().trim());
                    Double kilometriCamion = Double.parseDouble(kilometriCamionField.getText().trim());
                    String carburanteCamion = carburanteCamionCombo.getValue();
                    String cambioCamion = cambioCamionCombo.getValue();
                    Integer classeEmissioniCamion = classeEmissioniCombo.getValue();
                    String annoCamion = annoCamionField.getText().trim();
                    Double prezzoCamion = Double.parseDouble(prezzoCamionField.getText().trim());
                    String descrizioneCamion = descrizioneCamionArea.getText().trim();
                    String categoriaCamion = categoriaCamionCombo.getValue();
                    String chiaviCamion = chiaviCamionField.getText().trim();

                    boolean dbSuccess = dbconnessione.aggiungiCamion(idCamion, nomeCamion, modelloCamion,
                            potenzaCamion, kilometriCamion, carburanteCamion, cambioCamion,
                            classeEmissioniCamion, annoCamion, prezzoCamion, descrizioneCamion,
                            categoriaCamion, chiaviCamion);

                    if (dbSuccess) {
                        boolean imageSuccess = salvaImmagine(idCamion);
                        if (!imageSuccess && selectedImageFile != null) {
                            // Se l'immagine non è stata salvata ma era selezionata, avvisa l'utente
                            Platform.runLater(() ->
                                    showAlert("Avviso", Messaggi.admin_immagine_save_warning, Alert.AlertType.WARNING)
                            );
                        }
                        return true;
                    }

                    return false;

                } catch (NumberFormatException e) {
                    Platform.runLater(() -> showAlert("Errore Validazione",
                            Messaggi.admin_numeric_validation_error, Alert.AlertType.ERROR));
                    return false;
                }
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    aggiungiCamionBtn.setDisable(false);
                    aggiungiCamionBtn.setText("Aggiungi Camion");

                    if (getValue()) {
                        showAlert("Successo", Messaggi.admin_camion_aggiunto_success, Alert.AlertType.INFORMATION);
                        clearForm();
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    aggiungiCamionBtn.setDisable(false);
                    aggiungiCamionBtn.setText("Aggiungi Camion");
                    showAlert("Errore", Messaggi.admin_camion_aggiunta_error, Alert.AlertType.ERROR);
                });
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void rimuoviCamion() {
        String idCamion = idCamionRimuoviField.getText().trim();

        if (idCamion.isEmpty()) {
            showAlert("Errore Validazione", Messaggi.admin_id_camion_empty_error, Alert.AlertType.ERROR);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(Messaggi.admin_conferma_rimozione_title);
        alert.setHeaderText(Messaggi.admin_conferma_rimozione_header);
        alert.setContentText("ID Camion: " + idCamion);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            showAlert("Operazione annullata", Messaggi.admin_operazione_annullata, Alert.AlertType.INFORMATION);
            return;
        }

        rimuoviCamionBtn.setDisable(true);
        rimuoviCamionBtn.setText("Rimuovendo...");

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                System.out.println("Tentativo rimozione camion ID: " + idCamion);

                rimuoviImmagineEsistente(idCamion);

                boolean risultato = dbconnessione.rimuoviCamion(idCamion);
                System.out.println("Risultato rimozione DB: " + risultato);

                return risultato;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    rimuoviCamionBtn.setDisable(false);
                    rimuoviCamionBtn.setText("Rimuovi Camion");

                    if (getValue()) {
                        showAlert("Successo", Messaggi.admin_camion_rimosso_success, Alert.AlertType.INFORMATION);
                        idCamionRimuoviField.clear();
                        loadPrenotazioni();
                    } else {
                        showAlert("Attenzione", Messaggi.admin_camion_non_trovato, Alert.AlertType.WARNING);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    rimuoviCamionBtn.setDisable(false);
                    rimuoviCamionBtn.setText("Rimuovi Camion");

                    Throwable exception = getException();
                    String errorMessage = Messaggi.admin_rimozione_error;
                    if (exception != null) {
                        errorMessage += ": " + exception.getMessage();
                        exception.printStackTrace();
                    }

                    showAlert("Errore", errorMessage, Alert.AlertType.ERROR);
                });
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void clearForm() {
        idCamionField.clear();
        nomeCamionField.clear();
        modelloCamionField.clear();
        annoCamionField.clear();
        potenzaCamionField.clear();
        kilometriCamionField.clear();
        carburanteCamionCombo.setValue(null);
        cambioCamionCombo.setValue(null);
        classeEmissioniCombo.setValue(null);
        categoriaCamionCombo.setValue(null);
        prezzoCamionField.clear();
        chiaviCamionField.clear();
        descrizioneCamionArea.clear();

        // Pulisci anche l'immagine
        selectedImageFile = null;
        previewImageView.setImage(null);
        nomeImmagineLabel.setText("Nessuna immagine selezionata");
        nomeImmagineLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");
    }

    @FXML
    private void refreshPrenotazioni() {
        loadPrenotazioni();
    }

    private void loadPrenotazioni() {
        refreshPrenotazioniBtn.setDisable(true);
        refreshPrenotazioniBtn.setText("Caricando...");

        Task<ArrayList<Prenotazione>> task = new Task<ArrayList<Prenotazione>>() {
            @Override
            protected ArrayList<Prenotazione> call() throws Exception {
                try {
                    CompletableFuture<ArrayList<Prenotazione>> future = dbconnessione.getPrenotazioniAdmin();
                    return future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    prenotazioniData.clear();
                    if (getValue() != null) {
                        prenotazioniData.addAll(getValue());
                    }
                    refreshPrenotazioniBtn.setDisable(false);
                    refreshPrenotazioniBtn.setText("Aggiorna");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    refreshPrenotazioniBtn.setDisable(false);
                    refreshPrenotazioniBtn.setText("Aggiorna");
                    showAlert("Errore", Messaggi.admin_caricamento_prenotazioni_error, Alert.AlertType.ERROR);
                });
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private boolean validateForm() {
        if (idCamionField.getText().trim().isEmpty() ||
                nomeCamionField.getText().trim().isEmpty() ||
                modelloCamionField.getText().trim().isEmpty() ||
                annoCamionField.getText().trim().isEmpty() ||
                potenzaCamionField.getText().trim().isEmpty() ||
                kilometriCamionField.getText().trim().isEmpty() ||
                carburanteCamionCombo.getValue() == null ||
                cambioCamionCombo.getValue() == null ||
                classeEmissioniCombo.getValue() == null ||
                categoriaCamionCombo.getValue() == null ||
                prezzoCamionField.getText().trim().isEmpty() ||
                chiaviCamionField.getText().trim().isEmpty() ||
                descrizioneCamionArea.getText().trim().isEmpty()) {

            showAlert("Errore Validazione", Messaggi.admin_form_validation_error, Alert.AlertType.ERROR);
            return false;
        }

        try {
            Integer.parseInt(potenzaCamionField.getText().trim());
            Double.parseDouble(kilometriCamionField.getText().trim());
            Double.parseDouble(prezzoCamionField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Errore Validazione", Messaggi.admin_numeric_validation_error, Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
