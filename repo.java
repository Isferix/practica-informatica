import models.Persona;
import extra.ListQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
/**
 * Interfaz que define las operaciones permitidas para el repositorio.
 */
interface PersonaRepository {
    void agregar(Persona persona);
    boolean eliminar(int id);
    int cantidad();
    List<Persona> listar(ListQuery query);
    Optional<Persona> buscarPorId(int id); // Útil para saber si existe antes de eliminar
    Optional<Persona> buscarPorDni(String dni); // Para futuras búsquedas por DNI
    void exportarCSV(Path rutaArchivo) throws IOException;
}

/**
 * Implementación InMemory utilizando una lista mutable interna.
 */
class InMemoryPersonaRepository implements PersonaRepository {
    
    // Almacenamiento en memoria usando ArrayList
    private final List<Persona> datasource = new ArrayList<>();

    private boolean aplicarFiltro(Persona persona, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }

        // 1. Separar la parte de "Apellido-Nombre" y la parte de "DNI" (si existe el ':')
        String parteNombres;
        String filtroDni = "*"; // Por defecto asterisco significa que no aplica

        if (filter.contains(":")) {
            String[] partesColonChange = filter.split(":", 2);
            parteNombres = partesColonChange[0].trim();
            String dniRaw = partesColonChange[1].trim();
            
            // Validar que el DNI contenga solo letras y números (alfanumérico) u omitirse con '*'
            if (!dniRaw.equals("*") && !dniRaw.matches("^[a-zA-Z0-String0-9]+$")) {
                return false; // Si el DNI tiene caracteres inválidos (puntos, guiones, comas), no machea
            }
            filtroDni = dniRaw;
        } else {
            parteNombres = filter.trim();
        }

        // 2. Separar "Apellido" y "Nombre" usando el '-'
        String filtroApellido = "*";
        String filtroNombre = "*";

        if (parteNombres.contains("-")) {
            String[] partesGuion = parteNombres.split("-", 2);
            filtroApellido = partesGuion[0].trim();
            filtroNombre = partesGuion[1].trim();
        } else filtroApellido = parteNombres; // Si no viene el guión, asumimos que todo el bloque corresponde al apellido

        // 3. Validar reglas de caracteres para Apellido y Nombre (Solo letras y espacios)
        if (!filtroApellido.equals("*") && !filtroApellido.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) return false;
        if (!filtroNombre.equals("*") && !filtroNombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) return false;

        // 4. Evaluar las condiciones "Empieza con" (Ignorando mayúsculas/minúsculas para mejor UX)
        
        // Condición Apellido
        if (!filtroApellido.equals("*")) {
            if (persona.apellido() == null || !persona.apellido().toLowerCase().startsWith(filtroApellido.toLowerCase())) return false;
        }

        // Condición Nombre
        if (!filtroNombre.equals("*")) {
            if (persona.nombre() == null || !persona.nombre().toLowerCase().startsWith(filtroNombre.toLowerCase())) return false;
        }

        // Condición DNI
        if (!filtroDni.equals("*")) {
            if (persona.dni() == null || !persona.dni().toLowerCase().startsWith(filtroDni.toLowerCase())) return false;
        }

        // Si superó todos los filtros activos, la persona es válida
        return true;
    }

    @Override
    public void agregar(Persona persona) {
        if (persona == null) return;
        // Opcional: Validar si ya existe el DNI para evitar duplicados
        buscarPorDni(persona.dni()).ifPresent(p -> {throw new IllegalArgumentException("Ya existe una persona con el DNI: " + persona.dni());});
        datasource.add(persona);
    }

    
    @Override
    public boolean eliminar(int id) {return datasource.removeIf(persona -> persona.id() == id);} // removeIf devuelve true si encontró y eliminó el elemento

    @Override
    public int cantidad() {return datasource.size();}

    @Override
    public List<Persona> listar(ListQuery query) {
        if (query == null) return new ArrayList<>(datasource); // Devuelve todo si la query es nula
        return datasource.stream()
            // 1. Filtro por String (Por ahora pasa directo, luego me dices cómo interpretarlo)
            .filter(persona -> aplicarFiltro(persona, query.searchFilter()))
            // 2. Aplicar Offset (Saltar N elementos)
            .skip(Math.max(0, query.offset()))
            // 3. Aplicar Limit (Tomar N elementos)
            .limit(query.limit() > 0 ? query.limit() : Long.MAX_VALUE)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Persona> buscarPorId(int id) {
        return datasource.stream()
            .filter(persona -> persona.id() == id)
            .findFirst();
    }

    @Override
    public Optional<Persona> buscarPorDni(String dni) {
        if (dni == null || dni.isBlank()) {
            return Optional.empty();
        }
        return datasource.stream()
            .filter(persona -> dni.equals(persona.dni()))
            .findFirst();
    }

    @Override
    public void exportarCSV(Path rutaArchivo) throws IOException {
        if (rutaArchivo == null) {
            throw new IllegalArgumentException("La ruta del archivo no puede ser nula.");
        }

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