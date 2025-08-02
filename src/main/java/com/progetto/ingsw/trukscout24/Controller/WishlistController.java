package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.Model.Camion;
import com.progetto.ingsw.trukscout24.Database.DBConnessione;
import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class WishlistController implements Initializable {

    @FXML private Button clearAllButton;
    @FXML private FlowPane trucksFlowPane;
    @FXML private VBox emptyStateBox;
    @FXML private Button startBrowsingButton;
    @FXML private Label wishlistCountLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final SceneHandler sceneHandler = SceneHandler.getInstance();
    private final DBConnessione dbconnessione = DBConnessione.getInstance();
    private ObservableList<Camion> wishlistTrucks;
    private String currentUserEmail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        wishlistTrucks = FXCollections.observableArrayList();
        setupFlowPane();

        // Ottieni l'email dell'utente corrente (presumibilmente dalla sessione)
        currentUserEmail = getCurrentUserEmail();
        System.out.println(currentUserEmail);

        if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
            loadWishlistFromDatabase();
        } else {
            showEmptyState();
        }
    }

    private String getCurrentUserEmail() {
        return sceneHandler.getCurrentUserEmail(); // Assumendo che esista questo metodo
    }

    private void setupFlowPane() {
        trucksFlowPane.setPrefWrapLength(1600.0);
        trucksFlowPane.setHgap(60.0);
        trucksFlowPane.setVgap(40.0);
        trucksFlowPane.getStyleClass().add("trucks-flow");
        trucksFlowPane.setStyle("-fx-alignment: center;");
    }

    private void loadWishlistFromDatabase() {
        showLoadingState(true);

        Task<ArrayList<Camion>> loadTask = new Task<ArrayList<Camion>>() {
            @Override
            protected ArrayList<Camion> call() throws Exception {
                CompletableFuture<ArrayList<Camion>> future = dbconnessione.getWishlist(currentUserEmail);
                return future.get(); // Aspetta il completamento
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
                showErrorAlert("Errore Caricamento", "Impossibile caricare la wishlist dal database.");
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
        card.getStyleClass().add("truck-card");
        card.setPrefWidth(750);
        card.setMaxWidth(750);
        card.setMinWidth(750);
        card.setPrefHeight(420);
        card.setMinHeight(420);

        // Header con il cuore
        HBox cardHeader = new HBox();
        cardHeader.getStyleClass().add("card-header");
        cardHeader.setStyle("-fx-alignment: top-right; -fx-padding: 0 0 10 0;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button heartButton = new Button("❤️");
        heartButton.getStyleClass().add("heart-button");
        heartButton.setTooltip(new Tooltip("Rimuovi dai preferiti"));
        heartButton.setOnAction(e -> removeCamionFromWishlist(camion));

        heartButton.setOnMouseEntered(e -> {
            heartButton.setText("💔");
            heartButton.setStyle("-fx-text-fill: #ef4444;");
        });
        heartButton.setOnMouseExited(e -> {
            heartButton.setText("❤️");
            heartButton.setStyle("-fx-text-fill: #ff6b35;");
        });

        cardHeader.getChildren().addAll(spacer, heartButton);

        // IMMAGINE DEL CAMION - AGGIORNATA
        VBox imageContainer = new VBox();
        imageContainer.setStyle("-fx-alignment: center; -fx-padding: 10 0 20 0;");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(200);
        imageView.setFitWidth(250);
        imageView.setPreserveRatio(true);

        String imagePath = camion.id();
        boolean imageLoaded = false;

        if (imagePath != null && !imagePath.isEmpty()) {
            String fullPath = "/com/progetto/ingsw/trukscout24/immagini/" + imagePath + ".jpg";
            URL imageUrl = getClass().getResource(fullPath);

            if (imageUrl != null) {
                Image image = new Image(imageUrl.toExternalForm());
                imageView.setImage(image);
                imageContainer.getChildren().add(imageView);
                imageLoaded = true;
            }
        }

        // Fallback se l'immagine non è stata caricata
        if (!imageLoaded) {
            Label fallbackLabel = new Label("🚛");
            fallbackLabel.getStyleClass().add("truck-image");
            imageContainer.getChildren().add(fallbackLabel);
        }

        // Contenuto
        VBox contentBox = new VBox();
        contentBox.getStyleClass().add("truck-content");

        Label titleLabel = new Label(camion.nome() + " " + camion.modello());
        titleLabel.getStyleClass().add("truck-title");

        Label infoLabel = new Label("📅 %s • 🛣️  km" + camion.anno() + camion.kilometri());
        infoLabel.getStyleClass().add("truck-info");

        Label priceLabel = new Label("💰 €" + camion.prezzo());
        priceLabel.getStyleClass().add("truck-price");

        Label categoryLabel = new Label("📂 " + camion.categoria());
        categoryLabel.getStyleClass().add("truck-category");

        Label dateLabel = new Label("🕒 Nei tuoi preferiti");
        dateLabel.getStyleClass().add("truck-date");

        contentBox.getChildren().addAll(titleLabel, infoLabel, priceLabel, categoryLabel, dateLabel);
        card.getChildren().addAll(cardHeader, imageContainer, contentBox);

        // Effetti hover
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

        // Aggiorna il contatore
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

    // EVENT HANDLERS

    @FXML
    private void HomeClick(MouseEvent event) throws Exception {
        sceneHandler.setHomeScene();
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
        sceneHandler.setHomeScene();
    }

    // DATABASE OPERATIONS

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
                    // Animazione di rimozione
                    animateCardRemoval(camion);
                } else {
                    showErrorAlert("Errore", "Impossibile rimuovere il camion dai preferiti.");
                }
            });
        });

        removeTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                showErrorAlert("Errore Database", "Errore durante la rimozione dal database.");
            });
        });

        Thread removeThread = new Thread(removeTask);
        removeThread.setDaemon(true);
        removeThread.start();
    }

    private void animateCardRemoval(Camion camion) {
        // Trova la card corrispondente
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

        // Fallback
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
                    // Animazione svuotamento
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(400), trucksFlowPane);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(event -> {
                        wishlistTrucks.clear();
                        updateUI();
                    });
                    fadeOut.play();
                } else {
                    showErrorAlert("Errore", "Impossibile svuotare la wishlist.");
                }
            });
        });

        clearTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                showErrorAlert("Errore Database", "Errore durante lo svuotamento della wishlist.");
            });
        });

        Thread clearThread = new Thread(clearTask);
        clearThread.setDaemon(true);
        clearThread.start();
    }

    // PUBLIC METHODS PER INTEGRAZIONE

    public void addTruckToWishlist(Camion camion) {
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            showErrorAlert("Errore", "Devi essere autenticato per aggiungere ai preferiti.");
            return;
        }

        if (wishlistTrucks.size() >= 6) {
            showErrorAlert("Limite Raggiunto", "Puoi avere massimo 6 camion nei preferiti.");
            return;
        }

        if (containsCamion(camion.id())) {
            showInfoAlert("Già Presente", "Questo camion è già nei tuoi preferiti.");
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
                    showSuccessAlert("Aggiunto!", "Camion aggiunto ai preferiti con successo.");
                } else {
                    showErrorAlert("Errore", "Impossibile aggiungere il camion ai preferiti.");
                }
            });
        });

        addTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                showErrorAlert("Errore Database", "Errore durante l'aggiunta al database.");
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

    // UTILITY METHODS

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText("✅ " + message);
        alert.showAndWait();
    }
}
