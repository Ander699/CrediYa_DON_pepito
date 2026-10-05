package com.crediya.repository;

import com.crediya.model.Entidad;
import java.util.List;
import java.util.Optional;

public interface Repositorio<T extends Entidad> {
    void guardar(T entidad);

    void actualizar(T entidad);

    List<T> listar();

    default Optional<T> buscarPorId(int id) {
        return listar().stream().filter(e -> e.getId() == id).findFirst();
    }
}
