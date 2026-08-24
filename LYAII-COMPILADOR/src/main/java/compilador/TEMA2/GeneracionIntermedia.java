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
        String variable = null;
        String expresion = codigo;

        if (codigo.contains("=")) {
            String[] partes = codigo.split("=", 2);

            variable = partes[0].trim();
            expresion = partes[1].trim();

            if (!variable.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                throw new IllegalArgumentException("Variable de asignación no válida.");
            }

            if (expresion.isEmpty()) {
                throw new IllegalArgumentException("Falta la expresión.");
            }
        }

        List<String> tokens = tokenizar(expresion);
        List<String> postfija = convertirPostfija(tokens);

        Deque<String> pila = new ArrayDeque<>();
        StringBuilder intermedio = new StringBuilder();
        int temporal = 1;

        for (String token : postfija) {
            if (esOperador(token)) {
                if (pila.size() < 2) {
                    throw new IllegalArgumentException("Expresión incorrecta.");
                }

                String b = pila.pop();
                String a = pila.pop();
                String t = "T" + temporal++;

                intermedio.append(t)
                        .append(" = ")
                        .append(a)
                        .append(" ")
                        .append(token)
                        .append(" ")
                        .append(b)
                        .append("\n");

                pila.push(t);
            } else {
                pila.push(token);
            }
        }

        if (pila.size() != 1) {
            throw new IllegalArgumentException("Expresión incorrecta.");
        }

        String resultado = pila.pop();

        if (variable != null) {
            intermedio.append(variable)
                    .append(" = ")
                    .append(resultado)
                    .append("\n");
        }

        return """
                CÓDIGO FUENTE:
                %s

                CÓDIGO DE TRES DIRECCIONES:

                %s
                """.formatted(codigo, intermedio);
    }

    private List<String> tokenizar(String expresion) {
        List<String> tokens = new ArrayList<>();

        for (int i = 0; i < expresion.length();) {
            char c = expresion.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (Character.isLetter(c) || c == '_') {
                StringBuilder token = new StringBuilder();

                while (i < expresion.length()
                        && (Character.isLetterOrDigit(expresion.charAt(i))
                        || expresion.charAt(i) == '_')) {
                    token.append(expresion.charAt(i++));
                }

                tokens.add(token.toString());
                continue;
            }

            if (Character.isDigit(c)) {
                StringBuilder token = new StringBuilder();
                boolean punto = false;

                while (i < expresion.length()) {
                    char actual = expresion.charAt(i);

                    if (Character.isDigit(actual)) {
                        token.append(actual);
                        i++;
                    } else if (actual == '.' && !punto) {
                        punto = true;
                        token.append(actual);
                        i++;
                    } else {
                        break;
                    }
                }

                tokens.add(token.toString());
                continue;
            }

            if ("+-*/()".indexOf(c) >= 0) {
                tokens.add(String.valueOf(c));
                i++;
                continue;
            }

            throw new IllegalArgumentException(
                    "Carácter no válido: " + c
            );
        }

        return tokens;
    }

    private List<String> convertirPostfija(List<String> tokens) {
        List<String> salida = new ArrayList<>();
        Deque<String> operadores = new ArrayDeque<>();

        for (String token : tokens) {
            if (!esOperador(token)
                    && !token.equals("(")
                    && !token.equals(")")) {
                salida.add(token);
            } else if (token.equals("(")) {
                operadores.push(token);
            } else if (token.equals(")")) {
                while (!operadores.isEmpty()
                        && !operadores.peek().equals("(")) {
                    salida.add(operadores.pop());
                }

                if (operadores.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Paréntesis incorrectos."
                    );
                }

                operadores.pop();
            } else {
                while (!operadores.isEmpty()
                        && esOperador(operadores.peek())
                        && prioridad(operadores.peek()) >= prioridad(token)) {
                    salida.add(operadores.pop());
                }

                operadores.push(token);
            }
        }

        while (!operadores.isEmpty()) {
            if (operadores.peek().equals("(")) {
                throw new IllegalArgumentException(
                        "Paréntesis incorrectos."
                );
            }

            salida.add(operadores.pop());
        }

        return salida;
    }

    private boolean esOperador(String token) {
        return token.equals("+")
                || token.equals("-")
                || token.equals("*")
                || token.equals("/");
    }

    private int prioridad(String operador) {
        return operador.equals("*") || operador.equals("/") ? 2 : 1;
    }
}