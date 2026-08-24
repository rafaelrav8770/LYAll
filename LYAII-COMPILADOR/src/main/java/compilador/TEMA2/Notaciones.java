package compilador.TEMA2;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Stack;

public class Notaciones {

    private final Stage stage;
    private final Runnable regresar;

    public Notaciones(Stage stage, Runnable regresar) {
        this.stage = stage;
        this.regresar = regresar;
    }

    public void mostrar() {
        Label titulo = new Label("NOTACIONES");
        titulo.setStyle("-fx-font-size:30px;-fx-font-weight:bold;");

        TextField entrada = new TextField();
        entrada.setStyle("-fx-font-size:18px;");

        TextArea salida = new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-size:18px;");

        Button convertir = new Button("Convertir");
        convertir.setMaxWidth(Double.MAX_VALUE);

        convertir.setOnAction(e -> {
            String expresion = entrada.getText().trim();

            if (expresion.isEmpty()) {
                salida.setText("Ingresa una expresión.");
                return;
            }

            salida.setText(
                    "INFIJA:\n" + expresion +
                            "\n\nPOSTFIJA:\n" + postfija(expresion) +
                            "\n\nPREFIJA:\n" + prefija(expresion)
            );
        });

        Button volver = new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e -> regresar.run());

        VBox root = new VBox(15, titulo, entrada, convertir, salida, volver);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);

        stage.setScene(new Scene(root, 820, 620));
        stage.setTitle("Notaciones");
        stage.show();
    }

    private int prioridad(char c) {
        return switch (c) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            case '^' -> 3;
            default -> 0;
        };
    }

    private String postfija(String expresion) {
        StringBuilder salida = new StringBuilder();
        Stack<Character> pila = new Stack<>();

        for (char c : expresion.replaceAll("\\s+", "").toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                salida.append(c).append(" ");
            } else if (c == '(') {
                pila.push(c);
            } else if (c == ')') {
                while (!pila.isEmpty() && pila.peek() != '(')
                    salida.append(pila.pop()).append(" ");

                if (!pila.isEmpty()) pila.pop();
            } else {
                while (!pila.isEmpty() &&
                        pila.peek() != '(' &&
                        prioridad(pila.peek()) >= prioridad(c)) {
                    salida.append(pila.pop()).append(" ");
                }

                pila.push(c);
            }
        }

        while (!pila.isEmpty())
            salida.append(pila.pop()).append(" ");

        return salida.toString().trim();
    }

    private String prefija(String expresion) {
        String invertida = new StringBuilder(
                expresion.replaceAll("\\s+", "")
        ).reverse().toString();

        invertida = invertida
                .replace('(', '#')
                .replace(')', '(')
                .replace('#', ')');

        String post = postfija(invertida).replace(" ", "");

        return new StringBuilder(post).reverse().toString();
    }
}