package com.crediya.model;

import java.time.LocalDate;

public class Prestamo implements Entidad {
    private int id;
    private int clienteId;
    private int empleadoId;
    private double monto;      // capital prestado
    private double interes;    // porcentaje total sobre el capital (ej: 10 = 10%)
    private int cuotas;        // numero de cuotas mensuales
    private LocalDate fechaInicio;
    private EstadoPrestamo estado;

    public Prestamo(int id, int clienteId, int empleadoId, double monto, double interes,
                    int cuotas, LocalDate fechaInicio, EstadoPrestamo estado) {
        this.id = id;
        this.clienteId = clienteId;
        this.empleadoId = empleadoId;
        this.monto = monto;
        this.interes = interes;
        this.cuotas = cuotas;
        this.fechaInicio = fechaInicio;
        this.estado = estado;
    }

    public static double redondear(double v) { return Math.round(v * 100.0) / 100.0; }

    /** Monto total = capital + interes. */
    public double getMontoTotal() { return redondear(monto * (1 + interes / 100.0)); }

    /** Valor de cada cuota mensual. */
    public double getValorCuota() { return redondear(getMontoTotal() / cuotas); }

    public LocalDate getFechaVencimiento() { return fechaInicio.plusMonths(cuotas); }

    public boolean estaVencido(LocalDate hoy) {
        return estado == EstadoPrestamo.PENDIENTE && hoy.isAfter(getFechaVencimiento());
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public int getEmpleadoId() { return empleadoId; }
    public double getMonto() { return monto; }
    public double getInteres() { return interes; }
    public int getCuotas() { return cuotas; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public EstadoPrestamo getEstado() { return estado; }
    public void setEstado(EstadoPrestamo estado) { this.estado = estado; }
}
