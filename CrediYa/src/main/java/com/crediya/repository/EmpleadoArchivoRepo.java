package com.crediya.repository;

import com.crediya.model.Empleado;

public class EmpleadoArchivoRepo extends ArchivoRepositorio<Empleado> {
    public EmpleadoArchivoRepo() { super("empleados.txt"); }

    @Override
    protected String aLinea(Empleado e) {
        return String.join(";", String.valueOf(e.getId()), limpio(e.getNombre()), limpio(e.getDocumento()),
                limpio(e.getRol()), limpio(e.getCorreo()), String.valueOf(e.getSalario()));
    }

    @Override
    protected Empleado deLinea(String[] c) {
        return new Empleado(Integer.parseInt(c[0]), c[1], c[2], c[3], c[4], Double.parseDouble(c[5]));
    }
}
