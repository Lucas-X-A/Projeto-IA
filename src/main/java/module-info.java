module org.example.projeto_ia {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.projeto_ia to javafx.fxml;
    exports org.example.projeto_ia;
}