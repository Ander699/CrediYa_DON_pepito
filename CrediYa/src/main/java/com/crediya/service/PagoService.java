package com.crediya.service;

import com.crediya.exception.CrediYaException;
import com.crediya.model.EstadoPrestamo;
import com.crediya.model.Pago;
import com.crediya.model.Prestamo;
import com.crediya.repository.Repositorio;
import com.crediya.util.Validador;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PagoService {
    private final Repositorio<Pago> repo;
    private final GestorPrestamos prestamos;

    public PagoService(Repositorio<Pago> repo, GestorPrestamos prestamos) {
        this.repo = repo;
        this.prestamos = prestamos;
    }

    /** Registra un abono, y si el saldo llega a cero marca el prestamo como PAGADO. */
    public Pago registrarAbono(int prestamoId, double monto) {
        Prestamo p = prestamos.buscarPorId(prestamoId)
                .orElseThrow(() -> new CrediYaException("No existe el prestamo con id " + prestamoId));
        if (p.getEstado() == EstadoPrestamo.PAGADO)
            throw new CrediYaException("El prestamo ya esta pagado.");
        Validador.positivo(monto, "monto del abono");
        if (Prestamo.redondear(monto) <= 0)
            throw new CrediYaException("El abono es demasiado pequeno (minimo $0.01).");

        double saldo = saldoPendiente(p);
        if (monto > saldo + 0.001)
            throw new CrediYaException(String.format("El abono supera el saldo pendiente ($%,.2f).", saldo));

        Pago pago = new Pago(0, prestamoId, LocalDate.now(), Prestamo.redondear(monto));
        repo.guardar(pago);

        if (saldoPendiente(p) <= 0.009) prestamos.cambiarEstado(prestamoId, EstadoPrestamo.PAGADO);
        return pago;
    }

    public double totalPagado(int prestamoId) {
        return repo.listar().stream().filter(x -> x.getPrestamoId() == prestamoId)
                .mapToDouble(Pago::getMonto).sum();
    }

    public double saldoPendiente(Prestamo p) {
        if (p.getEstado() == EstadoPrestamo.PAGADO) return 0;
        return Math.max(0, Prestamo.redondear(p.getMontoTotal() - totalPagado(p.getId())));
    }

    public List<Pago> historial(int prestamoId) {
        return repo.listar().stream().filter(x -> x.getPrestamoId() == prestamoId)
                .sorted(Comparator.comparing(Pago::getFecha).thenComparingInt(Pago::getId))
                .collect(Collectors.toList());
    }

    public List<Pago> listar() { return repo.listar(); }
}
