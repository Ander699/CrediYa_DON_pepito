package com.crediya.repository;

import com.crediya.exception.CrediYaException;
import com.crediya.model.Entidad;
import com.crediya.util.ConexionDB;
import java.util.List;

public class RepositorioDual<T extends Entidad> implements Repositorio<T> {
    private final Repositorio<T> archivo;
    private final Repositorio<T> bd;

    public RepositorioDual(Repositorio<T> archivo, Repositorio<T> bd) {
        this.archivo = archivo;
        this.bd = bd;
    }

    private boolean usarBd() { return ConexionDB.getInstancia().isDisponible(); }

    private void aviso(CrediYaException e) {
        System.out.println("[AVISO] " + e.getMessage() + " -> se usa el archivo.");
    }

    @Override
    public void guardar(T entidad) {
        if (usarBd()) {
            try { bd.guardar(entidad); } catch (CrediYaException e) { aviso(e); }
        }
        archivo.guardar(entidad); // si la BD asigno el id, el archivo lo respeta
    }

    @Override
    public void actualizar(T entidad) {
        if (usarBd()) {
            try { bd.actualizar(entidad); } catch (CrediYaException e) { aviso(e); }
        }
        archivo.actualizar(entidad);
    }

    @Override
    public List<T> listar() {
        if (usarBd()) {
            try { return bd.listar(); } catch (CrediYaException e) { aviso(e); }
        }
        return archivo.listar();
    }
}
