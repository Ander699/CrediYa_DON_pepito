package com.crediya.model;

public class Cliente extends Persona {
    private String telefono;

    public Cliente(int id, String nombre, String documento, String correo, String telefono) {
        super(id, nombre, documento, correo);
        this.telefono = telefono;
    }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    @Override
    public String resumen() {
        return String.format("[%d] %s | Doc: %s | %s | Tel: %s",
                getId(), getNombre(), getDocumento(), getCorreo(), telefono);
    }
}
