package compilador.TEMA1;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.*;

public class TablaDirecciones {
    private final Stage stage;
    private final Runnable regresar;
    private int direccion=1000;
    private final Set<String> variables=new HashSet<>();

    public TablaDirecciones(Stage stage,Runnable regresar){
        this.stage=stage;
        this.regresar=regresar;
    }

    public void mostrar(){
        Label titulo=new Label("TABLA DE DIRECCIONES");
        titulo.setStyle("-fx-font-size:30px;-fx-font-weight:bold;");

        TextField variable=new TextField();
        variable.setPromptText("Nombre de variable");

        TextArea salida=new TextArea("VARIABLE       DIRECCIÓN\n");
        salida.setEditable(false);

        Button registrar=new Button("Asignar dirección");
        registrar.setMaxWidth(Double.MAX_VALUE);

        registrar.setOnAction(e->{
            String texto=variable.getText().trim();

            if(texto.isBlank()){
                error("El nombre de variable no puede estar vacío.");
                return;
            }

            if(!texto.matches("^[A-Za-z_][A-Za-z0-9_]*$")){
                error("Nombre inválido: \""+texto+"\".\nDebe empezar con una letra o '_' y solo contener letras, números o '_'.");
                return;
            }

            String nombre=texto.toUpperCase();

            if(variables.contains(nombre)){
                error("La variable \""+texto+"\" ya tiene una dirección asignada.");
                return;
            }

            salida.appendText(texto+"             "+direccion+"\n");
            variables.add(nombre);
            direccion+=4;
            variable.clear();
            variable.requestFocus();
        });

        Button volver=new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e->regresar.run());

        VBox root=new VBox(15,titulo,variable,registrar,salida,volver);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root,800,620));
        stage.setTitle("Tabla de direcciones");
        stage.show();
    }

    private void error(String mensaje){
        Alert alerta=new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error de validación");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}