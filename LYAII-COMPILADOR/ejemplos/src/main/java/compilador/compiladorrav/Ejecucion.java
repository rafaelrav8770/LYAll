package compilador.compiladorrav;

import compilador.compiladorrav.Objeto.*;
import compilador.compiladorrav.Modelo.*;


public final class Ejecucion {
    public record Resultado(String salida,long instrucciones,Fallo fallo) { public boolean correcto() { return fallo==null; } }
    public Resultado ejecutar(byte[] bytes) {
        Objeto imagen=Objeto.leer(bytes);
        Valor[] registros=new Valor[imagen.registros().size()];
        StringBuilder salida=new StringBuilder(); int pc=0; long pasos=0; Lugar lugar=Lugar.interno();
        try {
            while(pc<imagen.ordenes().size()) {
                Orden o=imagen.ordenes().get(pc); lugar=o.lugar();
                if(++pasos>200_000) throw new ArithmeticException("Límite de 200000 instrucciones alcanzado; revise el ciclo.");
                if(Thread.currentThread().isInterrupted()) throw new ArithmeticException("Ejecución cancelada.");
                pc++;
                switch(o.op()) {
                    case CONST -> registros[o.d()]=imagen.constantes().get(o.a());
                    case MOV -> registros[o.d()]=leer(registros,o.a());
                    case DOUBLE -> registros[o.d()]=leer(registros,o.a()).convertir(Tipo.DOUBLE);
                    case NEG -> registros[o.d()]=Valor.unaria("-",leer(registros,o.a()));
                    case NOT -> registros[o.d()]=Valor.unaria("!",leer(registros,o.a()));
                    case JMP -> pc=o.d();
                    case JZ -> { if(!leer(registros,o.a()).booleano()) pc=o.d(); }
                    case PRINT -> {
                        String texto=leer(registros,o.a()).texto();
                        if(salida.length()+texto.length()+1>65536) throw new ArithmeticException("Límite de salida de 65536 caracteres alcanzado.");
                        salida.append(texto).append('\n');
                    }
                    case HALT -> { return new Resultado(salida.toString(),pasos,null); }
                    default -> {
                        String op=switch(o.op()) { case ADD -> "+"; case SUB -> "-"; case MUL -> "*"; case DIV -> "/";
                            case MOD -> "%"; case EQ -> "=="; case NE -> "!="; case LT -> "<"; case LE -> "<="; case GT -> ">"; case GE -> ">=";
                            default -> throw new IllegalArgumentException("Opcode inválido."); };
                        registros[o.d()]=Valor.binaria(op,leer(registros,o.a()),leer(registros,o.b()));
                    }
                }
            }
            throw new ArithmeticException("El programa terminó sin HALT.");
        } catch(ArithmeticException ex) { return new Resultado(salida.toString(),pasos,new Fallo("DE EJECUCIÓN",lugar,ex.getMessage())); }
    }
    private Valor leer(Valor[] r,int n) { if(r[n]==null) throw new ArithmeticException("Lectura de un registro sin valor."); return r[n]; }
}
