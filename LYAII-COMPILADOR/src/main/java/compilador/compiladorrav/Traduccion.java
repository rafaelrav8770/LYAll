package compilador.compiladorrav;

import java.util.*;
import compilador.compiladorrav.Modelo.*;
import compilador.compiladorrav.Codigo.*;
import compilador.servicios.ServicioIntermedio;


public final class Traduccion {
    private final RevisionSemantica revision;
    private final List<Instruccion> codigo = new ArrayList<>();
    private int temporal, etiqueta;
    public Traduccion(RevisionSemantica revision) { this.revision = revision; }
    public List<Instruccion> generar(Programa p) { codigo.clear();temporal=0;etiqueta=0;sentencia(p.cuerpo());return List.copyOf(codigo); }
    private String etiqueta() { return "L"+etiqueta++; }
    private Operando temp(Tipo tipo) { return Operando.ranura("T"+(++temporal),tipo); }
    private void emitir(String op,Operando d,Operando a,Operando b,String e,Lugar l) {
        codigo.add(new Instruccion(op,d,a,b,e,l));
    }
    private void sentencia(Sentencia s) {
        if (s instanceof Bloque b) b.sentencias().forEach(this::sentencia);
        else if (s instanceof Declaracion d && d.valor()!=null) {
            var sim = revision.simbolo(d);
            emitir("MOV",Operando.ranura(sim.ranura(),sim.tipo()),expresion(d.valor()),null,null,d.lugar());
        } else if (s instanceof Asignacion a) {
            var sim=revision.simbolo(a);
            emitir("MOV",Operando.ranura(sim.ranura(),sim.tipo()),expresion(a.valor()),null,null,a.lugar());
        } else if (s instanceof Salida p) emitir("PRINT",null,expresion(p.valor()),null,null,p.lugar());
        else if (s instanceof Condicional c) {
            String otro=etiqueta(), fin=etiqueta();
            emitir("JZ",null,expresion(c.condicion()),null,otro,c.lugar()); sentencia(c.entonces());
            emitir("JMP",null,null,null,fin,c.lugar()); emitir("LABEL",null,null,null,otro,c.lugar());
            if(c.alternativa()!=null) sentencia(c.alternativa()); emitir("LABEL",null,null,null,fin,c.lugar());
        } else if (s instanceof Ciclo c) {
            String inicio=etiqueta(),fin=etiqueta(); emitir("LABEL",null,null,null,inicio,c.lugar());
            emitir("JZ",null,expresion(c.condicion()),null,fin,c.lugar()); sentencia(c.cuerpo());
            emitir("JMP",null,null,null,inicio,c.lugar()); emitir("LABEL",null,null,null,fin,c.lugar());
        }
    }
    private Operando expresion(Expresion e) {
        if (e instanceof Literal l) return Operando.constante(l.valor());
        if (e instanceof Variable v) { var s=revision.simbolo(v); return Operando.ranura(s.ranura(),s.tipo()); }
        if (e instanceof Unaria u) {
            Operando a=expresion(u.valor()),t=temp(revision.tipo(e));
            emitir(switch(u.operador()) { case "-" -> "NEG"; case "!" -> "NOT"; default -> "POS"; },t,a,null,null,u.lugar()); return t;
        }
        Binaria b=(Binaria)e;
        if (b.operador().equals("&&") || b.operador().equals("||")) {
            // El operando derecho sólo se calcula cuando es necesario.
            Operando resultado=temp(Tipo.BOOLEAN),izq=expresion(b.izquierda());
            String derecha=etiqueta(),fin=etiqueta(); boolean and=b.operador().equals("&&");
            emitir("MOV",resultado,Operando.constante(Valor.logico(!and)),null,null,b.lugar());
            emitir("JZ",null,izq,null,and?fin:derecha,b.lugar());
            if (!and) { emitir("JMP",null,null,null,fin,b.lugar()); emitir("LABEL",null,null,null,derecha,b.lugar()); }
            emitir("MOV",resultado,expresion(b.derecha()),null,null,b.lugar());
            emitir("LABEL",null,null,null,fin,b.lugar()); return resultado;
        }
        if (aritmetica(e)) return postfija(e);
        Operando a=expresion(b.izquierda()),d=expresion(b.derecha()),t=temp(revision.tipo(e));
        emitir(b.operador(),t,a,d,null,b.lugar()); return t;
    }
    private boolean aritmetica(Expresion e) {
        if (e instanceof Literal l) return l.valor().tipo().numerico();
        if (e instanceof Variable) return revision.tipo(e).numerico();
        return e instanceof Binaria b && Set.of("+","-","*","/").contains(b.operador())
                && aritmetica(b.izquierda()) && aritmetica(b.derecha());
    }
    private String infija(Expresion e,Map<String,Operando> hojas,List<Binaria> operaciones) {
        if (e instanceof Binaria b) {
            String a=infija(b.izquierda(),hojas,operaciones),d=infija(b.derecha(),hojas,operaciones);
            operaciones.add(b); return "("+a+b.operador()+d+")";
        }
        String nombre="h"+hojas.size(); hojas.put(nombre,expresion(e)); return nombre;
    }
    private Operando postfija(Expresion e) {
        Map<String,Operando> hojas=new LinkedHashMap<>(); List<Binaria> operaciones=new ArrayList<>();
        String infija=infija(e,hojas,operaciones); Iterator<Binaria> nodos=operaciones.iterator();
        ServicioIntermedio servicio=new ServicioIntermedio();
        List<String> post=servicio.convertirPostfija(servicio.tokenizar(infija));
        // Mismo algoritmo de Rafael: operandos en pila -> dos pops -> temporal -> push.
        Deque<Operando> pila=new ArrayDeque<>();
        for(String token:post) {
            if (hojas.containsKey(token)) pila.push(hojas.get(token));
            else {
                Operando b=pila.pop(),a=pila.pop(); Tipo tipo=a.tipo()==Tipo.DOUBLE || b.tipo()==Tipo.DOUBLE ? Tipo.DOUBLE : Tipo.INT;
                Operando t=temp(tipo); emitir(token,t,a,b,null,nodos.next().lugar()); pila.push(t);
            }
        }
        return pila.pop();
    }
}
