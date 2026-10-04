package compilador.compiladorrav;

import java.util.*;
import compilador.compiladorrav.Modelo.*;
import compilador.servicios.ReglasTipos;


public final class RevisionSemantica {
    public record Simbolo(int id, String nombre, Tipo tipo, int ambito, Lugar lugar) {
        public String ranura() { return "v" + id + "_" + nombre; }
    }
    private final Deque<Map<String,Simbolo>> ambitos = new ArrayDeque<>();
    private final List<Simbolo> simbolos = new ArrayList<>();
    private final IdentityHashMap<Expresion,Tipo> tipos = new IdentityHashMap<>();
    private final IdentityHashMap<Object,Simbolo> referencias = new IdentityHashMap<>();
    private Set<Integer> inicializados = new HashSet<>();
    public void revisar(Programa p) { ambitos.clear();simbolos.clear();tipos.clear();referencias.clear();inicializados.clear();bloque(p.cuerpo()); }
    public List<Simbolo> simbolos() { return List.copyOf(simbolos); }
    public Tipo tipo(Expresion e) { return tipos.get(e); }
    public Simbolo simbolo(Object n) { return referencias.get(n); }
    private void bloque(Bloque b) {
        ambitos.push(new LinkedHashMap<>());
        for (Sentencia s : b.sentencias()) sentencia(s);
        ambitos.pop().values().forEach(s -> inicializados.remove(s.id()));
    }
    private void sentencia(Sentencia s) {
        if (s instanceof Bloque b) bloque(b);
        else if (s instanceof Declaracion d) {
            // Como Java, no permite redeclarar un local mientras siga visible.
            if (buscar(d.nombre().texto()) != null) throw fallo(d.nombre().lugar(),"La variable '"+d.nombre().texto()+"' ya fue declarada en un ámbito visible.");
            Simbolo sim = new Simbolo(simbolos.size(),d.nombre().texto(),d.tipo(),ambitos.size(),d.nombre().lugar());
            simbolos.add(sim); ambitos.peek().put(sim.nombre(),sim); referencias.put(d,sim);
            if (d.valor() != null) { asignable(sim.tipo(),expresion(d.valor()),d.valor().lugar()); inicializados.add(sim.id()); }
        } else if (s instanceof Asignacion a) {
            Simbolo sim = exigir(a.nombre()); referencias.put(a,sim);
            asignable(sim.tipo(),expresion(a.valor()),a.valor().lugar()); inicializados.add(sim.id());
        } else if (s instanceof Salida p) expresion(p.valor());
        else if (s instanceof Condicional c) {
            condicion(c.condicion()); Set<Integer> antes = new HashSet<>(inicializados);
            bloque(c.entonces()); Set<Integer> rama = new HashSet<>(inicializados);
            inicializados = new HashSet<>(antes);
            if (c.alternativa() != null) bloque(c.alternativa());
            inicializados.retainAll(rama);
        } else if (s instanceof Ciclo c) {
            condicion(c.condicion()); Set<Integer> antes = new HashSet<>(inicializados);
            bloque(c.cuerpo()); inicializados = antes; // Puede ejecutar cero iteraciones.
        }
    }
    private void condicion(Expresion e) {
        if (expresion(e) != Tipo.BOOLEAN) throw fallo(e.lugar(),"La condición debe ser boolean.");
    }
    private Tipo expresion(Expresion e) {
        Tipo t;
        if (e instanceof Literal l) t = l.valor().tipo();
        else if (e instanceof Variable v) {
            Simbolo sim = exigir(v.nombre()); referencias.put(v,sim);
            if (!inicializados.contains(sim.id())) throw fallo(v.lugar(),"La variable '"+sim.nombre()+"' se usa antes de recibir un valor.");
            t = sim.tipo();
        } else if (e instanceof Unaria u) {
            Tipo a = expresion(u.valor());
            if (u.operador().equals("!")) {
                if (a != Tipo.BOOLEAN) throw fallo(u.lugar(),"'!' requiere boolean."); t = Tipo.BOOLEAN;
            } else { if (!a.numerico()) throw fallo(u.lugar(),"'"+u.operador()+"' requiere un número."); t = a; }
        } else {
            Binaria b = (Binaria)e; Tipo a = expresion(b.izquierda()), d = expresion(b.derecha());
            String op = b.operador();
            if (op.equals("&&") || op.equals("||")) {
                if (a != Tipo.BOOLEAN || d != Tipo.BOOLEAN) throw fallo(b.lugar(),"'"+op+"' requiere dos valores boolean."); t = Tipo.BOOLEAN;
            } else if (op.equals("==") || op.equals("!=")) {
                if (a != d && !(a.numerico() && d.numerico())) throw fallo(b.lugar(),"No se pueden comparar "+a.fuente()+" y "+d.fuente()+"."); t = Tipo.BOOLEAN;
            } else if (op.equals("+") && a == Tipo.STRING && d == Tipo.STRING) t = Tipo.STRING;
            else {
                if (!a.numerico() || !d.numerico()) throw fallo(b.lugar(),"'"+op+"' requiere operandos numéricos.");
                t = Set.of("<","<=",">",">=").contains(op) ? Tipo.BOOLEAN : a == Tipo.DOUBLE || d == Tipo.DOUBLE ? Tipo.DOUBLE : Tipo.INT;
            }
        }
        tipos.put(e,t); return t;
    }
    private Simbolo buscar(String nombre) {
        for (Map<String,Simbolo> a : ambitos) if (a.containsKey(nombre)) return a.get(nombre); return null;
    }
    private Simbolo exigir(Token t) {
        Simbolo s = buscar(t.texto());
        if (s == null) throw fallo(t.lugar(),"La variable '"+t.texto()+"' no está declarada."); return s;
    }
    private void asignable(Tipo d,Tipo o,Lugar l) {
        if (!ReglasTipos.compatible(d.fuente(),o.fuente())) throw fallo(l,"No se puede asignar "+o.fuente()+" a "+d.fuente()+".");
    }
    private Fallo fallo(Lugar l,String m) { return new Fallo("SEMÁNTICO",l,m); }
}
