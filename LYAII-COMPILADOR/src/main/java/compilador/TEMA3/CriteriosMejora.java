package compilador.TEMA3;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.regex.Pattern;

public class CriteriosMejora {
    private final Stage stage;
    private final Runnable regresar;

    public CriteriosMejora(Stage stage,Runnable regresar){
        this.stage=stage;
        this.regresar=regresar;
    }
    public void mostrar(){
        Label titulo=new Label("CRITERIOS PARA MEJORAR EL CÓDIGO");
        titulo.setStyle("-fx-font-size:27px;-fx-font-weight:bold;");
        TextArea entrada=new TextArea();
        entrada.setStyle("-fx-font-family:monospace;-fx-font-size:15px;");
        TextArea salida=new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-size:15px;");
        Button analizar=new Button("Analizar criterios");
        analizar.setMaxWidth(Double.MAX_VALUE);
        analizar.setOnAction(e->{
            if(entrada.getText().isBlank()){
                salida.setText("Ingresa código.");
                return;
            }
            salida.setText(analizarCriterios(entrada.getText()));
        });
        Button volver=new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e->regresar.run());
        VBox root=new VBox(15,titulo,entrada,analizar,salida,volver);
        root.setPadding(new Insets(30));
        stage.setScene(new Scene(root,850,650));
        stage.setTitle("Criterios para mejorar el código");
        stage.show();
    }
    private boolean esNumero(String s){ return s.matches("-?\\d+(\\.\\d+)?"); }
    private boolean esId(String s){ return s.matches("[A-Za-z_][A-Za-z0-9_]*"); }
    private String analizarCriterios(String codigo){
        List<String> lineas = new ArrayList<>();
        for(String l : codigo.split("\\R")){
            String t = l.trim();
            if(!t.isEmpty()) lineas.add(t);
        }
        List<String> hallazgos = new ArrayList<>();
        List<String> identidades = new ArrayList<>();
        Map<String,List<String>> expresiones = new LinkedHashMap<>();
        int copias = 0;
        List<String> constantesSinPlegar = new ArrayList<>();
        int nombresGenericos = 0;
        int totalAsignaciones = 0;

        for(String linea : lineas){
            String[] partes = linea.split("=", 2);
            if(partes.length != 2) continue;
            String lhs = partes[0].trim();
            if(!esId(lhs)) continue;
            String rhs = partes[1].trim();
            String[] tk = rhs.split("\\s+");
            totalAsignaciones++;

            if(lhs.matches("(?i)t(emp)?\\d*") || lhs.matches("[a-z]")){
                nombresGenericos++;
            }
            if(tk.length == 1){
                copias++;
            } else if(tk.length == 3){
                String a = tk[0], op = tk[1], b = tk[2];
                boolean identidad =
                        (op.equals("+") && (a.equals("0") || b.equals("0"))) ||
                                (op.equals("-") && b.equals("0")) ||
                                (op.equals("*") && (a.equals("1") || b.equals("1") || a.equals("0") || b.equals("0"))) ||
                                (op.equals("/") && b.equals("1"));
                if(identidad) identidades.add(linea);

                if(esNumero(a) && esNumero(b)) constantesSinPlegar.add(linea);

                String clave;
                if(op.equals("+") || op.equals("*")){
                    String x=a,y=b;
                    if(x.compareTo(y)>0){String t=x;x=y;y=t;}
                    clave = op+":"+x+","+y;
                } else {
                    clave = op+":"+a+","+b;
                }
                expresiones.computeIfAbsent(clave, k->new ArrayList<>()).add(linea);
            }
        }
        int distintosTemporales = (int) Pattern.compile("(?i)\\bt\\d+\\b")
                .matcher(codigo).results().map(m->m.group().toLowerCase()).distinct().count();
        if(!identidades.isEmpty()){
            hallazgos.add("Operaciones redundantes (identidades algebraicas) en:\n   " + String.join("\n   ", identidades));
        }
        long repetidas = expresiones.values().stream().filter(v -> v.size() > 1).count();
        if(repetidas > 0){
            StringBuilder sb = new StringBuilder("Cálculos repetidos (subexpresión común) en:\n");
            for(List<String> v : expresiones.values()){
                if(v.size() > 1) sb.append("   ").append(String.join(" / ", v)).append("\n");
            }
            hallazgos.add(sb.toString().trim());
        }

        if(copias >= 2){
            hallazgos.add("Cadena de copias que podría reducirse (" + copias + " asignaciones simples detectadas).");
        }

        if(!constantesSinPlegar.isEmpty()){
            hallazgos.add("Constantes que pudieron evaluarse en tiempo de compilación:\n   " + String.join("\n   ", constantesSinPlegar));
        }

        if(totalAsignaciones > 0 && nombresGenericos * 2 >= totalAsignaciones){
            hallazgos.add("Nombres de variables poco descriptivos (usa demasiadas variables genéricas tipo letra suelta o tN).");
        }

        if(distintosTemporales > 3){
            hallazgos.add("Exceso de variables temporales (" + distintosTemporales + " detectadas); considera consolidar cálculos.");
        }

        if(hallazgos.isEmpty()){
            return "No se detectaron problemas relevantes con estos criterios. El código luce razonable.";
        }

        StringBuilder out = new StringBuilder("CRITERIOS DE MEJORA DETECTADOS:\n");
        for(int i=0;i<hallazgos.size();i++){
            out.append((i+1)).append(". ").append(hallazgos.get(i)).append("\n");
        }
        return out.toString().trim();
    }
}