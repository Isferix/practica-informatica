package repo;

import models.Persona;
import extra.ListQuery;

import java.util.List;
import java.util.Optional;

import java.nio.file.Path;
import java.io.IOException;

/**
 * Interfaz que define las operaciones permitidas para el repositorio.
 */
public interface PersonaRepository {
    void agregar(Persona persona);
    boolean eliminar(int id);
    int cantidad();
    List<Persona> listar(ListQuery query);
    Optional<Persona> buscarPorId(int id); // Útil para saber si existe antes de eliminar
    Optional<Persona> buscarPorDni(String dni); // Para futuras búsquedas por DNI
    void exportarCSV(Path rutaArchivo) throws IOException;
}