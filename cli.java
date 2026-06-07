import extra.ListQuery;
import models.Persona;
import models.GrupoFamiliar;
import models.Ubicacion;
import models.FamiliarTipo;
import models.Familiar;
import models.FamiliarRelacion;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class cli {

    private final PersonaRepository repo;
    private final Scanner scanner;
    private Persona personaSeleccionada;
    private final Path rutaPredeterminadaCSV = Paths.get(".", "files", "personas.csv");

    public cli(PersonaRepository repo) {
        this.repo = repo;
        this.scanner = new Scanner(System.in);
        this.personaSeleccionada = null;
    }

    public void run() {
        boolean salir = false;
        System.out.println("=== Sistema de Gestión de Personas ===");

        while (!salir) {
            mostrarEstadoYMenu();
            String opcion = scanner.nextLine().trim();

            switch (opcion) {
                case "0" -> {
                    salir = true;
                    System.out.println("Saliendo del sistema... ¡Adiós!");
                }
                case "1" -> ejecutarExportarCSV();
                case "2" -> ejecutarAgregar();
                case "3" -> ejecutarEliminar();
                case "4" -> ejecutarListar();
                case "5" -> ejecutarBuscarPorId();
                case "6" -> ejecutarBuscarPorDni();
                case "7" -> ejecutarValidarDatos();
                case "8" -> ejecutarEsCabezaFamiliar();
                case "9" -> ejecutarGetFamiliaresAsociados();
                default -> System.out.println("Opción inválida. Intente nuevamente.");
            }
            System.out.println(); // Espacio estético
        }
    }

    
    private void mostrarEstadoYMenu() {
        System.out.println("------------------------------------------------");
        if (personaSeleccionada != null) {
            System.out.printf("CLIENTE SELECCIONADO: %s %s (ID: %d | DNI: %s)%n", 
                personaSeleccionada.nombre(), personaSeleccionada.apellido(), 
                personaSeleccionada.id(), personaSeleccionada.dni());
        } else {
            System.out.println("CLIENTE SELECCIONADO: [Ninguno]");
        }
        System.out.println("------------------------------------------------");
        System.out.println("0. Salir");
        System.out.println("1. Exportar a CSV");
        System.out.println("2. Agregar Persona");
        System.out.println("3. Eliminar Persona");
        System.out.println("4. Listar Personas (Paginado)");
        System.out.println("5. Buscar y Seleccionar por ID");
        System.out.println("6. Buscar y Seleccionar por DNI");
        
        // Bloque dinámico: Acciones de negocio si hay alguien seleccionado
        if (personaSeleccionada != null) {
            System.out.println("   --- Acciones sobre Cliente Seleccionado ---");
            System.out.println("7. [Acción] Validar Datos");
            System.out.println("8. [Acción] Verificar si es Cabeza de Familiar");
            System.out.println("9. [Acción] Mostrar Familiares Asociados");
        }
        System.out.print("Seleccione una opción: ");
    }

    private void ejecutarExportarCSV() {
        System.out.println("Iniciando proceso de exportación...");
        try {
            // Llama al repositorio pasando la ruta ./files/personas.csv
            repo.exportarCSV(rutaPredeterminadaCSV);
            
            // ToAbsolutePath ayuda a mostrarle al usuario exactamente dónde quedó el archivo en el sistema de archivos
            System.out.println("Exportación exitosa");
            System.out.println("Archivo guardado en: " + rutaPredeterminadaCSV.toAbsolutePath());
        } catch (IOException e) {System.out.println("Error crítico al escribir el archivo CSV: " + e.getMessage());}
    }    

    private void ejecutarAgregar() {
        try {
            // 1. Datos básicos de la Persona
            int id = repo.cantidad() + 1; // Generación simple de ID incremental
            System.out.println("\n--- Nueva Persona (ID Asignado: " + id + ") ---");
            System.out.print("Ingrese DNI: ");
            String dni = scanner.nextLine().trim();

            System.out.print("Ingrese Nombre: ");
            String nombre = scanner.nextLine().trim();

            System.out.print("Ingrese Apellido: ");
            String apellido = scanner.nextLine().trim();

            System.out.print("¿Es extranjero? (true/false): ");
            boolean extranjero = Boolean.parseBoolean(scanner.nextLine().trim());

            System.out.print("¿Es Family Owner (Cabeza)? (true/false): ");
            boolean familyOwner = Boolean.parseBoolean(scanner.nextLine().trim());

            System.out.print("¿Está cargado en Cáritas? (true/false): ");
            boolean cargadoEnCaritas = Boolean.parseBoolean(scanner.nextLine().trim());

            System.out.print("Descripción: ");
            String descripcion = scanner.nextLine().trim();

            // 2. Datos de la Ubicación
            System.out.println("\n--- Registrar Ubicación ---");
            System.out.print("Dirección: ");
            String direccion = scanner.nextLine().trim();

            System.out.print("Localidad: ");
            String localidad = scanner.nextLine().trim();

            Ubicacion nuevaUbicacion = new Ubicacion(direccion, localidad);


            // 3. Datos del Grupo Familiar
            GrupoFamiliar nuevoGrupo = null;
            if (familyOwner) {
                
                List<Familiar> listaFamiliares = new ArrayList<>();
                System.out.print("\n¿Desea agregar miembros al grupo familiar? (s/n): ");
                String respuestaFam = scanner.nextLine().trim().toLowerCase();

                while (respuestaFam.equals("s")) {
                    System.out.println("\n-> Datos del Familiar #" + (listaFamiliares.size() + 1));
                    System.out.print("   Nombre Completo: ");
                    String nomFam = scanner.nextLine().trim();
                    System.out.print("   DNI: ");
                    String dniFam = scanner.nextLine().trim();
                    System.out.print("   Descripción: ");
                    String descFam = scanner.nextLine().trim();
                    
                    // Selección del Enum FamiliarRelacion
                    System.out.println("   Seleccione el tipo de relación:");
                    FamiliarRelacion[] relaciones = FamiliarRelacion.values();
                    for (int i = 0; i < relaciones.length; i++) {
                        System.out.printf("     %d. %s%n", i + 1, relaciones[i]);
                    }
                    System.out.print("   Opción (número): ");
                    FamiliarRelacion relacionSeleccionada = FamiliarRelacion.otro; // por defecto
                    try {
                        int opcionRel = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if (opcionRel >= 0 && opcionRel < relaciones.length) relacionSeleccionada = relaciones[opcionRel];
                    } catch (NumberFormatException e) {System.out.println("Opción inválida. Se asignará 'otro'.");}

                    // Selección del Enum FamiliarTipo  
                    System.out.println("   Seleccione el tipo de familiar:");
                    FamiliarTipo[] tipos = FamiliarTipo.values();
                    for (int i = 0; i < tipos.length; i++) {
                        System.out.printf("     %d. %s%n", i + 1, tipos[i]);
                    }
                    System.out.print("   Opción (número): ");
                    FamiliarTipo tipoSeleccionado = FamiliarTipo.desconocido; // por defecto
                    try {
                        int opcionTipo = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if (opcionTipo >= 0 && opcionTipo < tipos.length) tipoSeleccionado = tipos[opcionTipo];
                    } catch (NumberFormatException e) {System.out.println("Opción inválida. Se asignará 'desconocido'.");}              

                    // Crear instancia de Familiar y añadir a la lista temporal
                    Familiar nuevoFamiliar = new Familiar(
                        relacionSeleccionada,
                        tipoSeleccionado,
                        descFam, 
                        dniFam, 
                        nomFam
                    );
                    listaFamiliares.add(nuevoFamiliar);

                    System.out.print("\n¿Desea agregar otro familiar? (s/n): ");
                    respuestaFam = scanner.nextLine().trim().toLowerCase();
                }

                // Construir el GrupoFamiliar final (usando el ID de la persona para el grupo)
                if (familyOwner && !listaFamiliares.isEmpty()) nuevoGrupo = new GrupoFamiliar(id, listaFamiliares.size(), listaFamiliares);
            }

            // 4. Construcción final de la Persona con sus agregaciones
            Persona nuevaPersona = new Persona(
                id, dni, nombre, apellido, extranjero, familyOwner, 
                cargadoEnCaritas, descripcion, nuevaUbicacion, nuevoGrupo
            );

            repo.agregar(nuevaPersona);
            System.out.println("\nPersona, ubicación y grupo familiar agregados exitosamente.");

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void ejecutarEliminar() {
        try {
            System.out.print("Ingrese el ID de la persona a eliminar: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            
            // Si eliminamos la persona actualmente seleccionada, limpiamos la selección
            if (personaSeleccionada != null && personaSeleccionada.id() == id) personaSeleccionada = null;

            if (repo.eliminar(id)) System.out.println("Persona eliminada correctamente."); 
            else System.out.println("No se encontró ninguna persona con ese ID.");

        } catch (NumberFormatException e) {
            System.out.println("Error: El ID debe ser numérico.");
        }
    }

    private void ejecutarListar() {
        try {
            System.out.print("Cantidad de elementos a traer (Limit): ");
            int limit = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Elementos a saltar (Offset): ");
            int offset = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Filtro de búsqueda (dejar vacío para ninguno): ");
            String filter = scanner.nextLine().trim();

            ListQuery query = new ListQuery(limit, offset, filter);
            List<Persona> resultado = repo.listar(query);

            if (resultado.isEmpty()) {
                System.out.println("La consulta no arrojó resultados.");
                return;
            }

            System.out.println("\n--- RESULTADO DE LA BÚSQUEDA ---");
            for (Persona p : resultado) {
                System.out.printf("ID: %d | %s, %s | DNI: %s%n", p.id(), p.apellido(), p.nombre(), p.dni());
            }
            System.out.println("---------------------------------");

            // Sub-flujo: Seleccionar un cliente de la lista o terminar
            System.out.print("Ingrese un ID para seleccionar un cliente (o Presione ENTER para volver): ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                int idSeleccion = Integer.parseInt(input);
                seleccionarPorIdDirecto(idSeleccion);
            }
        } catch (NumberFormatException e) {System.out.println("Entrada inválida. Volviendo al menú principal.");}
    }

    private void ejecutarBuscarPorId() {
        try {
            System.out.print("Ingrese ID a buscar: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            seleccionarPorIdDirecto(id);
        } catch (NumberFormatException e) {System.out.println("Error: El ID debe ser numérico.");}
    }

    private void seleccionarPorIdDirecto(int id) {
        Optional<Persona> encontrado = repo.buscarPorId(id);
        if (encontrado.isPresent()) {
            personaSeleccionada = encontrado.get();
            System.out.printf("Cliente '%s %s' seleccionado con éxito.%n", personaSeleccionada.nombre(), personaSeleccionada.apellido());
        } 
        else System.out.println("No se encontró ninguna persona con el ID provisto.");
    }

    private void ejecutarBuscarPorDni() {
        System.out.print("Ingrese DNI a buscar: ");
        String dni = scanner.nextLine().trim();
        Optional<Persona> encontrado = repo.buscarPorDni(dni);
        if (encontrado.isPresent()) {
            personaSeleccionada = encontrado.get();
            System.out.printf("Cliente '%s %s' seleccionado con éxito.%n", personaSeleccionada.nombre(), personaSeleccionada.apellido());
        } 
        else System.out.println("No se encontró ninguna persona con el DNI provisto.");
    }

    // --- Métodos Lógicos de Negocio Explicativos ---
    private void ejecutarValidarDatos() {
        if (personaSeleccionada == null) {
            System.out.println("Operación cancelada: Debe seleccionar un cliente primero.");
            return;
        }
        System.out.println("Validando datos...");
        if (personaSeleccionada.validarDatos()) System.out.println("RESULTADO: Los datos son consistentes. Cuenta con campos críticos válidos (DNI, Nombre y Apellido)."); 
        else System.out.println("RESULTADO: Los datos no consistentes. Faltan corregir campos obligatorios de identificación.");
        
    }

    private void ejecutarEsCabezaFamiliar() {
        if (personaSeleccionada == null) {
            System.out.println("Operación cancelada: Debe seleccionar un cliente primero.");
            return;
        }
        System.out.println("Analizando jerarquía del grupo familiar...");
        
        if (personaSeleccionada.esCabezaFamiliar()) System.out.printf("RESULTADO: %s %s está registrado como el Jefe/Cabeza de su Grupo Familiar.%n", personaSeleccionada.nombre(), personaSeleccionada.apellido());
        else System.out.printf("RESULTADO: %s %s no figura anotado como Cabeza de Familia.%n",  personaSeleccionada.nombre(), personaSeleccionada.apellido());
    }

    private void ejecutarGetFamiliaresAsociados() {
        if (personaSeleccionada == null) {
            System.out.println("Operación cancelada: Debe seleccionar un cliente primero.");
            return;
        }
        System.out.println("Solicitando trazabilidad de vínculos directos...");
        List<Familiar> familiares = personaSeleccionada.getFamiliaresAsociados();
        
        if (familiares == null || familiares.isEmpty()) System.out.printf("RESULTADO: %s %s no posee un núcleo de familiares asociados o el grupo familiar se encuentra vacío.%n", personaSeleccionada.nombre(), personaSeleccionada.apellido());
        else {
            System.out.printf("RESULTADO: Se encontraron %d vínculo(s) directo(s) registrado(s):%n", familiares.size());
            for (Familiar f : familiares) {
                System.out.printf("   %s [%s] - DNI: %s (Relación: %s) | Tipo: %s%n", 
                    f.nombreCompleto(), f.descripcion(), f.dni(), f.relacion(), f.tipo());
            }
        }
    }
}