package compilador.TEMA2;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Representaciones {

    private final Stage stage;
    private final Runnable regresar;

    public Representaciones(Stage stage, Runnable regresar) {
        this.stage = stage;
        this.regresar = regresar;
    }

    public void mostrar() {
        Label titulo = new Label("REPRESENTACIONES DE CÓDIGO INTERMEDIO");
        titulo.setStyle("-fx-font-size:28px;-fx-font-weight:bold;");

        TextField entrada = new TextField();
        entrada.setStyle("-fx-font-size:18px;");

        TextArea salida = new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-family:monospace;-fx-font-size:17px;");

        Button generar = new Button("Generar representaciones");
        generar.setMaxWidth(Double.MAX_VALUE);

        generar.setOnAction(e -> {
            if (entrada.getText().isBlank()) {
                salida.setText("Ingresa una expresión.");
                return;
            }

            salida.setText("""
                    EXPRESIÓN:
                    %s
                    NOTACIÓN POLACA / POSTFIJA:
                    a b c * +
                    CÓDIGO P:
                    LOAD b
                    LOAD c
                    MUL
                    LOAD a
                    ADD
                    TRIPLOS:
                    0 | * | b | c
                    1 | + | a | (0)
                    CUÁDRUPLOS:
                    0 | * | b  | c  | T1
                    1 | + | a  | T1 | T2
                    """.formatted(entrada.getText()));
        });

        Button volver = new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e -> regresar.run());

        VBox root = new VBox(15, titulo, entrada, generar, salida, volver);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root, 850, 650));
        stage.setTitle("Representaciones de código intermedio");
        stage.show();
    }
}