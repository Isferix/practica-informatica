public class main {
    public static void main(String[] args) {
        // Inicializamos nuestro repositorio en memoria
        PersonaRepository repository = new InMemoryPersonaRepository();
        
        // Pasamos el repositorio al CLI y lo ejecutamos
        cli interfaz = new cli(repository);
        interfaz.run();
    }
}