package compilador.TEMA3;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalisisFlujoDatos {
    private final Stage stage;
    private final Runnable regresar;

    public AnalisisFlujoDatos(Stage stage,Runnable regresar){
        this.stage=stage;
        this.regresar=regresar;
    }

    public void mostrar(){
        Label titulo=new Label("ANÁLISIS DEL FLUJO DE DATOS");
        titulo.setStyle("-fx-font-size:28px;-fx-font-weight:bold;");

        TextArea entrada=new TextArea();
        entrada.setStyle("-fx-font-family:monospace;-fx-font-size:15px;");

        TextArea salida=new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-family:monospace;-fx-font-size:15px;");

        Button analizar=new Button("Analizar flujo");
        analizar.setMaxWidth(Double.MAX_VALUE);

        analizar.setOnAction(e->{
            String codigo=entrada.getText().trim();

            if(codigo.isBlank()){
                salida.setText("Ingresa código.");
                return;
            }
            salida.setText(analizarFlujo(codigo));
        });

        Button volver=new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e->regresar.run());

        VBox root=new VBox(15,titulo,entrada,analizar,salida,volver);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root,850,650));
        stage.setTitle("Análisis del flujo de datos");
        stage.show();
    }

    private boolean esId(String s){ return s.matches("[A-Za-z_][A-Za-z0-9_]*"); }

    private Set<String> identificadores(String texto){
        Set<String> ids = new LinkedHashSet<>();
        Matcher m = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*").matcher(texto);
        while(m.find()) ids.add(m.group());
        return ids;
    }

    private String analizarFlujo(String codigo){
        List<String[]> lineas = new ArrayList<>();
        for(String l : codigo.split("\\R")){
            String t = l.trim();
            if(t.isEmpty()) continue;
            String[] partes = t.split("=", 2);
            if(partes.length != 2 || !esId(partes[0].trim())){
                continue;
            }
            lineas.add(new String[]{ partes[0].trim(), partes[1].trim(), t });
        }

        if(lineas.isEmpty()){
            return "No se detectaron asignaciones válidas para analizar.";
        }

        int n = lineas.size();
        StringBuilder cadenas = new StringBuilder();
        List<String> muertas = new ArrayList<>();
        List<String> noUsadas = new ArrayList<>();

        for(int i=0;i<n;i++){
            String lhs = lineas.get(i)[0];
            String rhs = lineas.get(i)[1];
            Set<String> usados = identificadores(rhs);

            List<String> partesCadena = new ArrayList<>();
            for(String v : usados){
                int origen = -1;
                for(int j=i-1;j>=0;j--){
                    if(lineas.get(j)[0].equals(v)){ origen = j; break; }
                }
                if(origen != -1){
                    partesCadena.add("'" + v + "' llega de línea " + (origen+1));
                } else {
                    partesCadena.add("'" + v + "' es una entrada externa (no definida antes)");
                }
            }
            if(!partesCadena.isEmpty()){
                cadenas.append("Línea ").append(i+1).append(" (").append(lineas.get(i)[2]).append("): ")
                        .append(String.join(", ", partesCadena)).append("\n");
            }

            boolean usada = false, redefinida = false;
            int lineaRedef = -1;
            for(int j=i+1;j<n;j++){
                Set<String> usosJ = identificadores(lineas.get(j)[1]);
                if(usosJ.contains(lhs)){ usada = true; break; }
                if(lineas.get(j)[0].equals(lhs)){ redefinida = true; lineaRedef = j; break; }
            }
            if(!usada && redefinida){
                muertas.add("Línea " + (i+1) + ": " + lineas.get(i)[2] +
                        "   (se sobrescribe en línea " + (lineaRedef+1) + " sin haberse usado)");
            } else if(!usada && !redefinida && i != n-1){
                noUsadas.add("Línea " + (i+1) + ": " + lineas.get(i)[2] + "   (nunca se vuelve a usar)");
            }
        }

        StringBuilder out = new StringBuilder("ANALISIS DE FLUJO DE DATOS:\n");
        out.append("CADENAS DEF-USO :\n");
        out.append(cadenas.length()>0 ? cadenas : "(no hay usos de variables definidas previamente)\n");

        out.append("\nDEFINICIONES MUERTAS (se sobrescriben antes de usarse):\n");
        out.append(muertas.isEmpty() ? "(ninguna)\n" : String.join("\n", muertas) + "\n");

        out.append("\nVARIABLES NUNCA UTILIZADAS:\n");
        out.append(noUsadas.isEmpty() ? "(ninguna)\n" : String.join("\n", noUsadas) + "\n");

        return out.toString();
    }
}