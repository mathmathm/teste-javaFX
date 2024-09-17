module com.example.cadastrousuario {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;

    opens com.example.cadastrousuario to javafx.fxml;
    exports com.example.cadastrousuario;
}