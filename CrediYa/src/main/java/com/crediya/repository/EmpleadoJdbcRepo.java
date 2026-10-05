package com.crediya.repository;

import com.crediya.model.Empleado;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmpleadoJdbcRepo extends JdbcRepositorio<Empleado> {
    @Override protected String tabla() { return "empleados"; }
    @Override protected String[] columnas() { return new String[]{"nombre", "documento", "rol", "correo", "salario"}; }

    @Override
    protected void llenar(PreparedStatement ps, Empleado e, int i) throws SQLException {
        ps.setString(i++, e.getNombre());
        ps.setString(i++, e.getDocumento());
        ps.setString(i++, e.getRol());
        ps.setString(i++, e.getCorreo());
        ps.setDouble(i, e.getSalario());
    }

    @Override
    protected Empleado mapear(ResultSet rs) throws SQLException {
        return new Empleado(rs.getInt("id"), rs.getString("nombre"), rs.getString("documento"),
                rs.getString("rol"), rs.getString("correo"), rs.getDouble("salario"));
    }
}
