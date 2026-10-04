package compilador.compiladorrav;

import compilador.interfaz.*;
import compilador.TEMA1.*;
import compilador.TEMA2.*;
import compilador.TEMA3.*;
import compilador.TEMA4.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;

/** Opcional: RAV_GUI_TESTS=1 en un equipo con pantalla (o Xvfb). */
@EnabledIfEnvironmentVariable(named="RAV_GUI_TESTS",matches="1")
class InterfazTest {
    static <T> T fx(Callable<T> accion) throws Exception {
        FutureTask<T> tarea=new FutureTask<>(accion);Platform.runLater(tarea);return tarea.get(15,TimeUnit.SECONDS);
    }
    static void esperar(Stage stage) throws Exception {
        long limite=System.nanoTime()+TimeUnit.SECONDS.toNanos(15);
        while(fx(()->((Button)stage.getScene().lookup("#compile")).isDisabled())) {
            if(System.nanoTime()>limite)fail("La interfaz no terminó el trabajo");Thread.sleep(20);
        }
    }
    static void foto(Stage stage,String nombre) throws Exception {
        fx(()->{
            stage.getScene().getRoot().applyCss();stage.getScene().getRoot().layout();
            WritableImage imagen=stage.getScene().snapshot(null);PixelReader pixeles=imagen.getPixelReader();
            int w=(int)imagen.getWidth(),h=(int)imagen.getHeight();Path ruta=Path.of("target/ui",nombre+".ppm");Files.createDirectories(ruta.getParent());
            try(OutputStream out=Files.newOutputStream(ruta)) {
                out.write(("P6\n"+w+" "+h+"\n255\n").getBytes(java.nio.charset.StandardCharsets.US_ASCII));
                for(int y=0;y<h;y++)for(int x=0;x<w;x++){int rgb=pixeles.getArgb(x,y);out.write(rgb>>16&255);out.write(rgb>>8&255);out.write(rgb&255);}
            }return null;
        });
    }
    @Test void editorPipelineEjecucionDiagnosticosYPracticas() throws Exception {
        java.util.Queue<Throwable> errores=new java.util.concurrent.ConcurrentLinkedQueue<>();
        CountDownLatch listo=new CountDownLatch(1);Platform.startup(()->{Thread.currentThread().setUncaughtExceptionHandler((t,e)->errores.add(e));Platform.setImplicitExit(false);listo.countDown();});assertTrue(listo.await(10,TimeUnit.SECONDS));
        Stage stage=fx(()->{Stage s=new Stage();new MesaTrabajo(s).mostrar();return s;});
        try {
            assertTrue(fx(()->((Button)stage.getScene().lookup("#execute")).isDisabled()));
            fx(()->{((Button)stage.getScene().lookup("#compile")).fire();return null;});esperar(stage);
            assertTrue(fx(()->((TextArea)stage.getScene().lookup("#build-output")).getText()).contains("COMPILACIÓN CORRECTA"));
            assertFalse(fx(()->((Button)stage.getScene().lookup("#execute")).isDisabled()));
            fx(()->{
                MenuBar menu=(MenuBar)stage.getScene().lookup(".menu-bar");
                Menu ver=menu.getMenus().stream().filter(m->m.getText().equals("Ver")).findFirst().orElseThrow();
                assertEquals(7,ver.getItems().size());ver.getItems().get(5).fire();
                assertTrue(stage.getScene().getRoot().lookupAll(".text-area").stream().anyMatch(n->((TextArea)n).getText().contains(".CODIGO")));
                ver.getItems().get(0).fire();return null;
            });
            foto(stage,"compilador");
            fx(()->{((Button)stage.getScene().lookup("#execute")).fire();return null;});esperar(stage);
            String resultado=fx(()->((TextArea)stage.getScene().lookup("#run-output")).getText());
            assertTrue(resultado.contains("Entradas confirmadas\n4\n300.0"),resultado);
            foto(stage,"ejecucion");
            // Recorrer los dos ejemplos adicionales mediante el menú visible.
            for(int n=1;n<=2;n++) {
                int indice=n;
                fx(()->{MenuBar barra=(MenuBar)stage.getScene().lookup(".menu-bar");
                    Menu herramientas=barra.getMenus().stream().filter(m->m.getText().equals("Herramientas")).findFirst().orElseThrow();
                    Menu ejemplos=(Menu)herramientas.getItems().get(2);ejemplos.getItems().get(indice).fire();
                    ((Button)stage.getScene().lookup("#compile")).fire();return null;});esperar(stage);
                assertTrue(fx(()->((TextArea)stage.getScene().lookup("#build-output")).getText()).contains("COMPILACIÓN CORRECTA"));
                fx(()->{((Button)stage.getScene().lookup("#execute")).fire();return null;});esperar(stage);
                String texto=fx(()->((TextArea)stage.getScene().lookup("#run-output")).getText());
                assertTrue(texto.contains(n==1?"Resultado correcto\n15":"3\n3.5\ntrue\nfalse"),texto);
            }
            for(String cuerpo:List.of("int x = @;","print(2;","print(x);")) {
                fx(()->{((TextArea)stage.getScene().lookup("#editor")).setText(PipelineTest.programa(cuerpo));assertTrue(((Button)stage.getScene().lookup("#execute")).isDisabled());((Button)stage.getScene().lookup("#compile")).fire();return null;});esperar(stage);
                assertTrue(fx(()->((TextArea)stage.getScene().lookup("#build-output")).getText()).contains("ERROR"));
                assertTrue(fx(()->((Button)stage.getScene().lookup("#execute")).isDisabled()));
            }
            foto(stage,"diagnostico");
            // Abrir todas las pantallas originales desde sus fuentes, no desde .class del ZIP.
            fx(()->{
                Stage a=new Stage();a.initOwner(stage);Runnable volver=()->{};
                List<Runnable> ventanas=List.of(
                    ()->new ArbolExpresiones(a,volver).mostrar(),()->new AccionesSemanticas(a,volver).mostrar(),
                    ()->new ComprobacionTipos(a,volver).mostrar(),()->new PilaSemantica(a,volver).mostrar(),
                    ()->new EsquemaTraduccion(a,volver).mostrar(),()->new TablaSimbolos(a,volver).mostrar(),
                    ()->new TablaDirecciones(a,volver).mostrar(),()->new ErroresSemanticos(a,volver).mostrar(),
                    ()->new Notaciones(a,volver).mostrar(),()->new Representaciones(a,volver).mostrar(),
                    ()->new GeneracionIntermedia(a,volver).mostrar(),()->new OptimizacionLocal(a,volver).mostrar(),
                    ()->new OptimizacionCiclos(a,volver).mostrar(),()->new OptimizacionGlobal(a,volver).mostrar(),
                    ()->new OptimizacionMirilla(a,volver).mostrar(),()->new AnalisisCostos(a,volver).mostrar(),
                    ()->new CriteriosMejora(a,volver).mostrar(),()->new AnalisisFlujoDatos(a,volver).mostrar(),
                    ()->new registros(a,volver).mostrar(),()->new Ensamblador(a,volver).mostrar(),
                    ()->new Maquina(a,volver).mostrar(),()->new Memoria(a,volver).mostrar());
                ventanas.forEach(r->{r.run();a.getScene().getRoot().applyCss();a.getScene().getRoot().layout();assertTrue(a.isShowing());});
                for(int tema=1;tema<=4;tema++)new Menutema(a,tema,new Menuprincipal(a)).mostrar();
                a.hide();return null;
            });
            assertTrue(errores.isEmpty(),errores.toString());
        } finally {fx(()->{stage.hide();Platform.exit();return null;});}
    }
}
