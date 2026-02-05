package com.banking.system.repository;

import com.banking.system.entity.Cuenta;
import com.banking.system.entity.Cuenta.TipoCuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceder a los datos de Cuenta
 */
@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    /**
     * Busca una cuenta por su número
     */
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    /**
     * Busca todas las cuentas de un cliente
     */
    List<Cuenta> findByClienteId(Long clienteId);

    /**
     * Busca todas las cuentas de un tipo específico
     */
    List<Cuenta> findByTipoCuenta(TipoCuenta tipoCuenta);

    /**
     * Verifica si existe una cuenta con ese número
     */
    boolean existsByNumeroCuenta(String numeroCuenta);

    /**
     * Obtiene el último número de cuenta creado de un tipo específico Para
     * generar el siguiente número
     */
    @Query("SELECT c.numeroCuenta FROM Cuenta c WHERE c.tipoCuenta = ?1 ORDER BY c.numeroCuenta DESC")
    List<String> findUltimoNumeroCuentaPorTipo(TipoCuenta tipoCuenta);

    /**
     * Cuenta cuántas cuentas tiene un cliente
     */
    long countByClienteId(Long clienteId);
}
