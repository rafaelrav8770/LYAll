package compilador.TEMA3;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class OptimizacionLocal {
    private final Stage stage;
    private final Runnable regresar;

    public OptimizacionLocal(Stage stage, Runnable regresar){
        this.stage = stage;
        this.regresar = regresar;
    }

    public void mostrar(){
        Label titulo = new Label("OPTIMIZACIÓN LOCAL");
        titulo.setStyle("-fx-font-size:28px;-fx-font-weight:bold;");

        TextArea entrada = new TextArea();
        entrada.setStyle("-fx-font-family:monospace;-fx-font-size:18px;");

        TextArea salida = new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-family:monospace;-fx-font-size:18px;");

        Button optimizar = new Button("Optimizar");
        optimizar.setMaxWidth(Double.MAX_VALUE);

        optimizar.setOnAction(e -> {
            String codigo = entrada.getText();
            if (codigo.isBlank()) {
                salida.setText("Ingresa código.");
                return;
            }
            String resultado = aplicarOptimizacionLocal(codigo);
            salida.setText("CODIGO OPTIMIZADO:\n" + resultado);
        });

        Button volver = new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e -> regresar.run());

        VBox root = new VBox(15, titulo, entrada, optimizar, salida, volver);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root, 850, 650));
        stage.setTitle("Optimización local");
        stage.show();
    }

    private String aplicarOptimizacionLocal(String codigo) {
        return new compilador.servicios.ServicioOptimizacionLocal().aplicarOptimizacionLocal(codigo);
    }
}
