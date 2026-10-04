package compilador.compiladorrav;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import compilador.compiladorrav.Modelo.*;


public final class LectorLexico {
    private static final Set<String> RESERVADAS = Set.of("class", "public", "static", "void", "main",
            "int", "double", "boolean", "String", "if", "else", "while", "print", "true", "false");
    private final String fuente;
    private int pos, linea = 1, columna = 1;
    public LectorLexico(String fuente) { this.fuente = fuente.replace("\r\n", "\n").replace('\r', '\n'); }
    public List<Token> leer() {
        List<Token> tokens = new ArrayList<>();
        while (pos < fuente.length()) {
            char c = ver(0);
            if (Character.isWhitespace(c)) { avanzar(); continue; }
            int inicio = pos, l = linea, col = columna;
            if (c == '/' && ver(1) == '/') {
                while (pos < fuente.length() && ver(0) != '\n') avanzar();
                continue;
            }
            if (c == '/' && ver(1) == '*') {
                avanzar(); avanzar();
                while (pos < fuente.length() && !(ver(0) == '*' && ver(1) == '/')) avanzar();
                if (pos == fuente.length()) throw fallo(l, col, inicio, "Comentario sin cierre '*/'.");
                avanzar(); avanzar(); continue;
            }
            if (letra(c)) {
                avanzar(); while (letra(ver(0)) || digito(ver(0))) avanzar();
                String palabra = fuente.substring(inicio, pos);
                tokens.add(new Token(RESERVADAS.contains(palabra) ? palabra : "ID", palabra, new Lugar(l,col,inicio,pos)));
                continue;
            }
            if (digito(c)) {
                avanzar(); while (digito(ver(0))) avanzar();
                boolean decimal = false;
                if (ver(0) == '.') {
                    decimal = true; avanzar();
                    if (!digito(ver(0))) throw fallo(l,col,inicio,"Faltan dígitos después del punto decimal.");
                    while (digito(ver(0))) avanzar();
                }
                if (letra(ver(0)) || ver(0) == '.') throw fallo(l,col,inicio,"Literal numérico mal formado.");
                tokens.add(new Token(decimal ? "DECIMAL" : "ENTERO",fuente.substring(inicio,pos),new Lugar(l,col,inicio,pos)));
                continue;
            }
            if (c == '"') {
                avanzar(); StringBuilder valor = new StringBuilder();
                while (pos < fuente.length() && ver(0) != '"') {
                    if (ver(0) == '\n') throw fallo(l,col,inicio,"Cadena sin cierre de comillas.");
                    char x = avanzar();
                    if (x == '\\') {
                        if (pos == fuente.length()) throw fallo(l,col,inicio,"Escape incompleto.");
                        x = avanzar();
                        x = switch (x) { case 'n' -> '\n'; case 'r' -> '\r'; case 't' -> '\t';
                            case '\\' -> '\\'; case '"' -> '"';
                            default -> throw fallo(linea,columna-1,pos-1,"Escape no reconocido."); };
                    }
                    valor.append(x);
                }
                if (pos == fuente.length()) throw fallo(l,col,inicio,"Cadena sin cierre de comillas.");
                avanzar(); tokens.add(new Token("CADENA",valor.toString(),new Lugar(l,col,inicio,pos))); continue;
            }
            String dos = "" + c + ver(1);
            if (Set.of("==", "!=", "<=", ">=", "&&", "||").contains(dos)) {
                avanzar(); avanzar(); tokens.add(new Token(dos,dos,new Lugar(l,col,inicio,pos))); continue;
            }
            if ("{}()[];=+-*/%!<>".indexOf(c) >= 0) {
                avanzar(); tokens.add(new Token(""+c,""+c,new Lugar(l,col,inicio,pos))); continue;
            }
            throw fallo(l,col,inicio,"Carácter no permitido: '" + c + "'.");
        }
        tokens.add(new Token("EOF","",new Lugar(linea,columna,pos,pos)));
        return List.copyOf(tokens);
    }
    private char ver(int n) { return pos+n < fuente.length() ? fuente.charAt(pos+n) : '\0'; }
    private char avanzar() { char c = fuente.charAt(pos++); if (c == '\n') { linea++; columna=1; } else columna++; return c; }
    private boolean letra(char c) { return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_'; }
    private boolean digito(char c) { return c >= '0' && c <= '9'; }
    private Fallo fallo(int l,int c,int i,String m) { return new Fallo("LÉXICO",new Lugar(l,c,i,pos),m); }
}
