package compilador.compiladorrav;

import java.util.*;
import compilador.compiladorrav.Codigo.*;
import compilador.compiladorrav.Modelo.Tipo;
import compilador.servicios.ServicioOptimizacionLocal;


public final class Mejora {
    public record Resultado(List<Instruccion> codigo,List<String> cambios) {}
    public Resultado optimizar(List<Instruccion> entrada) {
        List<Instruccion> salida=new ArrayList<>(); List<String> cambios=new ArrayList<>();
        Map<String,Valor> constantes=new HashMap<>();
        ServicioOptimizacionLocal reglas=new ServicioOptimizacionLocal();
        for (Instruccion i:entrada) {
            if (i.op().equals("LABEL")) constantes.clear();
            Operando a=resolver(i.a(),constantes),b=resolver(i.b(),constantes);
            Instruccion nuevo=new Instruccion(i.op(),i.destino(),a,b,i.etiqueta(),i.lugar());
            if (i.destino()!=null) {
                constantes.remove(i.destino().ranura());
                if (a!=null && a.constante()!=null && (b==null || b.constante()!=null)) {
                    try {
                        Valor v=switch(i.op()) {
                            case "MOV" -> a.constante().convertir(i.destino().tipo());
                            case "NEG" -> Valor.unaria("-",a.constante());
                            case "NOT" -> Valor.unaria("!",a.constante());
                            case "POS" -> a.constante();
                            default -> Valor.binaria(i.op(),a.constante(),b.constante());
                        };
                        nuevo=new Instruccion("MOV",i.destino(),Operando.constante(v),null,null,i.lugar());
                        constantes.put(i.destino().ranura(),v);
                    } catch (ArithmeticException ex) {
                        // Conservar la operación: puede pertenecer a una rama que nunca se ejecuta.
                    }
                }
                // Reutiliza identidades algebraicas de Rafael únicamente sobre int.
                // Para double, sumar 0 o multiplicar por 0 puede alterar -0.0 o NaN.
                if (nuevo.op().equals(i.op()) && b!=null && a.tipo()==Tipo.INT && b.tipo()==Tipo.INT) {
                    String simple=reglas.simplificarAlgebraico(a.texto(),i.op(),b.texto());
                    if(simple!=null) {
                        Operando valor=simple.equals(a.texto())?a:simple.equals(b.texto())?b:Operando.constante(Valor.entero(Integer.parseInt(simple)));
                        nuevo=new Instruccion("MOV",i.destino(),valor,null,null,i.lugar());
                        if(valor.constante()!=null) constantes.put(i.destino().ranura(),valor.constante());
                    }
                }
            }
            if (!nuevo.equals(i)) cambios.add(i.texto()+"  →  "+nuevo.texto());
            salida.add(nuevo);
            if (i.op().equals("JMP") || i.op().equals("JZ")) constantes.clear();
        }
        return new Resultado(List.copyOf(salida),List.copyOf(cambios));
    }
    private Operando resolver(Operando o,Map<String,Valor> mapa) {
        return o!=null && o.constante()==null && mapa.containsKey(o.ranura()) ? Operando.constante(mapa.get(o.ranura())) : o;
    }
}
