package com.crediya.ui;

import com.crediya.exception.CrediYaException;
import com.crediya.util.Validador;
import java.util.Scanner;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

public class Consola {
    private final Scanner sc = new Scanner(System.in);

    public String texto(String prompt) {
        System.out.print(prompt + ": ");
        return sc.nextLine().trim();
    }

    public int entero(String prompt) {
        try {
            return Integer.parseInt(texto(prompt));
        } catch (NumberFormatException e) {
            throw new CrediYaException("Debes ingresar un numero entero valido (solo digitos, sin letras ni decimales).");
        }
    }

    public double decimal(String prompt) {
        return Validador.dinero(texto(prompt), prompt);
    }

    // ---------- Lecturas con reintento: si el dato esta mal, vuelve a pedir SOLO ese campo ----------

    public String pedir(String prompt, Function<String, String> validador) {
        while (true) {
            String entrada = texto(prompt);
            cancelarSiPide(entrada);
            try {
                return validador.apply(entrada);
            } catch (CrediYaException e) {
                System.out.println("  [!] " + e.getMessage());
            }
        }
    }

    public int pedirEntero(String prompt, IntUnaryOperator validador) {
        while (true) {
            String entrada = texto(prompt);
            cancelarSiPide(entrada);
            try {
                return validador.applyAsInt(Integer.parseInt(entrada));
            } catch (NumberFormatException e) {
                System.out.println("  [!] Debes ingresar un numero entero (solo digitos, sin letras ni decimales).");
            } catch (CrediYaException e) {
                System.out.println("  [!] " + e.getMessage());
            }
        }
    }

    public double pedirDinero(String prompt, DoubleUnaryOperator validador) {
        while (true) {
            String entrada = texto(prompt);
            cancelarSiPide(entrada);
            try {
                return validador.applyAsDouble(Validador.dinero(entrada, prompt));
            } catch (CrediYaException e) {
                System.out.println("  [!] " + e.getMessage());
            }
        }
    }

    private void cancelarSiPide(String entrada) {
        if (entrada.equalsIgnoreCase("cancelar"))
            throw new CrediYaException("Operacion cancelada.");
    }

    public void pausa() {
        System.out.print("\nPresiona ENTER para continuar...");
        sc.nextLine();
    }
}
