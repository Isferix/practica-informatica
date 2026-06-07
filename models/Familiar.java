package models;

public record Familiar(
    FamiliarRelacion relacion,
    FamiliarTipo tipo,
    String descripcion,
    String dni,
    String nombreCompleto
) {}
