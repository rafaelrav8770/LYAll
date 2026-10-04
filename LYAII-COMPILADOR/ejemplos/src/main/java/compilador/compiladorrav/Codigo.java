package compilador.compiladorrav;

import java.util.List;
import compilador.compiladorrav.Modelo.*;


public final class Codigo {
    private Codigo() {}
    public record Operando(String ranura, Valor constante, Tipo tipo) {
        public static Operando constante(Valor v) { return new Operando(null,v,v.tipo()); }
        public static Operando ranura(String nombre,Tipo t) { return new Operando(nombre,null,t); }
        public String texto() { return constante == null ? ranura : constante.fuente(); }
    }
    public record Instruccion(String op, Operando destino, Operando a, Operando b, String etiqueta, Lugar lugar) {
        public String texto() {
            return switch (op) {
                case "LABEL" -> etiqueta+":";
                case "JMP" -> "goto "+etiqueta;
                case "JZ" -> "ifFalse "+a.texto()+" goto "+etiqueta;
                case "PRINT" -> "print "+a.texto();
                case "MOV" -> destino.texto()+" = "+a.texto();
                case "NEG", "NOT", "POS" -> destino.texto()+" = "+op+" "+a.texto();
                default -> destino.texto()+" = "+a.texto()+" "+op+" "+b.texto();
            };
        }
    }
    public static String texto(List<Instruccion> lista) {
        StringBuilder s = new StringBuilder();
        for (int i=0;i<lista.size();i++) s.append(String.format("%04d  %s%n",i,lista.get(i).texto()));
        return s.toString();
    }
}
