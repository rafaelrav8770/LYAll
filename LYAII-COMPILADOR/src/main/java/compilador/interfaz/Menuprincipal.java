package compilador.interfaz;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Menuprincipal {

    private final Stage stage;

    public Menuprincipal(Stage stage) {
        this.stage = stage;
    }

    public void mostrar() {

        Label titulo = new Label("LENGUAJES Y AUTÓMATAS II");
        titulo.getStyleClass().add("titulo");
        titulo.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");

        Label subtitulo = new Label("COMPILADOR");
        subtitulo.getStyleClass().add("subtitulo");
        subtitulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label autor = new Label("RAV");
        autor.getStyleClass().add("autor");
        autor.setStyle("-fx-font-size: 22px;");
        VBox encabezado = new VBox(6, titulo, subtitulo, autor);
        encabezado.setAlignment(Pos.CENTER);

        Button tema1 = crearBoton("Análisis semántico", () -> abrirtema(1));
        Button tema2 = crearBoton("Generación de código intermedio", () -> abrirtema(2));
        Button tema3 = crearBoton("Optimización", () -> abrirtema(3));
        Button tema4 = crearBoton("Generación de código objeto", () -> abrirtema(4));
        Button salir = crearBoton("Salir", Platform::exit);
        salir.getStyleClass().add("boton-salir");

        VBox botones = new VBox(12, tema1, tema2, tema3, tema4, salir);
        botones.setFillWidth(true);
        VBox.setVgrow(botones, Priority.ALWAYS);

        VBox root = new VBox(28, encabezado, botones);
        root.setPadding(new Insets(35, 55, 35, 55));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("root-panel");

        Scene scene = new Scene(root, 760, 560);
        aplicarCss(scene);

        stage.setTitle("Compilador - Lenguajes y Autómatas II -RAV");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    private Button crearBoton(String texto, Runnable accion) {
        Button boton = new Button(texto);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setPrefHeight(65);
        boton.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        boton.getStyleClass().add("boton-menu");
        boton.setOnAction(e -> accion.run());
        return boton;
    }

    private void abrirtema(int tema) {
        new Menutema(stage, tema, this).mostrar();
    }

    private void aplicarCss(Scene scene) {
        var recurso = getClass().getResource("/styles.css");
        if (recurso != null) {
            scene.getStylesheets().add(recurso.toExternalForm());
        }
    }
}
