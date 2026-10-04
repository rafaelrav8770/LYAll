package compilador.compiladorrav;

import java.util.List;


public final class Modelo {
    private Modelo() {}
    public record Lugar(int linea, int columna, int inicio, int fin) {
        public static Lugar interno() { return new Lugar(1, 1, 0, 0); }
    }
    public enum Tipo { INT, DOUBLE, BOOLEAN, STRING;
        public boolean numerico() { return this == INT || this == DOUBLE; }
        public String fuente() { return switch (this) {
            case INT -> "int"; case DOUBLE -> "double";
            case BOOLEAN -> "boolean"; case STRING -> "String";
        }; }
    }
    public record Token(String clase, String texto, Lugar lugar) {}
    public record Programa(String nombre, Bloque cuerpo) {}
    public sealed interface Sentencia permits Bloque, Declaracion, Asignacion, Salida, Condicional, Ciclo {
        Lugar lugar();
    }
    public record Bloque(List<Sentencia> sentencias, Lugar lugar) implements Sentencia {}
    public record Declaracion(Tipo tipo, Token nombre, Expresion valor, Lugar lugar) implements Sentencia {}
    public record Asignacion(Token nombre, Expresion valor, Lugar lugar) implements Sentencia {}
    public record Salida(Expresion valor, Lugar lugar) implements Sentencia {}
    public record Condicional(Expresion condicion, Bloque entonces, Bloque alternativa, Lugar lugar) implements Sentencia {}
    public record Ciclo(Expresion condicion, Bloque cuerpo, Lugar lugar) implements Sentencia {}
    public sealed interface Expresion permits Literal, Variable, Unaria, Binaria { Lugar lugar(); }
    public record Literal(Valor valor, Lugar lugar) implements Expresion {}
    public record Variable(Token nombre, Lugar lugar) implements Expresion {}
    public record Unaria(String operador, Expresion valor, Lugar lugar) implements Expresion {}
    public record Binaria(Expresion izquierda, String operador, Expresion derecha, Lugar lugar) implements Expresion {}

    public static String arbol(Programa programa) {
        StringBuilder s = new StringBuilder("class " + programa.nombre() + "\n");
        dibujar(programa.cuerpo(), s, "  ");
        return s.toString();
    }
    private static void dibujar(Object n, StringBuilder s, String margen) {
        String etiqueta;
        if (n instanceof Bloque) etiqueta = "Bloque";
        else if (n instanceof Declaracion d) etiqueta = d.tipo().fuente() + " " + d.nombre().texto();
        else if (n instanceof Asignacion a) etiqueta = "Asignar " + a.nombre().texto();
        else if (n instanceof Salida) etiqueta = "print";
        else if (n instanceof Condicional) etiqueta = "if";
        else if (n instanceof Ciclo) etiqueta = "while";
        else if (n instanceof Literal l) etiqueta = l.valor().fuente();
        else if (n instanceof Variable v) etiqueta = v.nombre().texto();
        else if (n instanceof Unaria u) etiqueta = u.operador();
        else etiqueta = ((Binaria)n).operador();
        s.append(margen).append(etiqueta).append('\n');
        String m = margen + "  ";
        if (n instanceof Bloque b) b.sentencias().forEach(x -> dibujar(x, s, m));
        else if (n instanceof Declaracion d && d.valor() != null) dibujar(d.valor(), s, m);
        else if (n instanceof Asignacion a) dibujar(a.valor(), s, m);
        else if (n instanceof Salida p) dibujar(p.valor(), s, m);
        else if (n instanceof Condicional c) {
            dibujar(c.condicion(), s, m); dibujar(c.entonces(), s, m);
            if (c.alternativa() != null) dibujar(c.alternativa(), s, m);
        } else if (n instanceof Ciclo c) { dibujar(c.condicion(), s, m); dibujar(c.cuerpo(), s, m); }
        else if (n instanceof Unaria u) dibujar(u.valor(), s, m);
        else if (n instanceof Binaria b) { dibujar(b.izquierda(), s, m); dibujar(b.derecha(), s, m); }
    }
}
