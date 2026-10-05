package com.crediya.repository;

import com.crediya.model.Pago;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PagoJdbcRepo extends JdbcRepositorio<Pago> {
    @Override protected String tabla() { return "pagos"; }
    @Override protected String[] columnas() { return new String[]{"prestamo_id", "fecha_pago", "monto"}; }

    @Override
    protected void llenar(PreparedStatement ps, Pago p, int i) throws SQLException {
        ps.setInt(i++, p.getPrestamoId());
        ps.setDate(i++, Date.valueOf(p.getFecha()));
        ps.setDouble(i, p.getMonto());
    }

    @Override
    protected Pago mapear(ResultSet rs) throws SQLException {
        return new Pago(rs.getInt("id"), rs.getInt("prestamo_id"), rs.getDate("fecha_pago").toLocalDate(),
                rs.getDouble("monto"));
    }
}
