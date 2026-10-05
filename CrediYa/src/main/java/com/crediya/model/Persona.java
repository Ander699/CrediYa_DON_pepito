package com.crediya.model;

public abstract class Persona implements Entidad {
    private int id;
    private String nombre;
    private String documento;
    private String correo;

    protected Persona(int id, String nombre, String documento, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.correo = correo;
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    /** Polimorfismo: cada subclase describe sus datos a su manera. */
    public abstract String resumen();

    @Override public String toString() { return resumen(); }
}
