package compilador.TEMA1;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.regex.*;

public class ErroresSemanticos {

    private final Stage stage;
    private final Runnable regresar;
    private final Map<String, String> variables = new LinkedHashMap<>();

    public ErroresSemanticos(Stage stage, Runnable regresar) {
        this.stage = stage;
        this.regresar = regresar;
    }

    public void mostrar() {
        Label titulo = new Label("ERRORES SEMÁNTICOS");
        titulo.setStyle("-fx-font-size:30px;-fx-font-weight:bold;");

        Label descripcion = new Label(
                "Ingresa instrucciones para detectar variables no declaradas, " +
                        "tipos incompatibles y llamadas incorrectas a funciones."
        );
        descripcion.setStyle("-fx-font-size:15px;");
        descripcion.setWrapText(true);

        TextArea codigo = new TextArea();

        codigo.setStyle("-fx-font-family:Consolas;-fx-font-size:15px;");
        codigo.setPrefHeight(90);

        Button analizar = new Button("Analizar");
        analizar.setMaxWidth(Double.MAX_VALUE);

        TextArea resultado = new TextArea();
        resultado.setEditable(false);
        resultado.setWrapText(true);
        resultado.setStyle("-fx-font-family:Consolas;-fx-font-size:14px;");
        VBox.setVgrow(resultado, Priority.ALWAYS);

        analizar.setOnAction(e -> resultado.setText(analizar(codigo.getText())));

        Button limpiar = new Button("Limpiar");
        limpiar.setMaxWidth(Double.MAX_VALUE);
        limpiar.setOnAction(e -> {
            codigo.clear();
            resultado.clear();
        });

        Button volver = new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e -> regresar.run());

        VBox root = new VBox(
                15,
                titulo,
                descripcion,
                new Label("Código a analizar:"),
                codigo,
                analizar,
                limpiar,
                new Label("Resultado:"),
                resultado,
                volver
        );

        root.setPadding(new Insets(30));
        stage.setScene(new Scene(root, 900, 750));
        stage.setTitle("Errores semánticos");
        stage.show();
    }

    private String analizar(String codigo) {
        variables.clear();
        List<String> errores = new ArrayList<>();

        if (codigo == null || codigo.isBlank())
            return "Ingrese código para analizar.";

        Matcher funciones = Pattern.compile(
                "(int|float|String|boolean)\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{([\\s\\S]*?)\\}"
        ).matcher(codigo);

        while (funciones.find()) {
            String esperado = funciones.group(1);
            Matcher r = Pattern.compile("return\\s+(.+?);").matcher(funciones.group(3));

            if (r.find()) {
                String real = tipo(r.group(1).trim(), errores);
                if (real != null && !compatible(esperado, real))
                    errores.add("La función \"" + funciones.group(2) +
                            "\" retorna " + real + " pero debería retornar " + esperado + ".");
            }
        }

        codigo = codigo.replaceAll(
                "(int|float|String|boolean)\\s+\\w+\\s*\\([^)]*\\)\\s*\\{[\\s\\S]*?\\}",
                ""
        );

        for (String linea : codigo.split(";")) {
            linea = linea.trim();
            if (linea.isEmpty()) continue;

            Matcher d = Pattern.compile(
                    "^(int|float|String|boolean)\\s+(\\w+)(?:\\s*=\\s*(.+))?$"
            ).matcher(linea);

            if (d.matches()) {
                String t = d.group(1), nombre = d.group(2), valor = d.group(3);

                if (variables.containsKey(nombre)) {
                    errores.add("La variable \"" + nombre + "\" ya fue declarada.");
                    continue;
                }

                variables.put(nombre, t);

                if (valor != null) {
                    String tv = tipo(valor, errores);
                    if (tv != null && !compatible(t, tv))
                        errores.add("No se puede asignar " + tv +
                                " a la variable \"" + nombre + "\" de tipo " + t + ".");
                }
                continue;
            }

            Matcher a = Pattern.compile("^(\\w+)\\s*=\\s*(.+)$").matcher(linea);

            if (a.matches()) {
                String nombre = a.group(1);

                if (!variables.containsKey(nombre))
                    errores.add("La variable \"" + nombre + "\" no ha sido declarada.");

                String te = tipo(a.group(2), errores);

                if (variables.containsKey(nombre) && te != null &&
                        !compatible(variables.get(nombre), te))
                    errores.add("La variable \"" + nombre + "\" es de tipo " +
                            variables.get(nombre) + " y recibe " + te + ".");
                continue;
            }

            Matcher f = Pattern.compile("^(\\w+)\\s*\\((.*)\\)$").matcher(linea);

            if (f.matches()) {
                String nombre = f.group(1);
                String args = f.group(2).trim();

                if (nombre.equals("suma")) {
                    int cantidad = args.isEmpty() ? 0 : args.split(",").length;
                    if (cantidad != 2)
                        errores.add("La función \"suma\" requiere 2 argumentos, pero recibió " +
                                cantidad + ".");
                } else {
                    errores.add("La función \"" + nombre + "\" no está declarada.");
                }
                continue;
            }

            errores.add("No se pudo analizar: " + linea);
        }

        if (errores.isEmpty())
            return "ANÁLISIS SEMÁNTICO\n\n✓ No se encontraron errores semánticos.";

        StringBuilder salida = new StringBuilder("ANÁLISIS SEMÁNTICO\n\n");

        for (int i = 0; i < errores.size(); i++)
            salida.append(i + 1).append(". ").append(errores.get(i)).append("\n\n");

        return salida.toString();
    }

    private String tipo(String e, List<String> errores) {
        e = e.trim();

        if (e.matches("\".*\"")) return "String";
        if (e.equals("true") || e.equals("false")) return "boolean";
        if (e.matches("-?\\d+")) return "int";
        if (e.matches("-?\\d+\\.\\d+")) return "float";

        if (e.matches("[a-zA-Z_]\\w*")) {
            if (!variables.containsKey(e)) {
                errores.add("La variable \"" + e + "\" no ha sido declarada.");
                return null;
            }
            return variables.get(e);
        }

        if (e.matches(".*[+\\-*/].*")) {
            boolean flotante = false;

            for (String p : e.split("\\s*[+\\-*/]\\s*")) {
                String t = tipo(p, errores);
                if (t == null) return null;

                if (!t.equals("int") && !t.equals("float")) {
                    errores.add("La expresión \"" + e + "\" contiene tipos incompatibles.");
                    return null;
                }

                if (t.equals("float")) flotante = true;
            }

            return flotante ? "float" : "int";
        }

        return null;
    }

    private boolean compatible(String destino, String origen) {
        return destino.equals(origen) ||
                (destino.equals("float") && origen.equals("int"));
    }
}