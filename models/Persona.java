package models;

import java.util.Collections;
import java.util.List;

public record Persona(
    int id,
    String dni,
    String nombre,
    String apellido,
    Boolean extranjero,
    Boolean familyOwner,
    Boolean cargadoEnCaritas,
    String descripcion,
    Ubicacion ubicacion,
    GrupoFamiliar grupoFamiliar
) {
    
    // Métodos definidos en el diagrama de clases:

    public Boolean validarDatos() {
        // Ejemplo de validación básica
        return dni != null && !dni.isBlank() && nombre != null && apellido != null;
    }

    public Boolean esCabezaFamiliar() {
        // Mapeo lógico del atributo familyOwner
        return this.familyOwner != null && this.familyOwner;
    }

    public List<Familiar> getFamiliaresAsociados() {
        // Accede de forma segura a la lista de familiares a través del grupo familiar
        if (this.grupoFamiliar != null && this.grupoFamiliar.familiares() != null) {
            return this.grupoFamiliar.familiares();
        }
        return Collections.emptyList();
    }
}
