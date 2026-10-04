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
            salida.setText(new compilador.servicios.ServicioEnsambladorAcademico().generar(entrada.getText()));
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