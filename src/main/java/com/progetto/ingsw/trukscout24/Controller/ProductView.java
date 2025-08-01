package com.progetto.ingsw.trukscout24.Controller;

import com.progetto.ingsw.trukscout24.Model.Camion;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

public class ProductView implements Initializable {

    @FXML private Label productTitle;
    @FXML private Label productSubtitle;
    @FXML private Label productPrice;
    @FXML private Label productDescription;

    // Specs labels
    @FXML private Label specPotenza;
    @FXML private Label specKilometri;
    @FXML private Label specCarburante;
    @FXML private Label specCambio;
    @FXML private Label specEmissioni;
    @FXML private Label specAnno;
    @FXML private Label specCategoria;

    // Image
    @FXML private ImageView mainImage;

    // Buttons
    @FXML private Button wishlistButton;

    // Data
    private Camion currentCamion;
    private final DecimalFormat priceFormatter = new DecimalFormat("#,###");
    private final DecimalFormat kmFormatter = new DecimalFormat("#,###");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize with sample data
        initializeSampleData();
    }

    private void initializeSampleData() {
        // Sample Camion data
        currentCamion = new Camion(
                "VOLVO_001",
                "Volvo",
                "FH16 750",
                750,
                450000.0,
                "Diesel",
                "Automatico",
                6,
                "2019",
                89500.0,
                "Volvo FH16 750 in ottime condizioni. Motore Euro 6 con potenza di 750 CV. Cambio automatico I-Shift. Interni in pelle, aria condizionata, sistema di navigazione avanzato. Manutenzione regolare presso officina autorizzata Volvo. Perfetto per trasporti a lungo raggio.",
                "Trattore Stradale"
        );

        loadCamionData(currentCamion);
    }

    public void loadCamionData(Camion camion) {
        this.currentCamion = camion;

        // Update UI elements
        productTitle.setText(camion.nome() + " " + camion.modello());

        // Build subtitle with key info
        String subtitle = "Euro " + camion.classeEmissioni() + " • " +
                camion.cambio() + " • " +
                kmFormatter.format(camion.kilometri()) + " km";
        productSubtitle.setText(subtitle);

        // Format and set price
        productPrice.setText("€ " + priceFormatter.format(camion.prezzo()));

        // Set description
        productDescription.setText(camion.descrizione());

        // Update specifications
        specPotenza.setText(camion.potenza() + " CV");
        specKilometri.setText(kmFormatter.format(camion.kilometri()) + " km");
        specCarburante.setText(camion.carburante());
        specCambio.setText(camion.cambio());
        specEmissioni.setText("Euro " + camion.classeEmissioni());
        specAnno.setText(camion.anno());
        specCategoria.setText(camion.categoria());

        // Load main image
        loadMainImage();
    }

    private void loadMainImage() {
        try {
            Image image = new Image(getClass().getResourceAsStream("../immagini/VOLVO/VVFH540.jpg"));
            mainImage.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
    }

    // Event Handlers
    @FXML
    private void onLogoClick() {
        System.out.println("Logo clicked - Navigate to Home");
        // TODO: Navigate to home page
    }

    @FXML
    private void onWishlistClick() {
        System.out.println("Adding to wishlist: " + currentCamion.id());
        // TODO: WishlistManager.addToWishlist(currentCamion);

        // Update button text to show added state
        if (wishlistButton.getText().contains("Salva")) {
            wishlistButton.setText("♥ Salvato");
            wishlistButton.getStyleClass().add("wishlist-button-active");
        } else {
            wishlistButton.setText("♥ Salva");
            wishlistButton.getStyleClass().remove("wishlist-button-active");
        }
    }

    // Public method to set camion data from other controllers
    public void setCamion(Camion camion) {
        loadCamionData(camion);
    }
}
