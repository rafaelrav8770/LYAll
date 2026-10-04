package compilador.interfaz;

import compilador.compiladorrav.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.text.Font;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;


public final class MesaTrabajo {
    private static final List<String> NOMBRES_VISTAS=List.of("Tokens","Árbol","Símbolos","Intermedio","Optimización","Ensamblador","Objeto");
    private final String fuenteCodigo=List.of("Consolas","DejaVu Sans Mono","Liberation Mono","Courier New").stream()
            .filter(Font.getFamilies()::contains).findFirst().orElse("Monospaced");
    private final Stage stage;
    private final TextArea editor=new TextArea(), numeros=new TextArea(), consola=new TextArea(), ejecucion=new TextArea();
    private final Map<String,TextArea> vistas=new LinkedHashMap<>();
    private final TabPane inspeccion=new TabPane(), salida=new TabPane();
    private final Label estado=new Label("Listo para compilar"), cursor=new Label(), archivo=new Label();
    private final Button compilar=new Button("Compilar"), ejecutar=new Button("Ejecutar");
    private final MenuItem menuCompilar=new MenuItem("Compilar"), menuEjecutar=new MenuItem("Ejecutar objeto"), menuExportar=new MenuItem("Exportar objeto RAVM…");
    private final ExecutorService trabajador=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"RAV-pipeline");t.setDaemon(true);return t;});
    private Compilacion.Resultado ultimo;
    private Path ruta;
    private String guardado="";
    private boolean ocupado;
    private final List<Stage> academicas=new ArrayList<>();

    public MesaTrabajo(Stage stage) { this.stage=stage; }
    public void mostrar() {
        editor.setId("editor"); consola.setId("build-output"); ejecucion.setId("run-output");
        compilar.setId("compile"); ejecutar.setId("execute"); estado.setId("status");
        editor.getStyleClass().add("editor"); editor.setWrapText(false); tipografiaCodigo(editor);
        numeros.getStyleClass().add("lineas"); numeros.setEditable(false); numeros.setFocusTraversable(false);
        tipografiaCodigo(numeros);
        numeros.setPrefWidth(64); numeros.setMinWidth(64); numeros.setMaxWidth(64);
        numeros.setMouseTransparent(true);
        editor.scrollTopProperty().addListener((o,a,b)->numeros.setScrollTop(b.doubleValue()));
        editor.textProperty().addListener((o,a,b)->fuenteCambiada());
        editor.caretPositionProperty().addListener((o,a,b)->actualizarCursor());
        compilar.getStyleClass().add("primario"); compilar.setOnAction(e->compilar()); ejecutar.setOnAction(e->ejecutar());
        compilar.setTooltip(new Tooltip("Analizar el programa completo y generar el objeto RAVM"));
        ejecutar.setTooltip(new Tooltip("Ejecutar los bytes del último programa compilado"));

        Label sello=new Label("RAV"); sello.getStyleClass().add("sello");Label curso=new Label("LENGUAJES Y AUTÓMATAS II"); curso.getStyleClass().add("curso");
        Label titulo=new Label("Compilador"); titulo.getStyleClass().add("titulo-app");
        VBox identidad=new VBox(2,curso,titulo); Region espacio=new Region(); HBox.setHgrow(espacio,Priority.ALWAYS);
        Label lenguaje=new Label("Java simplificado"); lenguaje.getStyleClass().add("etiqueta");
        HBox encabezado=new HBox(18,sello,identidad,espacio,lenguaje); encabezado.setAlignment(Pos.CENTER_LEFT); encabezado.getStyleClass().add("encabezado");

        Button abrir=new Button("Abrir"); abrir.setOnAction(e->abrir());
        Button guardar=new Button("Guardar"); guardar.setOnAction(e->guardar(false));
        archivo.getStyleClass().add("nombre-archivo"); Region flexible=new Region(); HBox.setHgrow(flexible,Priority.ALWAYS);
        HBox herramientas=new HBox(10,abrir,guardar,new Separator(Orientation.VERTICAL),compilar,ejecutar,flexible,archivo);
        herramientas.setAlignment(Pos.CENTER_LEFT); herramientas.getStyleClass().add("herramientas");
        VBox superior=new VBox(menu(),encabezado,herramientas);

        Label labelEditor=new Label("PROGRAMA FUENTE"); labelEditor.getStyleClass().add("rotulo");
        Label ayuda=new Label("Edita el programa y pulsa Compilar. El diagnóstico aparece abajo."); ayuda.getStyleClass().add("ayuda");
        HBox textos=new HBox(0,numeros,editor); HBox.setHgrow(editor,Priority.ALWAYS); VBox.setVgrow(textos,Priority.ALWAYS);
        VBox panelEditor=new VBox(10,labelEditor,ayuda,textos); panelEditor.getStyleClass().add("panel-editor");
        for(String nombre:NOMBRES_VISTAS) {
            TextArea area=areaSalida(); vistas.put(nombre,area); inspeccion.getTabs().add(tab(nombre,area));
        }
        inspeccion.getStyleClass().add("inspeccion"); inspeccion.setMinWidth(290);
        SplitPane trabajo=new SplitPane(panelEditor,inspeccion); trabajo.setDividerPositions(.55);
        prepararSalida(consola); prepararSalida(ejecucion);
        salida.getTabs().addAll(tab("Compilación",consola),tab("Ejecución",ejecucion)); salida.setMinHeight(220);
        SplitPane centro=new SplitPane(trabajo,salida); centro.setOrientation(Orientation.VERTICAL); centro.setDividerPositions(.58);

        Region pieEspacio=new Region(); HBox.setHgrow(pieEspacio,Priority.ALWAYS);
        HBox pie=new HBox(15,estado,pieEspacio,cursor); pie.getStyleClass().add("pie"); pie.setAlignment(Pos.CENTER_LEFT);
        BorderPane root=new BorderPane(centro,superior,null,pie,null);
        Scene scene=new Scene(root,1280,750);
        var css=getClass().getResource("/rav.css"); if(css!=null) scene.getStylesheets().add(css.toExternalForm());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F6),this::compilar);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F7),this::ejecutar);
        stage.setScene(scene); stage.setTitle("RAV · Compilador · Lenguajes y Autómatas II");
        stage.setMinWidth(920); stage.setMinHeight(640); stage.setResizable(true);
        stage.setOnCloseRequest(e->{if(!confirmarCambios()) e.consume(); else cerrar();});
        cargarEjemplo("inicio"); consola.setText("Salida de compilación\n\nEl editor contiene un programa de ejemplo.\nPulsa Compilar para analizarlo.\n");
        stage.show(); actualizarAcciones(); Platform.runLater(editor::requestFocus);
    }
    private MenuBar menu() {
        Menu archivoMenu=new Menu("Archivo");
        MenuItem nuevo=item("Nuevo programa",()->{if(confirmarCambios()){ruta=null;editor.setText(plantilla());guardado=editor.getText();actualizarArchivo();}});
        nuevo.setAccelerator(new KeyCodeCombination(KeyCode.N,KeyCombination.SHORTCUT_DOWN));
        MenuItem abrir=item("Abrir…",this::abrir); abrir.setAccelerator(new KeyCodeCombination(KeyCode.O,KeyCombination.SHORTCUT_DOWN));
        MenuItem guardar=item("Guardar",()->guardar(false)); guardar.setAccelerator(new KeyCodeCombination(KeyCode.S,KeyCombination.SHORTCUT_DOWN));
        archivoMenu.getItems().addAll(nuevo,abrir,guardar,item("Guardar como…",()->guardar(true)),new SeparatorMenuItem(),item("Salir",()->stage.fireEvent(new WindowEvent(stage,WindowEvent.WINDOW_CLOSE_REQUEST))));
        Menu construir=new Menu("Compilar"); menuCompilar.setOnAction(e->compilar()); menuEjecutar.setOnAction(e->ejecutar());
        construir.getItems().addAll(menuCompilar,menuEjecutar);
        Menu ver=new Menu("Ver");
        NOMBRES_VISTAS.forEach(n->ver.getItems().add(item(n,()->seleccionar(inspeccion,n))));
        Menu herramientas=new Menu("Herramientas"); menuExportar.setOnAction(e->exportar(true));
        herramientas.getItems().addAll(menuExportar,item("Exportar ensamblador…",()->exportar(false)));
        Menu ejemplos=new Menu("Ejemplos");
        ejemplos.getItems().addAll(item("Cálculo de entradas",()->reemplazarEjemplo("inicio")),item("Ciclo y condición",()->reemplazarEjemplo("ciclo")),item("Tipos y cortocircuito",()->reemplazarEjemplo("tipos")));
        herramientas.getItems().add(ejemplos);
        Menu temas=new Menu("Temas del curso");
        for(int n=1;n<=4;n++){int tema=n;temas.getItems().add(item("TEMA "+n+" · "+List.of("Análisis semántico","Código intermedio","Optimización","Código objeto").get(n-1),()->academico(tema)));}
        return new MenuBar(archivoMenu,construir,ver,herramientas,temas);
    }
    private MenuItem item(String texto,Runnable accion) { MenuItem i=new MenuItem(texto);i.setOnAction(e->accion.run());return i; }
    private Tab tab(String nombre,javafx.scene.Node contenido) { Tab t=new Tab(nombre,contenido);t.setClosable(false);return t; }
    private TextArea areaSalida() { TextArea t=new TextArea(); prepararSalida(t);return t; }
    private void tipografiaCodigo(TextArea t) { t.setStyle("-fx-font-family: '"+fuenteCodigo+"';"); }
    private void prepararSalida(TextArea t) { t.setEditable(false);t.setWrapText(false);t.getStyleClass().add("salida-codigo");tipografiaCodigo(t); }
    private void seleccionar(TabPane panel,String nombre) { panel.getTabs().stream().filter(t->t.getText().equals(nombre)).findFirst().ifPresent(t->panel.getSelectionModel().select(t)); }
    private void fuenteCambiada() {
        if(ultimo!=null) consola.setText("Fuente modificada.\nCompila la versión actual para obtener un nuevo diagnóstico.\n");
        ultimo=null; vistas.values().forEach(a->a.setText("Compila el programa para ver esta etapa."));
        ejecucion.clear(); if(!ocupado) estado.setText("Fuente modificada · requiere compilar");
        int total=editor.getText().split("\n",-1).length; StringBuilder b=new StringBuilder();
        for(int n=1;n<=total;n++) b.append(n).append('\n'); numeros.setText(b.toString());
        Platform.runLater(()->numeros.setScrollTop(editor.getScrollTop()));
        actualizarArchivo(); actualizarCursor(); actualizarAcciones();
    }
    private void actualizarCursor() {
        String antes=editor.getText().substring(0,Math.min(editor.getCaretPosition(),editor.getLength()));
        int linea=1; for(int i=0;i<antes.length();i++) if(antes.charAt(i)=='\n') linea++;
        cursor.setText("Línea "+linea+" · Columna "+(antes.length()-antes.lastIndexOf('\n')));
    }
    private void actualizarArchivo() { archivo.setText((ruta==null?"sin-título.rav":ruta.getFileName().toString())+(editor.getText().equals(guardado)?"":"  •")); }
    private void actualizarAcciones() {
        boolean valido=ultimo!=null && ultimo.correcto() && ultimo.fuente().equals(editor.getText());
        compilar.setDisable(ocupado); ejecutar.setDisable(ocupado || !valido);
        menuCompilar.setDisable(ocupado); menuEjecutar.setDisable(ocupado || !valido); menuExportar.setDisable(ocupado || !valido);
    }
    private void compilar() {
        if(ocupado)return;
        ultimo=null; ejecucion.clear(); vistas.values().forEach(TextArea::clear);
        String fuente=editor.getText(); ocupado=true; actualizarAcciones(); estado.setText("Compilando…");
        seleccionar(salida,"Compilación"); consola.setText("Compilación iniciada…\n");
        Task<Compilacion.Resultado> tarea=new Task<>() { protected Compilacion.Resultado call() {return new Compilacion().compilar(fuente);} };
        tarea.setOnSucceeded(e->{
            ocupado=false;
            if(!editor.getText().equals(fuente)) {estado.setText("La fuente cambió · vuelve a compilar");consola.setText("La fuente cambió durante la compilación. Compila la versión actual.");actualizarAcciones();return;}
            ultimo=tarea.getValue(); consola.setText(ultimo.consola()); consola.positionCaret(consola.getLength());mostrarEtapas();
            estado.setText(ultimo.correcto()?"Compilación correcta · objeto RAVM listo":"Compilación detenida · "+ultimo.fallo().fase().toLowerCase(Locale.ROOT));
            actualizarAcciones();
            if(ultimo.fallo()!=null) ubicar(ultimo.fallo());
        });
        tarea.setOnFailed(e->{ocupado=false;ultimo=null;consola.setText("FALLO INTERNO\n"+tarea.getException()+"\nNo se generó un objeto ejecutable.");estado.setText("Fallo interno");actualizarAcciones();});
        trabajador.submit(tarea);
    }
    private void mostrarEtapas() {
        StringBuilder t=new StringBuilder(String.format("%-14s %-26s %s%n","TOKEN","LEXEMA","LÍNEA:COL"));
        for(var token:ultimo.tokens()) t.append(String.format("%-14s %-26s %d:%d%n",token.clase(),token.texto().replace("\n","\\n").replace("\t","\\t"),token.lugar().linea(),token.lugar().columna()));
        vistas.get("Tokens").setText(t.toString());
        if(ultimo.programa()!=null) vistas.get("Árbol").setText(Modelo.arbol(ultimo.programa()));
        if(ultimo.revision()!=null) {
            StringBuilder s=new StringBuilder("RANURA              TIPO       ÁMBITO   LÍNEA:COL\n");
            for(var sim:ultimo.revision().simbolos()) s.append(String.format("%-20s %-10s %-8d %d:%d%n",sim.ranura(),sim.tipo().fuente(),sim.ambito(),sim.lugar().linea(),sim.lugar().columna()));
            vistas.get("Símbolos").setText(s.toString());
        }
        if(ultimo.correcto()) {
            vistas.get("Intermedio").setText(Codigo.texto(ultimo.intermedio()));
            vistas.get("Optimización").setText("CAMBIOS APLICADOS\n"+(ultimo.mejora().cambios().isEmpty()?"Sin cambios necesarios.":String.join("\n",ultimo.mejora().cambios()))+"\n\nCÓDIGO OPTIMIZADO\n"+Codigo.texto(ultimo.mejora().codigo()));
            vistas.get("Ensamblador").setText(ultimo.objeto().ensamblador()); vistas.get("Objeto").setText(ultimo.objeto().hexadecimal());
        }
    }
    private void ejecutar() {
        if(ocupado || ultimo==null || !ultimo.correcto() || !ultimo.fuente().equals(editor.getText()))return;
        byte[] bytes=ultimo.objeto().bytes(); String fuente=editor.getText(); ocupado=true;actualizarAcciones();estado.setText("Ejecutando RAVM…");
        seleccionar(salida,"Ejecución"); ejecucion.setText("Ejecución iniciada…\n");
        Task<Ejecucion.Resultado> tarea=new Task<>() {protected Ejecucion.Resultado call(){return new Ejecucion().ejecutar(bytes);}};
        tarea.setOnSucceeded(e->{ocupado=false;var r=tarea.getValue();
            if(!editor.getText().equals(fuente)){ejecucion.setText("La fuente cambió durante la ejecución. Vuelve a compilar.");estado.setText("Fuente modificada");}
            else {ejecucion.setText(r.salida()+"\n"+(r.correcto()?"EJECUCIÓN FINALIZADA · "+r.instrucciones()+" instrucciones":r.fallo().diagnostico()));ejecucion.positionCaret(ejecucion.getLength());estado.setText(r.correcto()?"Ejecución finalizada":"Error en ejecución");if(!r.correcto())ubicar(r.fallo());}
            actualizarAcciones();});
        tarea.setOnFailed(e->{ocupado=false;ejecucion.setText("Fallo interno al ejecutar: "+tarea.getException());estado.setText("Fallo interno");actualizarAcciones();});
        trabajador.submit(tarea);
    }
    private void ubicar(Fallo fallo) { int i=Math.min(fallo.lugar().inicio(),editor.getLength()),f=Math.min(Math.max(i+1,fallo.lugar().fin()),editor.getLength());editor.requestFocus();editor.selectRange(i,f); }
    private FileChooser selector(String titulo,String descripcion,String extension) { FileChooser c=new FileChooser();c.setTitle(titulo);c.getExtensionFilters().add(new FileChooser.ExtensionFilter(descripcion,extension));return c; }
    private void abrir() {
        if(!confirmarCambios())return;
        FileChooser c=selector("Abrir programa","Fuente RAV-J (*.rav, *.java)","*.rav");c.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java / Texto","*.java","*.txt"));
        File f=c.showOpenDialog(stage); if(f==null)return;
        try {if(Files.size(f.toPath())>1_000_000)throw new IOException("El archivo supera 1 MB.");String texto=Files.readString(f.toPath(),StandardCharsets.UTF_8).replace("\r\n","\n").replace('\r','\n');ruta=f.toPath();guardado=texto;editor.setText(texto);actualizarArchivo();}
        catch(IOException ex){error("No se pudo abrir el archivo",ex.getMessage());}
    }
    private boolean guardar(boolean como) {
        Path destino=ruta;
        if(como || destino==null){FileChooser c=selector("Guardar programa","Fuente RAV-J","*.rav");c.setInitialFileName(destino==null?"programa.rav":destino.getFileName().toString());File f=c.showSaveDialog(stage);if(f==null)return false;destino=f.toPath();}
        try{Files.writeString(destino,editor.getText(),StandardCharsets.UTF_8);ruta=destino;guardado=editor.getText();actualizarArchivo();return true;}
        catch(IOException ex){error("No se pudo guardar",ex.getMessage());return false;}
    }
    private boolean confirmarCambios() {
        if(editor.getText().equals(guardado))return true;
        ButtonType guardar=new ButtonType("Guardar"),descartar=new ButtonType("Descartar"),cancelar=new ButtonType("Cancelar",ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert a=new Alert(Alert.AlertType.CONFIRMATION,"El programa tiene cambios sin guardar.",guardar,descartar,cancelar);a.initOwner(stage);a.setHeaderText("Cambios en el programa");
        ButtonType respuesta=a.showAndWait().orElse(cancelar);return respuesta==descartar || respuesta==guardar && guardar(false);
    }
    private void exportar(boolean binario) {
        if(ocupado || ultimo==null || !ultimo.correcto() || !ultimo.fuente().equals(editor.getText())){error("Compila el programa","Es necesario generar un objeto válido antes de exportar.");return;}
        String ext=binario?"ravm":"asm";FileChooser c=selector("Exportar "+ext,"Archivo *."+ext,"*."+ext);c.setInitialFileName(ultimo.programa().nombre()+"."+ext);File f=c.showSaveDialog(stage);if(f==null)return;
        try{if(binario)Files.write(f.toPath(),ultimo.objeto().bytes());else Files.writeString(f.toPath(),ultimo.objeto().ensamblador(),StandardCharsets.UTF_8);estado.setText("Exportado: "+f.getName());}
        catch(IOException ex){error("No se pudo exportar",ex.getMessage());}
    }
    private void reemplazarEjemplo(String nombre) {if(confirmarCambios())cargarEjemplo(nombre);}
    private void cargarEjemplo(String nombre) {
        try(InputStream in=getClass().getResourceAsStream("/ejemplos/"+nombre)){
            if(in==null)throw new IOException("Ejemplo no encontrado");ruta=null;guardado=new String(in.readAllBytes(),StandardCharsets.UTF_8);editor.setText(guardado);actualizarArchivo();
        }catch(IOException ex){error("No se pudo cargar el ejemplo",ex.getMessage());}
    }
    private String plantilla(){return "class Programa {\n    public static void main() {\n        print(\"Hola desde RAV\");\n    }\n}\n";}
    private void academico(int tema) {
        Stage ventana=new Stage();ventana.initOwner(stage);academicas.add(ventana);ventana.setOnHidden(e->academicas.remove(ventana));
        new Menutema(ventana,tema,new Menuprincipal(ventana)).mostrar();
    }
    private void ayuda() {
        TextArea texto=areaSalida(); texto.setWrapText(true);texto.setText("RAV-J · Subconjunto de Java para Rafael\n\nclass Nombre { public static void main() { ... } }\nTambién acepta main(String[] args), sin usar args.\n\nTipos: int, double, boolean, String.\nDeclaración: int entradas = 4;\nSalida: print(entradas);\nControl: if (condición) { ... } else { ... }\n         while (condición) { ... }\nOperadores: + - * / % < <= > >= == != && || !\nComentarios: // y /* ... */\n\nLa asignación int → double es implícita. Las cadenas se concatenan con otras cadenas. Todas las variables deben declararse e inicializarse antes de usarlas.\n\nCompilar: F6. Ejecutar: F7. Archivo: Ctrl/Cmd+O y Ctrl/Cmd+S.\nLas etapas se consultan en las pestañas de la derecha.\nEl destino es RAVM, una máquina virtual propia, no x86.\n\nLimitaciones: sin funciones adicionales, clases múltiples, objetos, arrays, for, break, continue, entrada por teclado ni bibliotecas Java. Consulta docs/LENGUAJE.md para la gramática completa.");
        Stage ventana=new Stage();ventana.initOwner(stage);ventana.setTitle("RAV-J · Guía del lenguaje");Scene scene=new Scene(texto,680,580);scene.getStylesheets().add(getClass().getResource("/rav.css").toExternalForm());ventana.setScene(scene);ventana.show();
    }
    private void error(String titulo,String mensaje){Alert a=new Alert(Alert.AlertType.ERROR);a.initOwner(stage);a.setHeaderText(titulo);a.setContentText(mensaje);a.showAndWait();}
    private void cerrar(){trabajador.shutdownNow();new ArrayList<>(academicas).forEach(Stage::close);stage.hide();Platform.exit();}
}
