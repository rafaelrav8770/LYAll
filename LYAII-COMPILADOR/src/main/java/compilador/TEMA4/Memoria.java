package compilador.TEMA4;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class Memoria {
    private final Stage stage;
    private final Runnable regresar;

    public Memoria(Stage stage,Runnable regresar){
        this.stage=stage;
        this.regresar=regresar;
    }

    public void mostrar(){
        Label titulo=new Label("ADMINISTRACION DE MEMORIA");
        titulo.setStyle("-fx-font-size:28px;-fx-font-weight:bold;");

        TextField entrada=new TextField();
        entrada.setStyle("-fx-font-family:monospace;-fx-font-size:15px;");

        TextArea salida=new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-family:monospace;-fx-font-size:18px;");

        Button generar=new Button("Asignar memoria");
        generar.setMaxWidth(Double.MAX_VALUE);

        generar.setOnAction(e->{
            String exp=entrada.getText().replaceAll("\\s+","");

            if(!exp.matches("[a-zA-Z]=[a-zA-Z0-9]+[+\\-*/][a-zA-Z0-9]+")){
                salida.setText("Expresion no valida.\n\nEjemplos:\na = b + c\nx = y - z\nr = a * b\nd = x / y");
                return;
            }

            String dest=exp.substring(0,1);
            String[] v=exp.substring(2).split("[+\\-*/]");
            char op=exp.charAt(2+v[0].length());

            Map<String,Integer> memoria=new LinkedHashMap<>();
            List<String> constantes=new ArrayList<>();
            int dir=0x1000;

            memoria.put(dest,dir);
            dir+=2;
            for(String x:v){
                if(Character.isDigit(x.charAt(0))) constantes.add(x);
                else if(!memoria.containsKey(x)){
                    memoria.put(x,dir);
                    dir+=2;
                }
            }

            StringBuilder tabla=new StringBuilder("VARIABLE   DIRECCION\n--------------------\n");
            memoria.forEach((var,d)->tabla.append(String.format("%-10s %04Xh%n",var,d)));

            salida.setText(
                    "EXPRESION: "+dest+" = "+v[0]+" "+op+" "+v[1]+
                            "\n\nASIGNACION DE MEMORIA\n"+tabla+
                            (constantes.isEmpty()?"":"\nCONSTANTES: "+String.join(", ",constantes)+" (valor inmediato, sin memoria)\n")+
                            "\nTAMANO POR VARIABLE: 2 bytes (16 bits)"+
                            "\nMEMORIA UTILIZADA: "+memoria.size()*2+" bytes"
            );
        });

        Button limpiar=new Button("Limpiar");
        limpiar.setMaxWidth(Double.MAX_VALUE);
        limpiar.setOnAction(e->{
            entrada.clear();
            salida.clear();
        });

        Button volver=new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e->regresar.run());

        entrada.setOnAction(generar.getOnAction());

        VBox root=new VBox(15,titulo,entrada,generar,salida,limpiar,volver);
        VBox.setVgrow(salida,Priority.ALWAYS);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root,850,650));
        stage.setTitle("Administracion de memoria");
        stage.show();
    }
}