package com.progetto.ingsw.trukscout24.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Arrays;
import java.util.List;

public class HomeController implements Initializable {

    // Header Controls
    @FXML private TextField mainSearchField;
    @FXML private Button mainSearchButton;
    @FXML private Button wishlistButton;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    // Advanced Search Controls
    @FXML private Button advancedSearchToggle;
    @FXML private VBox advancedSearchPanel;
    @FXML private ComboBox<String> brandFilterCombo;
    @FXML private ComboBox<String> priceRangeCombo;
    @FXML private ComboBox<String> yearRangeCombo;
    @FXML private ComboBox<String> locationCombo;

    // Brand Controls
    @FXML private Button volvoButton;
    @FXML private Button mercedesButton;
    @FXML private Button manButton;
    @FXML private Button scaniaButton;
    @FXML private Button ivecoButton;
    @FXML private Button renaultButton;
    @FXML private Button dafButton;

    // Trucks Section Controls
    @FXML private Label featuredLabel;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Label countLabel;
    @FXML private GridPane trucksGrid;
    @FXML private Button viewMoreButton;

    // Footer
    @FXML private Label statusLabel;

    private boolean advancedSearchVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        populateSampleTrucks();
        updateTruckCount();
    }

    private void setupComboBoxes() {
        // Brand Filter
        brandFilterCombo.getItems().addAll(
                "Tutti i marchi", "Volvo", "Mercedes-Benz", "MAN", "Scania", "Iveco", "Renault", "DAF"
        );

        // Price Range
        priceRangeCombo.getItems().addAll(
                "Tutte le fasce", "€ 0 - 25.000", "€ 25.000 - 50.000", "€ 50.000 - 100.000", "€ 100.000+"
        );

        // Year Range
        yearRangeCombo.getItems().addAll(
                "Tutti gli anni", "2020-2024", "2015-2019", "2010-2014", "Prima del 2010"
        );

        // Location
        locationCombo.getItems().addAll(
                "Tutta Italia", "Nord Italia", "Centro Italia", "Sud Italia", "Milano", "Roma", "Napoli"
        );

        // Sort Options
        sortCombo.getItems().addAll(
                "Più recenti", "Prezzo crescente", "Prezzo decrescente", "Anno crescente", "Anno decrescente", "Km crescenti"
        );
        sortCombo.setValue("Più recenti");
    }

    // Header Actions
    @FXML
    private void onMainSearchClick() {
        String searchText = mainSearchField.getText();
        if (!searchText.trim().isEmpty()) {
            performSearch(searchText);
        }
    }

    @FXML
    private void onWishlistClick() {
        showAlert("Wishlist", "Funzionalità Wishlist in sviluppo");
    }

    @FXML
    private void onLoginClick() {
        showAlert("Login", "Apertura finestra di login");
    }

    @FXML
    private void onRegisterClick() {
        showAlert("Registrazione", "Apertura finestra di registrazione");
    }

    // Advanced Search
    @FXML
    private void onAdvancedSearchToggle() {
        advancedSearchVisible = !advancedSearchVisible;
        advancedSearchPanel.setVisible(advancedSearchVisible);
        advancedSearchPanel.setManaged(advancedSearchVisible);

        advancedSearchToggle.setText(advancedSearchVisible ? "🔧 Nascondi Ricerca Avanzata" : "🔧 Ricerca Avanzata");
    }

    @FXML
    private void onApplyFilters() {
        String brand = brandFilterCombo.getValue();
        String price = priceRangeCombo.getValue();
        String year = yearRangeCombo.getValue();
        String location = locationCombo.getValue();

        applyAdvancedFilters(brand, price, year, location);
    }

    // Brand Actions
    @FXML
    private void onBrandClick() {
        Button clickedButton = (Button) ((VBox) ((Button) trucksGrid.getScene().getFocusOwner()).getParent()).getChildren().get(1);
        String brandName = clickedButton.getText();
        filterByBrand(brandName);
    }

    // Truck Actions
    @FXML
    private void onSortChange() {
        String sortOption = sortCombo.getValue();
        if (sortOption != null) {
            sortTrucks(sortOption);
        }
    }

    @FXML
    private void onViewMoreClick() {
        loadMoreTrucks();
    }

    // Private Methods
    private void performSearch(String searchText) {
        System.out.println("Ricerca per: " + searchText);
        // Implementare logica di ricerca
        updateTruckCount();
    }

    private void applyAdvancedFilters(String brand, String price, String year, String location) {
        System.out.println("Filtri applicati - Brand: " + brand + ", Price: " + price + ", Year: " + year + ", Location: " + location);
        // Implementare logica filtri avanzati
        updateTruckCount();
    }

    private void filterByBrand(String brandName) {
        System.out.println("Filtro per marca: " + brandName);
        brandFilterCombo.setValue(brandName);
        // Implementare filtro per marca
        updateTruckCount();
    }

    private void sortTrucks(String sortOption) {
        System.out.println("Ordinamento: " + sortOption);
        // Implementare logica di ordinamento
    }

    private void loadMoreTrucks() {
        System.out.println("Caricamento altri camion...");
        // Implementare caricamento additional trucks
    }

    private void populateSampleTrucks() {
        // Sample truck data
        List<TruckData> sampleTrucks = Arrays.asList(
                new TruckData("Volvo FH16", "2022", "45.000", "540", "€ 125.000", "Milano", "https://example.com/volvo.jpg"),
                new TruckData("Mercedes Actros", "2021", "62.000", "450", "€ 98.000", "Roma", "https://example.com/mercedes.jpg"),
                new TruckData("Scania R730", "2023", "28.000", "730", "€ 165.000", "Torino", "https://example.com/scania.jpg"),
                new TruckData("MAN TGX", "2020", "78.500", "480", "€ 85.000", "Napoli", "https://example.com/man.jpg"),
                new TruckData("Iveco S-Way", "2022", "35.200", "460", "€ 95.000", "Bologna", "https://example.com/iveco.jpg"),
                new TruckData("DAF XF", "2021", "52.000", "530", "€ 110.000", "Firenze", "https://example.com/daf.jpg")
        );

        trucksGrid.getChildren().clear();

        for (int i = 0; i < sampleTrucks.size() && i < 6; i++) {
            TruckData truck = sampleTrucks.get(i);
            VBox truckCard = createTruckCard(truck);

            int col = i % 2;
            int row = i / 2;
            trucksGrid.add(truckCard, col, row);
        }
    }

    private VBox createTruckCard(TruckData truck) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        card.setPrefWidth(480);
        card.setPrefHeight(360);

        // Truck Image (Larger)
        ImageView imageView = new ImageView();
        imageView.setFitWidth(450);
        imageView.setFitHeight(220);
        imageView.setPreserveRatio(false);
        imageView.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");

        // Placeholder for truck image
        Label imagePlaceholder = new Label("📷 " + truck.model);
        imagePlaceholder.setStyle("-fx-font-size: 18; -fx-text-fill: #7f8c8d;");

        StackPane imageContainer = new StackPane(imagePlaceholder);
        imageContainer.setPrefSize(450, 220);
        imageContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        // Truck Details
        VBox details = new VBox(5);
        details.setAlignment(Pos.CENTER_LEFT);

        Label modelLabel = new Label(truck.model);
        modelLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        modelLabel.setStyle("-fx-text-fill: #2c3e50;");

        HBox specs = new HBox(15);
        specs.setAlignment(Pos.CENTER_LEFT);

        Label yearLabel = new Label("📅 " + truck.year);
        yearLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        Label kmLabel = new Label("🛣️ " + truck.kilometers + " km");
        kmLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        Label powerLabel = new Label("⚡ " + truck.horsepower + " CV");
        powerLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        specs.getChildren().addAll(yearLabel, kmLabel, powerLabel);

        HBox priceLocation = new HBox();
        priceLocation.setAlignment(Pos.CENTER_LEFT);

        Label priceLabel = new Label(truck.price);
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        priceLabel.setStyle("-fx-text-fill: #ff6b35;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label locationLabel = new Label("📍 " + truck.location);
        locationLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        priceLocation.getChildren().addAll(priceLabel, spacer, locationLabel);

        details.getChildren().addAll(modelLabel, specs, priceLocation);

        card.getChildren().addAll(imageContainer, details);

        // Add click event
        card.setOnMouseClicked(e -> onTruckCardClick(truck));
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "; -fx-cursor: hand;"));

        return card;
    }

    private void onTruckCardClick(TruckData truck) {
        showAlert("Dettagli Camion", "Apertura dettagli per: " + truck.model);
    }

    private void updateTruckCount() {
        // Update with actual count logic
        countLabel.setText("(6 veicoli)");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper class for truck data
    private static class TruckData {
        final String model;
        final String year;
        final String kilometers;
        final String horsepower;
        final String price;
        final String location;
        final String imageUrl;

        TruckData(String model, String year, String kilometers, String horsepower,
                  String price, String location, String imageUrl) {
            this.model = model;
            this.year = year;
            this.kilometers = kilometers;
            this.horsepower = horsepower;
            this.price = price;
            this.location = location;
            this.imageUrl = imageUrl;
        }
    }
}
