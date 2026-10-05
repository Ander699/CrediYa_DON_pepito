package com.crediya.repository;

import com.crediya.exception.CrediYaException;
import com.crediya.model.Entidad;
import com.crediya.util.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class JdbcRepositorio<T extends Entidad> implements Repositorio<T> {

    protected abstract String tabla();

    protected abstract String[] columnas();

    protected abstract void llenar(PreparedStatement ps, T entidad, int desde) throws SQLException;

    protected abstract T mapear(ResultSet rs) throws SQLException;

    @Override
    public void guardar(T entidad) {
        boolean conId = entidad.getId() > 0;
        String cols = (conId ? "id," : "") + String.join(",", columnas());
        String marcas = Stream.generate(() -> "?")
                .limit(columnas().length + (conId ? 1 : 0)).collect(Collectors.joining(","));
        String sql = "INSERT INTO " + tabla() + " (" + cols + ") VALUES (" + marcas + ")";
        try (Connection c = ConexionDB.getInstancia().obtener();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int i = 1;
            if (conId) ps.setInt(i++, entidad.getId());
            llenar(ps, entidad, i);
            ps.executeUpdate();
            if (!conId) {
                try (ResultSet k = ps.getGeneratedKeys()) {
                    if (k.next()) entidad.setId(k.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new CrediYaException("Error de BD al guardar en " + tabla() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(T entidad) {
        String set = Stream.of(columnas()).map(col -> col + "=?").collect(Collectors.joining(","));
        String sql = "UPDATE " + tabla() + " SET " + set + " WHERE id=?";
        try (Connection c = ConexionDB.getInstancia().obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            llenar(ps, entidad, 1);
            ps.setInt(columnas().length + 1, entidad.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrediYaException("Error de BD al actualizar " + tabla() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<T> listar() {
        List<T> lista = new ArrayList<>();
        try (Connection c = ConexionDB.getInstancia().obtener();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM " + tabla() + " ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            throw new CrediYaException("Error de BD al listar " + tabla() + ": " + e.getMessage(), e);
        }
        return lista;
    }
}
