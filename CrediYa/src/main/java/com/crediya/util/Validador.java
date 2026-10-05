package com.crediya.util;

import com.crediya.exception.CrediYaException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validaciones reutilizables de entrada.
 * Cada metodo valida, NORMALIZA (limpia el dato) y devuelve el valor listo para guardar.
 * Si algo esta mal lanza CrediYaException con un mensaje que explica que corregir.
 */
public final class Validador {
    private Validador() {}

    // ---------- Limites (cambialos aqui y se aplican en todo el programa) ----------
    public static final int NOMBRE_MIN = 3;
    public static final int NOMBRE_MAX = 60;
    public static final int CEDULA_MIN = 6;
    public static final int CEDULA_MAX = 10;
    public static final int CELULAR_DIGITOS = 10;       // celular colombiano sin indicativo
    public static final int CORREO_MAX = 100;
    public static final double SALARIO_MIN = 100_000;
    public static final double SALARIO_MAX = 100_000_000;
    public static final double MONTO_MIN = 10_000;
    public static final double MONTO_MAX = 1_000_000_000;
    public static final double INTERES_MAX = 100;       // % total sobre el capital
    public static final int CUOTAS_MAX = 120;

    public static final List<String> ROLES = Arrays.asList("Asesor", "Cobrador", "Administrador");

    private static final Pattern NOMBRE = Pattern.compile("^\\p{L}+(?:[ '.\\-]\\p{L}+)*\\.?$");
    private static final Pattern CORREO = Pattern.compile(
            "^[A-Za-z0-9]+(?:[._+\\-][A-Za-z0-9]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9\\-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$");

    // ---------- Texto ----------
    /** Obligatorio: no nulo, no vacio. Quita espacios sobrantes (tambien los dobles del medio). */
    public static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank())
            throw new CrediYaException("El campo '" + campo + "' es obligatorio y no puede quedar vacio.");
        return valor.trim().replaceAll("\\s+", " ");
    }

    /** Nombre de persona: solo letras (con tildes y ñ), espacios, apostrofe, punto y guion. */
    public static String nombre(String valor, String campo) {
        String v = texto(valor, campo);
        if (v.length() < NOMBRE_MIN || v.length() > NOMBRE_MAX)
            throw new CrediYaException("El " + campo + " debe tener entre " + NOMBRE_MIN + " y "
                    + NOMBRE_MAX + " caracteres (tiene " + v.length() + ").");
        if (!NOMBRE.matcher(v).matches())
            throw new CrediYaException("El " + campo + " solo puede tener letras y espacios "
                    + "(sin numeros ni simbolos como @, #, ;).");
        return v;
    }

    // ---------- Documento / cedula ----------
    /** Quita puntos, espacios y guiones: "1.234.567" -> "1234567". No lanza error. */
    public static String normalizarDocumento(String valor) {
        return valor == null ? "" : valor.replaceAll("[.\\s\\-]", "");
    }

    /** Cedula: solo digitos, entre CEDULA_MIN y CEDULA_MAX, sin cero inicial ni todos iguales. */
    public static String cedula(String valor) {
        texto(valor, "documento");
        String d = normalizarDocumento(valor);
        if (!d.matches("\\d+"))
            throw new CrediYaException("El documento solo puede tener numeros (puedes usar puntos, ej: 1.098.765.432).");
        if (d.length() < CEDULA_MIN || d.length() > CEDULA_MAX)
            throw new CrediYaException("El documento debe tener entre " + CEDULA_MIN + " y " + CEDULA_MAX
                    + " digitos (ingresaste " + d.length() + ").");
        if (d.startsWith("0"))
            throw new CrediYaException("El documento no puede empezar por 0.");
        if (d.chars().distinct().count() == 1)
            throw new CrediYaException("El documento no es valido (todos los digitos son iguales).");
        return d;
    }

    // ---------- Celular ----------
    /** Celular colombiano: 10 digitos que empiezan por 3. Acepta +57, espacios, guiones y parentesis. */
    public static String celular(String valor) {
        texto(valor, "telefono");
        String d = valor.replaceAll("[\\s\\-().]", "");
        if (d.startsWith("+57")) d = d.substring(3);
        else if (d.startsWith("0057")) d = d.substring(4);
        else if (d.length() == CELULAR_DIGITOS + 2 && d.startsWith("57")) d = d.substring(2);

        if (!d.matches("\\d+"))
            throw new CrediYaException("El telefono solo puede tener numeros (se permite el prefijo +57, espacios y guiones).");
        if (d.length() != CELULAR_DIGITOS)
            throw new CrediYaException("El celular debe tener exactamente " + CELULAR_DIGITOS
                    + " digitos (ingresaste " + d.length() + ").");
        if (d.charAt(0) != '3')
            throw new CrediYaException("El celular debe empezar por 3 (ej: 3001234567).");
        if (d.chars().distinct().count() == 1)
            throw new CrediYaException("El celular no es valido (todos los digitos son iguales).");
        return d;
    }

    // ---------- Correo ----------
    /** Correo: formato estandar, sin puntos seguidos, dominio con punto. Se guarda en minusculas. */
    public static String correo(String valor) {
        texto(valor, "correo");
        String v = valor.trim();
        if (v.contains(" "))
            throw new CrediYaException("El correo no puede tener espacios.");
        if (v.length() > CORREO_MAX)
            throw new CrediYaException("El correo es muy largo (maximo " + CORREO_MAX + " caracteres).");
        int arroba = v.indexOf('@');
        if (arroba < 0 || arroba != v.lastIndexOf('@'))
            throw new CrediYaException("El correo debe tener una sola '@' (ej: nombre@dominio.com).");
        if (arroba == 0)
            throw new CrediYaException("Falta la parte antes de la '@' en el correo.");
        if (arroba > 64)
            throw new CrediYaException("La parte antes de la '@' es demasiado larga (maximo 64).");
        if (v.indexOf('.', arroba) < 0)
            throw new CrediYaException("Al dominio del correo le falta el punto (ej: gmail.com).");
        if (!CORREO.matcher(v).matches())
            throw new CrediYaException("El correo no tiene un formato valido (ej: nombre@dominio.com). "
                    + "Revisa que no tenga puntos seguidos, tildes ni simbolos raros.");
        return v.toLowerCase();
    }

    // ---------- Rol ----------
    /** Solo se aceptan los roles definidos; devuelve el nombre canonico ("asesor" -> "Asesor"). */
    public static String rol(String valor) {
        String v = texto(valor, "rol");
        for (String r : ROLES) if (r.equalsIgnoreCase(v)) return r;
        throw new CrediYaException("Rol invalido. Opciones: " + String.join(", ", ROLES) + ".");
    }

    // ---------- Numeros ----------
    /** Convierte texto a dinero. Acepta 1500000, 1.500.000, 1,500,000, 1.500.000,50, $ 2000000 o 2500.75. */
    public static double dinero(String texto, String campo) {
        String t = texto(texto, campo).replace("$", "").replace(" ", "");
        String limpio;
        if (t.matches("\\d{1,3}(\\.\\d{3})+(,\\d{1,2})?")) limpio = t.replace(".", "").replace(',', '.');
        else if (t.matches("\\d{1,3}(,\\d{3})+(\\.\\d{1,2})?")) limpio = t.replace(",", "");
        else if (t.matches("\\d+([.,]\\d{1,2})?")) limpio = t.replace(',', '.');
        else throw new CrediYaException("El valor de '" + campo + "' no es un numero valido "
                    + "(ej: 1500000 o 1.500.000, maximo 2 decimales, sin letras ni signo negativo).");
        return Double.parseDouble(limpio);
    }

    public static void positivo(double valor, String campo) {
        if (Double.isNaN(valor) || Double.isInfinite(valor))
            throw new CrediYaException("'" + campo + "' no es un numero valido.");
        if (valor <= 0) throw new CrediYaException("'" + campo + "' debe ser mayor que cero.");
    }

    public static double rango(double valor, double min, double max, String campo) {
        positivo(valor, campo);
        if (valor < min || valor > max)
            throw new CrediYaException(String.format(java.util.Locale.US,
                    "'%s' debe estar entre $%,.0f y $%,.0f.", campo, min, max));
        return valor;
    }

    public static double salario(double valor) { return rango(valor, SALARIO_MIN, SALARIO_MAX, "salario"); }

    public static double monto(double valor) { return rango(valor, MONTO_MIN, MONTO_MAX, "monto"); }

    public static double interes(double valor) {
        if (Double.isNaN(valor) || Double.isInfinite(valor))
            throw new CrediYaException("'interes' no es un numero valido.");
        if (valor < 0 || valor > INTERES_MAX)
            throw new CrediYaException("El interes total debe estar entre 0 y " + (int) INTERES_MAX + " %.");
        return valor;
    }

    public static int cuotas(int valor) {
        if (valor < 1 || valor > CUOTAS_MAX)
            throw new CrediYaException("Las cuotas deben estar entre 1 y " + CUOTAS_MAX + ".");
        return valor;
    }

    public static int id(int valor, String campo) {
        if (valor <= 0) throw new CrediYaException("El " + campo + " debe ser un numero mayor que cero.");
        return valor;
    }
}
