package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.Model.Camion;
import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class WishlistController implements Initializable {

    @FXML
    private Button clearAllButton;
    @FXML
    private FlowPane trucksFlowPane;
    @FXML
    private VBox emptyStateBox;
    @FXML
    private Button startBrowsingButton;

    private final SceneHandler sceneHandler = SceneHandler.getInstance();
    private ObservableList<Camion> allTrucks;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupData();
        setupFlowPane();
        updateUI();
    }

    private void setupData() {
        allTrucks = FXCollections.observableArrayList();
        addSampleData();
    }

    private void setupFlowPane() {
        // Configurazione per esattamente 2 card molto grandi per riga
        // 2 card da 750px + gap di 60px = 1560px totale
        trucksFlowPane.setPrefWrapLength(1600.0);
        trucksFlowPane.setHgap(60.0);
        trucksFlowPane.setVgap(40.0);
        trucksFlowPane.getStyleClass().add("trucks-flow");

        // Forza l'allineamento al centro
        trucksFlowPane.setStyle("-fx-alignment: center;");
    }

    private VBox createTruckCard(Camion camion) {
        // Container principale della card - LAYOUT VERTICALE
        VBox card = new VBox();
        card.getStyleClass().add("truck-card");

        // Dimensioni molto grandi per avere solo 2 per riga
        card.setPrefWidth(750);
        card.setMaxWidth(750);
        card.setMinWidth(750);
        card.setPrefHeight(420);
        card.setMinHeight(420);

        // Header con il cuore posizionato in alto a destra
        HBox cardHeader = new HBox();
        cardHeader.getStyleClass().add("card-header");
        cardHeader.setStyle("-fx-alignment: top-right; -fx-padding: 0 0 10 0;");

        // Spacer per spingere il cuore a destra
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Pulsante cuore più grande e visibile
        Button heartButton = new Button("❤️");
        heartButton.getStyleClass().add("heart-button");
        heartButton.setTooltip(new Tooltip("Rimuovi dai preferiti"));
        heartButton.setOnAction(e -> removeCamion(camion));

        // Effetti hover più evidenti per il cuore
        heartButton.setOnMouseEntered(e -> {
            heartButton.setText("💔");
            heartButton.setStyle("-fx-text-fill: #ef4444;");
        });
        heartButton.setOnMouseExited(e -> {
            heartButton.setText("❤️");
            heartButton.setStyle("-fx-text-fill: #ff6b35;");
        });

        cardHeader.getChildren().addAll(spacer, heartButton);

        // Immagine del camion - MOLTO PIÙ GRANDE E CENTRATA
        VBox imageContainer = new VBox();
        imageContainer.setStyle("-fx-alignment: center; -fx-padding: 10 0 20 0;");

        Label imageLabel = new Label("🚛");
        imageLabel.getStyleClass().add("truck-image");
        imageContainer.getChildren().add(imageLabel);

        // Contenuto sotto l'immagine - CENTRATO E PIÙ GRANDE
        VBox contentBox = new VBox();
        contentBox.getStyleClass().add("truck-content");

        // Titolo molto più grande
        Label titleLabel = new Label(camion.nome() + " " + camion.modello());
        titleLabel.getStyleClass().add("truck-title");

        // Info con icone più visibili
        Label infoLabel = new Label(String.format("📅 %s • 🛣️ %,d km",
                camion.anno(), camion.kilometri().intValue()));
        infoLabel.getStyleClass().add("truck-info");

        // Prezzo ENORME e ben visibile
        Label priceLabel = new Label("💰 €" + String.format("%,.0f", camion.prezzo()));
        priceLabel.getStyleClass().add("truck-price");

        // Categoria più visibile
        Label categoryLabel = new Label("📂 " + camion.categoria());
        categoryLabel.getStyleClass().add("truck-category");

        // Data più visibile
        Label dateLabel = new Label("🕒 Aggiunto oggi");
        dateLabel.getStyleClass().add("truck-date");

        contentBox.getChildren().addAll(titleLabel, infoLabel, priceLabel, categoryLabel, dateLabel);

        // Assemblaggio finale - LAYOUT VERTICALE
        card.getChildren().addAll(cardHeader, imageContainer, contentBox);

        // Effetti hover più evidenti per la card
        card.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });
        card.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return card;
    }

    private void refreshTrucksDisplay() {
        trucksFlowPane.getChildren().clear();

        for (Camion camion : allTrucks) {
            VBox card = createTruckCard(camion);

            // Animazione di entrata
            card.setOpacity(0.0);
            trucksFlowPane.getChildren().add(card);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), card);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    // HANDLER SEMPLICI SENZA ANIMAZIONI COMPLICATE

    @FXML
    private void HomeClick(MouseEvent event) throws Exception {
        SceneHandler.getInstance().setHomeScene();
    }

    @FXML
    private void handleClearAll() {
        clearAllTrucks();
    }

    @FXML
    private void handleStartBrowsing() throws Exception {
        sceneHandler.setHomeScene();
    }

    private void updateUI() {
        boolean isEmpty = allTrucks.isEmpty();
        emptyStateBox.setVisible(isEmpty);
        emptyStateBox.setManaged(isEmpty);
        trucksFlowPane.setVisible(!isEmpty);
        trucksFlowPane.setManaged(!isEmpty);

        if (!isEmpty) {
            refreshTrucksDisplay();
        }

        // Animazione cambio stato
        if (isEmpty) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), emptyStateBox);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    private void removeCamion(Camion camion) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("💔 Rimuovi dai Preferiti");
        alert.setHeaderText("Sei sicuro di voler rimuovere questo camion dai preferiti?");
        alert.setContentText("🚛 " + camion.nome() + " " + camion.modello());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Trova e anima la rimozione della card
                trucksFlowPane.getChildren().stream()
                        .filter(node -> node instanceof VBox)
                        .map(node -> (VBox) node)
                        .findFirst()
                        .ifPresent(card -> {
                            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), card);
                            fadeOut.setFromValue(1.0);
                            fadeOut.setToValue(0.0);
                            fadeOut.setOnFinished(e -> {
                                allTrucks.remove(camion);
                                updateUI();
                            });
                            fadeOut.play();
                        });

                // Fallback diretto
                if (trucksFlowPane.getChildren().isEmpty()) {
                    allTrucks.remove(camion);
                    updateUI();
                }
            }
        });
    }

    private void clearAllTrucks() {
        if (allTrucks.isEmpty()) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("🗑️ Elimina Tutti i Preferiti");
        alert.setHeaderText("Sei sicuro di voler rimuovere tutti i camion dai preferiti?");
        alert.setContentText("⚠️ Questa operazione non può essere annullata.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Animazione svuotamento
                FadeTransition fadeOut = new FadeTransition(Duration.millis(400), trucksFlowPane);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    allTrucks.clear();
                    updateUI();
                });
                fadeOut.play();
            }
        });
    }

    // Metodi pubblici per gestione wishlist
    public void addTruck(Camion camion) {
        if (!allTrucks.contains(camion)) {
            allTrucks.add(camion);
            updateUI();
        }
    }

    public void removeCamion(String camionId) {
        allTrucks.removeIf(truck -> truck.id().equals(camionId));
        updateUI();
    }

    public boolean containsCamion(String camionId) {
        return allTrucks.stream().anyMatch(truck -> truck.id().equals(camionId));
    }

    public ObservableList<Camion> getAllCamion() {
        return FXCollections.unmodifiableObservableList(allTrucks);
    }

    // Dati di esempio - PIÙ CAMION PER TESTARE MULTIPLE RIGHE
    private void addSampleData() {
        LocalDateTime now = LocalDateTime.now();

        allTrucks.addAll(
                createCamionWithDate("1", "Volvo", "FH16", "2020", 180000.0, 125000, 40.0, now),
                createCamionWithDate("2", "MAN", "TGX", "2019", 85000.0, 180000, 26.0, now),
                createCamionWithDate("3", "Mercedes", "Actros", "2021", 220000.0, 95000, 35.0, now),
                createCamionWithDate("4", "Scania", "R450", "2018", 95000.0, 220000, 32.0, now),
                createCamionWithDate("5", "Iveco", "Stralis", "2020", 75000.0, 160000, 28.0, now),
                createCamionWithDate("6", "DAF", "XF", "2022", 95000.0, 45000, 28.5, now)
        );
    }

    // Factory Camion con data
    private Camion createCamionWithDate(String id, String brand, String model, String year,
                                        double price, int kilometers, double payload, LocalDateTime dateAdded) {
        return new Camion(
                id,
                brand,
                model,
                480, // potenza placeholder
                (double) kilometers,
                "Diesel",      // carburante placeholder
                "Automatico",  // cambio placeholder
                6,             // classeEmissioni placeholder
                year,
                price,
                "Descrizione di esempio",
                "Categoria di esempio"
        );
    }
}
