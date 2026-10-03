package compilador.TEMA4;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class Ensamblador {
    private final Stage stage;
    private final Runnable regresar;

    public Ensamblador(Stage stage,Runnable regresar){
        this.stage=stage;
        this.regresar=regresar;
    }

    public void mostrar(){
        Label titulo=new Label("LENGUAJE ENSAMBLADOR");
        titulo.setStyle("-fx-font-size:28px;-fx-font-weight:bold;");

        TextField entrada=new TextField();
        entrada.setStyle("-fx-font-family:monospace;-fx-font-size:15px;");

        TextArea salida=new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-family:monospace;-fx-font-size:18px;");

        Button generar=new Button("Generar ensamblador");
        generar.setMaxWidth(Double.MAX_VALUE);

        generar.setOnAction(e->{
            String exp=entrada.getText().replaceAll("\\s+","");
            String opnd="([a-zA-Z][a-zA-Z0-9]*|\\d{1,5})";

            if(!exp.matches("[a-zA-Z]="+opnd+"[+\\-*/]"+opnd)){
                salida.setText("Expresion no valida.\n\nEjemplos:\na = b + c\nx = y - z\nr = a * b\nd = x / 5");
                return;
            }

            String dest=exp.substring(0,1);
            String[] v=exp.substring(2).split("[+\\-*/]");
            char op=exp.charAt(2+v[0].length());

            Set<String> vars=new LinkedHashSet<>();
            vars.add(dest);
            for(String x:v) if(!Character.isDigit(x.charAt(0))) vars.add(x);

            StringBuilder datos=new StringBuilder();
            for(String x:vars) datos.append("    ").append(String.format("%-6s DW ?%n",x));

            List<String> ins=new ArrayList<>();
            ins.add("MOV AX, "+v[0]);
            switch(op){
                case '+'->ins.add("ADD AX, "+v[1]);
                case '-'->ins.add("SUB AX, "+v[1]);
                case '*'->{
                    ins.add("MOV BX, "+v[1]);
                    ins.add("MUL BX");
                }
                default->{
                    ins.add("MOV BX, "+v[1]);
                    ins.add("XOR DX, DX");
                    ins.add("DIV BX");
                }
            }
            ins.add("MOV "+dest+", AX");

            StringBuilder cod=new StringBuilder();
            for(String i:ins) cod.append("    ").append(i).append("\n");

            salida.setText(
                    "EXPRESION: "+dest+" = "+v[0]+" "+op+" "+v[1]+
                            "\nPROGRAMA GENERADO\n"+
                            ".MODEL SMALL\n.STACK 100h\n.DATA\n"+datos+
                            ".CODE\nMAIN PROC\n    MOV AX, @DATA\n    MOV DS, AX\n"+cod+
                            "\n    MOV AH, 4Ch\n    INT 21h\nMAIN ENDP\nEND MAIN"+
                            "\nINSTRUCCIONES DE LA EXPRESION: "+ins.size()
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
        stage.setTitle("Lenguaje ensamblador");
        stage.show();
    }
}