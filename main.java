import repo.InMemory;
import repo.PersonaRepository;

public class main {
    public static void main(String[] args) {
        // Inicializamos nuestro repositorio en memoria
        PersonaRepository repository = new InMemory();
        
        // Pasamos el repositorio al CLI y lo ejecutamos
        cli interfaz = new cli(repository);
        interfaz.run();
    }
}
