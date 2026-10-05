package com.crediya.repository;

import com.crediya.exception.CrediYaException;
import com.crediya.model.Entidad;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ArchivoRepositorio<T extends Entidad> implements Repositorio<T> {
    private final Path ruta;

    protected ArchivoRepositorio(String nombreArchivo) {
        this.ruta = Paths.get("datos", nombreArchivo);
        try {
            Files.createDirectories(ruta.getParent());
            if (Files.notExists(ruta)) Files.createFile(ruta);
        } catch (IOException e) {
            throw new CrediYaException("No se pudo crear el archivo " + ruta, e);
        }
    }

    protected abstract String aLinea(T entidad);

    protected abstract T deLinea(String[] campos);

    protected static String limpio(String s) {
        return s == null ? "" : s.replace(";", ",").replace("\n", " ");
    }

    @Override
    public synchronized List<T> listar() {
        try {
            return Files.readAllLines(ruta, StandardCharsets.UTF_8).stream()
                    .filter(l -> !l.isBlank())
                    .map(l -> deLinea(l.split(";", -1)))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new CrediYaException("Error leyendo " + ruta, e);
        }
    }

    @Override
    public synchronized void guardar(T entidad) {
        if (entidad.getId() == 0) {
            int max = listar().stream().mapToInt(Entidad::getId).max().orElse(0);
            entidad.setId(max + 1);
        }
        try {
            Files.writeString(ruta, aLinea(entidad) + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new CrediYaException("Error escribiendo " + ruta, e);
        }
    }

    @Override
    public synchronized void actualizar(T entidad) {
        List<String> lineas = listar().stream()
                .map(e -> e.getId() == entidad.getId() ? aLinea(entidad) : aLinea(e))
                .collect(Collectors.toList());
        try {
            Files.write(ruta, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CrediYaException("Error actualizando " + ruta, e);
        }
    }
}
