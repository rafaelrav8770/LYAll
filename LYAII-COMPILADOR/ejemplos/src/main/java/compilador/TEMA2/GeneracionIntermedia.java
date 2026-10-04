package compilador.TEMA2;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class GeneracionIntermedia {

    private final Stage stage;
    private final Runnable regresar;

    public GeneracionIntermedia(Stage stage, Runnable regresar) {
        this.stage = stage;
        this.regresar = regresar;
    }

    public void mostrar() {
        Label titulo = new Label("ESQUEMA DE GENERACIÓN DE CÓDIGO INTERMEDIO");
        titulo.setStyle("-fx-font-size:28px;-fx-font-weight:bold;");

        TextField entrada = new TextField();
        entrada.setStyle("-fx-font-size:18px;");

        TextArea salida = new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-family:monospace;-fx-font-size:18px;");

        Button generar = new Button("Generar código intermedio");
        generar.setMaxWidth(Double.MAX_VALUE);

        generar.setOnAction(e -> {
            String codigo = entrada.getText().trim();

            if (codigo.isEmpty()) {
                salida.setText("Ingresa una expresión o instrucción.");
                return;
            }

            try {
                salida.setText(generarCodigo(codigo));
            } catch (Exception ex) {
                salida.setText("Error: " + ex.getMessage());
            }
        });

        Button volver = new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e -> regresar.run());

        VBox root = new VBox(15, titulo, entrada, generar, salida, volver);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root, 850, 620));
        stage.setTitle("Esquema de generación de código intermedio");
        stage.show();
    }

    private String generarCodigo(String codigo) {
        return new compilador.servicios.ServicioIntermedio().generarCodigo(codigo);
    }
}
