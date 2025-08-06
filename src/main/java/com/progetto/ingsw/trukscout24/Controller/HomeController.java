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
    @FXML private Button wishlistButton;
    @FXML private Button loginButton;

    // Header elements
    @FXML private TextField mainSearchField;
    @FXML private Button mainSearchButton;

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
            manageUserButtonsVisibility();
        } catch (SQLException e) {
            throw new RuntimeException(Messaggi.home_db_connection_error, e);
        }
    }

    private void manageUserButtonsVisibility() {
        if (!scenehandler.isUserAuthenticated()) {
            // Non autenticato
            loginButton.setVisible(true);
            wishlistButton.setVisible(false);
            userButton.setVisible(false);
            AdminButton.setVisible(false);
        } else {
            // Autenticato
            loginButton.setVisible(false);
            userButton.setVisible(true);
            wishlistButton.setVisible(true);
            AdminButton.setVisible(scenehandler.isCurrentUserAdmin());
        }
    }

    private void loadInitialCamions() {
        statusLabel.setText(Messaggi.home_loading_camions);

        CompletableFuture.supplyAsync(() -> {
            try {
                return db.addHomePageCamion().get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, databaseExecutor).thenAccept(camions -> {
            Platform.runLater(() -> {
                populateGridWithCamions(camions);
                statusLabel.setText(Messaggi.home_showing_all_camions + " (" + camions.size() + ")");
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                statusLabel.setText(Messaggi.home_loading_error);
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
                SceneHandler.getInstance().showAlert("Errore", Messaggi.home_product_view_error, 0);
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
        imageView.setFitHeight(300);
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("camion-image");

        String imagePath = camion.id();
        if (imagePath != null && !imagePath.isEmpty()) {
            String fullPath = "/com/progetto/ingsw/trukscout24/immagini/" + imagePath + ".jpg";
            URL imageUrl = getClass().getResource(fullPath);
            if (imageUrl != null) {
                imageView.setImage(new Image(imageUrl.toExternalForm()));
            } else {
                System.out.println("Immagine non trovata: " + fullPath);
            }
        }

        Label nameLabel = new Label(camion.nome());
        nameLabel.getStyleClass().add("camion-name");

        Label priceLabel = new Label("💰 " + camion.prezzo() + " €");
        Label kmLabel = new Label("🛣 " + camion.kilometri() + " KM");
        Label yearLabel = new Label("📅 " + camion.anno());
        Label powerLabel = new Label("⚡ " + camion.potenza() + " CV");

        priceLabel.getStyleClass().addAll("camion-info", "camion-price");
        kmLabel.getStyleClass().add("camion-info");
        yearLabel.getStyleClass().add("camion-info");
        powerLabel.getStyleClass().add("camion-info");

        HBox infoRow = new HBox(12);
        infoRow.setAlignment(Pos.CENTER);
        infoRow.getChildren().addAll(priceLabel, kmLabel, yearLabel, powerLabel);

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

    private Button createHeartButton(Camion camion) {
        Button heartButton = new Button("♡");
        heartButton.getStyleClass().add("heart-button");
        heartButton.setTooltip(new Tooltip("Aggiungi/Rimuovi dai preferiti"));
        heartButton.setUserData(false);
        checkInitialWishlistState(heartButton, camion);

        heartButton.setOnAction(e -> {
            e.consume();
            handleWishlistClick(heartButton, camion);
        });

        return heartButton;
    }

    private void checkInitialWishlistState(Button heartButton, Camion camion) {
        String userEmail = scenehandler.getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            heartButton.setText("♡");
            heartButton.getStyleClass().add("heart-empty");
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
            final boolean inWishlist = isInWishlist;
            Platform.runLater(() -> {
                heartButton.setText(inWishlist ? "♥" : "♡");
                heartButton.getStyleClass().add(inWishlist ? "heart-filled" : "heart-empty");
                heartButton.setUserData(inWishlist);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                heartButton.setText("♡");
                heartButton.getStyleClass().add("heart-empty");
                heartButton.setUserData(false);
            });
            return null;
        });
    }

    private void handleWishlistClick(Button heartButton, Camion camion) {
        String userEmail = scenehandler.getCurrentUserEmail();

        if (userEmail == null || userEmail.isEmpty()) {
            scenehandler.showAlert("Errore", Messaggi.home_wishlist_login_required, 0);
            return;
        }

        String operationKey = userEmail + "_" + camion.id();
        if (processingWishlist.contains(operationKey)) {
            return;
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
                    heartButton.setText("♥");
                    heartButton.getStyleClass().removeAll("heart-empty");
                    heartButton.getStyleClass().add("heart-filled");
                    heartButton.setUserData(true);
                    scenehandler.showAlert("Successo",
                            "🚛 " + camion.nome() + " " + Messaggi.home_camion_added_to_wishlist, 1);
                } else {
                    scenehandler.showAlert("Attenzione", Messaggi.home_wishlist_add_error, 0);
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                processingWishlist.remove(operationKey);
                heartButton.setDisable(false);
                scenehandler.showAlert("Errore",
                        Messaggi.home_wishlist_add_exception + ": " + throwable.getMessage(), 0);
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
                    heartButton.setText("♡");
                    heartButton.getStyleClass().removeAll("heart-filled");
                    heartButton.getStyleClass().add("heart-empty");
                    heartButton.setUserData(false);
                    scenehandler.showAlert("Successo",
                            "🚛 " + camion.nome() + " " + Messaggi.home_camion_removed_from_wishlist, 1);
                } else {
                    scenehandler.showAlert("Errore", Messaggi.home_wishlist_remove_error, 0);
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                processingWishlist.remove(operationKey);
                heartButton.setDisable(false);
                scenehandler.showAlert("Errore",
                        Messaggi.home_wishlist_remove_exception + ": " + throwable.getMessage(), 0);
            });
            return null;
        });
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

        statusLabel.setText(Messaggi.home_search_in_progress + ": " + searchText + "...");
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
                            Thread.sleep(500);
                            return db.getSearchedCamion();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }, databaseExecutor).thenAccept(searchedCamions -> {
                        Platform.runLater(() -> {
                            searchInProgress = false;
                            if (searchedCamions != null && !searchedCamions.isEmpty()) {
                                populateGridWithCamions(searchedCamions);
                                statusLabel.setText(Messaggi.home_search_results_found + " " + searchedCamions.size() + " risultati per: " + searchText);
                            } else {
                                trucksGrid.getChildren().clear();
                                statusLabel.setText(Messaggi.home_search_no_results + ": " + searchText);
                            }
                        });
                    }).exceptionally(e -> {
                        Platform.runLater(() -> {
                            searchInProgress = false;
                            statusLabel.setText("Errore nella ricerca");
                            SceneHandler.getInstance().showAlert("Errore", Messaggi.home_search_error, 0);
                        });
                        return null;
                    });
                } else {
                    Platform.runLater(() -> {
                        searchInProgress = false;
                        trucksGrid.getChildren().clear();
                        statusLabel.setText(Messaggi.home_search_no_results + ": " + searchText);
                    });
                }
            } else {
                Platform.runLater(() -> {
                    searchInProgress = false;
                    trucksGrid.getChildren().clear();
                    statusLabel.setText(Messaggi.home_search_no_results + ": " + searchText);
                });
            }
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                searchInProgress = false;
                statusLabel.setText("Errore nella ricerca");
                SceneHandler.getInstance().showAlert("Errore", Messaggi.home_search_error, 0);
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
        statusLabel.setText(Messaggi.home_brand_loading + " " + brand + "...");
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
                    statusLabel.setText(Messaggi.home_brand_showing + " " + filteredCamions.size() + " camion " + brand);
                } else {
                    trucksGrid.getChildren().clear();
                    statusLabel.setText(Messaggi.home_brand_no_results + " " + brand);
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                statusLabel.setText(Messaggi.home_loading_error);
                SceneHandler.getInstance().showAlert("Errore", Messaggi.home_brand_loading_error, 0);
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