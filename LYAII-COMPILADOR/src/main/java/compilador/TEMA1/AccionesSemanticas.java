package compilador.TEMA1;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.*;

public class AccionesSemanticas {
    private final Stage stage;
    private final Runnable regresar;
    private final Map<String,String> simbolos = new HashMap<>();

    public AccionesSemanticas(Stage stage, Runnable regresar) {
        this.stage = stage;
        this.regresar = regresar;
    }

    public void mostrar() {
        Label titulo = new Label("ACCIONES SEMÁNTICAS");
        titulo.setStyle("-fx-font-size:30px;-fx-font-weight:bold;");

        Label descripcion = new Label("Verifica declaraciones, asignaciones y operaciones para determinar si una instrucción tiene un significado semántico válido.");
        descripcion.setStyle("-fx-font-size:16px;");
        descripcion.setWrapText(true);

        TextField entrada = new TextField();
        entrada.setStyle("-fx-font-size:16px;");

        Button analizar = new Button("Analizar");
        TextArea salida = new TextArea();
        salida.setEditable(false);
        salida.setWrapText(true);
        salida.setStyle("-fx-font-size:16px;");

        analizar.setOnAction(e -> salida.setText(analizar(entrada.getText())));
        entrada.setOnAction(e -> salida.setText(analizar(entrada.getText())));

        HBox fila = new HBox(10, entrada, analizar);
        HBox.setHgrow(entrada, Priority.ALWAYS);

        Button volver = new Button("← Regresar");
        volver.setOnAction(e -> regresar.run());

        VBox root = new VBox(15, titulo, descripcion, fila, salida, volver);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root, 850, 600));
        stage.setTitle("Acciones semánticas");
        stage.show();
    }

    private String analizar(String texto) {
        if (texto == null || texto.isBlank()) return "Ingrese una instrucción.";

        String s = texto.trim().replaceAll(";$", "");

        if (s.matches("(int|float|string|boolean)\\s+\\w+")) {
            String[] p = s.split("\\s+");
            if (simbolos.containsKey(p[1])) return "ERROR SEMÁNTICO\n\nLa variable '" + p[1] + "' ya fue declarada.";
            simbolos.put(p[1], p[0]);
            return "DECLARACIÓN\n\nVariable: " + p[1] + "\nTipo: " + p[0] + "\n\n✓ Instrucción semánticamente válida.";
        }

        if (s.matches("(int|float|string|boolean)\\s+\\w+\\s*=\\s*.+")) {
            String[] l = s.split("=", 2);
            String[] d = l[0].trim().split("\\s+");
            String tipo = d[0], var = d[1], valor = l[1].trim();

            if (simbolos.containsKey(var)) return "ERROR SEMÁNTICO\n\nLa variable '" + var + "' ya fue declarada.";

            String tv = tipo(valor);
            if (tv == null) return "ERROR SEMÁNTICO\n\nNo se pudo determinar el tipo de " + valor;
            if (!compatible(tipo, tv)) return "ERROR SEMÁNTICO\n\nTipo declarado: " + tipo + "\nTipo del valor: " + tv + "\n\n✗ Tipos incompatibles.";

            simbolos.put(var, tipo);
            return "DECLARACIÓN Y ASIGNACIÓN\n\nVariable: " + var + "\nTipo: " + tipo + "\nValor: " + valor + "\n\n✓ Instrucción semánticamente válida.";
        }

        if (s.matches("\\w+\\s*=\\s*.+")) {
            String[] p = s.split("=", 2);
            String var = p[0].trim(), valor = p[1].trim();

            if (!simbolos.containsKey(var)) return "ERROR SEMÁNTICO\n\nLa variable '" + var + "' no ha sido declarada.";

            String tv = tipo(valor), tipoVar = simbolos.get(var);
            if (tv == null || !compatible(tipoVar, tv))
                return "ERROR SEMÁNTICO\n\nVariable: " + var + "\nTipo esperado: " + tipoVar + "\nTipo recibido: " + tv;

            return "ASIGNACIÓN\n\nVariable: " + var + "\nValor: " + valor + "\n\n✓ Asignación válida.";
        }

        if (s.matches(".+\\s*[+\\-*/]\\s*.+")) {
            String[] p = s.split("\\s*[+\\-*/]\\s*", 2);
            String t1 = tipo(p[0].trim()), t2 = tipo(p[1].trim());

            if (t1 == null || t2 == null) return "ERROR SEMÁNTICO\n\nNo se pudo determinar el tipo de los operandos.";

            boolean n1 = t1.equals("int") || t1.equals("float");
            boolean n2 = t2.equals("int") || t2.equals("float");

            if (!n1 || !n2) return "ERROR SEMÁNTICO\n\nLa operación requiere valores numéricos.";

            String r = t1.equals("float") || t2.equals("float") ? "float" : "int";
            return "OPERACIÓN\n\nTipo operando 1: " + t1 + "\nTipo operando 2: " + t2 + "\nResultado: " + r + "\n\n✓ Operación válida.";
        }

        return "Instrucción no reconocida.\n\nEjemplos:\nint x;\nint x = 10;\nx = 20;\nx + 5";
    }

    private String tipo(String v) {
        v = v.trim();
        if (simbolos.containsKey(v)) return simbolos.get(v);
        if (v.matches("-?\\d+")) return "int";
        if (v.matches("-?\\d+\\.\\d+")) return "float";
        if (v.matches("\".*\"")) return "string";
        if (v.equals("true") || v.equals("false")) return "boolean";
        return null;
    }

    private boolean compatible(String destino, String origen) {
        return destino.equals(origen) || destino.equals("float") && origen.equals("int");
    }
}