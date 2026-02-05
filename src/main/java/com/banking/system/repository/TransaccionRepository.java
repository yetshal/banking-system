package com.banking.system.repository;

import com.banking.system.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceder a los datos de Transaccion
 */
@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    /**
     * Obtener todas las transacciones de una cuenta (como origen) Ordenadas por
     * fecha descendente (más recientes primero)
     */
    List<Transaccion> findByCuentaOrigenIdOrderByFechaTransaccionDesc(Long cuentaId);

    /**
     * Obtener todas las transacciones de una cuenta (como origen o destino)
     * Para obtener el historial completo
     */
    @Query("SELECT t FROM Transaccion t WHERE t.cuentaOrigen.id = ?1 OR t.cuentaDestino.id = ?1 "
            + "ORDER BY t.fechaTransaccion DESC")
    List<Transaccion> findAllByCuentaId(Long cuentaId);

    /**
     * Obtener transacciones por tipo
     */
    List<Transaccion> findByTipoTransaccionOrderByFechaTransaccionDesc(Transaccion.TipoTransaccion tipo);
}
