package compilador.TEMA4;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class registros {

    private final Stage stage;
    private final Runnable regresar;

    public registros(Stage stage, Runnable regresar) {
        this.stage = stage;
        this.regresar = regresar;
    }

    public void mostrar() {

        Label titulo = new Label("REGISTROS");
        titulo.setStyle("-fx-font-size:28px;-fx-font-weight:bold;");

        TextField entrada = new TextField();
        entrada.setStyle("-fx-font-family:monospace;-fx-font-size:15px;");

        TextArea salida = new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-family:monospace;-fx-font-size:18px;");

        Button generar = new Button("Generar registros");
        Button limpiar = new Button("Limpiar");
        Button volver = new Button("← Regresar");

        generar.setMaxWidth(Double.MAX_VALUE);
        limpiar.setMaxWidth(Double.MAX_VALUE);
        volver.setMaxWidth(Double.MAX_VALUE);

        generar.setOnAction(e -> {

            String exp = entrada.getText().replaceAll("\\s+", "");

            if (!exp.matches("[a-zA-Z]=[a-zA-Z0-9]+[+\\-*/][a-zA-Z0-9]+")) {
                salida.setText("""
                        Expresión no válida.
                        Ejemplos:
                        a = b + c
                        x = y - z
                        r = a * b
                        d = x / y
                        """);
                return;
            }

            String dest = exp.substring(0, 1);
            String[] v = exp.substring(2).split("[+\\-*/]");
            char op = exp.charAt(2 + v[0].length());

            String asm = switch (op) {
                case '+' -> "ADD AX, BX";
                case '-' -> "SUB AX, BX";
                case '*' -> "MUL BX";
                case '/' -> "XOR DX, DX\nDIV BX";
                default -> "";
            };

            String extra = switch (op) {
                case '*' -> "\nDX -> parte alta del producto";
                case '/' -> "\nDX -> residuo de la division";
                default -> "";
            };

            String resultado = switch (op) {
                case '*' -> "Producto en DX:AX\n" + dest + " recibe la parte baja desde AX";
                case '/' -> "AX = cociente\nDX = residuo\n" + dest + " recibe el cociente desde AX";
                default -> dest + " se almacena desde AX";
            };

            salida.setText(
                    "EXPRESION: " + dest + " = " + v[0] + " " + op + " " + v[1] +
                            "\nREGISTROS\n" +
                            v[0] + " -> AX\n" +
                            v[1] + " -> BX" + extra +
                            "\nOPERACIONES\n" +
                            "MOV AX, " + v[0] + "\n" +
                            "MOV BX, " + v[1] + "\n" +
                            asm + "\n" +
                            "MOV " + dest + ", AX" +
                            "\nRESULTADO\n" +
                            resultado
            );
        });

        limpiar.setOnAction(e -> {
            entrada.clear();
            salida.clear();
        });

        volver.setOnAction(e -> regresar.run());
        entrada.setOnAction(generar.getOnAction());

        VBox root = new VBox(15, titulo, entrada, generar, salida, limpiar, volver);
        VBox.setVgrow(salida, Priority.ALWAYS);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root, 850, 650));
        stage.setTitle("Registros");
        stage.show();
    }
}