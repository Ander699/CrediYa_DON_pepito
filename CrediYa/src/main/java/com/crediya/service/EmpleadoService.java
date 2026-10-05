package com.crediya.service;

import com.crediya.exception.CrediYaException;
import com.crediya.model.Empleado;
import com.crediya.model.Prestamo;
import com.crediya.repository.Repositorio;
import com.crediya.util.Validador;
import java.util.List;
import java.util.Optional;

public class EmpleadoService {
    private final Repositorio<Empleado> repo;

    public EmpleadoService(Repositorio<Empleado> repo) { this.repo = repo; }

    public Empleado registrar(String nombre, String documento, String rol, String correo, double salario) {
        String nom = Validador.nombre(nombre, "nombre");
        String doc = Validador.cedula(documento);
        String rolOk = Validador.rol(rol);
        String mail = Validador.correo(correo);
        Validador.salario(salario);
        if (buscarPorDocumento(doc).isPresent())
            throw new CrediYaException("Ya existe un empleado con el documento " + doc + ".");
        if (repo.listar().stream().anyMatch(e -> mail.equalsIgnoreCase(e.getCorreo())))
            throw new CrediYaException("Ya existe un empleado con el correo " + mail + ".");
        Empleado e = new Empleado(0, nom, doc, rolOk, mail, Prestamo.redondear(salario));
        repo.guardar(e);
        return e;
    }

    public List<Empleado> listar() { return repo.listar(); }

    public Optional<Empleado> buscarPorId(int id) { return repo.buscarPorId(id); }

    /** Busca ignorando puntos y espacios: "1.234.567" encuentra "1234567". */
    public Optional<Empleado> buscarPorDocumento(String documento) {
        String buscado = Validador.normalizarDocumento(documento);
        return repo.listar().stream()
                .filter(e -> Validador.normalizarDocumento(e.getDocumento()).equals(buscado)).findFirst();
    }
}
