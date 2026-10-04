package compilador.compiladorrav;

import compilador.compiladorrav.Modelo.Tipo;



public record Valor(Tipo tipo, Object dato) {
    public static Valor entero(int v) { return new Valor(Tipo.INT, v); }
    public static Valor decimal(double v) { return new Valor(Tipo.DOUBLE, v); }
    public static Valor logico(boolean v) { return new Valor(Tipo.BOOLEAN, v); }
    public static Valor cadena(String v) { return new Valor(Tipo.STRING, v); }
    public double numero() { return ((Number)dato).doubleValue(); }
    public boolean booleano() { return (Boolean)dato; }
    public Valor convertir(Tipo destino) {
        if (tipo == destino) return this;
        if (destino == Tipo.DOUBLE && tipo == Tipo.INT) return decimal(numero());
        throw new IllegalArgumentException("Conversión incompatible: " + tipo + " a " + destino);
    }
    public String texto() { return String.valueOf(dato); }
    public String fuente() {
        if (tipo != Tipo.STRING) return texto();
        return "\"" + texto().replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }
    public static Valor unaria(String op, Valor a) {
        return switch (op) {
            case "!" -> logico(!a.booleano());
            case "+" -> a;
            case "-" -> a.tipo == Tipo.INT ? entero(-((Integer)a.dato)) : decimal(-a.numero());
            default -> throw new IllegalArgumentException(op);
        };
    }
    public static Valor binaria(String op, Valor a, Valor b) {
        if (op.equals("+") && a.tipo == Tipo.STRING) return cadena(a.texto() + b.texto());
        if (op.equals("&&")) return logico(a.booleano() && b.booleano());
        if (op.equals("||")) return logico(a.booleano() || b.booleano());
        if (op.equals("==") || op.equals("!=")) {
            boolean igual = a.tipo.numerico() && b.tipo.numerico() ? a.numero() == b.numero() : a.dato.equals(b.dato);
            return logico(op.equals("==") ? igual : !igual);
        }
        if (op.equals("<")) return logico(a.numero() < b.numero());
        if (op.equals("<=")) return logico(a.numero() <= b.numero());
        if (op.equals(">")) return logico(a.numero() > b.numero());
        if (op.equals(">=")) return logico(a.numero() >= b.numero());
        // RAV-J trata cualquier división entre cero como fallo en ejecución (también double).
        if ((op.equals("/") || op.equals("%")) && b.numero() == 0) throw new ArithmeticException("División entre cero.");
        if (a.tipo == Tipo.INT && b.tipo == Tipo.INT) {
            int x = (Integer)a.dato, y = (Integer)b.dato;
            return entero(switch (op) { case "+" -> x + y; case "-" -> x - y;
                case "*" -> x * y; case "/" -> x / y; case "%" -> x % y;
                default -> throw new IllegalArgumentException(op); });
        }
        double x = a.numero(), y = b.numero();
        return decimal(switch (op) { case "+" -> x + y; case "-" -> x - y;
            case "*" -> x * y; case "/" -> x / y; case "%" -> x % y;
            default -> throw new IllegalArgumentException(op); });
    }
}
