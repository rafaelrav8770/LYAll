package compilador.TEMA3;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.regex.*;

public class OptimizacionCiclos {
    private final Stage stage;
    private final Runnable regresar;

    public OptimizacionCiclos(Stage stage, Runnable regresar){
        this.stage = stage;
        this.regresar = regresar;
    }

    public void mostrar(){
        Label titulo = new Label("OPTIMIZACIÓN DE CICLOS");
        titulo.setStyle("-fx-font-size:28px;-fx-font-weight:bold;");

        TextArea entrada = new TextArea();
        entrada.setStyle("-fx-font-family:monospace;-fx-font-size:18px;");

        TextArea salida = new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-family:monospace;-fx-font-size:18px;");

        Button optimizar = new Button("Optimizar ciclo");
        optimizar.setMaxWidth(Double.MAX_VALUE);

        optimizar.setOnAction(e -> {
            if (entrada.getText().isBlank()) {
                salida.setText("Ingresa un ciclo.");
                return;
            }
            salida.setText(optimizarCiclo(entrada.getText()));
        });

        Button volver = new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e -> regresar.run());

        VBox root = new VBox(15, titulo, entrada, optimizar, salida, volver);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root, 850, 650));
        stage.setTitle("Optimización de ciclos");
        stage.show();
    }

    private boolean esNumero(String s) {
        return s.matches("-?\\d+(\\.\\d+)?");
    }

    private String foldConstante(String rhs) {
        String[] tokens = rhs.trim().split("\\s+");
        if (tokens.length == 3 && esNumero(tokens[0]) && esNumero(tokens[2])) {
            double a = Double.parseDouble(tokens[0]);
            double b = Double.parseDouble(tokens[2]);
            double r;
            switch (tokens[1]) {
                case "+": r = a + b; break;
                case "-": r = a - b; break;
                case "*": r = a * b; break;
                case "/": r = (b != 0) ? a / b : Double.NaN; break;
                default: return rhs;
            }
            if (r == Math.floor(r) && !Double.isInfinite(r)) return String.valueOf((long) r);
            return String.valueOf(r);
        }
        return rhs;
    }

    private Set<String> identificadores(String texto) {
        Set<String> ids = new LinkedHashSet<>();
        Matcher m = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*").matcher(texto);
        while (m.find()) ids.add(m.group());
        return ids;
    }

    private String optimizarCiclo(String codigo) {
        Pattern loopPattern = Pattern.compile("for\\s*\\(([^;]*);([^;]*);([^)]*)\\)\\s*\\{([\\s\\S]*)\\}", Pattern.DOTALL);
        Matcher m = loopPattern.matcher(codigo);
        if (!m.find()) {
            return "No se detectó un ciclo for válido.";
        }

        String init = m.group(1).trim();
        String cond = m.group(2).trim();
        String incr = m.group(3).trim();
        String cuerpo = m.group(4);

        Matcher initVar = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*=").matcher(init);
        String loopVar = initVar.find() ? initVar.group(1) : "i";

        List<String[]> statements = new ArrayList<>();
        for (String parte : cuerpo.split(";")) {
            String s = parte.trim();
            if (s.isEmpty()) continue;
            String[] partes = s.split("=", 2);
            if (partes.length != 2) continue;
            statements.add(new String[]{ partes[0].trim(), partes[1].trim() });
        }

        Set<String> variant = new HashSet<>();
        variant.add(loopVar);

        for (String[] st : statements) {
            if (identificadores(st[1]).contains(st[0])) {
                variant.add(st[0]);
            }
        }

        boolean cambiado = true;
        while (cambiado) {
            cambiado = false;
            for (String[] st : statements) {
                String lhs = st[0], rhs = st[1];
                if (variant.contains(lhs)) continue;
                for (String id : identificadores(rhs)) {
                    if (variant.contains(id)) {
                        variant.add(lhs);
                        cambiado = true;
                        break;
                    }
                }
            }
        }

        List<String[]> invariantes = new ArrayList<>();
        List<String[]> variantes = new ArrayList<>();
        for (String[] st : statements) {
            if (variant.contains(st[0])) variantes.add(st);
            else invariantes.add(st);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CÓDIGO OPTIMIZADO:\n");
        for (String[] st : invariantes) {
            sb.append(st[0]).append(" = ").append(foldConstante(st[1])).append(";\n");
        }
        if (!invariantes.isEmpty()) sb.append("\n");

        sb.append("for(").append(init).append(";").append(cond).append(";").append(incr).append("){\n");
        for (String[] st : variantes) {
            sb.append("    ").append(st[0]).append(" = ").append(st[1]).append(";\n");
        }
        sb.append("}\n");

        if (!invariantes.isEmpty()) {
            sb.append("Se movieron fuera del ciclo las expresiones invariantes\n" +
                    " (no dependen de la variable de control ni de datos\n" +
                    " que cambian dentro del ciclo).\n");
        } else {
            sb.append("No se encontraron expresiones invariantes para mover fuera del ciclo.\n");
        }

        return sb.toString();
    }
}