package com.crediya.repository;

import com.crediya.model.EstadoPrestamo;
import com.crediya.model.Prestamo;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PrestamoJdbcRepo extends JdbcRepositorio<Prestamo> {
    @Override protected String tabla() { return "prestamos"; }

    @Override
    protected String[] columnas() {
        return new String[]{"cliente_id", "empleado_id", "monto", "interes", "cuotas", "fecha_inicio", "estado"};
    }

    @Override
    protected void llenar(PreparedStatement ps, Prestamo p, int i) throws SQLException {
        ps.setInt(i++, p.getClienteId());
        ps.setInt(i++, p.getEmpleadoId());
        ps.setDouble(i++, p.getMonto());
        ps.setDouble(i++, p.getInteres());
        ps.setInt(i++, p.getCuotas());
        ps.setDate(i++, Date.valueOf(p.getFechaInicio()));
        ps.setString(i, p.getEstado().name());
    }

    @Override
    protected Prestamo mapear(ResultSet rs) throws SQLException {
        return new Prestamo(rs.getInt("id"), rs.getInt("cliente_id"), rs.getInt("empleado_id"),
                rs.getDouble("monto"), rs.getDouble("interes"), rs.getInt("cuotas"),
                rs.getDate("fecha_inicio").toLocalDate(), EstadoPrestamo.valueOf(rs.getString("estado")));
    }
}
