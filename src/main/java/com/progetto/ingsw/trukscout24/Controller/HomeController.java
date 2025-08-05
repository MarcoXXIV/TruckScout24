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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.concurrent.Task;

import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

public class HomeController implements Initializable {

    @FXML private Button AdminButton;
    @FXML private Button userButton;

    // Header elements
    @FXML private TextField mainSearchField;
    @FXML private Button mainSearchButton;
    @FXML private Button wishlistButton;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    // Main content
    @FXML private GridPane trucksGrid;
    @FXML private Button viewMoreButton;
    @FXML private Button showAllButton;

    // Footer
    @FXML private Label statusLabel;

    private final SceneHandler scenehandler = SceneHandler.getInstance();
    private final DBConnessione db = DBConnessione.getInstance();
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Set<String> processingWishlist = Collections.synchronizedSet(new HashSet<>());
    private volatile boolean searchInProgress = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            DBConnessione dbConnessione = DBConnessione.getInstance();
            dbConnessione.createConnection();
            mainSearchField.setOnAction(e -> onMainSearchAction());
            loadInitialCamions();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nella connessione al DB", e);
        }
    }

    private void loadInitialCamions() {
        statusLabel.setText("Caricamento camion...");

        CompletableFuture.supplyAsync(() -> {
            try {
                return db.addHomePageCamion().get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, databaseExecutor).thenAccept(camions -> {
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
        if (allCamions != null && !allCamions.isEmpty()) {
            Collections.shuffle(allCamions);
        }

        List<Camion> selectedCamions = allCamions != null ?
                allCamions.subList(0, Math.min(12, allCamions.size())) :
                new ArrayList<>();

        trucksGrid.getChildren().clear();
        trucksGrid.setHgap(20);
        trucksGrid.setVgap(20);
        trucksGrid.setPadding(new Insets(20));

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
        box.getStyleClass().add("camion-card");
        box.setPrefWidth(300);
        box.setAlignment(Pos.CENTER);

        box.setOnMouseClicked(event -> {
            if (event.getTarget() instanceof Button) return;
            try {
                openProductView(camion);
            } catch (Exception e) {
                System.err.println("Errore nell'apertura della product view: " + e.getMessage());
                SceneHandler.getInstance().showAlert("Errore", "Impossibile aprire i dettagli del camion", 0);
            }
        });

        box.setOnMouseEntered(event -> {
            box.getStyleClass().add("camion-card-hover");
        });

        box.setOnMouseExited(event -> {
            box.getStyleClass().remove("camion-card-hover");
        });

        HBox cardHeader = new HBox();
        cardHeader.getStyleClass().add("card-header");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button heartButton = createHeartButton(camion);
        cardHeader.getChildren().addAll(spacer, heartButton);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(320);
        imageView.setFitWidth(320);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("camion-image");

        String imagePath = camion.id();
        System.out.println(imagePath);

        if (imagePath != null && !imagePath.isEmpty()) {
            String fullPath = "/com/progetto/ingsw/trukscout24/immagini/" + imagePath + ".jpg";
            URL imageUrl = getClass().getResource(fullPath);
            if (imageUrl != null) {
                imageView.setImage(new Image(imageUrl.toExternalForm()));
            } else {
                System.out.println("Immagine non trovata: " + fullPath);
            }
        } else {
            System.out.println("ID immagine non trovato o immagine non disponibile.");
        }

        Label nameLabel = new Label("🚚 " + camion.nome());
        nameLabel.getStyleClass().add("camion-name");

        Label modelLabel = new Label("🔧 " + camion.modello());
        Label priceLabel = new Label("💰 € " + camion.prezzo());
        Label kmLabel = new Label("🛣 " + camion.kilometri());
        Label yearLabel = new Label("📅 " + camion.anno());
        Label powerLabel = new Label("⚡ " + camion.potenza() + " CV");

        modelLabel.getStyleClass().add("camion-info");
        priceLabel.getStyleClass().addAll("camion-info", "camion-price");
        kmLabel.getStyleClass().add("camion-info");
        yearLabel.getStyleClass().add("camion-info");
        powerLabel.getStyleClass().add("camion-info");

        HBox infoRow = new HBox(12);
        infoRow.setAlignment(Pos.CENTER);
        infoRow.getChildren().addAll(modelLabel, priceLabel, kmLabel, yearLabel, powerLabel);

        VBox infoBox = new VBox(8);
        infoBox.getStyleClass().add("camion-info-container");
        infoBox.getChildren().addAll(nameLabel, infoRow);

        box.getChildren().addAll(cardHeader, imageView, infoBox);
        return box;
    }

    private void openProductView(Camion camion) throws Exception {
        scenehandler.setSelectedCamion(camion);
        scenehandler.setProductViewScene();
    }

    private void checkInitialWishlistState(Button heartButton, Camion camion) {
        String userEmail = scenehandler.getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            heartButton.getStyleClass().addAll("heart-button", "heart-empty");
            heartButton.setUserData(false);
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                return db.getWishlist(userEmail).get().stream()
                        .anyMatch(truck -> truck.id().equals(camion.id()));
            } catch (Exception e) {
                return false;
            }
        }, databaseExecutor).thenAccept(isInWishlist -> {
            Platform.runLater(() -> {
                if (isInWishlist) {
                    heartButton.getStyleClass().addAll("heart-button", "heart-filled");
                } else {
                    heartButton.getStyleClass().addAll("heart-button", "heart-empty");
                }
                heartButton.setUserData(isInWishlist);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                heartButton.getStyleClass().addAll("heart-button", "heart-empty");
                heartButton.setUserData(false);
            });
            return null;
        });
    }

    private void addToWishlist(Button heartButton, Camion camion, String userEmail) {
        String operationKey = userEmail + "_" + camion.id();

        CompletableFuture.supplyAsync(() -> {
            try {
                return db.insertWishlistCamionIntoDB(userEmail, camion.id());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, databaseExecutor).thenAccept(success -> {
            Platform.runLater(() -> {
                processingWishlist.remove(operationKey);
                heartButton.setDisable(false);

                if (success) {
                    heartButton.getStyleClass().removeAll("heart-empty");
                    heartButton.getStyleClass().add("heart-filled");
                    heartButton.setUserData(true);
                    scenehandler.showAlert("Successo",
                            "🚛 " + camion.nome() + " aggiunto ai preferiti!", 1);
                } else {
                    scenehandler.showAlert("Attenzione",
                            "Impossibile aggiungere ai preferiti.\n" +
                                    "• Hai raggiunto il limite massimo (6 camion)\n" +
                                    "• Il camion è già presente nella lista", 0);
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                processingWishlist.remove(operationKey);
                heartButton.setDisable(false);
                scenehandler.showAlert("Errore",
                        "Errore durante l'aggiunta ai preferiti: " + throwable.getMessage(), 0);
            });
            return null;
        });
    }

    private void removeFromWishlist(Button heartButton, Camion camion, String userEmail) {
        String operationKey = userEmail + "_" + camion.id();

        CompletableFuture.supplyAsync(() -> {
            try {
                return db.removeCamionFromWishlist(userEmail, camion.id());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, databaseExecutor).thenAccept(success -> {
            Platform.runLater(() -> {
                processingWishlist.remove(operationKey);
                heartButton.setDisable(false);

                if (success) {
                    heartButton.getStyleClass().removeAll("heart-filled");
                    heartButton.getStyleClass().add("heart-empty");
                    heartButton.setUserData(false);
                    scenehandler.showAlert("Successo",
                            "🚛 " + camion.nome() + " rimosso dai preferiti!", 1);
                } else {
                    scenehandler.showAlert("Errore",
                            "Impossibile rimuovere dai preferiti", 0);
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                processingWishlist.remove(operationKey);
                heartButton.setDisable(false);
                scenehandler.showAlert("Errore",
                        "Errore durante la rimozione dai preferiti: " + throwable.getMessage(), 0);
            });
            return null;
        });
    }

    private void handleWishlistClick(Button heartButton, Camion camion) {
        String userEmail = scenehandler.getCurrentUserEmail();

        if (userEmail == null || userEmail.isEmpty()) {
            scenehandler.showAlert("Errore", "Devi essere autenticato per usare la wishlist", 0);
            return;
        }

        String operationKey = userEmail + "_" + camion.id();
        if (processingWishlist.contains(operationKey)) {
            return; // Operazione già in corso
        }

        processingWishlist.add(operationKey);
        heartButton.setDisable(true);

        Boolean isCurrentlyInWishlist = (Boolean) heartButton.getUserData();
        if (isCurrentlyInWishlist == null) isCurrentlyInWishlist = false;

        if (isCurrentlyInWishlist) {
            removeFromWishlist(heartButton, camion, userEmail);
        } else {
            addToWishlist(heartButton, camion, userEmail);
        }
    }

    private Button createHeartButton(Camion camion) {
        Button heartButton = new Button("♡"); // Cuore vuoto con bordo
        heartButton.getStyleClass().addAll("heart-button", "heart-empty");
        heartButton.setTooltip(new Tooltip("Aggiungi/Rimuovi dai preferiti"));

        checkInitialWishlistState(heartButton, camion);

        heartButton.setOnAction(e -> {
            e.consume(); // Evita la propagazione del click al VBox
            handleWishlistClick(heartButton, camion);
        });

        return heartButton;
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
        if (searchInProgress) {
            return;
        }

        System.out.println("Ricerca per: " + searchText);
        searchInProgress = true;

        statusLabel.setText("Ricerca in corso per: " + searchText + "...");
        trucksGrid.getChildren().clear();

        CompletableFuture.supplyAsync(() -> {
            try {
                db.clearSearchedList();
                return db.searchCamion(searchText).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, databaseExecutor).thenAccept(resultLabel -> {
            String results = resultLabel.getText();

            if (results != null && !results.isEmpty()) {
                String[] productIds = results.split(";");

                List<String> validIds = new ArrayList<>();
                for (String id : productIds) {
                    if (id != null && !id.trim().isEmpty()) {
                        validIds.add(id.trim());
                    }
                }
                if (!validIds.isEmpty()) {
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            db.addSearchedCamion(validIds.toArray(new String[0]));
                            Thread.sleep(500); // Piccola pausa per completare la query
                            return db.getSearchedCamion();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }, databaseExecutor).thenAccept(searchedCamions -> {
                        Platform.runLater(() -> {
                            searchInProgress = false;
                            if (searchedCamions != null && !searchedCamions.isEmpty()) {
                                populateGridWithCamions(searchedCamions);
                                statusLabel.setText("Trovati " + searchedCamions.size() + " risultati per: " + searchText);
                            } else {
                                trucksGrid.getChildren().clear();
                                statusLabel.setText("Nessun risultato trovato per: " + searchText);
                            }
                        });
                    }).exceptionally(e -> {
                        Platform.runLater(() -> {
                            searchInProgress = false;
                            statusLabel.setText("Errore nella ricerca");
                            SceneHandler.getInstance().showAlert("Errore", "Errore durante la ricerca", 0);
                        });
                        return null;
                    });
                } else {
                    Platform.runLater(() -> {
                        searchInProgress = false;
                        trucksGrid.getChildren().clear();
                        statusLabel.setText("Nessun risultato trovato per: " + searchText);
                    });
                }
            } else {
                Platform.runLater(() -> {
                    searchInProgress = false;
                    trucksGrid.getChildren().clear();
                    statusLabel.setText("Nessun risultato trovato per: " + searchText);
                });
            }
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                searchInProgress = false;
                statusLabel.setText("Errore nella ricerca");
                SceneHandler.getInstance().showAlert("Errore", "Errore durante la ricerca", 0);
            });
            return null;
        });
    }

    @FXML
    private void onTruckImageClick(MouseEvent event) throws Exception {
        scenehandler.setTruckDetailsScene();
    }

    @FXML
    private void onBrandLogoClick(MouseEvent event) {
        ImageView clickedLogo = (ImageView) event.getSource();
        String brand = (String) clickedLogo.getUserData();

        if (brand != null && !brand.isEmpty()) {
            handleBrandSelection(brand);
        }
    }

    @FXML private void onVolvoLogoClick(MouseEvent event) { handleBrandSelection("VOLVO"); }
    @FXML private void onMercedesLogoClick(MouseEvent event) { handleBrandSelection("MERCEDES BENZ"); }
    @FXML private void onManLogoClick(MouseEvent event) { handleBrandSelection("MAN"); }
    @FXML private void onScaniaLogoClick(MouseEvent event) { handleBrandSelection("SCANIA"); }
    @FXML private void onIvecoLogoClick(MouseEvent event) { handleBrandSelection("IVECO"); }
    @FXML private void onRenaultLogoClick(MouseEvent event) { handleBrandSelection("RENAULT"); }
    @FXML private void onDafLogoClick(MouseEvent event) { handleBrandSelection("DAF"); }

    @FXML
    private void onBrandClick(ActionEvent event) {
        String brand = ((Button) event.getSource()).getText();
        handleBrandSelection(brand);
    }

    @FXML private void onVolvoClick() { handleBrandSelection("VOLVO"); }
    @FXML private void onMercedesClick() { handleBrandSelection("MERCEDES BENZ"); }
    @FXML private void onManClick() { handleBrandSelection("MAN"); }
    @FXML private void onScaniaClick() { handleBrandSelection("SCANIA"); }
    @FXML private void onIvecoClick() { handleBrandSelection("IVECO"); }
    @FXML private void onRenaultClick() { handleBrandSelection("RENAULT"); }
    @FXML private void onDafClick() { handleBrandSelection("DAF"); }

    private void handleBrandSelection(String brand) {
        statusLabel.setText("Caricamento camion " + brand + "...");
        trucksGrid.getChildren().clear();

        CompletableFuture.supplyAsync(() -> {
            try {
                db.clearCategoryCamionList();
                db.addCategoryCamion(brand);
                Thread.sleep(500);
                return db.getCategoryCamion();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, databaseExecutor).thenAccept(filteredCamions -> {
            Platform.runLater(() -> {
                if (filteredCamions != null && !filteredCamions.isEmpty()) {
                    populateGridWithCamions(filteredCamions);
                    statusLabel.setText("Visualizzando " + filteredCamions.size() + " camion " + brand);
                } else {
                    trucksGrid.getChildren().clear();
                    statusLabel.setText("Nessun camion trovato per la marca " + brand);
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("Errore nel caricamento");
                SceneHandler.getInstance().showAlert("Errore", "Errore nel caricamento dei camion", 0);
            });
            return null;
        });
    }

    @FXML
    private void onShowAllTrucksClick() {
        mainSearchField.clear();
        loadInitialCamions();
    }

    @FXML
    private void userClick(MouseEvent event) throws Exception {
        scenehandler.setUtenteScene();
    }

    @FXML
    private void wishlistClick(MouseEvent event) throws Exception {
        scenehandler.setWishlistScene();
    }

    @FXML
    private void loginClick(MouseEvent event) throws Exception {
        scenehandler.setLoginScene();
    }

    @FXML
    public void HomeClick(MouseEvent event) throws Exception {
        scenehandler.setHomeScene();
    }

    public void shutdown() {
        if (databaseExecutor != null && !databaseExecutor.isShutdown()) {
            databaseExecutor.shutdown();
            try {
                if (!databaseExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    databaseExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                databaseExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void AdminClick(MouseEvent event) throws Exception {
        scenehandler.setAdminScene();
    }
}