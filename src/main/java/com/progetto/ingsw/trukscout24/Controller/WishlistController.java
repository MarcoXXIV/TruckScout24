package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.Model.Camion;
import com.progetto.ingsw.trukscout24.Model.Utente;
import com.progetto.ingsw.trukscout24.Database.DBConnessione;
import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.animation.*;
import com.progetto.ingsw.trukscout24.Messaggi;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class WishlistController implements Initializable {

    @FXML private Label statusLabel;
    @FXML private Button clearAllButton, startBrowsingButton;
    @FXML private FlowPane trucksFlowPane;
    @FXML private VBox emptyStateBox;
    @FXML private Label wishlistCountLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final SceneHandler scenehandler = SceneHandler.getInstance();
    private final DBConnessione dbconnessione = DBConnessione.getInstance();
    private ObservableList<Camion> wishlistTrucks;
    private String currentUserEmail;
    private Utente currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        wishlistTrucks = FXCollections.observableArrayList();
        setupFlowPane();

        currentUser = scenehandler.getCurrentUser();

        if (currentUser != null) {
            currentUserEmail = currentUser.email();
            loadWishlistFromDatabase();
        } else {
            showEmptyState();
        }
    }

    private void setupFlowPane() {
        trucksFlowPane.setPrefWrapLength(1440);
        trucksFlowPane.setHgap(30);
        trucksFlowPane.setVgap(50);
        trucksFlowPane.setAlignment(Pos.CENTER);
    }

    private void loadWishlistFromDatabase() {
        showLoadingState(true);

        Task<ArrayList<Camion>> loadTask = new Task<ArrayList<Camion>>() {
            @Override
            protected ArrayList<Camion> call() throws Exception {
                CompletableFuture<ArrayList<Camion>> future = dbconnessione.getWishlist(currentUserEmail);
                return future.get();
            }
        };

        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                ArrayList<Camion> loadedTrucks = loadTask.getValue();
                wishlistTrucks.clear();
                if (loadedTrucks != null) {
                    wishlistTrucks.addAll(loadedTrucks);
                }
                showLoadingState(false);
                updateUI();
            });
        });

        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                showLoadingState(false);
                scenehandler.showAlert("Errore",Messaggi.WISHLIST_CARICAMENTO_ERRORE, 0);
                showEmptyState();
            });
        });

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void showLoadingState(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
            loadingIndicator.setManaged(show);
        }
        trucksFlowPane.setVisible(!show);
        emptyStateBox.setVisible(!show && wishlistTrucks.isEmpty());
    }

    private VBox createTruckCard(Camion camion) {
        VBox card = new VBox();
        card.getStyleClass().add("truck-card-compact");
        card.setPrefWidth(480);
        card.setAlignment(Pos.CENTER);
        card.setSpacing(10);
        card.setPadding(new Insets(10));

        card.setOnMouseClicked(event -> {
            if (event.getTarget() instanceof Button) return;
            try {
                System.out.println("Apertura dettagli camion: " + camion.nome());
            } catch (Exception e) {
                scenehandler.showAlert("Errore",Messaggi.WISHLIST_CAMION_ERRORE_APERTURA, 0);
            }
        });

        HBox cardHeader = new HBox();
        cardHeader.getStyleClass().add("card-header-compact");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button heartButton = createHeartButton(camion);
        cardHeader.getChildren().addAll(spacer, heartButton);

        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("truck-image-compact");
        imageView.setFitHeight(220);
        imageView.setFitWidth(440);
        imageView.setPreserveRatio(true);

        String imagePath = camion.id();
        boolean imageLoaded = false;

        if (imagePath != null && !imagePath.isEmpty()) {
            String fullPath = "/com/progetto/ingsw/trukscout24/immagini/" + imagePath + ".jpg";
            URL imageUrl = getClass().getResource(fullPath);

            if (imageUrl != null) {
                Image image = new Image(imageUrl.toExternalForm());
                imageView.setImage(image);
                imageLoaded = true;
            } else {
                scenehandler.showAlert("Errore", Messaggi.immagine_non_trovata,0);
            }
        }

        if (!imageLoaded) {
            scenehandler.showAlert("Errore", Messaggi.immagine_non_trovata,0);
        }

        imageView.setOnMouseClicked(event -> {
            try {
                openProductView(camion);
            } catch (Exception e) {
                scenehandler.showAlert("Errore",Messaggi.WISHLIST_CAMION_ERRORE_APERTURA, 0);
            }
            event.consume();
        });

        Label nameLabel = new Label(camion.nome());
        nameLabel.getStyleClass().add("truck-name-compact");

        Label priceLabel = new Label("💰 " + camion.prezzo() + " €");
        Label kmLabel = new Label("🛣 " + camion.kilometri() + " km");
        Label yearLabel = new Label("📅 " + camion.anno());
        Label powerLabel = new Label("⚡ " + camion.potenza() + " CV");

        priceLabel.getStyleClass().add("truck-price-compact");
        kmLabel.getStyleClass().add("truck-info-compact");
        yearLabel.getStyleClass().add("truck-info-compact");
        powerLabel.getStyleClass().add("truck-info-compact");

        HBox infoRow = new HBox();
        infoRow.getStyleClass().add("info-row-compact");
        infoRow.getChildren().addAll(priceLabel, kmLabel, yearLabel, powerLabel);

        VBox infoBox = new VBox();
        infoBox.getStyleClass().add("info-box-compact");
        infoBox.getChildren().addAll(nameLabel, infoRow);

        card.getChildren().addAll(cardHeader, imageView, infoBox);
        return card;
    }

    private void openProductView(Camion camion) throws Exception {
        scenehandler.setSelectedCamion(camion);
        scenehandler.setProductViewScene();
    }

    private Button createHeartButton(Camion camion) {
        Button heartButton = new Button("♥");
        heartButton.getStyleClass().add("heart-button-filled");
        heartButton.setUserData(true);
        heartButton.setTooltip(new Tooltip("Rimuovi dai preferiti"));

        heartButton.setOnAction(e -> {
            e.consume();
            removeCamionFromWishlist(camion);
        });

        return heartButton;
    }

    private void refreshTrucksDisplay() {
        trucksFlowPane.getChildren().clear();

        for (Camion camion : wishlistTrucks) {
            VBox card = createTruckCard(camion);
            card.setOpacity(0.0);
            trucksFlowPane.getChildren().add(card);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), card);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    private void updateUI() {
        boolean isEmpty = wishlistTrucks.isEmpty();

        emptyStateBox.setVisible(isEmpty);
        emptyStateBox.setManaged(isEmpty);
        trucksFlowPane.setVisible(!isEmpty);
        trucksFlowPane.setManaged(!isEmpty);

        if (wishlistCountLabel != null) {
            wishlistCountLabel.setText(String.format("(%d/6)", wishlistTrucks.size()));
        }

        if (!isEmpty) {
            refreshTrucksDisplay();
        } else {
            showEmptyState();
        }
    }

    private void showEmptyState() {
        if (emptyStateBox != null) {
            emptyStateBox.setVisible(true);
            emptyStateBox.setManaged(true);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), emptyStateBox);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    @FXML
    private void HomeClick(MouseEvent event) throws Exception {
        try{
            scenehandler.setHomeScene();
        }catch (Exception e){
            scenehandler.showAlert("Errore", Messaggi.errore_generico, 0);
            scenehandler.setHomeScene();
        }
    }

    @FXML
    private void handleClearAll() {
        if (wishlistTrucks.isEmpty()) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("🗑️ Elimina Tutti i Preferiti");
        alert.setHeaderText("Sei sicuro di voler rimuovere tutti i camion dai preferiti?");
        alert.setContentText("⚠️ Questa operazione rimuoverà tutti i " + wishlistTrucks.size() + " camion dalla tua wishlist.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                clearAllFromDatabase();
            }
        });
    }

    @FXML
    private void handleStartBrowsing() throws Exception {
        try{
            scenehandler.setHomeScene();
        }catch (Exception e){
            scenehandler.showAlert("Errore", Messaggi.errore_generico, 0);
            scenehandler.setHomeScene();
        }
    }

    private void removeCamionFromWishlist(Camion camion) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("💔 Rimuovi dai Preferiti");
        alert.setHeaderText("Sei sicuro di voler rimuovere questo camion dai preferiti?");
        alert.setContentText("🚛 " + camion.nome() + " " + camion.modello());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                removeCamionFromDatabase(camion);
            }
        });
    }

    private void removeCamionFromDatabase(Camion camion) {
        Task<Boolean> removeTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return dbconnessione.removeCamionFromWishlist(currentUserEmail, camion.id());
            }
        };

        removeTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                if (removeTask.getValue()) {
                    animateCardRemoval(camion);
                } else {
                    scenehandler.showAlert("Errore",Messaggi.WISHLIST_RIMOZIONE_ERRORE, 0);
                }
            });
        });

        removeTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                scenehandler.showAlert("Errore",Messaggi.WISHLIST_RIMOZIONE_CAMION_ERRORE, 0);
            });
        });

        Thread removeThread = new Thread(removeTask);
        removeThread.setDaemon(true);
        removeThread.start();
    }

    private void animateCardRemoval(Camion camion) {
        trucksFlowPane.getChildren().stream()
                .filter(node -> node instanceof VBox)
                .map(node -> (VBox) node)
                .findFirst()
                .ifPresent(card -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), card);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(e -> {
                        wishlistTrucks.remove(camion);
                        updateUI();
                    });
                    fadeOut.play();
                });

        if (!wishlistTrucks.remove(camion)) {
            updateUI();
        }
    }

    private void clearAllFromDatabase() {
        Task<Boolean> clearTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return dbconnessione.clearWishlist(currentUserEmail);
            }
        };

        clearTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                if (clearTask.getValue()) {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(400), trucksFlowPane);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(event -> {
                        wishlistTrucks.clear();
                        updateUI();
                    });
                    fadeOut.play();
                } else {
                    scenehandler.showAlert("Errore",Messaggi.WISHLIST_SVUOTAMENTO_ERRORE, 0);
                }
            });
        });

        clearTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                scenehandler.showAlert("Errore",Messaggi.WISHLIST_ERRORE, 0);
            });
        });

        Thread clearThread = new Thread(clearTask);
        clearThread.setDaemon(true);
        clearThread.start();
    }

    public void addTruckToWishlist(Camion camion) {
        if (currentUser == null) {
            scenehandler.showAlert("Errore",Messaggi.WISHLIST_CAMION_ERRORE_AGGIUNTA, 0);
            return;
        }

        if (wishlistTrucks.size() >= 6) {
            scenehandler.showAlert("Errore",Messaggi.WISHLIST_LIMITE_RAGGIUNTO, 0);
            return;
        }

        if (containsCamion(camion.id())) {
            scenehandler.showAlert("Errore",Messaggi.WISHLIST_CAMION_GIA_PRESENTE, 0);
            return;
        }

        Task<Boolean> addTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return dbconnessione.insertWishlistCamionIntoDB(currentUserEmail, camion.id());
            }
        };

        addTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                if (addTask.getValue()) {
                    wishlistTrucks.add(camion);
                    updateUI();
                    scenehandler.showAlert("Successo",Messaggi.WISHLIST_CAMION_AGGIUNTO, 1);
                } else {
                    scenehandler.showAlert("Errore", Messaggi.WISHLIST_CAMION_ERRORE_AGGIUNTA, 0);
                }
            });
        });

        addTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                scenehandler.showAlert("Errore",Messaggi.WISHLIST_ERRORE, 0);
            });
        });

        Thread addThread = new Thread(addTask);
        addThread.setDaemon(true);
        addThread.start();
    }

    public boolean containsCamion(String camionId) {
        return wishlistTrucks.stream().anyMatch(truck -> truck.id().equals(camionId));
    }

    public ObservableList<Camion> getAllCamion() {
        return FXCollections.unmodifiableObservableList(wishlistTrucks);
    }

    public int getWishlistSize() {
        return wishlistTrucks.size();
    }

    public boolean isWishlistFull() {
        return wishlistTrucks.size() >= 6;
    }

    public void refreshWishlist() {
        loadWishlistFromDatabase();
    }


    @FXML private void onInfoAppClick(MouseEvent event) throws Exception {
        try{
            scenehandler.showAlert("Informazioni sull'app", Messaggi.app_information, 1);
        }catch (Exception e){
            scenehandler.showAlert("Errore", Messaggi.errore_generico, 0);
            scenehandler.setHomeScene();
        }
    }

    @FXML private void onPrivacyClick(MouseEvent event) throws Exception {
        try {
            scenehandler.showAlert("Privacy", Messaggi.privacy_information, 1);
        } catch (Exception e) {
            scenehandler.showAlert("Errore", Messaggi.errore_generico, 0);
            scenehandler.setHomeScene();
        }
    }

    @FXML private void onCondizioniClick(MouseEvent event) throws Exception {
        try{
            scenehandler.showAlert("Condizioni generali", Messaggi.general_condition, 1);
        } catch (Exception e){
            scenehandler.showAlert("Errore", Messaggi.errore_generico, 0);
            scenehandler.setHomeScene();
        }
    }
}
