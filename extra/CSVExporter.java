package extra;

import repo.PersonaRepository;
import models.Persona;

import java.util.List;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CSVExporter {
    private final Path rutaArchivo;

    public CSVExporter(Path rutaArchivo) {
        if (rutaArchivo == null) {
            throw new IllegalArgumentException("La ruta del archivo no puede ser nula.");
        }

        this.rutaArchivo = rutaArchivo;
    }

    public Path getPath() {
        return this.rutaArchivo;
    }

    public void exportar(PersonaRepository repo) throws IOException {
        List<Persona> datasource = repo.listar(new ListQuery()); // Obtenemos la lista completa de personas sin filtros

        // Crear los directorios padres si no existen (ej: la carpeta ./files)
        if (rutaArchivo.getParent() != null) {
            Files.createDirectories(rutaArchivo.getParent());
        }

        // Abrimos el escritor de archivos utilizando un bloque try-with-resources para asegurar el cierre
        try (BufferedWriter writer = Files.newBufferedWriter(rutaArchivo)) {
            // 1. Escribir la cabecera del CSV (Usamos punto y coma ';' para máxima compatibilidad con Excel)
            writer.write("ID;DNI;Nombre;Apellido;Extranjero;FamilyOwner;CargadoCaritas;Descripcion;" +
                         "Direccion;Localidad;CantidadFamiliares");
            writer.newLine();

            // 2. Recorrer y escribir cada registro
            for (Persona p : datasource) {
                // Obtener datos de ubicación de forma segura
                String direccion = (p.ubicacion() != null) ? p.ubicacion().direccion() : "";
                String localidad = (p.ubicacion() != null) ? p.ubicacion().localidad() : "";
                
                // Obtener cantidad de familiares de forma segura
                int cantFamiliares = (p.grupoFamiliar() != null) ? p.grupoFamiliar().cantidad() : 0;

                // Limpiar textos por si contienen caracteres que rompan el CSV (comas o puntos y comas)
                String descLimpia = (p.descripcion() != null) ? p.descripcion().replace(";", " ") : "";
                String dirLimpia = direccion.replace(";", " ");
                String locLimpia = localidad.replace(";", " ");

                // Construir la fila
                String fila = String.format("%d;%s;%s;%s;%b;%b;%b;%s;%s;%s;%d",
                    p.id(),
                    p.dni(),
                    p.nombre(),
                    p.apellido(),
                    p.extranjero(),
                    p.familyOwner(),
                    p.cargadoEnCaritas(),
                    descLimpia,
                    dirLimpia,
                    locLimpia,
                    cantFamiliares
                );

                writer.write(fila);
                writer.newLine();
            }
        }        
    }
}
