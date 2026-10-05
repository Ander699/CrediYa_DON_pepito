package com.crediya.service;

import com.crediya.exception.CrediYaException;
import com.crediya.model.Cliente;
import com.crediya.repository.Repositorio;
import com.crediya.util.Validador;
import java.util.List;
import java.util.Optional;

public class ClienteService {
    private final Repositorio<Cliente> repo;

    public ClienteService(Repositorio<Cliente> repo) { this.repo = repo; }

    public Cliente registrar(String nombre, String documento, String correo, String telefono) {
        String nom = Validador.nombre(nombre, "nombre");
        String doc = Validador.cedula(documento);
        String mail = Validador.correo(correo);
        String tel = Validador.celular(telefono);
        if (buscarPorDocumento(doc).isPresent())
            throw new CrediYaException("Ya existe un cliente con el documento " + doc + ".");
        if (repo.listar().stream().anyMatch(c -> mail.equalsIgnoreCase(c.getCorreo())))
            throw new CrediYaException("Ya existe un cliente con el correo " + mail + ".");
        Cliente c = new Cliente(0, nom, doc, mail, tel);
        repo.guardar(c);
        return c;
    }

    public List<Cliente> listar() { return repo.listar(); }

    public Optional<Cliente> buscarPorId(int id) { return repo.buscarPorId(id); }

    /** Busca ignorando puntos y espacios: "1.234.567" encuentra "1234567". */
    public Optional<Cliente> buscarPorDocumento(String documento) {
        String buscado = Validador.normalizarDocumento(documento);
        return repo.listar().stream()
                .filter(c -> Validador.normalizarDocumento(c.getDocumento()).equals(buscado)).findFirst();
    }
}
