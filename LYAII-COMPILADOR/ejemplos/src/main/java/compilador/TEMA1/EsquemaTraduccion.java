package compilador.TEMA1;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EsquemaTraduccion {

    private final Stage stage;
    private final Runnable regresar;

    public EsquemaTraduccion(Stage stage, Runnable regresar) {
        this.stage = stage;
        this.regresar = regresar;
    }

    public void mostrar() {
        Label titulo = new Label("ESQUEMA DE TRADUCCIÓN");
        titulo.setStyle("-fx-font-size:30px;-fx-font-weight:bold;");

        Label descripcion = new Label("""
                Ingresa una expresión para comprobar si pertenece a la gramática. Se reconocen identificadores, números, operadores +, -, *, / y paréntesis.
                """);
        descripcion.setStyle("-fx-font-size:13px;");
        descripcion.setWrapText(true);

        Label gramatica = new Label("""
                GRAMÁTICA
                S → E
                E → E + T | E - T | T
                T → T * F | T / F | F
                F → (E) | ID | NUM
                """);
        gramatica.setStyle("-fx-font-family:Consolas;-fx-font-size:13px;");

        TextField entrada = new TextField();
        entrada.setStyle("-fx-font-size:16px;");

        Button analizar = new Button("Analizar expresión");
        analizar.setMaxWidth(Double.MAX_VALUE);

        TextArea salida = new TextArea();
        salida.setEditable(false);
        salida.setWrapText(true);
        salida.setStyle("-fx-font-family:Consolas;-fx-font-size:13px;");
        VBox.setVgrow(salida, Priority.ALWAYS);

        analizar.setOnAction(e ->
                salida.setText(new Analizador(entrada.getText()).analizar())
        );

        Button limpiar = new Button("Limpiar");
        limpiar.setMaxWidth(Double.MAX_VALUE);
        limpiar.setOnAction(e -> {
            entrada.clear();
            salida.clear();
        });

        Button volver = new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e -> regresar.run());

        VBox root = new VBox(
                12,
                titulo,
                descripcion,
                gramatica,
                new Label("Expresión a analizar:"),
                entrada,
                analizar,
                limpiar,
                new Label("Resultado:"),
                salida,
                volver
        );

        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root, 800, 760));
        stage.setTitle("Esquema de traducción");
        stage.show();
    }

    private static class Analizador {

        private final String texto;
        private int pos = 0;
        private final StringBuilder pasos = new StringBuilder();

        Analizador(String texto) {
            this.texto = texto == null ? "" : texto.replaceAll("\\s+", "");
        }

        String analizar() {
            if (texto.isBlank())
                return "Ingrese una expresión para analizar.";

            pasos.append("ESQUEMA DE TRADUCCIÓN\n");
            pasos.append("=====================\n");
            pasos.append("Expresión: ").append(texto).append("\n");
            pasos.append("PRODUCCIONES APLICADAS\n");
            pasos.append("S → E\n");

            boolean valido = expresion();

            if (valido && pos == texto.length()) {
                pasos.append("\nRESULTADO\n");
                pasos.append("✓ La expresión pertenece a la gramática.\n");
            } else {
                pasos.append("RESULTADO");
                pasos.append("✗ La expresión no pertenece a la gramática.\n");

                if (pos < texto.length())
                    pasos.append("Error cerca de: ")
                            .append(texto.substring(pos))
                            .append("\n");
            }

            return pasos.toString();
        }

        private boolean expresion() {
            pasos.append("E → T\n");

            if (!termino())
                return false;

            while (pos < texto.length()) {
                char c = texto.charAt(pos);

                if (c == '+' || c == '-') {
                    pos++;

                    pasos.append(c == '+'
                            ? "E → E + T\n"
                            : "E → E - T\n");

                    if (!termino())
                        return false;
                } else {
                    break;
                }
            }

            return true;
        }

        private boolean termino() {
            pasos.append("T → F\n");

            if (!factor())
                return false;

            while (pos < texto.length()) {
                char c = texto.charAt(pos);

                if (c == '*' || c == '/') {
                    pos++;

                    pasos.append(c == '*'
                            ? "T → T * F\n"
                            : "T → T / F\n");

                    if (!factor())
                        return false;
                } else {
                    break;
                }
            }

            return true;
        }

        private boolean factor() {
            if (pos >= texto.length())
                return false;

            char c = texto.charAt(pos);

            if (c == '(') {
                pasos.append("F → (E)\n");
                pos++;

                if (!expresion())
                    return false;

                if (pos >= texto.length() || texto.charAt(pos) != ')')
                    return false;

                pos++;
                return true;
            }

            if (Character.isLetter(c) || c == '_') {
                String id = leerIdentificador();

                pasos.append("F → ID\n");
                pasos.append("ID → ").append(id).append("");

                return true;
            }

            if (Character.isDigit(c)) {
                String num = leerNumero();

                pasos.append("F → NUM\n");
                pasos.append("NUM → ").append(num).append("\n");

                return true;
            }

            return false;
        }

        private String leerIdentificador() {
            int inicio = pos;

            while (pos < texto.length()) {
                char c = texto.charAt(pos);

                if (Character.isLetterOrDigit(c) || c == '_')
                    pos++;
                else
                    break;
            }

            return texto.substring(inicio, pos);
        }

        private String leerNumero() {
            int inicio = pos;
            boolean punto = false;

            while (pos < texto.length()) {
                char c = texto.charAt(pos);

                if (Character.isDigit(c)) {
                    pos++;
                } else if (c == '.' && !punto) {
                    punto = true;
                    pos++;
                } else {
                    break;
                }
            }

            return texto.substring(inicio, pos);
        }
    }
}