package com.crediya.model;

import java.time.LocalDate;

public class Pago implements Entidad {
    private int id;
    private int prestamoId;
    private LocalDate fecha;
    private double monto;

    public Pago(int id, int prestamoId, LocalDate fecha, double monto) {
        this.id = id;
        this.prestamoId = prestamoId;
        this.fecha = fecha;
        this.monto = monto;
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    public int getPrestamoId() { return prestamoId; }
    public LocalDate getFecha() { return fecha; }
    public double getMonto() { return monto; }
}
