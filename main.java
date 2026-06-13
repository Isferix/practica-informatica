import repo.InMemory;
import repo.PersonaRepository;

import extra.CSVExporter;
import java.nio.file.Path;
public class main {
    public static void main(String[] args) {
        // Inicializamos nuestro repositorio en memoria
        PersonaRepository repository = new InMemory();
        CSVExporter exporter = new CSVExporter(Path.of("./files/personas.csv"));
        
        // Pasamos el repositorio al CLI y lo ejecutamos
        cli interfaz = new cli(repository, exporter);
        interfaz.run();
    }
}
