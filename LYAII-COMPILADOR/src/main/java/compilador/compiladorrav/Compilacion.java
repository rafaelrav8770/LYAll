package compilador.compiladorrav;

import java.util.*;
import compilador.compiladorrav.Modelo.*;
import compilador.compiladorrav.Codigo.*;
import compilador.servicios.ServicioEnsamblado;


public final class Compilacion {
    public record Resultado(String fuente,List<Token> tokens,Programa programa,RevisionSemantica revision,
                            List<Instruccion> intermedio,Mejora.Resultado mejora,Objeto objeto,String consola,Fallo fallo) {
        public boolean correcto() { return fallo==null && objeto!=null; }
    }
    public Resultado compilar(String fuente) {
        fuente=fuente.replace("\r\n","\n").replace('\r','\n');
        StringBuilder consola=new StringBuilder("Compilación iniciada...\n");
        List<Token> tokens=List.of(); Programa programa=null; RevisionSemantica revision=null;
        List<Instruccion> ir=List.of(); Mejora.Resultado mejora=null; Objeto objeto=null; Fallo fallo=null;
        try {
            if(fuente.length()>200_000) throw new Fallo("LÉXICO",Lugar.interno(),"El programa supera 200000 caracteres.");
            tokens=new LectorLexico(fuente).leer(); consola.append("✓ Análisis léxico\n");
            programa=new LectorSintactico(tokens).leer(); consola.append("✓ Análisis sintáctico\n");
            revision=new RevisionSemantica(); revision.revisar(programa); consola.append("✓ Análisis semántico\n");
            ir=new Traduccion(revision).generar(programa); consola.append("✓ Código de tres direcciones\n");
            mejora=new Mejora().optimizar(ir); consola.append("✓ Optimización local: ").append(mejora.cambios().size()).append(" cambios\n");
            objeto=new ServicioEnsamblado().generar(mejora.codigo()); Objeto.leer(objeto.bytes());
            consola.append("✓ Ensamblador y objeto RAVM\n\nCOMPILACIÓN CORRECTA\n").append(objeto.bytes().length).append(" bytes • Listo para ejecutar.\n");
        } catch(Fallo ex) { fallo=ex; consola.append('\n').append(ex.diagnostico()).append("\n\nCompilación detenida.\n"); }
        catch(StackOverflowError ex) { fallo=new Fallo("SINTÁCTICO",Lugar.interno(),"Anidamiento excesivo; simplifique la expresión o los bloques."); consola.append('\n').append(fallo.diagnostico()).append("\nCompilación detenida.\n"); }
        return new Resultado(fuente,tokens,programa,revision,ir,mejora,objeto,consola.toString(),fallo);
    }
}
