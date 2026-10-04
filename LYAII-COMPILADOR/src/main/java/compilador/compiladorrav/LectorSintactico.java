package compilador.compiladorrav;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import compilador.compiladorrav.Modelo.*;

public final class LectorSintactico {
    private final List<Token> tokens;
    private int pos;
    public LectorSintactico(List<Token> tokens) { this.tokens = tokens; }
    public Programa leer() {
        esperar("class"); Token nombre = esperar("ID"); esperar("{");
        esperar("public"); esperar("static"); esperar("void"); esperar("main"); esperar("(");
        if (aceptar("String")) { esperar("["); esperar("]"); esperar("ID"); }
        esperar(")"); Bloque cuerpo = bloque(); esperar("}"); esperar("EOF");
        return new Programa(nombre.texto(), cuerpo);
    }
    private Bloque bloque() {
        Token t = esperar("{"); List<Sentencia> lista = new ArrayList<>();
        while (!es("}") && !es("EOF")) lista.add(sentencia());
        esperar("}"); return new Bloque(List.copyOf(lista), t.lugar());
    }
    private Sentencia sentencia() {
        Token t = actual();
        if (es("{")) return bloque();
        if (Set.of("int","double","boolean","String").contains(t.clase())) {
            pos++; Tipo tipo = switch (t.clase()) { case "int" -> Tipo.INT; case "double" -> Tipo.DOUBLE;
                case "boolean" -> Tipo.BOOLEAN; default -> Tipo.STRING; };
            Token nombre = esperar("ID"); Expresion valor = aceptar("=") ? expresion() : null;
            esperar(";"); return new Declaracion(tipo,nombre,valor,t.lugar());
        }
        if (aceptar("print")) {
            esperar("("); Expresion e = expresion(); esperar(")"); esperar(";"); return new Salida(e,t.lugar());
        }
        if (aceptar("if")) {
            esperar("("); Expresion e = expresion(); esperar(")"); Bloque b = bloque();
            Bloque otro = aceptar("else") ? bloque() : null; return new Condicional(e,b,otro,t.lugar());
        }
        if (aceptar("while")) {
            esperar("("); Expresion e = expresion(); esperar(")"); return new Ciclo(e,bloque(),t.lugar());
        }
        if (aceptar("ID")) {
            esperar("="); Expresion e = expresion(); esperar(";"); return new Asignacion(t,e,t.lugar());
        }
        throw fallo(t,"Se esperaba una declaración, asignación, print, if, while o bloque.");
    }
    private Expresion expresion() { return binaria(0); }
    private static final List<Set<String>> NIVELES = List.of(Set.of("||"),Set.of("&&"),Set.of("==","!="),
            Set.of("<","<=",">",">="),Set.of("+","-"),Set.of("*","/","%"));
    private Expresion binaria(int nivel) {
        if (nivel == NIVELES.size()) return unaria();
        Expresion e = binaria(nivel+1);
        while (NIVELES.get(nivel).contains(actual().clase())) {
            Token op = tokens.get(pos++); e = new Binaria(e,op.texto(),binaria(nivel+1),op.lugar());
        }
        return e;
    }
    private Expresion unaria() {
        Token t = actual();
        if (aceptar("!") || aceptar("-") || aceptar("+")) {
            // El mínimo int se escribe como -2147483648; el literal positivo no cabe en int.
            if (t.clase().equals("-") && es("ENTERO") && actual().texto().equals("2147483648")) {
                pos++; return new Literal(Valor.entero(Integer.MIN_VALUE),t.lugar());
            }
            return new Unaria(t.texto(),unaria(),t.lugar());
        }
        if (aceptar("(")) { Expresion e = expresion(); esperar(")"); return e; }
        if (aceptar("ID")) return new Variable(t,t.lugar());
        try {
            if (aceptar("ENTERO")) return new Literal(Valor.entero(Integer.parseInt(t.texto())),t.lugar());
            if (aceptar("DECIMAL")) {
                double d = Double.parseDouble(t.texto());
                if (!Double.isFinite(d)) throw new NumberFormatException();
                return new Literal(Valor.decimal(d),t.lugar());
            }
        } catch (NumberFormatException ex) { throw new Fallo("SEMÁNTICO",t.lugar(),"Literal fuera del rango numérico soportado."); }
        if (aceptar("CADENA")) return new Literal(Valor.cadena(t.texto()),t.lugar());
        if (aceptar("true") || aceptar("false")) return new Literal(Valor.logico(t.texto().equals("true")),t.lugar());
        throw fallo(t,"Se esperaba una expresión.");
    }
    private boolean es(String clase) { return actual().clase().equals(clase); }
    private boolean aceptar(String clase) { if (!es(clase)) return false; pos++; return true; }
    private Token actual() { return tokens.get(Math.min(pos,tokens.size()-1)); }
    private Token esperar(String clase) {
        Token t = actual();
        if (!aceptar(clase)) throw fallo(t,"Se esperaba " + (clase.equals("ID") ? "un identificador" : "'"+clase+"'") + ".");
        return t;
    }
    private Fallo fallo(Token t,String m) { return new Fallo("SINTÁCTICO",t.lugar(),m); }
}
