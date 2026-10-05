package com.crediya.model;

import java.util.Locale;

public class Empleado extends Persona {
    private String rol;
    private double salario;

    public Empleado(int id, String nombre, String documento, String rol, String correo, double salario) {
        super(id, nombre, documento, correo);
        this.rol = rol;
        this.salario = salario;
    }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }

    @Override
    public String resumen() {
        return String.format(Locale.US, "[%d] %s | Doc: %s | Rol: %s | %s | Salario: $%,.2f",
                getId(), getNombre(), getDocumento(), rol, getCorreo(), salario);
    }
}
