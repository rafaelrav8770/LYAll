package compilador.TEMA1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.*;

public class ArbolExpresiones {
    private final Stage stage;
    private final Runnable regresar;
    private String entrada;
    private int pos;

    public ArbolExpresiones(Stage stage, Runnable regresar) {
        this.stage = stage;
        this.regresar = regresar;
    }

    public void mostrar() {
        Label titulo = new Label("ÁRBOLES DE EXPRESIONES");
        titulo.setStyle("-fx-font-size:30px;-fx-font-weight:bold;");

        Label descripcion = new Label("Ingresa una expresión sencilla para mostrar su árbol.");
        descripcion.setStyle("-fx-font-size:17px;");

        TextField expresion = new TextField();
        expresion.setStyle("-fx-font-size:18px;");
        expresion.setPrefHeight(45);

        Button generar = new Button("Generar árbol");
        generar.setPrefHeight(50);
        generar.setMaxWidth(Double.MAX_VALUE);
        generar.setStyle("-fx-font-size:18px;-fx-font-weight:bold;");

        TextArea resultado = new TextArea();
        resultado.setEditable(false);
        resultado.setStyle("-fx-font-family:monospace;-fx-font-size:20px;");
        resultado.setPrefHeight(250);

        generar.setOnAction(e -> {
            String texto = expresion.getText().trim();
            if (texto.isEmpty()) {
                resultado.setText("Escribe una expresión.");
                return;
            }
            resultado.setText(generarArbol(texto));
        });

        Button volver = new Button("← Regresar");
        volver.setPrefHeight(45);
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setStyle("-fx-font-size:17px;");
        volver.setOnAction(e -> regresar.run());

        VBox root = new VBox(15, titulo, descripcion, expresion, generar, resultado, volver);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);

        stage.setTitle("Árboles de expresiones");
        stage.setScene(new Scene(root, 800, 620));
        stage.centerOnScreen();
        stage.show();
    }

    private static class Nodo {
        String valor;
        Nodo izq, der;

        Nodo(String valor) {
            this.valor = valor;
        }

        Nodo(String valor, Nodo izq, Nodo der) {
            this.valor = valor;
            this.izq = izq;
            this.der = der;
        }

        boolean esHoja() {
            return izq == null && der == null;
        }
    }

    private String generarArbol(String texto) {
        try {
            entrada = texto.replaceAll("\\s+", "");
            pos = 0;
            if (entrada.isEmpty()) return "Escribe una expresión.";

            Nodo raiz = parseExpr();
            if (pos < entrada.length())
                return "Expresión inválida cerca de: " + entrada.substring(pos);

            return imprimirArbol(raiz);
        } catch (RuntimeException e) {
            return "Expresión inválida.\nUsa operandos y +, -, *, /, ( )";
        }
    }

    private Nodo parseExpr() {
        Nodo izq = parseTerm();
        while (pos < entrada.length() && (actual() == '+' || actual() == '-')) {
            char op = actual();
            pos++;
            izq = new Nodo(String.valueOf(op), izq, parseTerm());
        }
        return izq;
    }

    private Nodo parseTerm() {
        Nodo izq = parseFactor();
        while (pos < entrada.length() && (actual() == '*' || actual() == '/')) {
            char op = actual();
            pos++;
            izq = new Nodo(String.valueOf(op), izq, parseFactor());
        }
        return izq;
    }

    private Nodo parseFactor() {
        if (pos < entrada.length() && actual() == '(') {
            pos++;
            Nodo nodo = parseExpr();
            if (pos >= entrada.length() || actual() != ')')
                throw new RuntimeException();
            pos++;
            return nodo;
        }
        return parseOperando();
    }

    private Nodo parseOperando() {
        int inicio = pos;
        while (pos < entrada.length() && esOperando(actual())) pos++;
        if (pos == inicio) throw new RuntimeException();
        return new Nodo(entrada.substring(inicio, pos));
    }

    private boolean esOperando(char c) {
        return Character.isLetterOrDigit(c) || c == '.' || c == '_';
    }

    private char actual() {
        return entrada.charAt(pos);
    }

    private static class Caja {
        List<String> lineas;
        int ancho, altura, centro;

        Caja(List<String> lineas, int ancho, int altura, int centro) {
            this.lineas = lineas;
            this.ancho = ancho;
            this.altura = altura;
            this.centro = centro;
        }
    }

    private String repetir(String s, int n) {
        return s.repeat(Math.max(0, n));
    }

    private Caja construirCaja(Nodo nodo) {
        if (nodo.esHoja())
            return new Caja(new ArrayList<>(List.of(nodo.valor)), nodo.valor.length(), 1, nodo.valor.length() / 2);

        if (nodo.der == null) {
            Caja izq = construirCaja(nodo.izq);
            String s = nodo.valor;
            int u = s.length(), n = izq.ancho, x = izq.centro;

            List<String> lineas = new ArrayList<>();
            lineas.add(repetir(" ", x + 1) + repetir("_", n - x - 1) + s);
            lineas.add(repetir(" ", x) + "/" + repetir(" ", n - x - 1 + u));
            for (String l : izq.lineas) lineas.add(l + repetir(" ", u));

            return new Caja(lineas, n + u, izq.altura + 2, n + u / 2);
        }

        if (nodo.izq == null) {
            Caja der = construirCaja(nodo.der);
            String s = nodo.valor;
            int u = s.length(), m = der.ancho, y = der.centro;

            List<String> lineas = new ArrayList<>();
            lineas.add(s + repetir("_", y) + repetir(" ", m - y));
            lineas.add(repetir(" ", u + y) + "\\" + repetir(" ", m - y - 1));
            for (String l : der.lineas) lineas.add(repetir(" ", u) + l);

            return new Caja(lineas, m + u, der.altura + 2, u / 2);
        }

        Caja izq = construirCaja(nodo.izq);
        Caja der = construirCaja(nodo.der);

        String s = nodo.valor;
        int u = s.length(), n = izq.ancho, x = izq.centro;
        int m = der.ancho, y = der.centro;

        List<String> li = new ArrayList<>(izq.lineas);
        List<String> ld = new ArrayList<>(der.lineas);

        while (li.size() < ld.size()) li.add(repetir(" ", n));
        while (ld.size() < li.size()) ld.add(repetir(" ", m));

        List<String> lineas = new ArrayList<>();
        lineas.add(repetir(" ", x + 1) + repetir("_", n - x - 1) + s +
                repetir("_", y) + repetir(" ", m - y));
        lineas.add(repetir(" ", x) + "/" +
                repetir(" ", n - x - 1 + u + y) + "\\" +
                repetir(" ", m - y - 1));

        for (int i = 0; i < li.size(); i++)
            lineas.add(li.get(i) + repetir(" ", u) + ld.get(i));

        return new Caja(lineas, n + m + u, Math.max(izq.altura, der.altura) + 2, n + u / 2);
    }

    private String imprimirArbol(Nodo nodo) {
        return String.join("\n", construirCaja(nodo).lineas);
    }
}