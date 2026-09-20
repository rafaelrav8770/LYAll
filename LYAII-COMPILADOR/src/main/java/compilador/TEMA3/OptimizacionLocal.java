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

    private boolean esNumero(String s) {
        return s.matches("-?\\d+(\\.\\d+)?");
    }

    private String aplicarOptimizacionLocal(String codigo) {
        Map<String, String> valorActual = new HashMap<>();
        Map<String, String> expresionAVar = new LinkedHashMap<>();
        StringBuilder salida = new StringBuilder();

        String[] lineas = codigo.split("\\R");
        for (String lineaOriginal : lineas) {
            String linea = lineaOriginal.trim();
            if (linea.isEmpty()) continue;

            String[] partes = linea.split("=", 2);
            if (partes.length != 2) {
                salida.append(lineaOriginal).append("\n");
                continue;
            }

            String lhs = partes[0].trim();
            String rhs = partes[1].trim();
            String[] tokens = rhs.split("\\s+");
            String resultado;

            if (tokens.length == 1) {
                String operando = resolver(tokens[0], valorActual);
                resultado = operando;
                valorActual.put(lhs, resultado);

            } else if (tokens.length == 3) {
                String op1 = resolver(tokens[0], valorActual);
                String operador = tokens[1];
                String op2 = resolver(tokens[2], valorActual);

                if (esNumero(op1) && esNumero(op2)) {
                    double a = Double.parseDouble(op1);
                    double b = Double.parseDouble(op2);
                    double r;
                    switch (operador) {
                        case "+": r = a + b; break;
                        case "-": r = a - b; break;
                        case "*": r = a * b; break;
                        case "/": r = (b != 0) ? a / b : Double.NaN; break;
                        default:  r = Double.NaN;
                    }
                    resultado = formatearNumero(r);
                    valorActual.put(lhs, resultado);

                } else {
                    String simplificado = simplificarAlgebraico(op1, operador, op2);
                    if (simplificado != null) {
                        resultado = simplificado;
                        valorActual.put(lhs, resultado);
                    } else {
                        String clave = clave(operador, op1, op2);
                        if (expresionAVar.containsKey(clave)) {
                            resultado = expresionAVar.get(clave);
                            valorActual.put(lhs, resultado);
                        } else {
                            resultado = op1 + " " + operador + " " + op2;
                            expresionAVar.put(clave, lhs);
                            valorActual.remove(lhs);
                        }
                    }
                }
            } else {
                salida.append(lineaOriginal).append("\n");
                continue;
            }

            salida.append(lhs).append(" = ").append(resultado).append("\n");
        }
        return salida.toString();
    }

    private String resolver(String token, Map<String, String> valorActual) {
        if (esNumero(token)) return token;
        return valorActual.getOrDefault(token, token);
    }

    private String simplificarAlgebraico(String op1, String operador, String op2) {
        switch (operador) {
            case "+":
                if (op2.equals("0")) return op1;
                if (op1.equals("0")) return op2;
                break;
            case "-":
                if (op2.equals("0")) return op1;
                break;
            case "*":
                if (op2.equals("1")) return op1;
                if (op1.equals("1")) return op2;
                if (op1.equals("0") || op2.equals("0")) return "0";
                break;
            case "/":
                if (op2.equals("1")) return op1;
                break;
        }
        return null;
    }

    private String clave(String operador, String op1, String op2) {
        if (operador.equals("+") || operador.equals("*")) {
            String a = op1, b = op2;
            if (a.compareTo(b) > 0) { String t = a; a = b; b = t; }
            return operador + ":" + a + "," + b;
        }
        return operador + ":" + op1 + "," + op2;
    }

    private String formatearNumero(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d)) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }
}