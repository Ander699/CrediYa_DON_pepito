package com.crediya.repository;

import com.crediya.model.Pago;
import java.time.LocalDate;

public class PagoArchivoRepo extends ArchivoRepositorio<Pago> {
    public PagoArchivoRepo() { super("pagos.txt"); }

    @Override
    protected String aLinea(Pago p) {
        return String.join(";", String.valueOf(p.getId()), String.valueOf(p.getPrestamoId()),
                p.getFecha().toString(), String.valueOf(p.getMonto()));
    }

    @Override
    protected Pago deLinea(String[] c) {
        return new Pago(Integer.parseInt(c[0]), Integer.parseInt(c[1]), LocalDate.parse(c[2]),
                Double.parseDouble(c[3]));
    }
}
