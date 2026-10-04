package compilador.TEMA1;

import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TablaSimbolos {
    private final Stage stage;
    private final Runnable regresar;

    public TablaSimbolos(Stage stage,Runnable regresar){
        this.stage=stage;
        this.regresar=regresar;
    }

    public void mostrar(){
        Label titulo=new Label("TABLA DE SÍMBOLOS");
        titulo.setStyle("-fx-font-size:30px;-fx-font-weight:bold;");

        TextField nombre=new TextField();
        nombre.setPromptText("Nombre de variable");

        ComboBox<String> tipo=new ComboBox<>();
        tipo.getItems().addAll("int","float","String","boolean");
        tipo.setValue("int");

        ObservableList<String> datos=FXCollections.observableArrayList();
        ListView<String> tabla=new ListView<>(datos);

        Button agregar=new Button("Agregar símbolo");
        agregar.setMaxWidth(Double.MAX_VALUE);

        agregar.setOnAction(e->{
            String texto=nombre.getText().trim();

            if(texto.isBlank()){
                error("El nombre de variable no puede estar vacío.");
                return;
            }

            if(!texto.matches("^[A-Za-z_][A-Za-z0-9_]*$")){
                error("Nombre inválido: \""+texto+"\".\nDebe empezar con una letra o '_' y solo contener letras, números o '_'.");
                return;
            }

            boolean existe=datos.stream()
                    .anyMatch(d->d.split("\\s*\\|\\s*")[0].trim().equalsIgnoreCase(texto));

            if(existe){
                error("La variable \""+texto+"\" ya existe en la tabla de símbolos.");
                return;
            }

            datos.add(texto+"      |      "+tipo.getValue());
            nombre.clear();
            nombre.requestFocus();
        });

        Button volver=new Button("← Regresar");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e->regresar.run());

        VBox root=new VBox(15,titulo,nombre,tipo,agregar,tabla,volver);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root,800,620));
        stage.setTitle("Tabla de símbolos");
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