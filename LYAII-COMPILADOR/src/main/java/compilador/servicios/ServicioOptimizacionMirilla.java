package compilador.servicios;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class ServicioOptimizacionMirilla {
    private static final Pattern MOV = Pattern.compile("^(?i)MOV\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*,\\s*([A-Za-z_][A-Za-z0-9_]*)$");
    private static final Pattern MOV_ZERO = Pattern.compile("^(?i)MOV\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*,\\s*0$");
    private static final Pattern GOTO = Pattern.compile("^(?i)(goto|jmp)\\s+\\S+$");
    private static final Pattern LABEL = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*:$");
    private static final Pattern NOOP_ARITH = Pattern.compile("^(?i)(ADD|SUB)\\s+[A-Za-z_][A-Za-z0-9_]*\\s*,\\s*0$|^(?i)(MUL|DIV)\\s+[A-Za-z_][A-Za-z0-9_]*\\s*,\\s*1$");
    public String optimizarMirilla(String codigo){
        List<String> lineas = new ArrayList<>();
        for(String l : codigo.split("\\R")){
            String t = l.trim();
            if(!t.isEmpty()) lineas.add(t);
        }
        boolean cambiado = true;
        while(cambiado){
            cambiado = false;
            for(int i=0;i<lineas.size()-1;i++){
                if(GOTO.matcher(lineas.get(i)).matches() && !LABEL.matcher(lineas.get(i+1)).matches()){
                    lineas.remove(i+1);
                    cambiado = true;
                    break;
                }
            }
            if(cambiado) continue;
            for(int i=0;i<lineas.size()-1;i++){
                Matcher m1 = MOV.matcher(lineas.get(i));
                Matcher m2 = MOV.matcher(lineas.get(i+1));
                if(m1.matches() && m2.matches()){
                    String d1=m1.group(1), s1=m1.group(2);
                    String d2=m2.group(1), s2=m2.group(2);
                    if(d2.equalsIgnoreCase(s1) && s2.equalsIgnoreCase(d1)){
                        lineas.remove(i+1);
                        cambiado = true;
                        break;
                    }
                }
            }
            if(cambiado) continue;
            for(int i=0;i<lineas.size();i++){
                if(NOOP_ARITH.matcher(lineas.get(i)).matches()){
                    lineas.remove(i);
                    cambiado = true;
                    break;
                }
            }
            if(cambiado) continue;
            for(int i=0;i<lineas.size();i++){
                Matcher m = MOV_ZERO.matcher(lineas.get(i));
                if(m.matches()){
                    lineas.set(i, "CLR " + m.group(1));
                    cambiado = true;
                    break;
                }
            }
        }
        return String.join("\n", lineas);
    }
}
