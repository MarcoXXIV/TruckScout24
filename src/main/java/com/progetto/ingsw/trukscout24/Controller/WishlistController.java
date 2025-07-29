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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class WishlistController implements Initializable {

    @FXML
    private Label truckCountLabel;
    @FXML
    private Button clearAllButton;
    @FXML
    private ListView<Camion> wishlistView;
    @FXML
    private VBox emptyStateBox;
    @FXML
    private Button startBrowsingButton;
    @FXML
    private HBox logoSection;
    @FXML
    private Label appTitleLabel;

    private final SceneHandler sceneHandler = SceneHandler.getInstance();
    private ObservableList<Camion> allTrucks;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupData();
        setupListView();
        updateUI();
    }


    private void setupData() {
        allTrucks = FXCollections.observableArrayList();
        addSampleData();
    }

    private void setupListView() {
        wishlistView.setItems(allTrucks);
        wishlistView.setCellFactory(createTruckCellFactory());
        wishlistView.getStyleClass().add("truck-listview");
    }

    private Callback<ListView<Camion>, ListCell<Camion>> createTruckCellFactory() {
        return listView -> new ListCell<Camion>() {
            @Override
            protected void updateItem(Camion camion, boolean empty) {
                super.updateItem(camion, empty);

                if (empty || camion == null) {
                    setGraphic(null);
                } else {
                    VBox card = createTruckCard(camion);
                    setGraphic(card);

                    // Animazione di entrata
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), card);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                }
            }
        };
    }

    private VBox createTruckCard(Camion camion) {
        VBox card = new VBox(15);
        card.getStyleClass().add("truck-card");

        // Header con HBox
        HBox headerBox = new HBox(18);
        headerBox.setStyle("-fx-alignment: center-left;");

        // Spacer iniziale (padding sinistro)
        Region leftSpacer = new Region();
        leftSpacer.setPrefWidth(25);
        HBox.setHgrow(leftSpacer, Priority.NEVER);

        // Immagine camion
        Label imageLabel = new Label("🚛");
        imageLabel.getStyleClass().add("truck-image");

        // Dettagli camion
        VBox detailsBox = new VBox(8);
        Label titleLabel = new Label(camion.modello());
        titleLabel.getStyleClass().add("truck-title");

        Label infoLabel = new Label(String.format("📅 %s • 🛣️ %,d km",
                camion.anno(), camion.kilometri().intValue()));
        infoLabel.getStyleClass().add("truck-info");

        Label priceLabel = new Label("💰 €" + String.format("%,.0f", camion.prezzo()));
        priceLabel.getStyleClass().add("truck-price");

        Label categoryLabel = new Label("📂 " + camion.categoria());
        categoryLabel.getStyleClass().add("truck-category");

        detailsBox.getChildren().addAll(titleLabel, infoLabel, priceLabel, categoryLabel);

        // Spacer centrale
        Region centerSpacer = new Region();
        centerSpacer.setPrefWidth(20);
        HBox.setHgrow(centerSpacer, Priority.ALWAYS);

        // Pulsante rimozione
        VBox buttonsBox = new VBox(12);
        buttonsBox.setStyle("-fx-alignment: center-right;");
        Button removeButton = new Button("🗑️");
        removeButton.getStyleClass().add("remove-button");
        removeButton.setTooltip(new Tooltip("Rimuovi dalla wishlist"));
        removeButton.setOnAction(e -> removeCamion(camion));
        buttonsBox.getChildren().add(removeButton);

        // Spacer finale (padding destro)
        Region rightSpacer = new Region();
        rightSpacer.setPrefWidth(25);
        HBox.setHgrow(rightSpacer, Priority.NEVER);

        headerBox.getChildren().addAll(
                leftSpacer,
                imageLabel,
                detailsBox,
                centerSpacer,
                buttonsBox,
                rightSpacer
        );
        card.getChildren().add(headerBox);

        // Effetto hover
        card.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.01);
            scale.setToY(1.01);
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


    // HANDLER SEMPLICI SENZA ANIMAZIONI COMPLICATE

    @FXML
    private void handleLogoClick(MouseEvent event) throws Exception {
        sceneHandler.setHomeScene();
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
        int count = allTrucks.size();
        truckCountLabel.setText(String.valueOf(count));

        boolean isEmpty = count == 0;
        emptyStateBox.setVisible(isEmpty);
        emptyStateBox.setManaged(isEmpty);
        wishlistView.setVisible(!isEmpty);
        wishlistView.setManaged(!isEmpty);

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
        alert.setTitle("🗑️ Rimuovi dalla Wishlist");
        alert.setHeaderText("Sei sicuro di voler rimuovere questo camion dalla wishlist?");
        alert.setContentText("🚛 " + camion.nome() + " " + camion.modello());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Animazione rimozione
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), wishlistView);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.7);
                fadeOut.setOnFinished(e -> {
                    allTrucks.remove(camion);
                    updateUI();
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), wishlistView);
                    fadeIn.setFromValue(0.7);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                fadeOut.play();
            }
        });
    }

    private void clearAllTrucks() {
        if (allTrucks.isEmpty()) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("🗑️ Svuota Wishlist");
        alert.setHeaderText("Sei sicuro di voler rimuovere tutti i camion dalla wishlist?");
        alert.setContentText("⚠️ Questa operazione non può essere annullata.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Animazione svuotamento
                FadeTransition fadeOut = new FadeTransition(Duration.millis(400), wishlistView);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    allTrucks.clear();
                    updateUI();
                });
                fadeOut.play();
            }
        });
    }

    // Aggiunta camion
    public void addTruck(Camion camion) {
        if (!allTrucks.contains(camion)) {
            allTrucks.add(camion);
            updateUI();

            // Animazione aggiunta
            Timeline delay = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), wishlistView);
                fadeIn.setFromValue(0.8);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }));
            delay.play();
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

    // Dati di esempio
    private void addSampleData() {
        LocalDateTime now = LocalDateTime.now();

        allTrucks.addAll(
                createCamionWithDate("1", "Volvo", "FH16", "2020", 180000.0, 125000, 40.0, now),
                createCamionWithDate("2", "MAN", "TGX", "2019", 85000.0, 180000, 26.0, now),
                createCamionWithDate("3", "Mercedes", "Actros", "2021", 220000.0, 95000, 35.0, now),
                createCamionWithDate("4", "Scania", "R450", "2018", 95000.0, 220000, 32.0, now),
                createCamionWithDate("5", "Iveco", "Stralis", "2020", 75000.0, 160000, 28.0, now),
                createCamionWithDate("6", "DAF", "XF", "2022", 95000.0, 45000, 28.5, now),
                createCamionWithDate("7", "Renault", "T High", "2021", 140000.0, 78000, 35.0, now)
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
