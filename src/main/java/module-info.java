module org.example.projeto.ia {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.projeto_ia to javafx.fxml;
    exports org.example.projeto_ia;
    exports org.example.qlearning;
}