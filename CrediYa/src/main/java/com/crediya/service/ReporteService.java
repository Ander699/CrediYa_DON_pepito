package com.crediya.service;

import com.crediya.model.Cliente;
import com.crediya.model.EstadoPrestamo;
import com.crediya.model.Prestamo;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ReporteService {
    private final GestorPrestamos prestamos;
    private final ClienteService clientes;
    private final EmpleadoService empleados;
    private final PagoService pagos;

    public ReporteService(GestorPrestamos prestamos, ClienteService clientes,
                          EmpleadoService empleados, PagoService pagos) {
        this.prestamos = prestamos;
        this.clientes = clientes;
        this.empleados = empleados;
        this.pagos = pagos;
    }

    public List<Prestamo> prestamosActivos() {
        return prestamos.listar().stream()
                .filter(p -> p.getEstado() == EstadoPrestamo.PENDIENTE)
                .collect(Collectors.toList());
    }

    public List<Prestamo> prestamosVencidos() {
        LocalDate hoy = LocalDate.now();
        return prestamos.listar().stream()
                .filter(p -> p.estaVencido(hoy))
                .collect(Collectors.toList());
    }

    /** Clientes con al menos un prestamo vencido sin pagar. */
    public List<Cliente> clientesMorosos() {
        Set<Integer> ids = prestamosVencidos().stream()
                .map(Prestamo::getClienteId).collect(Collectors.toSet());
        return clientes.listar().stream()
                .filter(c -> ids.contains(c.getId()))
                .collect(Collectors.toList());
    }

    public double carteraPendiente() {
        return prestamosActivos().stream().mapToDouble(pagos::saldoPendiente).sum();
    }

    /** Deuda pendiente agrupada por cliente, de mayor a menor. */
    public Map<String, Double> deudaPorCliente() {
        Map<Integer, String> nombres = clientes.listar().stream()
                .collect(Collectors.toMap(Cliente::getId, Cliente::getNombre));
        return prestamosActivos().stream()
                .collect(Collectors.groupingBy(
                        p -> nombres.getOrDefault(p.getClienteId(), "Cliente " + p.getClienteId()),
                        Collectors.summingDouble(pagos::saldoPendiente)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, java.util.LinkedHashMap::new));
    }

    public Map<String, Long> prestamosPorEmpleado() {
        Map<Integer, String> nombres = empleados.listar().stream()
                .collect(Collectors.toMap(e -> e.getId(), e -> e.getNombre()));
        return prestamos.listar().stream()
                .collect(Collectors.groupingBy(
                        p -> nombres.getOrDefault(p.getEmpleadoId(), "Empleado " + p.getEmpleadoId()),
                        Collectors.counting()));
    }

    public List<Prestamo> prestamosMayoresA(double monto) {
        return prestamos.listar().stream()
                .filter(p -> p.getMonto() > monto)
                .sorted((a, b) -> Double.compare(b.getMonto(), a.getMonto()))
                .collect(Collectors.toList());
    }
}
