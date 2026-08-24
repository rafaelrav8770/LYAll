package compilador.TEMA1;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ComprobacionTipos {
    private final Stage stage;
    private final Runnable regresar;

    public ComprobacionTipos(Stage stage, Runnable regresar) {
        this.stage = stage;
        this.regresar = regresar;
    }

    public void mostrar() {
        Label titulo = new Label("COMPROBACIÓN DE TIPOS");
        titulo.setStyle("-fx-font-size:30px;-fx-font-weight:bold;");

        ComboBox<String> tipoVariable = new ComboBox<>();
        tipoVariable.getItems().addAll("int", "float", "String");
        tipoVariable.setValue("int");

        ComboBox<String> tipoValor = new ComboBox<>();
        tipoValor.getItems().addAll("int", "float", "String");
        tipoValor.setValue("int");

        Label resultado = new Label();

        Button comprobar = new Button("Comprobar");
        comprobar.setMaxWidth(Double.MAX_VALUE);

        comprobar.setOnAction(e -> {
            String variable = tipoVariable.getValue();
            String valor = tipoValor.getValue();

            if (variable.equals(valor))
                resultado.setText("✓ Asignación válida: " + valor + " → " + variable);
            else if (variable.equals("float") && valor.equals("int"))
                resultado.setText("✓ Válido con conversión implícita int → float");
            else
                resultado.setText("✗ ERROR SEMÁNTICO: no se puede asignar " + valor + " a " + variable);
        });

        Button volver = new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e -> regresar.run());

        VBox root = new VBox(18,
                titulo,
                new Label("Tipo de la variable:"), tipoVariable,
                new Label("Tipo del valor:"), tipoValor,
                comprobar, resultado, volver);

        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root, 800, 620));
        stage.setTitle("Comprobación de tipos");
        stage.show();
    }
}