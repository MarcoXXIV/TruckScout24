package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.Database.DBConnessione;
import com.progetto.ingsw.trukscout24.Model.Camion;
import com.progetto.ingsw.trukscout24.Messaggi;
import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeController implements Initializable {

    public Button userButton;

    // Header elements
    @FXML private TextField mainSearchField;
    @FXML private Button mainSearchButton;
    @FXML private Button wishlistButton;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    // Main content
    @FXML private GridPane trucksGrid;
    @FXML private Button viewMoreButton;
    @FXML private Button showAllButton; // Pulsante per mostrare tutti i camion

    // Footer
    @FXML private Label statusLabel;

    private final SceneHandler sceneHandler = SceneHandler.getInstance();
    private final DBConnessione db = DBConnessione.getInstance();

    // Servizi e connessione
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            DBConnessione dbConnessione = DBConnessione.getInstance();
            dbConnessione.createConnection();

            // Aggiungi listener per la ricerca con INVIO
            mainSearchField.setOnAction(e -> onMainSearchAction());

            loadInitialCamions();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nella connessione al DB", e);
        }
    }

    private void loadInitialCamions() {
        statusLabel.setText("Caricamento camion...");
        db.addHomePageCamion().thenAccept(camions -> {
            Platform.runLater(() -> {
                populateGridWithCamions(camions);
                statusLabel.setText("Visualizzando tutti i camion disponibili (" + camions.size() + ")");
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("Errore nel caricamento");
                SceneHandler.getInstance().showAlert("Errore DB", Messaggi.thread_error, 0);
            });
            return null;
        });
    }

    private void populateGridWithCamions(List<Camion> allCamions) {
        // Mescola la lista di tutti i camion se non è vuota
        if (allCamions != null && !allCamions.isEmpty()) {
            Collections.shuffle(allCamions);
        }

        // Prendi i primi 12 camion dalla lista mescolata
        List<Camion> selectedCamions = allCamions != null ?
                allCamions.subList(0, Math.min(12, allCamions.size())) :
                new ArrayList<>();

        trucksGrid.getChildren().clear();
        trucksGrid.setHgap(20); // Spazio orizzontale tra colonne
        trucksGrid.setVgap(20); // Spazio verticale tra righe
        trucksGrid.setPadding(new Insets(20)); // Padding interno

        int column = 0;
        int row = 0;

        for (Camion camion : selectedCamions) {
            VBox card = createCamionCard(camion);
            trucksGrid.add(card, column, row);

            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createCamionCard(Camion camion) {
        VBox box = new VBox(5);
        box.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-width: 1;");
        box.setPrefWidth(300);

        // ImageView
        ImageView imageView = new ImageView();
        imageView.setFitHeight(250);
        imageView.setFitWidth(250);
        imageView.setPreserveRatio(true); // Mantiene le proporzioni

        String imagePath = camion.id();
        System.out.println(imagePath);

        if (imagePath != null && !imagePath.isEmpty()) {
            String fullPath = "/com/progetto/ingsw/trukscout24/immagini/" + imagePath + ".jpg";
            URL imageUrl = getClass().getResource(fullPath);

            if (imageUrl != null) {
                Image image = new Image(imageUrl.toExternalForm());
                imageView.setImage(image);
            } else {
                System.out.println("Immagine non trovata: " + fullPath);
            }
        } else {
            System.out.println("ID immagine non trovato o immagine non disponibile.");
        }

        // Label informazioni
        Label nameLabel = new Label(camion.nome());
        Label modelLabel = new Label("Modello: " + camion.modello());
        Label priceLabel = new Label("€ " + camion.prezzo());
        Label kmLabel = new Label("Km: " + camion.kilometri());
        Label yearLabel = new Label("Anno: " + camion.anno());
        Label powerLabel = new Label("Potenza: " + camion.potenza() + " CV");

        // Aggiungi immagine + info alla VBox
        box.getChildren().addAll(imageView, nameLabel, modelLabel, priceLabel, kmLabel, yearLabel, powerLabel);

        return box;
    }

    @FXML
    private void onMainSearchAction() {
        String searchText = mainSearchField.getText();
        if (!searchText.trim().isEmpty()) {
            performSearch(searchText);
        }
    }

    @FXML
    private void onMainSearchClick(ActionEvent event) {
        onMainSearchAction();
    }

    private void performSearch(String searchText) {
        System.out.println("Ricerca per: " + searchText);

        // Mostra indicatore di caricamento
        statusLabel.setText("Ricerca in corso per: " + searchText + "...");
        trucksGrid.getChildren().clear();

        // Pulisci la lista precedente
        db.clearSearchedList();

        // Esegui la ricerca
        db.searchCamion(searchText).thenAccept(resultLabel -> {
            String results = resultLabel.getText();

            if (results != null && !results.isEmpty()) {
                // Dividi gli ID dei risultati
                String[] productIds = results.split(";");

                // Filtra gli ID vuoti
                List<String> validIds = new ArrayList<>();
                for (String id : productIds) {
                    if (id != null && !id.trim().isEmpty()) {
                        validIds.add(id.trim());
                    }
                }

                if (!validIds.isEmpty()) {
                    // Carica i camion trovati
                    db.addSearchedCamion(validIds.toArray(new String[0]));

                    // Aspetta un momento per permettere al thread di completare la query
                    executorService.submit(() -> {
                        try {
                            Thread.sleep(500);

                            // Ottieni i camion cercati
                            ArrayList<Camion> searchedCamions = db.getSearchedCamion();

                            // Aggiorna la UI nel thread JavaFX
                            Platform.runLater(() -> {
                                if (searchedCamions != null && !searchedCamions.isEmpty()) {
                                    populateGridWithCamions(searchedCamions);
                                    statusLabel.setText("Trovati " + searchedCamions.size() + " risultati per: " + searchText);
                                } else {
                                    trucksGrid.getChildren().clear();
                                    statusLabel.setText("Nessun risultato trovato per: " + searchText);
                                }
                            });

                        } catch (InterruptedException e) {
                            Platform.runLater(() -> {
                                statusLabel.setText("Errore nella ricerca");
                                SceneHandler.getInstance().showAlert("Errore", "Errore durante la ricerca", 0);
                            });
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        trucksGrid.getChildren().clear();
                        statusLabel.setText("Nessun risultato trovato per: " + searchText);
                    });
                }
            } else {
                Platform.runLater(() -> {
                    trucksGrid.getChildren().clear();
                    statusLabel.setText("Nessun risultato trovato per: " + searchText);
                });
            }
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("Errore nella ricerca");
                SceneHandler.getInstance().showAlert("Errore", "Errore durante la ricerca", 0);
            });
            return null;
        });
    }

    @FXML
    private void onTruckImageClick(MouseEvent event) throws Exception {
        sceneHandler.setTruckDetailsScene();
    }

    // Metodi per gestire i click sui loghi (se sono ImageView)
    @FXML
    private void onBrandLogoClick(MouseEvent event) {
        ImageView clickedLogo = (ImageView) event.getSource();
        String brand = (String) clickedLogo.getUserData();

        if (brand != null && !brand.isEmpty()) {
            handleBrandSelection(brand);
        }
    }

    // Metodi specifici per ogni marca (ImageView)
    @FXML private void onVolvoLogoClick(MouseEvent event) { handleBrandSelection("VOLVO"); }
    @FXML private void onMercedesLogoClick(MouseEvent event) { handleBrandSelection("MERCEDES BENZ"); }
    @FXML private void onManLogoClick(MouseEvent event) { handleBrandSelection("MAN"); }
    @FXML private void onScaniaLogoClick(MouseEvent event) { handleBrandSelection("SCANIA"); }
    @FXML private void onIvecoLogoClick(MouseEvent event) { handleBrandSelection("IVECO"); }
    @FXML private void onRenaultLogoClick(MouseEvent event) { handleBrandSelection("RENAULT"); }
    @FXML private void onDafLogoClick(MouseEvent event) { handleBrandSelection("DAF"); }

    // Metodi per gestire i click sui pulsanti (se sono Button)
    @FXML
    private void onBrandClick(ActionEvent event) {
        String brand = ((Button) event.getSource()).getText();
        handleBrandSelection(brand);
    }

    // Metodi specifici per ogni marca (Button)
    @FXML private void onVolvoClick() { handleBrandSelection("VOLVO"); }
    @FXML private void onMercedesClick() { handleBrandSelection("MERCEDES BENZ"); }
    @FXML private void onManClick() { handleBrandSelection("MAN"); }
    @FXML private void onScaniaClick() { handleBrandSelection("SCANIA"); }
    @FXML private void onIvecoClick() { handleBrandSelection("IVECO"); }
    @FXML private void onRenaultClick() { handleBrandSelection("RENAULT"); }
    @FXML private void onDafClick() { handleBrandSelection("DAF"); }

    private void handleBrandSelection(String brand) {
        System.out.println("Selezionato brand: " + brand);

        // Mostra indicatore di caricamento
        statusLabel.setText("Caricamento camion " + brand + "...");
        trucksGrid.getChildren().clear();

        // Pulisci la lista precedente
        db.clearCategoryCamionList();

        // Carica i camion della categoria selezionata
        db.addCategoryCamion(brand);

        // Aspetta un momento per permettere al thread di completare la query
        // e poi aggiorna la UI
        executorService.submit(() -> {
            try {
                // Piccola pausa per permettere al database di completare la query
                Thread.sleep(500);

                // Ottieni i camion filtrati
                ArrayList<Camion> filteredCamions = db.getCategoryCamion();

                // Aggiorna la UI nel thread JavaFX
                Platform.runLater(() -> {
                    if (filteredCamions != null && !filteredCamions.isEmpty()) {
                        populateGridWithCamions(filteredCamions);
                        statusLabel.setText("Visualizzando " + filteredCamions.size() + " camion " + brand);
                    } else {
                        trucksGrid.getChildren().clear();
                        statusLabel.setText("Nessun camion trovato per la marca " + brand);
                    }
                });

            } catch (InterruptedException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Errore nel caricamento");
                    SceneHandler.getInstance().showAlert("Errore", "Errore nel caricamento dei camion", 0);
                });
            }
        });
    }

    @FXML
    private void onShowAllTrucksClick() {
        mainSearchField.clear(); // Pulisci il campo di ricerca
        loadInitialCamions(); // Ricarica tutti i camion
    }

    @FXML
    private void userClick(MouseEvent event) throws Exception {
        sceneHandler.setUtenteScene();
    }

    @FXML
    private void wishlistClick(MouseEvent event) throws Exception {
        sceneHandler.setWishlistScene();
    }

    @FXML
    private void loginClick(MouseEvent event) throws Exception {
        sceneHandler.setLoginScene();
    }

    @FXML
    private void registrazioneClick(MouseEvent event) throws Exception {
        sceneHandler.setRegistrationScene();
    }

    @FXML
    private void openTruckDetails(String truckId) throws Exception {
        sceneHandler.setTruckDetailsScene();
    }

    @FXML
    private void onViewMoreClick() {
        System.out.println("Caricamento altri camion...");
        // Implementa qui la logica di paginazione se necessario
    }

    @FXML
    public void HomeClick(MouseEvent event) throws Exception {
        sceneHandler.setHomeScene();
    }
}
