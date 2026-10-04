package compilador.servicios;

/** Regla de compatibilidad extraída de AccionesSemanticas y ComprobacionTipos. */
public final class ReglasTipos {
    private ReglasTipos() {}
    public static boolean compatible(String destino, String origen) {
        if (destino == null || origen == null) return false;
        String d = normalizar(destino), o = normalizar(origen);
        return d.equals(o) || (d.equals("float") || d.equals("double")) && o.equals("int");
    }
    private static String normalizar(String s) { return s.equals("String") ? "string" : s; }
}
