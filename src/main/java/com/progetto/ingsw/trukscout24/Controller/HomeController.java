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

    private WishlistController wishlistController; // Per accedere ai metodi della wishlist
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

    private final SceneHandler scenehandler = SceneHandler.getInstance();
    private final DBConnessione db = DBConnessione.getInstance();

    // MODIFICATO: Un solo thread executor per tutte le operazioni database
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    // MODIFICATO: Set per tracciare le operazioni wishlist in corso
    private final Set<String> processingWishlist = Collections.synchronizedSet(new HashSet<>());

    // MODIFICATO: Lock per le operazioni di ricerca
    private volatile boolean searchInProgress = false;

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

    // MODIFICATO: Caricamento iniziale sicuro
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
        box.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-width: 1; -fx-cursor: hand;");
        box.setPrefWidth(300);
        box.setAlignment(Pos.CENTER);  // Centra il contenuto nella VBox

        // Aggiungi il click handler al VBox (escluso il cuore)
        box.setOnMouseClicked(event -> {
            // Controlla se il click è sul pulsante cuore
            if (event.getTarget() instanceof Button) {
                return; // Non aprire product view se si clicca sul cuore
            }
            try {
                openProductView(camion);
            } catch (Exception e) {
                System.err.println("Errore nell'apertura della product view: " + e.getMessage());
                SceneHandler.getInstance().showAlert("Errore", "Impossibile aprire i dettagli del camion", 0);
            }
        });

        // Aggiungi effetto hover
        box.setOnMouseEntered(event -> {
            box.setStyle("-fx-padding: 10; -fx-border-color: #ff6b35; -fx-border-width: 2; -fx-cursor: hand; -fx-background-color: #f0f8ff;");
        });

        box.setOnMouseExited(event -> {
            box.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-width: 1; -fx-cursor: hand;");
        });

        // HEADER CON CUORE WISHLIST
        HBox cardHeader = new HBox();
        cardHeader.setStyle("-fx-alignment: top-right; -fx-padding: 0 0 10 0;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button heartButton = createHeartButton(camion);
        cardHeader.getChildren().addAll(spacer, heartButton);

        // ImageView (centrata)
        ImageView imageView = new ImageView();
        imageView.setFitHeight(220); // Ridotto per fare spazio al cuore
        imageView.setFitWidth(250);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-alignment: center;"); // Allineamento al centro

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

        // Label informazioni (tutte centrate)
        Label nameLabel = new Label(camion.nome());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-alignment: center;");

        Label modelLabel = new Label("Modello: " + camion.modello());
        Label priceLabel = new Label("€ " + camion.prezzo());
        priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ff6b35; -fx-font-size: 16px; -fx-text-alignment: center;");

        Label kmLabel = new Label("Km: " + camion.kilometri());
        Label yearLabel = new Label("Anno: " + camion.anno());
        Label powerLabel = new Label("Potenza: " + camion.potenza() + " CV");

        // Disporre le etichette in colonna (centrate)
        VBox infoBox = new VBox(5);  // Distanza di 5 px tra gli elementi
        infoBox.getChildren().addAll(nameLabel, modelLabel, priceLabel, kmLabel, yearLabel, powerLabel);
        infoBox.setStyle("-fx-alignment: center;");  // Centra il contenuto orizzontalmente

        // Aggiungi tutto alla VBox: header + immagine centrata + informazioni in colonna
        box.getChildren().addAll(cardHeader, imageView, infoBox);

        return box;
    }



    private void openProductView(Camion camion) throws Exception {
        System.out.println("Apertura product view per camion: " + camion.id());

        // Salva il camion selezionato in SceneHandler per passarlo alla ProductView
        scenehandler.setSelectedCamion(camion);

        // Naviga alla scene della product view
        scenehandler.setProductViewScene();
    }

    // MODIFICATO: Controllo stato wishlist sicuro
    private void checkInitialWishlistState(Button heartButton, Camion camion) {
        String userEmail = scenehandler.getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            setHeartButtonEmptyStyle(heartButton);
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
                    setHeartButtonFilledStyle(heartButton);
                } else {
                    setHeartButtonEmptyStyle(heartButton);
                }
                heartButton.setUserData(isInWishlist);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                setHeartButtonEmptyStyle(heartButton);
                heartButton.setUserData(false);
            });
            return null;
        });
    }

    // MODIFICATO: Aggiunta sicura alla wishlist
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
                    setHeartButtonFilledStyle(heartButton);
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

    // MODIFICATO: Rimozione sicura dalla wishlist
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
                    setHeartButtonEmptyStyle(heartButton);
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

    // MODIFICATO: Gestione sicura del click wishlist
    private void handleWishlistClick(Button heartButton, Camion camion) {
        String userEmail = scenehandler.getCurrentUserEmail();

        if (userEmail == null || userEmail.isEmpty()) {
            scenehandler.showAlert("Errore", "Devi essere autenticato per usare la wishlist", 0);
            return;
        }

        // Previeni operazioni multiple sullo stesso camion
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

    private void setHeartButtonEmptyStyle(Button heartButton) {
        heartButton.setText("♡"); // Cuore vuoto con bordo
        heartButton.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.9); " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 15px; " +
                        "-fx-background-radius: 15px; " +
                        "-fx-font-size: 18px; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 5px 8px; " +
                        "-fx-min-width: 35px; " +
                        "-fx-min-height: 35px;"
        );
    }

    private void setHeartButtonFilledStyle(Button heartButton) {
        heartButton.setText("♥"); // Cuore pieno
        heartButton.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.9); " +
                        "-fx-border-color: #ff6b35; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 15px; " +
                        "-fx-background-radius: 15px; " +
                        "-fx-font-size: 18px; " +
                        "-fx-text-fill: #ff6b35; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 5px 8px; " +
                        "-fx-min-width: 35px; " +
                        "-fx-min-height: 35px;"
        );
    }

    private Button createHeartButton(Camion camion) {
        Button heartButton = new Button("♡"); // Cuore vuoto con bordo
        heartButton.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.9); " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 15px; " +
                        "-fx-background-radius: 15px; " +
                        "-fx-font-size: 18px; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 5px 8px; " +
                        "-fx-min-width: 35px; " +
                        "-fx-min-height: 35px;"
        );
        heartButton.setTooltip(new Tooltip("Aggiungi/Rimuovi dai preferiti"));

        // Controlla stato iniziale wishlist
        checkInitialWishlistState(heartButton, camion);

        // Click handler
        heartButton.setOnAction(e -> {
            e.consume(); // Evita la propagazione del click al VBox
            handleWishlistClick(heartButton, camion);
        });

        // Effetti hover per cuore vuoto
        heartButton.setOnMouseEntered(e -> {
            Boolean isInWishlist = (Boolean) heartButton.getUserData();
            if (isInWishlist != null && isInWishlist) {
                // Hover per cuore pieno (rosso -> rosso scuro)
                heartButton.setStyle(
                        "-fx-background-color: rgba(255, 255, 255, 0.95); " +
                                "-fx-border-color: #cc0000; " +
                                "-fx-border-width: 2px; " +
                                "-fx-border-radius: 15px; " +
                                "-fx-background-radius: 15px; " +
                                "-fx-font-size: 18px; " +
                                "-fx-text-fill: #cc0000; " +
                                "-fx-cursor: hand; " +
                                "-fx-padding: 5px 8px; " +
                                "-fx-min-width: 35px; " +
                                "-fx-min-height: 35px;"
                );
            } else {
                // Hover per cuore vuoto
                heartButton.setStyle(
                        "-fx-background-color: rgba(255, 255, 255, 0.95); " +
                                "-fx-border-color: #ff6b35; " +
                                "-fx-border-width: 2px; " +
                                "-fx-border-radius: 15px; " +
                                "-fx-background-radius: 15px; " +
                                "-fx-font-size: 18px; " +
                                "-fx-text-fill: #ff6b35; " +
                                "-fx-cursor: hand; " +
                                "-fx-padding: 5px 8px; " +
                                "-fx-min-width: 35px; " +
                                "-fx-min-height: 35px;"
                );
            }
        });

        heartButton.setOnMouseExited(e -> {
            Boolean isInWishlist = (Boolean) heartButton.getUserData();
            if (isInWishlist != null && isInWishlist) {
                // Stile normale per cuore pieno
                setHeartButtonFilledStyle(heartButton);
            } else {
                // Stile normale per cuore vuoto
                setHeartButtonEmptyStyle(heartButton);
            }
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

    // MODIFICATO: Ricerca sicura
    private void performSearch(String searchText) {
        // Previeni ricerche multiple simultanee
        if (searchInProgress) {
            return;
        }

        System.out.println("Ricerca per: " + searchText);
        searchInProgress = true;

        // Mostra indicatore di caricamento
        statusLabel.setText("Ricerca in corso per: " + searchText + "...");
        trucksGrid.getChildren().clear();

        CompletableFuture.supplyAsync(() -> {
            try {
                // Pulisci la lista precedente
                db.clearSearchedList();

                // Esegui la ricerca
                return db.searchCamion(searchText).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, databaseExecutor).thenAccept(resultLabel -> {
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
                    // Carica i camion trovati usando il database executor
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

    // MODIFICATO: Selezione marca sicura
    private void handleBrandSelection(String brand) {
        System.out.println("Selezionato brand: " + brand);

        // Mostra indicatore di caricamento
        statusLabel.setText("Caricamento camion " + brand + "...");
        trucksGrid.getChildren().clear();

        CompletableFuture.supplyAsync(() -> {
            try {
                // Pulisci la lista precedente
                db.clearCategoryCamionList();

                // Carica i camion della categoria selezionata
                db.addCategoryCamion(brand);

                // Piccola pausa per permettere al database di completare la query
                Thread.sleep(500);

                // Ottieni i camion filtrati
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
        mainSearchField.clear(); // Pulisci il campo di ricerca
        loadInitialCamions(); // Ricarica tutti i camion
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
    private void registrazioneClick(MouseEvent event) throws Exception {
        scenehandler.setRegistrationScene();
    }

    @FXML
    private void onViewMoreClick() {
        System.out.println("Caricamento altri camion...");
        // Implementa qui la logica di paginazione se necessario
    }

    @FXML
    public void HomeClick(MouseEvent event) throws Exception {
        scenehandler.setHomeScene();
    }

    // AGGIUNTO: Metodo per chiudere correttamente le risorse
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
}
