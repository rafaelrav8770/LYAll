package compilador.servicios;

import java.util.*;

/** Generador x86 original, conservado sólo para las prácticas de TEMA4. */
public final class ServicioMaquinaAcademico {
    public String generar(String fuente) {
            String exp=fuente.replaceAll("\\s+","");
            String opnd="([a-zA-Z][a-zA-Z0-9]*|\\d{1,5})";

            if(!exp.matches("[a-zA-Z]="+opnd+"[+\\-*/]"+opnd)){
                return ("Expresion no valida.\nEjemplos:\na = b + c\nx = y - z\nr = a * b\nd = x / 5");
            }

            String dest=exp.substring(0,1);
            String[] v=exp.substring(2).split("[+\\-*/]");
            char op=exp.charAt(2+v[0].length());

            Map<String,Integer> mem=new LinkedHashMap<>();
            int dir=0x1000;
            mem.put(dest,dir);
            dir+=2;
            for(String x:v){
                if(!Character.isDigit(x.charAt(0))&&!mem.containsKey(x)){
                    mem.put(x,dir);
                    dir+=2;
                }
            }

            List<String[]> ins=new ArrayList<>();
            ins.add(new String[]{"MOV AX, "+v[0],cargar(v[0],"B8","A1",mem)});
            ins.add(new String[]{"MOV BX, "+v[1],cargar(v[1],"BB","8B 1E",mem)});
            switch(op){
                case '+'->ins.add(new String[]{"ADD AX, BX","01 D8"});
                case '-'->ins.add(new String[]{"SUB AX, BX","29 D8"});
                case '*'->ins.add(new String[]{"MUL BX","F7 E3"});
                default->{
                    ins.add(new String[]{"XOR DX, DX","31 D2"});
                    ins.add(new String[]{"DIV BX","F7 F3"});
                }
            }
            ins.add(new String[]{"MOV "+dest+", AX","A3 "+le(mem.get(dest))});

            StringBuilder dirs=new StringBuilder();
            mem.forEach((var,d)->dirs.append(var).append(" -> ").append(String.format("%04Xh",d)).append("\n"));

            StringBuilder hex=new StringBuilder(String.format("%-12s %s%n","ENSAMBLADOR","HEX"));
            StringBuilder bin=new StringBuilder();
            int bytes=0;
            for(String[] i:ins){
                hex.append(String.format("%-12s %s%n",i[0],i[1]));
                bin.append(binario(i[1])).append("\n");
                bytes+=i[1].split(" ").length;
            }

            return (
                    "EXPRESION: "+dest+" = "+v[0]+" "+op+" "+v[1]+
                            "\nDIRECCIONES"+dirs+
                            "CODIGO MAQUINA x86"+hex+
                            "BINARIO\n"+bin+
                            "TAMANO DEL PROGRAMA: "+bytes+" bytes"
            );
    }
    private String cargar(String x,String inmediato,String memoria,Map<String,Integer> mem){
        if(Character.isDigit(x.charAt(0))) return inmediato+" "+le(Integer.parseInt(x)&0xFFFF);
        return memoria+" "+le(mem.get(x));
    }

    private String le(int n){
        return String.format("%02X %02X",n&0xFF,(n>>8)&0xFF);
    }

    private String binario(String hex){
        StringBuilder sb=new StringBuilder();
        for(String b:hex.split(" ")){
            sb.append(String.format("%8s",Integer.toBinaryString(Integer.parseInt(b,16))).replace(' ','0')).append(" ");
        }
        return sb.toString().trim();
    }
}
