package com.crediya.repository;

import com.crediya.model.Cliente;

public class ClienteArchivoRepo extends ArchivoRepositorio<Cliente> {
    public ClienteArchivoRepo() { super("clientes.txt"); }

    @Override
    protected String aLinea(Cliente c) {
        return String.join(";", String.valueOf(c.getId()), limpio(c.getNombre()), limpio(c.getDocumento()),
                limpio(c.getCorreo()), limpio(c.getTelefono()));
    }

    @Override
    protected Cliente deLinea(String[] c) {
        return new Cliente(Integer.parseInt(c[0]), c[1], c[2], c[3], c[4]);
    }
}
