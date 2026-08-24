package compilador;

import compilador.interfaz.Menuprincipal;
import javafx.application.Application;
import javafx.stage.Stage;

public class AplicacionFX extends Application {

    @Override
    public void start(Stage stage) {
        Menuprincipal menu = new Menuprincipal(stage);
        menu.mostrar();
    }
}