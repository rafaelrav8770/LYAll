package compilador.compiladorrav;

import java.io.*;
import java.util.*;
import compilador.compiladorrav.Modelo.*;


public final class Objeto {
    public enum Op {
        CONST, MOV, DOUBLE, ADD, SUB, MUL, DIV, MOD, NEG, NOT,
        EQ, NE, LT, LE, GT, GE, JMP, JZ, PRINT, HALT
    }
    public record Registro(String nombre,Tipo tipo) {}
    public record Orden(Op op,int d,int a,int b,Lugar lugar) {}
    private final List<Valor> constantes;
    private final List<Registro> registros;
    private final List<Orden> ordenes;
    public Objeto(List<Valor> constantes,List<Registro> registros,List<Orden> ordenes) {
        this.constantes=List.copyOf(constantes); this.registros=List.copyOf(registros); this.ordenes=List.copyOf(ordenes);
    }
    public List<Valor> constantes() { return constantes; }
    public List<Registro> registros() { return registros; }
    public List<Orden> ordenes() { return ordenes; }
    public byte[] bytes() {
        try {
            ByteArrayOutputStream buffer=new ByteArrayOutputStream(); DataOutputStream out=new DataOutputStream(buffer);
            out.writeInt(0x5241564D); out.writeShort(1); out.writeInt(constantes.size());
            for (Valor v:constantes) {
                out.writeByte(v.tipo().ordinal());
                switch(v.tipo()) { case INT -> out.writeInt((Integer)v.dato()); case DOUBLE -> out.writeDouble(v.numero());
                    case BOOLEAN -> out.writeBoolean(v.booleano()); case STRING -> cadena(out,v.texto()); }
            }
            out.writeInt(registros.size());
            for(Registro r:registros) { cadena(out,r.nombre()); out.writeByte(r.tipo().ordinal()); }
            out.writeInt(ordenes.size());
            for(Orden o:ordenes) {
                out.writeByte(o.op().ordinal()+1); out.writeInt(o.d()); out.writeInt(o.a()); out.writeInt(o.b());
                out.writeInt(o.lugar().linea()); out.writeInt(o.lugar().columna()); out.writeInt(o.lugar().inicio()); out.writeInt(o.lugar().fin());
            }
            out.flush(); return buffer.toByteArray();
        } catch(IOException ex) { throw new UncheckedIOException(ex); }
    }
    public static Objeto leer(byte[] bytes) {
        try {
            DataInputStream in=new DataInputStream(new ByteArrayInputStream(bytes));
            if(in.readInt()!=0x5241564D || in.readUnsignedShort()!=1) throw new IOException("Cabecera RAVM incompatible.");
            List<Valor> constantes=new ArrayList<>(); int n=cuenta(in);
            for(int i=0;i<n;i++) {
                Tipo t=tipo(in.readUnsignedByte());
                constantes.add(switch(t) { case INT -> Valor.entero(in.readInt()); case DOUBLE -> Valor.decimal(in.readDouble());
                    case BOOLEAN -> Valor.logico(in.readBoolean()); case STRING -> Valor.cadena(cadena(in)); });
            }
            List<Registro> registros=new ArrayList<>(); n=cuenta(in);
            for(int i=0;i<n;i++) registros.add(new Registro(cadena(in),tipo(in.readUnsignedByte())));
            List<Orden> ordenes=new ArrayList<>(); n=cuenta(in);
            for(int i=0;i<n;i++) {
                int op=in.readUnsignedByte()-1;
                if(op<0 || op>=Op.values().length) throw new IOException("Opcode desconocido.");
                ordenes.add(new Orden(Op.values()[op],in.readInt(),in.readInt(),in.readInt(),
                        new Lugar(in.readInt(),in.readInt(),in.readInt(),in.readInt())));
            }
            if(in.available()!=0) throw new IOException("Bytes sobrantes en RAVM.");
            Objeto imagen=new Objeto(constantes,registros,ordenes); imagen.validar(); return imagen;
        } catch(IOException ex) { throw new IllegalArgumentException("Objeto RAVM inválido: "+ex.getMessage(),ex); }
    }
    private void validar() throws IOException {
        if(registros.size()<2 || ordenes.isEmpty()) throw new IOException("Imagen vacía.");
        for(Orden o:ordenes) {
            switch(o.op()) {
                case CONST -> { registro(o.d()); if(o.a()<0 || o.a()>=constantes.size()) throw new IOException("Constante inválida."); }
                case MOV, DOUBLE, NEG, NOT -> { registro(o.d()); registro(o.a()); }
                case JMP -> salto(o.d());
                case JZ -> { salto(o.d()); registro(o.a()); }
                case PRINT -> registro(o.a());
                case HALT -> {}
                default -> { registro(o.d()); registro(o.a()); registro(o.b()); }
            }
        }
    }
    private void registro(int n) throws IOException { if(n<0 || n>=registros.size()) throw new IOException("Registro inválido."); }
    private void salto(int n) throws IOException { if(n<0 || n>=ordenes.size()) throw new IOException("Salto inválido."); }
    private static Tipo tipo(int n) throws IOException { if(n>=Tipo.values().length) throw new IOException("Tipo inválido."); return Tipo.values()[n]; }
    private static int cuenta(DataInputStream in) throws IOException { int n=in.readInt(); if(n<0 || n>1_000_000) throw new IOException("Tamaño inválido."); return n; }
    private static void cadena(DataOutputStream out,String s) throws IOException { byte[] b=s.getBytes(java.nio.charset.StandardCharsets.UTF_8); out.writeInt(b.length); out.write(b); }
    private static String cadena(DataInputStream in) throws IOException { int n=cuenta(in); byte[] b=in.readNBytes(n); if(b.length!=n) throw new EOFException(); return new String(b,java.nio.charset.StandardCharsets.UTF_8); }
    public String ensamblador() {
        StringBuilder s=new StringBuilder("; RAVM v1 • máquina virtual de registros\n.CONSTANTES\n");
        for(int i=0;i<constantes.size();i++) s.append("C").append(i).append(" = ").append(constantes.get(i).fuente()).append('\n');
        s.append("\n.REGISTROS\nR0, R1: operandos de trabajo\n");
        for(int i=2;i<registros.size();i++) s.append("R").append(i).append(": ").append(registros.get(i).nombre()).append(" : ").append(registros.get(i).tipo().fuente()).append('\n');
        s.append("\n.CODIGO\n");
        for(int i=0;i<ordenes.size();i++) {
            Orden o=ordenes.get(i); String operandos=switch(o.op()) {
                case CONST -> "R"+o.d()+", C"+o.a();
                case MOV, DOUBLE, NEG, NOT -> "R"+o.d()+", R"+o.a();
                case JMP -> "@"+o.d(); case JZ -> "R"+o.a()+", @"+o.d();
                case PRINT -> "R"+o.a(); case HALT -> "";
                default -> "R"+o.d()+", R"+o.a()+", R"+o.b(); };
            s.append(String.format("%04d  %-7s %s%n",i,o.op(),operandos));
        }
        return s.toString();
    }
    public String hexadecimal() {
        byte[] b=bytes(); StringBuilder s=new StringBuilder("RAVM v1 • "+b.length+" bytes\n\n");
        for(int i=0;i<b.length;i++) { if(i%16==0) s.append(String.format("%06X  ",i)); s.append(String.format("%02X ",b[i]&255)); if(i%16==15 || i==b.length-1) s.append('\n'); }
        return s.toString();
    }
}
