package compilador.TEMA1;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Stack;

public class PilaSemantica {
    private final Stage stage;
    private final Runnable regresar;
    private final Stack<String> pila = new Stack<>();

    public PilaSemantica(Stage stage, Runnable regresar) {
        this.stage = stage;
        this.regresar = regresar;
    }

    public void mostrar() {
        Label titulo = new Label("PILA SEMÁNTICA");
        titulo.setStyle("-fx-font-size:30px;-fx-font-weight:bold;");

        TextField valor = new TextField();
        valor.setPromptText("Valor para la pila");

        TextArea salida = new TextArea();
        salida.setEditable(false);

        Button push = new Button("PUSH");
        Button pop = new Button("POP");

        push.setOnAction(e -> {
            if (!valor.getText().isBlank()) {
                pila.push(valor.getText());
                salida.setText(pila.toString());
                valor.clear();
            }
        });

        pop.setOnAction(e -> {
            if (!pila.isEmpty()) pila.pop();
            salida.setText(pila.toString());
        });

        Button volver = new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e -> regresar.run());

        VBox root = new VBox(15, titulo, valor, push, pop, salida, volver);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root, 800, 620));
        stage.setTitle("Pila semántica");
        stage.show();
    }
}