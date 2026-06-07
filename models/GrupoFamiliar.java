package models;

import java.util.List;

public record GrupoFamiliar(
    int id,
    int cantidad,
    List<Familiar> familiares
) {
    // Implementación del método del diagrama
    public void calcularCantidad() {
        // En un record los campos son finales. 
        // Si necesitas modificar "cantidad", se suele usar una clase tradicional,
        // pero aquí podemos imprimir o validar el conteo:
        System.out.println("Cantidad actual de familiares: " + familiares.size());
    }
}
