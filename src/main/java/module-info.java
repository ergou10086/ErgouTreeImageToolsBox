module org.ergoutree.ergoutreeimagebox {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.desktop;
    requires javafx.swing;

    opens org.ergoutree.ergoutreeimagebox to javafx.fxml;
    exports org.ergoutree.ergoutreeimagebox;
}