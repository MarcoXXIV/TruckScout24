package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ResourceBundle;

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

    // Footer
    @FXML private Label statusLabel;

    private SceneHandler sceneHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sceneHandler = SceneHandler.getInstance();
    }

    // ==================== SEARCH FUNCTIONALITY ====================

    @FXML
    private void onMainSearchAction() {
        String searchText = mainSearchField.getText();
        if (!searchText.trim().isEmpty()) {
            performSearch(searchText);
        }
    }

    @FXML
    private void onMainSearchClick(ActionEvent event) {
        onMainSearchAction();  // Unified handling
    }

    private void performSearch(String searchText) {
        System.out.println("Ricerca per: " + searchText);
        //sceneHandler.showAlert("Ricerca", "Ricerca per: " + searchText,
        //SceneHandler.AlertType.INFORMATION);
    }

    // ==================== BRAND SELECTION HANDLERS ====================

    @FXML
    private void onTruckImageClick(MouseEvent event) {
        System.out.println("Truck image clicked!");
    }

    @FXML
    private void onBrandClick(MouseEvent event) {
        String brand = ((Button) event.getSource()).getText();
        handleBrandSelection(brand);
    }

    @FXML
    private void onVolvoClick() {
        handleBrandSelection("VOLVO");
    }

    @FXML
    private void onMercedesClick() {
        handleBrandSelection("MERCEDES-BENZ");
    }

    @FXML
    private void onManClick() {
        handleBrandSelection("MAN");
    }

    @FXML
    private void onScaniaClick() {
        handleBrandSelection("SCANIA");
    }

    @FXML
    private void onIvecoClick() {
        handleBrandSelection("IVECO");
    }

    @FXML
    private void onRenaultClick() {
        handleBrandSelection("RENAULT");
    }

    @FXML
    private void onDafClick() {
        handleBrandSelection("DAF");
    }

    private void handleBrandSelection(String brand) {
        System.out.println("Selezionato brand: " + brand);
        //sceneHandler.showAlert("Filtro Brand", "Filtrato per marca: " + brand,
        // SceneHandler.AlertType.INFORMATION);
    }

    // ==================== USER ACTIONS ====================

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
        System.out.println("Registrati cliccato!");
        sceneHandler.setRegistrationScene();
    }

    // ==================== TRUCK ACTIONS ====================

    @FXML
    private void openTruckDetails(String truckId) {
        System.out.println("Apertura dettagli per camion ID: " + truckId);
        sceneHandler.setTruckDetailsScene();
    }

    @FXML
    private void onViewMoreClick() {
        System.out.println("Caricamento altri camion...");
        //sceneHandler.showAlert("Carica Altri", "Caricamento di altri camion...",
        // SceneHandler.AlertType.INFORMATION);
    }
}
