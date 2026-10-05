package com.crediya.repository;

import com.crediya.model.EstadoPrestamo;
import com.crediya.model.Prestamo;
import java.time.LocalDate;

public class PrestamoArchivoRepo extends ArchivoRepositorio<Prestamo> {
    public PrestamoArchivoRepo() { super("prestamos.txt"); }

    @Override
    protected String aLinea(Prestamo p) {
        return String.join(";", String.valueOf(p.getId()), String.valueOf(p.getClienteId()),
                String.valueOf(p.getEmpleadoId()), String.valueOf(p.getMonto()), String.valueOf(p.getInteres()),
                String.valueOf(p.getCuotas()), p.getFechaInicio().toString(), p.getEstado().name());
    }

    @Override
    protected Prestamo deLinea(String[] c) {
        return new Prestamo(Integer.parseInt(c[0]), Integer.parseInt(c[1]), Integer.parseInt(c[2]),
                Double.parseDouble(c[3]), Double.parseDouble(c[4]), Integer.parseInt(c[5]),
                LocalDate.parse(c[6]), EstadoPrestamo.valueOf(c[7]));
    }
}
