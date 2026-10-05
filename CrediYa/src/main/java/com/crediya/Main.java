package com.crediya;

import com.crediya.exception.CrediYaException;
import com.crediya.model.*;
import com.crediya.repository.*;
import com.crediya.service.*;
import com.crediya.ui.Consola;
import com.crediya.util.ConexionDB;
import com.crediya.util.Validador;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Main {
    private static final Consola in = new Consola();
    private static EmpleadoService empleados;
    private static ClienteService clientes;
    private static GestorPrestamos prestamos;
    private static PagoService pagos;
    private static ReporteService reportes;

    public static void main(String[] args) {
        boolean bd = ConexionDB.getInstancia().probar();
        System.out.println(bd ? "Conectado a MySQL (crediya_db) + archivos en /datos"
                              : "Modo solo archivos (/datos)");

        empleados = new EmpleadoService(new RepositorioDual<>(new EmpleadoArchivoRepo(), new EmpleadoJdbcRepo()));
        clientes = new ClienteService(new RepositorioDual<>(new ClienteArchivoRepo(), new ClienteJdbcRepo()));
        prestamos = new GestorPrestamos(new RepositorioDual<>(new PrestamoArchivoRepo(), new PrestamoJdbcRepo()),
                clientes, empleados);
        pagos = new PagoService(new RepositorioDual<>(new PagoArchivoRepo(), new PagoJdbcRepo()), prestamos);
        reportes = new ReporteService(prestamos, clientes, empleados, pagos);

        int op = -1;
        while (op != 0) {
            System.out.println("\n===== CREDIYA S.A.S. =====");
            System.out.println("1. Empleados\n2. Clientes\n3. Gestion De Prestamos\n4. Pagos\n5. Reportes\n0. Salir");
            try {
                op = in.entero("Opcion");
                switch (op) {
                    case 1: menuEmpleados(); break;
                    case 2: menuClientes(); break;
                    case 3: menuPrestamos(); break;
                    case 4: menuPagos(); break;
                    case 5: menuReportes(); break;
                    case 0: System.out.println("Hasta pronto."); break;
                    default: System.out.println("Opcion invalida.");
                }
            } catch (CrediYaException e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    // ---------- Empleados ----------
    private static void menuEmpleados() {
        System.out.println("\n-- EMPLEADOS --\n1. Registrar\n2. Listar\n3. Buscar por documento");
        try {
            switch (in.entero("Opcion")) {
                case 1:
                    System.out.println("(escribe 'cancelar' en cualquier campo para salir)");
                    String nom = in.pedir("Nombre", v -> Validador.nombre(v, "nombre"));
                    String doc = in.pedir("Documento (6 a 10 digitos)", Validador::cedula);
                    String rol = in.pedir("Rol (Asesor/Cobrador/Administrador)", Validador::rol);
                    String mail = in.pedir("Correo", Validador::correo);
                    double sal = in.pedirDinero("Salario", Validador::salario);
                    Empleado e = empleados.registrar(nom, doc, rol, mail, sal);
                    System.out.println("Registrado: " + e);
                    break;
                case 2: imprimir(empleados.listar()); break;
                case 3:
                    System.out.println(empleados.buscarPorDocumento(in.pedir("Documento", Validador::cedula))
                            .map(Empleado::resumen).orElse("No encontrado."));
                    break;
                default: System.out.println("Opcion invalida.");
            }
        } catch (CrediYaException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
    }

    // ---------- Clientes ----------
    private static void menuClientes() {
        System.out.println("\n-- CLIENTES --\n1. Registrar\n2. Listar\n3. Ver prestamos de un cliente");
        try {
            switch (in.entero("Opcion")) {
                case 1:
                    System.out.println("(escribe 'cancelar' en cualquier campo para salir)");
                    String nom = in.pedir("Nombre", v -> Validador.nombre(v, "nombre"));
                    String doc = in.pedir("Documento (6 a 10 digitos)", Validador::cedula);
                    String mail = in.pedir("Correo", Validador::correo);
                    String tel = in.pedir("Celular (10 digitos, empieza por 3)", Validador::celular);
                    Cliente c = clientes.registrar(nom, doc, mail, tel);
                    System.out.println("Registrado: " + c);
                    break;
                case 2: imprimir(clientes.listar()); break;
                case 3:
                    int id = in.pedirEntero("Id del cliente", v -> Validador.id(v, "id del cliente"));
                    Cliente cli = clientes.buscarPorId(id)
                            .orElseThrow(() -> new CrediYaException("No existe el cliente " + id));
                    System.out.println("Prestamos de " + cli.getNombre() + ":");
                    List<Prestamo> lista = prestamos.porCliente(id);
                    if (lista.isEmpty()) System.out.println("  (sin prestamos)");
                    lista.forEach(p -> System.out.println("  " + linea(p)));
                    break;
                default: System.out.println("Opcion invalida.");
            }
        } catch (CrediYaException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
    }

    // ---------- Prestamos ----------
    private static void menuPrestamos() {
        System.out.println("\n-- GESTOR DE PRESTAMOS --\n1. Crear\n2. Listar\n3. Cambiar estado\n4. Mostrar Prestamos Con Estado Activo\n5. Mostrar Estado Prestamo");
        try {
            switch (in.entero("Opcion")) {
                case 1:
                    System.out.println("(escribe 'cancelar' en cualquier campo para salir)");
                    int cid = in.pedirEntero("Id cliente", v -> Validador.id(v, "id del cliente"));
                    int eid = in.pedirEntero("Id empleado", v -> Validador.id(v, "id del empleado"));
                    double monto = in.pedirDinero("Monto", Validador::monto);
                    double interes = in.pedirDinero("Interes total (%)", Validador::interes);
                    int cuotas = in.pedirEntero("Numero de cuotas (1 a " + Validador.CUOTAS_MAX + ")", Validador::cuotas);
                    Prestamo p = prestamos.crear(cid, eid, monto, interes, cuotas);
                    System.out.println("Prestamo registrado correctamente:\n  " + linea(p));
                    break;
                case 2: prestamos.listar().forEach(x -> System.out.println(linea(x))); break;
                case 3:
                    int id = in.pedirEntero("Id del prestamo", v -> Validador.id(v, "id del prestamo"));
                    String est = in.pedir("Nuevo estado (PENDIENTE/PAGADO)", v -> {
                        String u = Validador.texto(v, "estado").toUpperCase();
                        if (!u.equals("PENDIENTE") && !u.equals("PAGADO"))
                            throw new CrediYaException("Estado invalido. Usa PENDIENTE o PAGADO.");
                        return u;
                    });
                    prestamos.cambiarEstado(id, EstadoPrestamo.valueOf(est));
                    System.out.println("Estado actualizado.");
                    break;
                case 4: reportes.prestamosActivos().forEach(x -> System.out.println(linea(x))); break;
                case 5:
                    int idp = in.pedirEntero("Id del prestamo", v -> Validador.id(v, "id del prestamo"));
                    p = prestamos.buscarPorId(idp)
                            .orElseThrow(() -> new CrediYaException("No existe el prestamo " + idp));
                    System.out.println("ID " + p.getId() + " Estado " + p.getEstado());
                        return;
                default: System.out.println("Opcion invalida.");
            }
        } catch (CrediYaException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
    }

    // ---------- Pagos ----------
    private static void menuPagos() {
        System.out.println("\n-- PAGOS --\n1. Registrar abono\n2. Historico de un prestamo");
        try {
            switch (in.entero("Opcion")) {
                case 1:
                    int id = in.pedirEntero("Id del prestamo", v -> Validador.id(v, "id del prestamo"));
                    double abono = in.pedirDinero("Monto del abono", v -> { Validador.positivo(v, "monto del abono"); return v; });
                    Pago pago = pagos.registrarAbono(id, abono);
                    Prestamo p = prestamos.buscarPorId(id).get();
                    System.out.printf(Locale.US, "Abono #%d registrado. Saldo pendiente: $%,.2f | Estado: %s%n",
                            pago.getId(), pagos.saldoPendiente(p), p.getEstado());
                    break;
                case 2:
                    int pid = in.pedirEntero("Id del prestamo", v -> Validador.id(v, "id del prestamo"));
                    List<Pago> h = pagos.historial(pid);
                    if (h.isEmpty()) System.out.println("Sin pagos registrados.");
                    h.forEach(x -> System.out.printf(Locale.US, "  #%d | %s | $%,.2f%n",
                            x.getId(), x.getFecha(), x.getMonto()));
                    System.out.printf(Locale.US, "Total abonado: $%,.2f%n", pagos.totalPagado(pid));
                    break;
                default: System.out.println("Opcion invalida.");
            }
        } catch (CrediYaException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
    }

    // ---------- Reportes ----------
    private static void menuReportes() {
        System.out.println("\n-- REPORTES --\n1. Prestamos activos\n2. Prestamos vencidos\n3. Clientes morosos"
                + "\n4. Cartera pendiente total\n5. Deuda por cliente\n6. Prestamos por empleado"
                + "\n7. Prestamos con monto mayor a...");
        try {
            switch (in.entero("Opcion")) {
                case 1: reportes.prestamosActivos().forEach(x -> System.out.println(linea(x))); break;
                case 2: reportes.prestamosVencidos().forEach(x -> System.out.println(linea(x))); break;
                case 3: imprimir(reportes.clientesMorosos()); break;
                case 4: System.out.printf(Locale.US, "Cartera pendiente: $%,.2f%n", reportes.carteraPendiente()); break;
                case 5:
                    reportes.deudaPorCliente().forEach((k, v) ->
                            System.out.printf(Locale.US, "  %-25s $%,.2f%n", k, v));
                    break;
                case 6:
                    for (Map.Entry<String, Long> e : reportes.prestamosPorEmpleado().entrySet())
                        System.out.println("  " + e.getKey() + ": " + e.getValue());
                    break;
                case 7:
                    reportes.prestamosMayoresA(in.pedirDinero("Monto minimo", v -> { Validador.positivo(v, "monto minimo"); return v; })).forEach(x -> System.out.println(linea(x)));
                    break;
                default: System.out.println("Opcion invalida.");
            }
        } catch (CrediYaException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
    }

    // ---------- Helpers ----------
    private static <T> void imprimir(List<T> lista) {
        if (lista.isEmpty()) System.out.println("(sin resultados)");
        lista.forEach(System.out::println);
    }

    private static String linea(Prestamo p) {
        String cli = clientes.buscarPorId(p.getClienteId()).map(Cliente::getNombre).orElse("?");
        return String.format(Locale.US,
                "[%d] %s | Capital: $%,.2f | Int: %.1f%% | Total: $%,.2f | Cuota: $%,.2f x %d | Inicio: %s | Vence: %s | Saldo: $%,.2f | %s",
                p.getId(), cli, p.getMonto(), p.getInteres(), p.getMontoTotal(), p.getValorCuota(),
                p.getCuotas(), p.getFechaInicio(), p.getFechaVencimiento(), pagos.saldoPendiente(p), p.getEstado());
    }
}
