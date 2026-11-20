package cl.electromechanics.operations.dto;

import cl.electromechanics.operations.entity.OrdenTrabajo;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

public final class OrdenDtos {
    private OrdenDtos() {}

    public record Crear(
        @NotNull UUID id,
        @NotNull @Positive Long tecnicoId,
        @NotBlank @Size(max = 120) String cliente,
        @NotBlank @Size(max = 200) String direccion,
        @NotBlank @Size(max = 500) String descripcion,
        @NotNull LocalDate fecha
    ) {}

    public record CambioEstado(@NotNull OrdenTrabajo.Estado estado) {}

    public record Respuesta(UUID id, Long tecnicoId, String cliente, String direccion,
                            String descripcion, LocalDate fecha, OrdenTrabajo.Estado estado) {
        public static Respuesta desde(OrdenTrabajo orden) {
            return new Respuesta(
                orden.getId(), orden.getTecnicoId(), orden.getCliente(), orden.getDireccion(),
                orden.getDescripcion(), orden.getFecha(), orden.getEstado()
            );
        }
    }
}
