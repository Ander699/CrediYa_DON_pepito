package com.crediya.repository;

import com.crediya.model.Cliente;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClienteJdbcRepo extends JdbcRepositorio<Cliente> {
    @Override protected String tabla() { return "clientes"; }
    @Override protected String[] columnas() { return new String[]{"nombre", "documento", "correo", "telefono"}; }

    @Override
    protected void llenar(PreparedStatement ps, Cliente c, int i) throws SQLException {
        ps.setString(i++, c.getNombre());
        ps.setString(i++, c.getDocumento());
        ps.setString(i++, c.getCorreo());
        ps.setString(i, c.getTelefono());
    }

    @Override
    protected Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(rs.getInt("id"), rs.getString("nombre"), rs.getString("documento"),
                rs.getString("correo"), rs.getString("telefono"));
    }
}
