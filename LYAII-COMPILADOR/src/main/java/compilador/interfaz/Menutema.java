package compilador.interfaz;

import compilador.TEMA1.ArbolExpresiones;
import compilador.TEMA1.AccionesSemanticas;
import compilador.TEMA1.ComprobacionTipos;
import compilador.TEMA1.PilaSemantica;
import compilador.TEMA1.EsquemaTraduccion;
import compilador.TEMA1.TablaSimbolos;
import compilador.TEMA1.TablaDirecciones;
import compilador.TEMA1.ErroresSemanticos;

import compilador.TEMA2.Notaciones;
import compilador.TEMA2.Representaciones;
import compilador.TEMA2.GeneracionIntermedia;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Menutema {

    private final Stage stage;
    private final int tema;
    private final Menuprincipal principal;
    private final VBox opciones = new VBox(10);

    public Menutema(Stage stage, int tema, Menuprincipal principal) {
        this.stage = stage;
        this.tema = tema;
        this.principal = principal;
    }

    public void mostrar() {
        Label titulo = new Label(titulotema(tema));
        titulo.getStyleClass().add("titulo-tema");

        opciones.getChildren().clear();
        opciones.setFillWidth(true);
        cargarOpciones();

        ScrollPane scroll = new ScrollPane(opciones);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("scroll-tema");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Button regresar = new Button("← Regresar al menú principal");
        regresar.setMaxWidth(Double.MAX_VALUE);
        regresar.getStyleClass().add("boton-regresar");
        regresar.setOnAction(e -> principal.mostrar());

        VBox root = new VBox(22, titulo, scroll, regresar);
        root.setPadding(new Insets(30, 45, 30, 45));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("root-panel");

        Scene scene = new Scene(root, 780, 620);
        aplicarCss(scene);

        stage.setTitle(titulotema(tema));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    private void cargarOpciones() {
        switch (tema) {
            case 1 -> {
                agregar("1. Árboles de expresiones", "arbol");
                agregar("2. Acciones semánticas", "acciones");
                agregar("3. Comprobación de tipos", "tipos");
                agregar("4. Pila semántica", "pila");
                agregar("5. Esquema de traducción", "traduccion");
                agregar("6. Tabla de símbolos", "simbolos");
                agregar("7. Tabla de direcciones", "direcciones");
                agregar("8. Manejo de errores semánticos", "errores");
            }
            case 2 -> {
                agregar("1. Notaciones: prefija, infija y postfija", "notaciones");
                agregar("2. Representaciones: Polaca, Código P, Triplos y Cuádruplos", "representaciones");
                agregar("3. Generación de código intermedio", "generacion");
            }

            case 3 -> {
                agregar("1. Optimización local", "local");
                agregar("2. Optimización de ciclos", "ciclos");
                agregar("3. Optimización global", "global");
                agregar("4. Optimización de mirilla", "mirilla");
                agregar("5. Análisis de costos", "costos");
                agregar("6. Criterios para mejorar el código", "criterios");
                agregar("7. Análisis del flujo de datos", "flujo");
            }
            case 4 -> {
                agregar("1. Registros", "registros");
                agregar("2. Lenguaje ensamblador", "ensamblador");
                agregar("3. Lenguaje máquina", "maquina");
                agregar("4. Administración de memoria", "memoria");
            }
        }
    }

    private void agregar(String texto, String modulo) {
        Button boton = new Button(texto);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setAlignment(Pos.CENTER_LEFT);
        boton.getStyleClass().add("boton-opcion");
        boton.setOnAction(e -> abrirModulo(modulo));
        opciones.getChildren().add(boton);
    }

    private void abrirModulo(String modulo) {
        if (tema == 1) {
            switch (modulo) {
                case "arbol" -> new ArbolExpresiones(stage, this::mostrar).mostrar();
                case "acciones" -> new AccionesSemanticas(stage, this::mostrar).mostrar();
                case "tipos" -> new ComprobacionTipos(stage, this::mostrar).mostrar();
                case "pila" -> new PilaSemantica(stage, this::mostrar).mostrar();
                case "traduccion" -> new EsquemaTraduccion(stage, this::mostrar).mostrar();
                case "simbolos" -> new TablaSimbolos(stage, this::mostrar).mostrar();
                case "direcciones" -> new TablaDirecciones(stage, this::mostrar).mostrar();
                case "errores" -> new ErroresSemanticos(stage, this::mostrar).mostrar();
            }
            return;
        }
        if (tema == 2) {
            switch (modulo) {
                case "notaciones" -> new Notaciones(stage, this::mostrar).mostrar();
                case "representaciones" -> new Representaciones(stage, this::mostrar).mostrar();
                case "generacion" -> new GeneracionIntermedia(stage, this::mostrar).mostrar();
            }
            return;
        }
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Módulo");
        alerta.setHeaderText(null);
        alerta.setContentText("Este modulo falta  .");
        alerta.initOwner(stage);
        alerta.showAndWait();
    }

    private String titulotema(int tema) {
        return switch (tema) {
            case 1 -> "TEMA 1 - Análisis semántico";
            case 2 -> "TEMA 2 - Generación de código intermedio";
            case 3 -> "TEMA 3 - Optimización";
            case 4 -> "TEMA 4 - Generación de código objeto";
            default -> "TEMA";
        };
    }

    private void aplicarCss(Scene scene) {
        var recurso = getClass().getResource("/styles.css");
        if (recurso != null) scene.getStylesheets().add(recurso.toExternalForm());
    }
}