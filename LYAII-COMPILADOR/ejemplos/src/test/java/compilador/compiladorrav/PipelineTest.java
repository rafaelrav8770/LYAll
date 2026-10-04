package compilador.compiladorrav;

import compilador.servicios.*;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {
    static String programa(String cuerpo) { return "class Prueba { public static void main() { "+cuerpo+" } }"; }
    static Compilacion.Resultado compilar(String cuerpo) { return new Compilacion().compilar(programa(cuerpo)); }
    static String ejecutar(String cuerpo) {
        var c=compilar(cuerpo); assertTrue(c.correcto(),c.consola());
        var e=new Ejecucion().ejecutar(c.objeto().bytes()); assertTrue(e.correcto(),()->e.fallo().diagnostico()); return e.salida();
    }
    static Stream<Arguments> programasValidos() {
        return Stream.of(
            Arguments.of("print(2 + 3 * 4);","14\n"),
            Arguments.of("print((2 + 3) * 4);","20\n"),
            Arguments.of("print(20 / 2 / 2);","5\n"),
            Arguments.of("print(20 - 5 - 2);","13\n"),
            Arguments.of("print(-3 * 4 + +2);","-10\n"),
            Arguments.of("int x = 7 / 2; print(x);","3\n"),
            Arguments.of("double x = 7.0 / 2; print(x);","3.5\n"),
            Arguments.of("double x = 3; print(x);","3.0\n"),
            Arguments.of("print(17 % 5); print(-7 / 2);","2\n-3\n"),
            Arguments.of("print(2147483647 + 1); print(-2147483648);","-2147483648\n-2147483648\n"),
            Arguments.of("int a = 1; int b = a; a = 9; print(b); print(a);","1\n9\n"),
            Arguments.of("int a = 2; int b = a + 3; a = 8; int c = a + 3; print(b); print(c);","5\n11\n"),
            Arguments.of("int T1 = 3; int x = T1 * 2 + T1; print(x);","9\n"),
            Arguments.of("String s = \"RAV\" + \"\\nHola\\t\\\"R\\\"\"; print(s);","RAV\nHola\t\"R\"\n"),
            Arguments.of("print(\"dos\" == \"do\" + \"s\");","true\n"),
            Arguments.of("print(2 == 2.0); print(3 != 3); print(!false);","true\nfalse\ntrue\n"),
            Arguments.of("boolean b = 2 < 3 && 4 >= 4; print(b);","true\n"),
            Arguments.of("print(false || true && false);","false\n"),
            Arguments.of("int z = 0; print(false && (1 / z > 0)); print(true || (1 / z > 0));","false\ntrue\n"),
            Arguments.of("print(true && true); print(false || true);","true\ntrue\n"),
            Arguments.of("int x; if (true) { x = 4; } else { x = 8; } print(x);","4\n"),
            Arguments.of("int x = 1; if (false) { x = 4; } else { x = 8; } print(x);","8\n"),
            Arguments.of("int x = 1; if (false) { x = 1 / 0; } print(x);","1\n"),
            Arguments.of("int i = 1; int s = 0; while (i <= 5) { s = s + i; i = i + 1; } print(s);","15\n"),
            Arguments.of("int i = 0; while (i < 3) { int j = 0; while (j < 2) { print(i * 10 + j); j = j + 1; } i = i + 1; }","0\n1\n10\n11\n20\n21\n"),
            Arguments.of("int a = 1; { int b = a + 3; print(b); } { int b = 8; print(b); }","4\n8\n"),
            Arguments.of("double d = -0.0; print(d + 0.0);","0.0\n"),
            Arguments.of("// comentario\n int x = 4; /* multilínea\n comentario */ print(x);","4\n"),
            Arguments.of("int a = 4; int b = a * 1; print(b);","4\n"),
            Arguments.of("double a = 1.5; int i = 0; while (i < 2) { a = a + 0.5; i = i + 1; } print(a);","2.5\n")
        );
    }
    @ParameterizedTest @MethodSource("programasValidos")
    void compilaYEjecutaBytes(String cuerpo,String esperado) { assertEquals(esperado,ejecutar(cuerpo)); }
    static Stream<Arguments> programasInvalidos() {
        return Stream.of(
            Arguments.of("int x = @;","LÉXICO"),
            Arguments.of("int x = 12abc;","LÉXICO"),
            Arguments.of("double x = 1.2.3;","LÉXICO"),
            Arguments.of("print(\"sin cierre);","LÉXICO"),
            Arguments.of("print(\"\\q\");","LÉXICO"),
            Arguments.of("/* sin cierre","LÉXICO"),
            Arguments.of("int x = 2 print(x);","SINTÁCTICO"),
            Arguments.of("print(2;","SINTÁCTICO"),
            Arguments.of("int x = ;","SINTÁCTICO"),
            Arguments.of("if (true) print(1);","SINTÁCTICO"),
            Arguments.of("int x = 2; x++;","SINTÁCTICO"),
            Arguments.of("x = 2;","SEMÁNTICO"),
            Arguments.of("int x; print(x);","SEMÁNTICO"),
            Arguments.of("int x = x + 1;","SEMÁNTICO"),
            Arguments.of("int x = 2; int x = 4;","SEMÁNTICO"),
            Arguments.of("int x = 2; { int x = 4; }","SEMÁNTICO"),
            Arguments.of("int x = 2.5;","SEMÁNTICO"),
            Arguments.of("String x = 2;","SEMÁNTICO"),
            Arguments.of("boolean x = 1;","SEMÁNTICO"),
            Arguments.of("int x = true + 1;","SEMÁNTICO"),
            Arguments.of("if (1) { print(2); }","SEMÁNTICO"),
            Arguments.of("print(\"x\" + 2);","SEMÁNTICO"),
            Arguments.of("print(true < false);","SEMÁNTICO"),
            Arguments.of("int x; if (true) { x = 2; } print(x);","SEMÁNTICO"),
            Arguments.of("int x; while (false) { x = 2; } print(x);","SEMÁNTICO"),
            Arguments.of("{ int x = 2; } print(x);","SEMÁNTICO"),
            Arguments.of("int x = 2147483648;","SEMÁNTICO"),
            Arguments.of("print(false && desconocida > 0);","SEMÁNTICO")
        );
    }
    @ParameterizedTest @MethodSource("programasInvalidos")
    void detectaFaseAutomaticamente(String cuerpo,String fase) {
        var c=compilar(cuerpo); assertFalse(c.correcto()); assertNull(c.objeto());assertEquals(fase,c.fallo().fase());
        assertTrue(c.consola().contains("Compilación detenida"));
    }
    @Test void diagnosticoConPosicionReal() {
        String fuente="class Demo {\n public static void main() {\n  print(1;\n }\n}";
        var c=new Compilacion().compilar(fuente);assertEquals("SINTÁCTICO",c.fallo().fase());
        assertEquals(3,c.fallo().lugar().linea());assertEquals(10,c.fallo().lugar().columna());
    }
    @Test void errorLexicoNormalizaCrlf() {
        var c=new Compilacion().compilar("class X {\r\n public static void main(){\r\n  @\r\n }}");
        assertEquals(3,c.fallo().lugar().linea());assertEquals(3,c.fallo().lugar().columna());
    }
    @Test void divisionEnEjecucionApuntaAlOperadorInterno() {
        String cuerpo="int z = 0; print(2 + (10 / z));";
        var c=compilar(cuerpo);assertTrue(c.correcto());var e=new Ejecucion().ejecutar(c.objeto().bytes());
        assertFalse(e.correcto());assertEquals(programa(cuerpo).indexOf('/'),e.fallo().lugar().inicio());
    }
    @Test void limitesDeEjecucion() {
        var c=compilar("while (true) { }");var e=new Ejecucion().ejecutar(c.objeto().bytes());
        assertFalse(e.correcto());assertTrue(e.fallo().getMessage().contains("200000"));
    }
    @Test void limiteDeSalida() {
        var c=compilar("while (true) { print(\"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\"); }");var e=new Ejecucion().ejecutar(c.objeto().bytes());
        assertFalse(e.correcto());assertTrue(e.fallo().getMessage().contains("salida"));
    }
    @Test void compilacionesIndependientes() {
        Compilacion motor=new Compilacion();assertTrue(motor.compilar(programa("int x=2;")).correcto());
        assertFalse(motor.compilar(programa("print(x);")).correcto());
        assertTrue(motor.compilar(programa("int x=4;print(x);")).correcto());
    }
    @Test void bytecodeRoundTripYRechazoDeObjetoTruncado() {
        var c=compilar("print(\"México\");");byte[] bytes=c.objeto().bytes();
        assertArrayEquals(bytes,Objeto.leer(bytes).bytes());
        assertThrows(IllegalArgumentException.class,()->Objeto.leer(Arrays.copyOf(bytes,bytes.length-1)));
    }
    @Test void optimizacionConservaResultadosEnVariosProgramas() {
        programasValidos().forEach(a->{var c=compilar((String)a.get()[0]);
            var original=new ServicioEnsamblado().generar(c.intermedio());
            var a1=new Ejecucion().ejecutar(original.bytes());var b1=new Ejecucion().ejecutar(c.objeto().bytes());
            assertEquals(a1.salida(),b1.salida());assertEquals(a1.correcto(),b1.correcto());
        });
    }
    @Test void serviciosHistoricosFuncionanSinJavafx() {
        ServicioIntermedio s=new ServicioIntermedio();assertEquals(List.of("a","b","c","*","+"),s.convertirPostfija(s.tokenizar("a+b*c")));
        assertTrue(s.generarCodigo("x = a + b * c").contains("T1 = b * c"));
        assertTrue(new ServicioEnsambladorAcademico().generar("a = b + c").contains("ADD AX"));
        assertTrue(new ServicioMaquinaAcademico().generar("a = b + c").contains("01 D8"));
        assertEquals("CLR AX",new ServicioOptimizacionMirilla().optimizarMirilla("MOV AX, 0"));
        assertEquals("x",new ServicioOptimizacionLocal().simplificarAlgebraico("x","*","1"));
        assertTrue(ReglasTipos.compatible("double","int"));assertFalse(ReglasTipos.compatible("int","double"));
    }
    @Test void mainConFirmaJavaSinUsarArgumentos() {
        var c=new Compilacion().compilar("class X { public static void main(String[] args) { print(2); } }");assertTrue(c.correcto(),c.consola());
    }
}
