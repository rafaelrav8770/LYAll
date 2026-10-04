package compilador.TEMA3;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import java.util.*;

public class OptimizacionGlobal {
    private final Stage stage;
    private final Runnable regresar;

    public OptimizacionGlobal(Stage stage,Runnable regresar){
        this.stage=stage;
        this.regresar=regresar;
    }

    public void mostrar(){
        Label titulo=new Label("OPTIMIZACIÓN GLOBAL");
        titulo.setStyle("-fx-font-size:28px;-fx-font-weight:bold;");
        TextArea entrada=new TextArea();
        entrada.setStyle("-fx-font-family:monospace;-fx-font-size:15px;");
        entrada.setPrefRowCount(8);
        TextArea salida=new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-family:monospace;-fx-font-size:15px;");
        salida.setPrefRowCount(15);
        VBox.setVgrow(salida, Priority.ALWAYS);
        Button optimizar=new Button("Optimizar globalmente");
        optimizar.setMaxWidth(Double.MAX_VALUE);
        optimizar.setOnAction(e->{
            if(entrada.getText().isBlank()){ salida.setText("Ingresa código."); return; }
            salida.setText("CÓDIGO OPTIMIZADO:\n"+optimizarGlobalmente(entrada.getText())+
                    "Se aplicó eliminación de subexpresiones comunes\n " +
                    "entre bloques y propagación de copias.");
        });
        Button volver=new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e->regresar.run());
        VBox root=new VBox(15,titulo,entrada,optimizar,salida,volver);
        root.setPadding(new Insets(30));
        stage.setScene(new Scene(root,850,650));
        stage.setTitle("Optimización global");
        stage.show();
    }
    private boolean esNumero(String s){ return s.matches("-?\\d+(\\.\\d+)?"); }
    private boolean esId(String s){ return s.matches("[a-zA-Z_][a-zA-Z0-9_]*"); }

    private String optimizarGlobalmente(String codigo){
        Map<String,String> valor=new HashMap<>();
        Map<String,String> exprVar=new LinkedHashMap<>();
        StringBuilder out=new StringBuilder();
        for(String lineaOriginal: codigo.split("\\R")){
            String linea=lineaOriginal.trim();
            if(linea.isEmpty()) continue;
            String[] partes=linea.split("=",2);
            String lhs=null, rhs=null;
            if(partes.length==2 && esId(partes[0].trim())){ lhs=partes[0].trim(); rhs=partes[1].trim(); }
            if(lhs==null){ out.append(lineaOriginal).append("\n"); continue; }
            String[] tk=rhs.split("\\s+");
            String res;
            if(tk.length==1){
                res=resolver(tk[0], valor);
                valor.put(lhs,res);
            } else if(tk.length==3){
                String a=resolver(tk[0],valor), op=tk[1], b=resolver(tk[2],valor);
                if(esNumero(a) && esNumero(b)){
                    res=plegar(Double.parseDouble(a), op, Double.parseDouble(b));
                    valor.put(lhs,res);
                } else {
                    String s=simplificar(a,op,b);
                    if(s!=null){ res=s; valor.put(lhs,res); }
                    else {
                        String clave=clave(op,a,b);
                        if(exprVar.containsKey(clave)){ res=exprVar.get(clave); valor.put(lhs,res); }
                        else { res=a+" "+op+" "+b; exprVar.put(clave,lhs); valor.remove(lhs); }
                    }
                }
            } else { out.append(lineaOriginal).append("\n"); continue; }
            out.append(lhs).append(" = ").append(res).append("\n");
        }
        return out.toString();
    }
    private String resolver(String t, Map<String,String> valor){ return esNumero(t) ? t : valor.getOrDefault(t,t); }
    private String simplificar(String a, String op, String b){
        switch(op){
            case "+": if(b.equals("0")) return a; if(a.equals("0")) return b; break;
            case "-": if(b.equals("0")) return a; break;
            case "*": if(b.equals("1")) return a; if(a.equals("1")) return b; if(a.equals("0")||b.equals("0")) return "0"; break;
            case "/": if(b.equals("1")) return a; break;
        }
        return null;
    }
    private String clave(String op, String a, String b){
        if((op.equals("+")||op.equals("*")) && a.compareTo(b)>0){ String t=a; a=b; b=t; }
        return op+":"+a+","+b;
    }

    private String plegar(double a, String op, double b){
        double r=switch(op){
            case "+" -> a+b;
            case "-" -> a-b;
            case "*" -> a*b;
            case "/" -> (b!=0)? a/b : Double.NaN;
            default -> Double.NaN;
        };
        return (r==Math.floor(r) && !Double.isInfinite(r)) ? String.valueOf((long)r) : String.valueOf(r);
    }
}