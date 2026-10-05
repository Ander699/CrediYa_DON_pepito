package com.crediya.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Patron Singleton: una unica configuracion de conexion para toda la aplicacion. */
public final class ConexionDB {
    private static ConexionDB instancia;
    private final String url;
    private final String user;
    private final String password;
    private boolean disponible;

    private ConexionDB() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("config.properties")) {
            p.load(in);
        } catch (IOException e) {
            System.out.println("[AVISO] No se encontro config.properties, se usan valores por defecto.");
        }
        this.url = p.getProperty("db.url", "jdbc:mysql://localhost:3306/crediya_db?serverTimezone=UTC");
        this.user = p.getProperty("db.user", "root");
        this.password = p.getProperty("db.password", "");
    }

    public static synchronized ConexionDB getInstancia() {
        if (instancia == null) instancia = new ConexionDB();
        return instancia;
    }

    public Connection obtener() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /** Prueba la conexion y guarda el resultado. */
    public boolean probar() {
        try (Connection c = obtener()) {
            disponible = true;
        } catch (SQLException e) {
            disponible = false;
            System.out.println("[AVISO] MySQL no disponible (" + e.getMessage() + "). Se trabaja solo con archivos.");
        }
        return disponible;
    }

    public boolean isDisponible() { return disponible; }
}
