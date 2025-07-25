module com.progetto.ingsw.trukscout24 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.progetto.ingsw.trukscout24 to javafx.fxml;
    exports com.progetto.ingsw.trukscout24;
    exports com.progetto.ingsw.trukscout24.Controller;
    opens com.progetto.ingsw.trukscout24.Controller to javafx.fxml;
}