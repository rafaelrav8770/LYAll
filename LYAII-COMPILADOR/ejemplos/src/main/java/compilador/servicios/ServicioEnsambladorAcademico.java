package compilador.servicios;

import java.util.*;

/** Generador x86 original, conservado sólo para las prácticas de TEMA4. */
public final class ServicioEnsambladorAcademico {
    public String generar(String fuente) {
            String exp=fuente.replaceAll("\\s+","");
            String opnd="([a-zA-Z][a-zA-Z0-9]*|\\d{1,5})";

            if(!exp.matches("[a-zA-Z]="+opnd+"[+\\-*/]"+opnd)){
                return ("Expresion no valida.\n\nEjemplos:\na = b + c\nx = y - z\nr = a * b\nd = x / 5");
            }

            String dest=exp.substring(0,1);
            String[] v=exp.substring(2).split("[+\\-*/]");
            char op=exp.charAt(2+v[0].length());

            Set<String> vars=new LinkedHashSet<>();
            vars.add(dest);
            for(String x:v) if(!Character.isDigit(x.charAt(0))) vars.add(x);

            StringBuilder datos=new StringBuilder();
            for(String x:vars) datos.append("    ").append(String.format("%-6s DW ?%n",x));

            List<String> ins=new ArrayList<>();
            ins.add("MOV AX, "+v[0]);
            switch(op){
                case '+'->ins.add("ADD AX, "+v[1]);
                case '-'->ins.add("SUB AX, "+v[1]);
                case '*'->{
                    ins.add("MOV BX, "+v[1]);
                    ins.add("MUL BX");
                }
                default->{
                    ins.add("MOV BX, "+v[1]);
                    ins.add("XOR DX, DX");
                    ins.add("DIV BX");
                }
            }
            ins.add("MOV "+dest+", AX");

            StringBuilder cod=new StringBuilder();
            for(String i:ins) cod.append("    ").append(i).append("\n");

            return (
                    "EXPRESION: "+dest+" = "+v[0]+" "+op+" "+v[1]+
                            "\nPROGRAMA GENERADO\n"+
                            ".MODEL SMALL\n.STACK 100h\n.DATA\n"+datos+
                            ".CODE\nMAIN PROC\n    MOV AX, @DATA\n    MOV DS, AX\n"+cod+
                            "\n    MOV AH, 4Ch\n    INT 21h\nMAIN ENDP\nEND MAIN"+
                            "\nINSTRUCCIONES DE LA EXPRESION: "+ins.size()
            );
    }
}
