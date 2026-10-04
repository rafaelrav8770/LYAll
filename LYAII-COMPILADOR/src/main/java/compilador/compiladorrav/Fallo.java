package compilador.compiladorrav;

import compilador.compiladorrav.Modelo.Lugar;


public final class Fallo extends RuntimeException {
    private final String fase;
    private final Lugar lugar;
    public Fallo(String fase, Lugar lugar, String mensaje) {
        super(mensaje); this.fase = fase; this.lugar = lugar;
    }
    public String fase() { return fase; }
    public Lugar lugar() { return lugar; }
    public String diagnostico() {
        return "ERROR " + fase + "\nLínea " + lugar.linea() + ", columna " + lugar.columna() + "\n" + getMessage();
    }
}
