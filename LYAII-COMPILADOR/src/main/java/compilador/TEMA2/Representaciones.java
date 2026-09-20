package compilador.TEMA2;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.*;

public class Representaciones {
    private final Stage stage;
    private final Runnable regresar;
    private int temporal=1;

    public Representaciones(Stage stage,Runnable regresar){
        this.stage=stage;
        this.regresar=regresar;
    }
    public void mostrar(){
        Label titulo=new Label("REPRESENTACIONES DE CÓDIGO INTERMEDIO");
        titulo.setStyle("-fx-font-size:28px;-fx-font-weight:bold;");

        TextField entrada=new TextField();
        entrada.setPromptText("Ejemplo: a * b + c / d");
        entrada.setStyle("-fx-font-size:18px;");

        TextArea salida=new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-family:monospace;-fx-font-size:20px;");
        salida.setPrefHeight(450);

        Button generar=new Button("Generar representaciones");
        generar.setMaxWidth(Double.MAX_VALUE);

        generar.setOnAction(e->{
            String expresion=entrada.getText().trim();
            if(expresion.isBlank()){
                salida.setText("Ingresa una expresión.");
                return;
            }

            temporal=1;
            List<String> postfija=convertirPostfija(expresion);
            String codigoP=generarCodigoP(postfija);
            String[] intermedio=generarIntermedio(postfija);

            salida.setText(
                    "EXPRESIÓN:\n"+expresion+
                            "\nNOTACIÓN POLACA / POSTFIJA:\n"+String.join(" ",postfija)+
                            "\nCÓDIGO P:\n"+codigoP+
                            "\nTRIPLOS:\n"+intermedio[0]+
                            "\nCUÁDRUPLOS:\n"+intermedio[1]
            );
        });

        Button volver=new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e->regresar.run());

        VBox root=new VBox(15,titulo,entrada,generar,salida,volver);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root,850,650));
        stage.setTitle("Representaciones de código intermedio");
        stage.show();
    }

    private int prioridad(String op){
        return switch(op){
            case "+","-"->1;
            case "*","/"->2;
            default->0;
        };
    }

    private List<String> convertirPostfija(String expresion){
        List<String> salida=new ArrayList<>();
        Stack<String> pila=new Stack<>();
        String[] tokens=expresion.replace("("," ( ").replace(")"," ) ")
                .replace("+"," + ").replace("-"," - ")
                .replace("*"," * ").replace("/"," / ")
                .trim().split("\\s+");

        for(String token:tokens){
            if(token.matches("[a-zA-Z0-9.]+")) salida.add(token);
            else if(token.equals("(")) pila.push(token);
            else if(token.equals(")")){
                while(!pila.isEmpty()&&!pila.peek().equals("(")) salida.add(pila.pop());
                if(!pila.isEmpty()) pila.pop();
            }else{
                while(!pila.isEmpty()&&!pila.peek().equals("(")&&prioridad(pila.peek())>=prioridad(token))
                    salida.add(pila.pop());
                pila.push(token);
            }
        }
        while(!pila.isEmpty()) salida.add(pila.pop());
        return salida;
    }
    private String generarCodigoP(List<String> postfija){
        StringBuilder codigo=new StringBuilder();

        for(String token:postfija){
            if(token.matches("[a-zA-Z0-9.]+")) codigo.append("LOAD ").append(token).append("\n");
            else codigo.append(switch(token){
                case "+"->"ADD\n";
                case "-"->"SUB\n";
                case "*"->"MUL\n";
                case "/"->"DIV\n";
                default->"";
            });
        }
        return codigo.toString();
    }
    private String[] generarIntermedio(List<String> postfija){
        Stack<String> pila=new Stack<>();
        StringBuilder triplos=new StringBuilder();
        StringBuilder cuadruplos=new StringBuilder();
        int indice=0;

        for(String token:postfija){
            if(token.matches("[a-zA-Z0-9.]+")) pila.push(token);
            else{
                String der=pila.pop(),izq=pila.pop(),temp="T"+temporal++;

                triplos.append(indice).append(" | ").append(token).append(" | ")
                        .append(izq).append(" | ").append(der).append("\n");

                cuadruplos.append(indice).append(" | ").append(token).append(" | ")
                        .append(izq).append(" | ").append(der).append(" | ")
                        .append(temp).append("\n");

                pila.push(temp);
                indice++;
            }
        }
        return new String[]{triplos.toString(),cuadruplos.toString()};
    }
}