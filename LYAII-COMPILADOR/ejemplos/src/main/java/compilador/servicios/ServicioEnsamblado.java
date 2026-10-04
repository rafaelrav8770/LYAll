package compilador.servicios;

import java.util.*;
import compilador.compiladorrav.*;
import compilador.compiladorrav.Modelo.*;
import compilador.compiladorrav.Codigo.*;
import compilador.compiladorrav.Objeto.*;

/** Generación desde IR. Mantiene la estrategia de TEMA4: cargar, operar en registros, almacenar. */
public final class ServicioEnsamblado {
    private final List<Valor> constantes=new ArrayList<>();
    private final List<Registro> registros=new ArrayList<>();
    private final Map<String,Integer> ranuras=new LinkedHashMap<>(), etiquetas=new HashMap<>();
    private final List<Orden> ordenes=new ArrayList<>();
    private final Map<Integer,String> pendientes=new HashMap<>();
    public Objeto generar(List<Instruccion> ir) {
        constantes.clear();registros.clear();ranuras.clear();etiquetas.clear();ordenes.clear();pendientes.clear();
        registros.add(new Registro("trabajo_A",Tipo.INT)); registros.add(new Registro("trabajo_B",Tipo.INT));
        for(Instruccion i:ir) {
            if(i.op().equals("LABEL")) { etiquetas.put(i.etiqueta(),ordenes.size()); continue; }
            if(i.op().equals("JMP")) { salto(Op.JMP,i); continue; }
            if(i.a()!=null) cargar(i.a(),0,i.lugar());
            if(i.op().equals("JZ")) { salto(Op.JZ,i); continue; }
            if(i.op().equals("PRINT")) { emitir(Op.PRINT,-1,0,-1,i.lugar()); continue; }
            if(i.b()!=null) cargar(i.b(),1,i.lugar());
            Op op=switch(i.op()) {
                case "+" -> Op.ADD; case "-" -> Op.SUB; case "*" -> Op.MUL; case "/" -> Op.DIV; case "%" -> Op.MOD;
                case "==" -> Op.EQ; case "!=" -> Op.NE; case "<" -> Op.LT; case "<=" -> Op.LE; case ">" -> Op.GT; case ">=" -> Op.GE;
                case "NEG" -> Op.NEG; case "NOT" -> Op.NOT; case "MOV", "POS" -> null;
                default -> throw new IllegalArgumentException("IR no soportada: "+i.op()); };
            if(op!=null) emitir(op,0,0,i.b()==null?-1:1,i.lugar());
            if(i.destino().tipo()==Tipo.DOUBLE) emitir(Op.DOUBLE,0,0,-1,i.lugar());
            emitir(Op.MOV,ranura(i.destino()),0,-1,i.lugar());
        }
        emitir(Op.HALT,-1,-1,-1,Lugar.interno());
        pendientes.forEach((pos,nombre)-> {
            Integer destino=etiquetas.get(nombre); if(destino==null) throw new IllegalArgumentException("Etiqueta desconocida: "+nombre);
            Orden o=ordenes.get(pos); ordenes.set(pos,new Orden(o.op(),destino,o.a(),o.b(),o.lugar()));
        });
        return new Objeto(constantes,registros,ordenes);
    }
    private void cargar(Operando o,int r,Lugar l) {
        if(o.constante()!=null) {
            int n=constantes.indexOf(o.constante()); if(n<0) { n=constantes.size(); constantes.add(o.constante()); }
            emitir(Op.CONST,r,n,-1,l);
        } else emitir(Op.MOV,r,ranura(o),-1,l);
    }
    private int ranura(Operando o) {
        return ranuras.computeIfAbsent(o.ranura(),n->{int i=registros.size(); registros.add(new Registro(n,o.tipo())); return i;});
    }
    private void salto(Op op,Instruccion i) { pendientes.put(ordenes.size(),i.etiqueta()); emitir(op,-1,op==Op.JZ?0:-1,-1,i.lugar()); }
    private void emitir(Op op,int d,int a,int b,Lugar l) { ordenes.add(new Orden(op,d,a,b,l)); }
}
