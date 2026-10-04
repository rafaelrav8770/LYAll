package compilador;

import compilador.interfaz.MesaTrabajo;
import javafx.application.Application;
import javafx.stage.Stage;

public class AplicacionFX extends Application {

    @Override
    public void start(Stage stage) {
        new MesaTrabajo(stage).mostrar();
    }
}
