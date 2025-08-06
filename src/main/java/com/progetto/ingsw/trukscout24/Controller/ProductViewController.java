package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.Database.DBConnessione;
import com.progetto.ingsw.trukscout24.Model.Camion;
import com.progetto.ingsw.trukscout24.Messaggi;
import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class ProductViewController implements Initializable {

    @FXML private Label productTitle;
    @FXML private Label productPrice;
    @FXML private Label productDescription;

    @FXML private Label specPotenza, specKilometri, specCarburante, specCambio, specEmissioni, specAnno, specModello;
    @FXML private ImageView mainImage;
    @FXML private Button wishlistButton;
    @FXML private DatePicker datePicker;
    @FXML private Button prenotaButton;

    private Camion currentCamion;
    private boolean isInWishlist = false;

    private final SceneHandler sceneHandler = SceneHandler.getInstance();
    private final DBConnessione dbconnessione = DBConnessione.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateWishlistButtonState();
        setupDatePicker();
    }

    private void setupDatePicker() {
        if (datePicker != null) {
            datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(LocalDate.now().plusDays(1)));
                }
            });
            datePicker.setValue(LocalDate.now().plusDays(1));
        }
    }

    private String getCurrentUserEmail() {
        return sceneHandler.getCurrentUserEmail();
    }

    public void loadCamionData(Camion camion) {
        if (camion == null) return;

        this.currentCamion = camion;
        productTitle.setText(camion.nome());
        productPrice.setText("€ " + camion.prezzo());
        productDescription.setText(camion.descrizione());
        specPotenza.setText(camion.potenza() + " CV");
        specKilometri.setText(camion.kilometri() + " km");
        specCarburante.setText(camion.carburante());
        specCambio.setText(camion.cambio());
        specEmissioni.setText("Euro " + camion.classeEmissioni());
        specAnno.setText(camion.anno());
        specModello.setText(camion.modello());

        loadMainImage();
        checkWishlistStatus();
    }

    private void checkWishlistStatus() {
        String userEmail = getCurrentUserEmail();
        if (currentCamion == null || userEmail == null) return;

        Task<Boolean> checkTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                CompletableFuture<ArrayList<Camion>> future = dbconnessione.getWishlist(userEmail);
                ArrayList<Camion> wishlist = future.get();
                return wishlist.stream().anyMatch(truck -> truck.id().equals(currentCamion.id()));
            }
        };

        checkTask.setOnSucceeded(e -> Platform.runLater(() -> {
            isInWishlist = checkTask.getValue();
            updateWishlistButtonState();
        }));

        checkTask.setOnFailed(e -> Platform.runLater(() ->
                System.out.println("Errore nel controllo wishlist: " + e.getSource().getException())
        ));

        Thread checkThread = new Thread(checkTask);
        checkThread.setDaemon(true);
        checkThread.start();
    }

    private void updateWishlistButtonState() {
        if (isInWishlist) {
            wishlistButton.setText("♥");
            wishlistButton.getStyleClass().removeAll("wishlist-button-inactive");
            if (!wishlistButton.getStyleClass().contains("wishlist-button-active")) {
                wishlistButton.getStyleClass().add("wishlist-button-active");
            }
        } else {
            wishlistButton.setText("♡");
            wishlistButton.getStyleClass().removeAll("wishlist-button-active");
            if (!wishlistButton.getStyleClass().contains("wishlist-button-inactive")) {
                wishlistButton.getStyleClass().add("wishlist-button-inactive");
            }
        }
    }

    private void loadMainImage() {
        if (currentCamion == null || currentCamion.id() == null) return;

        try {
            String imagePath = "/com/progetto/ingsw/trukscout24/immagini/" + currentCamion.id() + ".jpg";
            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl != null) {
                mainImage.setImage(new Image(imageUrl.toExternalForm()));
            } else {
                mainImage.setImage(null);
            }
        } catch (Exception e) {
            mainImage.setImage(null);
        }
    }

    @FXML
    private void onLogoClick() throws Exception {
        SceneHandler.getInstance().setHomeScene();
    }

    @FXML
    private void onWishlistClick() {
        String userEmail = getCurrentUserEmail();

        if (currentCamion == null) {
            sceneHandler.showAlert("Errore", Messaggi.productview_no_camion_selected, 0);
            return;
        }

        if (userEmail == null || userEmail.isEmpty()) {
            sceneHandler.showAlert("Errore", Messaggi.productview_login_required, 0);
            return;
        }

        wishlistButton.setDisable(true);

        if (isInWishlist) {
            removeFromWishlist(userEmail);
        } else {
            addToWishlist(userEmail);
        }
    }

    @FXML
    private void onPrenotaClick() {
        String userEmail = getCurrentUserEmail();

        if (currentCamion == null) {
            sceneHandler.showAlert("Errore", Messaggi.productview_no_camion_selected, 0);
            return;
        }

        if (userEmail == null || userEmail.isEmpty()) {
            sceneHandler.showAlert("Errore", Messaggi.productview_login_required, 0);
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            sceneHandler.showAlert("Errore", Messaggi.productview_no_date_selected, 0);
            return;
        }

        if (selectedDate.isBefore(LocalDate.now().plusDays(1))) {
            sceneHandler.showAlert("Errore", Messaggi.productview_invalid_date, 0);
            return;
        }

        prenotaButton.setDisable(true);
        prenotaCamion(userEmail, selectedDate);
    }

    private void prenotaCamion(String userEmail, LocalDate date) {
        Task<Void> prenotaTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                dbconnessione.insertPrenotazioneIntoDB(
                        userEmail, currentCamion.nome(),
                        date.getDayOfMonth(), date.getMonthValue(), date.getYear()
                );
                return null;
            }
        };

        prenotaTask.setOnSucceeded(e -> Platform.runLater(() -> prenotaButton.setDisable(false)));

        prenotaTask.setOnFailed(e -> Platform.runLater(() -> {
            prenotaButton.setDisable(false);
            sceneHandler.showAlert("Errore",
                    Messaggi.productview_booking_failed + e.getSource().getException().getMessage(), 0);
        }));

        Thread prenotaThread = new Thread(prenotaTask);
        prenotaThread.setDaemon(true);
        prenotaThread.start();
    }

    private void addToWishlist(String userEmail) {
        Task<Boolean> addTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return dbconnessione.insertWishlistCamionIntoDB(userEmail, currentCamion.id());
            }
        };

        addTask.setOnSucceeded(e -> Platform.runLater(() -> {
            wishlistButton.setDisable(false);

            if (addTask.getValue()) {
                isInWishlist = true;
                updateWishlistButtonState();
                sceneHandler.showAlert("Successo",
                        "🚛 " + currentCamion.nome() + " " + Messaggi.wishlist_added, 1);
            } else {
                sceneHandler.showAlert("Attenzione", Messaggi.wishlist_limit_or_duplicate, 0);
            }
        }));

        addTask.setOnFailed(e -> Platform.runLater(() -> {
            wishlistButton.setDisable(false);
            sceneHandler.showAlert("Errore",
                    Messaggi.wishlist_add_failed + e.getSource().getException().getMessage(), 0);
        }));

        Thread addThread = new Thread(addTask);
        addThread.setDaemon(true);
        addThread.start();
    }

    private void removeFromWishlist(String userEmail) {
        Task<Boolean> removeTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return dbconnessione.removeCamionFromWishlist(userEmail, currentCamion.id());
            }
        };

        removeTask.setOnSucceeded(e -> Platform.runLater(() -> {
            wishlistButton.setDisable(false);

            if (removeTask.getValue()) {
                isInWishlist = false;
                updateWishlistButtonState();
                sceneHandler.showAlert("Successo",
                        "🚛 " + currentCamion.nome() + " " + Messaggi.wishlist_removed, 1);
            } else {
                sceneHandler.showAlert("Errore", Messaggi.wishlist_remove_failed, 0);
            }
        }));

        removeTask.setOnFailed(e -> Platform.runLater(() -> {
            wishlistButton.setDisable(false);
            sceneHandler.showAlert("Errore",
                    Messaggi.wishlist_remove_error + e.getSource().getException().getMessage(), 0);
        }));

        Thread removeThread = new Thread(removeTask);
        removeThread.setDaemon(true);
        removeThread.start();
    }

    public void setCamion(Camion camion) {
        loadCamionData(camion);
    }
}
