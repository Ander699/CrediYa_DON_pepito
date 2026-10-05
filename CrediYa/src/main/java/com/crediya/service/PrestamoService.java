package com.crediya.service;

import com.crediya.exception.CrediYaException;
import com.crediya.model.EstadoPrestamo;
import com.crediya.model.Prestamo;
import com.crediya.repository.Repositorio;
import com.crediya.util.Validador;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PrestamoService {
    private final Repositorio<Prestamo> repo;
    private final ClienteService clientes;
    private final EmpleadoService empleados;

    public PrestamoService(Repositorio<Prestamo> repo, ClienteService clientes, EmpleadoService empleados) {
        this.repo = repo;
        this.clientes = clientes;
        this.empleados = empleados;
    }

    public Prestamo crear(int clienteId, int empleadoId, double monto, double interes, int cuotas) {
        Validador.id(clienteId, "id del cliente");
        Validador.id(empleadoId, "id del empleado");
        clientes.buscarPorId(clienteId)
                .orElseThrow(() -> new CrediYaException("No existe el cliente con id " + clienteId));
        empleados.buscarPorId(empleadoId)
                .orElseThrow(() -> new CrediYaException("No existe el empleado con id " + empleadoId));
        Validador.monto(monto);
        Validador.interes(interes);
        Validador.cuotas(cuotas);

        Prestamo p = new Prestamo(0, clienteId, empleadoId, Prestamo.redondear(monto), interes, cuotas,
                LocalDate.now(), EstadoPrestamo.PENDIENTE);
        repo.guardar(p);
        return p;
    }

    public void cambiarEstado(int prestamoId, EstadoPrestamo nuevo) {
        Prestamo p = buscarPorId(prestamoId)
                .orElseThrow(() -> new CrediYaException("No existe el prestamo con id " + prestamoId));
        p.setEstado(nuevo);
        repo.actualizar(p);
    }

    public List<Prestamo> listar() { return repo.listar(); }

    public Optional<Prestamo> buscarPorId(int id) { return repo.buscarPorId(id); }

    public List<Prestamo> porCliente(int clienteId) {
        return repo.listar().stream().filter(p -> p.getClienteId() == clienteId).collect(Collectors.toList());
    }
}
