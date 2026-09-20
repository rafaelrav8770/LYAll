package compilador.TEMA3;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AnalisisCostos {
    private final Stage stage;
    private final Runnable regresar;

    public AnalisisCostos(Stage stage,Runnable regresar){
        this.stage=stage;
        this.regresar=regresar;
    }

    public void mostrar(){
        Label titulo=new Label("ANÁLISIS DE COSTOS");
        titulo.setStyle("-fx-font-size:28px;-fx-font-weight:bold;");

        TextArea entrada=new TextArea();
        entrada.setStyle("-fx-font-family:monospace;-fx-font-size:15px;");

        TextArea salida=new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-family:monospace;-fx-font-size:18px;");

        Button analizar=new Button("Analizar costos");
        analizar.setMaxWidth(Double.MAX_VALUE);

        analizar.setOnAction(e->{
            String codigo=entrada.getText().trim();

            if(codigo.isBlank()){
                salida.setText("Ingresa código intermedio.");
                return;
            }

            int instrucciones=codigo.split("\\R").length;
            int operaciones=contar(codigo,"+")+contar(codigo,"-")
                    +contar(codigo,"*")+contar(codigo,"/");
            int temporales=contarTemporales(codigo);

            salida.setText(
                    "INSTRUCCIONES: "+instrucciones+
                            "\nOPERACIONES: "+operaciones+
                            "\nTEMPORALES: "+temporales+
                            "\nMEMORIA ESTIMADA: "+(instrucciones*4)+" unidades"+
                            "\nREGISTROS ESTIMADOS: "+Math.max(1,temporales)+
                            "\nUSO DE PILA: Básico"
            );
        });

        Button volver=new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e->regresar.run());

        VBox root=new VBox(15,titulo,entrada,analizar,salida,volver);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root,850,650));
        stage.setTitle("Análisis de costos");
        stage.show();
    }

    private int contar(String texto,String buscar){
        int cantidad=0,pos=0;
        while((pos=texto.indexOf(buscar,pos))!=-1){
            cantidad++;
            pos++;
        }
        return cantidad;
    }

    private int contarTemporales(String texto){
        return (int)java.util.regex.Pattern.compile("(?i)\\bt\\d+\\b")
                .matcher(texto).results()
                .map(m->m.group().toLowerCase())
                .distinct()
                .count();
    }
}