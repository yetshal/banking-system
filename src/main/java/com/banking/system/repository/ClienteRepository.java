package com.banking.system.repository;

import com.banking.system.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para acceder a los datos de Cliente JpaRepository nos da métodos
 * automáticos como save(), findById(), findAll(), delete()
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca un cliente por su número de identificación
     * @param numeroIdentificacion número de documento
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByNumeroIdentificacion(String numeroIdentificacion);

    /**
     * Busca un cliente por su correo electrónico
     * @param correo correo del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByCorreoElectronico(String correo);

    /**
     * Verifica si existe un cliente con ese número de identificación
     * @param numeroIdentificacion número de documento
     * @return true si existe
     */
    boolean existsByNumeroIdentificacion(String numeroIdentificacion);

    /**
     * Verifica si existe un cliente con ese correo (excluyendo un ID
     * específico) Útil para validar al actualizar un cliente
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cliente c "
            + "WHERE c.correoElectronico = ?1 AND c.id != ?2")
    boolean existsByCorreoElectronicoAndIdNot(String correo, Long id);

    /**
     * Verifica si existe un cliente con ese número de identificación
     * (excluyendo un ID)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cliente c "
            + "WHERE c.numeroIdentificacion = ?1 AND c.id != ?2")
    boolean existsByNumeroIdentificacionAndIdNot(String numeroIdentificacion, Long id);
}
