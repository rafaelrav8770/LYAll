package compilador.TEMA3;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptimizacionMirilla {
    private final Stage stage;
    private final Runnable regresar;
    private static final Pattern MOV = Pattern.compile("^(?i)MOV\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*,\\s*([A-Za-z_][A-Za-z0-9_]*)$");
    private static final Pattern MOV_ZERO = Pattern.compile("^(?i)MOV\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*,\\s*0$");
    private static final Pattern GOTO = Pattern.compile("^(?i)(goto|jmp)\\s+\\S+$");
    private static final Pattern LABEL = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*:$");
    private static final Pattern NOOP_ARITH = Pattern.compile("^(?i)(ADD|SUB)\\s+[A-Za-z_][A-Za-z0-9_]*\\s*,\\s*0$|^(?i)(MUL|DIV)\\s+[A-Za-z_][A-Za-z0-9_]*\\s*,\\s*1$");
    public OptimizacionMirilla(Stage stage,Runnable regresar){
        this.stage=stage;
        this.regresar=regresar;
    }
    public void mostrar(){
        Label titulo=new Label("OPTIMIZACIÓN DE MIRILLA");
        titulo.setStyle("-fx-font-size:28px;-fx-font-weight:bold;");
        TextArea entrada=new TextArea();
        entrada.setStyle("-fx-font-family:monospace;-fx-font-size:15px;");
        TextArea salida=new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-family:monospace;-fx-font-size:18px;");
        Button optimizar=new Button("Optimizar mirilla");
        optimizar.setMaxWidth(Double.MAX_VALUE);

        optimizar.setOnAction(e->{
            String codigo=entrada.getText();

            if(codigo.isBlank()){
                salida.setText("Ingresa instrucciones.");
                return;
            }
            String resultado=optimizarMirilla(codigo);
            salida.setText("CÓDIGO OPTIMIZADO:\n"+resultado);
        });
        Button volver=new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e->regresar.run());
        VBox root=new VBox(15,titulo,entrada,optimizar,salida,volver);
        root.setPadding(new Insets(30));
        stage.setScene(new Scene(root,850,650));
        stage.setTitle("Optimización de mirilla");
        stage.show();
    }
    private String optimizarMirilla(String codigo) {
        return new compilador.servicios.ServicioOptimizacionMirilla().optimizarMirilla(codigo);
    }
}
