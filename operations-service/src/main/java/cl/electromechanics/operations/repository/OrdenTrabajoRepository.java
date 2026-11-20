package cl.electromechanics.operations.repository;

import cl.electromechanics.operations.entity.OrdenTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, UUID> {
    List<OrdenTrabajo> findByTecnicoIdOrderByFechaDescIdAsc(Long tecnicoId);
    Optional<OrdenTrabajo> findByIdAndTecnicoId(UUID id, Long tecnicoId);
}
