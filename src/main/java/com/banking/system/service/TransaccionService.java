package com.banking.system.service;

import com.banking.system.entity.Cuenta;
import com.banking.system.entity.Transaccion;
import com.banking.system.entity.Transaccion.TipoTransaccion;
import com.banking.system.repository.CuentaRepository;
import com.banking.system.repository.TransaccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de Transacciones Maneja la lógica de negocio de las operaciones
 * financieras
 */
@Service
@Transactional
public class TransaccionService {

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    /**
     * Realizar una consignación (depósito) Aumenta el saldo de la cuenta
     */
    public Transaccion realizarConsignacion(Long cuentaId, BigDecimal monto, String descripcion) {
        // Validar que el monto sea positivo
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }

        // Obtener la cuenta
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada con ID: " + cuentaId));

        // Verificar que la cuenta esté activa
        if (cuenta.getEstado() != Cuenta.EstadoCuenta.ACTIVA) {
            throw new IllegalArgumentException("La cuenta debe estar activa para realizar transacciones");
        }

        // Aumentar el saldo
        cuenta.aumentarSaldo(monto);
        cuentaRepository.save(cuenta);

        // Crear y guardar la transacción
        Transaccion transaccion = new Transaccion();
        transaccion.setTipoTransaccion(TipoTransaccion.CONSIGNACION);
        transaccion.setMonto(monto);
        transaccion.setDescripcion(descripcion != null ? descripcion : "Consignación");
        transaccion.setCuentaOrigen(cuenta);
        transaccion.setSaldoPosterior(cuenta.getSaldo());

        return transaccionRepository.save(transaccion);
    }

    /**
     * Realizar un retiro Disminuye el saldo de la cuenta
     */
    public Transaccion realizarRetiro(Long cuentaId, BigDecimal monto, String descripcion) {
        // Validar que el monto sea positivo
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }

        // Obtener la cuenta
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada con ID: " + cuentaId));

        // Verificar que la cuenta esté activa
        if (cuenta.getEstado() != Cuenta.EstadoCuenta.ACTIVA) {
            throw new IllegalArgumentException("La cuenta debe estar activa para realizar transacciones");
        }

        // Validar que haya saldo suficiente
        if (cuenta.getSaldo().compareTo(monto) < 0) {
            throw new IllegalArgumentException(
                    "Saldo insuficiente. Saldo disponible: $" + cuenta.getSaldo()
                    + ", Monto solicitado: $" + monto
            );
        }

        // Disminuir el saldo (esto también valida si es cuenta de ahorro)
        cuenta.disminuirSaldo(monto);
        cuentaRepository.save(cuenta);

        // Crear y guardar la transacción
        Transaccion transaccion = new Transaccion();
        transaccion.setTipoTransaccion(TipoTransaccion.RETIRO);
        transaccion.setMonto(monto);
        transaccion.setDescripcion(descripcion != null ? descripcion : "Retiro");
        transaccion.setCuentaOrigen(cuenta);
        transaccion.setSaldoPosterior(cuenta.getSaldo());

        return transaccionRepository.save(transaccion);
    }

    /**
     * Realizar una transferencia entre cuentas Genera dos transacciones: una de
     * envío y otra de recepción
     */
    public List<Transaccion> realizarTransferencia(Long cuentaOrigenId, Long cuentaDestinoId,
            BigDecimal monto, String descripcion) {
        // Validar que el monto sea positivo
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }

        // Validar que no sean la misma cuenta
        if (cuentaOrigenId.equals(cuentaDestinoId)) {
            throw new IllegalArgumentException("No se puede transferir a la misma cuenta");
        }

        // Obtener ambas cuentas
        Cuenta cuentaOrigen = cuentaRepository.findById(cuentaOrigenId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta origen no encontrada con ID: " + cuentaOrigenId));

        Cuenta cuentaDestino = cuentaRepository.findById(cuentaDestinoId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta destino no encontrada con ID: " + cuentaDestinoId));

        // Verificar que ambas cuentas estén activas
        if (cuentaOrigen.getEstado() != Cuenta.EstadoCuenta.ACTIVA) {
            throw new IllegalArgumentException("La cuenta origen debe estar activa");
        }
        if (cuentaDestino.getEstado() != Cuenta.EstadoCuenta.ACTIVA) {
            throw new IllegalArgumentException("La cuenta destino debe estar activa");
        }

        // Validar saldo suficiente en cuenta origen
        if (cuentaOrigen.getSaldo().compareTo(monto) < 0) {
            throw new IllegalArgumentException(
                    "Saldo insuficiente en cuenta origen. Saldo disponible: $" + cuentaOrigen.getSaldo()
            );
        }

        // Realizar la transferencia
        cuentaOrigen.disminuirSaldo(monto);
        cuentaDestino.aumentarSaldo(monto);

        cuentaRepository.save(cuentaOrigen);
        cuentaRepository.save(cuentaDestino);

        // Crear transacción de envío
        Transaccion transaccionEnvio = new Transaccion();
        transaccionEnvio.setTipoTransaccion(TipoTransaccion.TRANSFERENCIA_ENVIADA);
        transaccionEnvio.setMonto(monto);
        transaccionEnvio.setDescripcion(descripcion != null ? descripcion
                : "Transferencia a cuenta " + cuentaDestino.getNumeroCuenta());
        transaccionEnvio.setCuentaOrigen(cuentaOrigen);
        transaccionEnvio.setCuentaDestino(cuentaDestino);
        transaccionEnvio.setSaldoPosterior(cuentaOrigen.getSaldo());

        // Crear transacción de recepción
        Transaccion transaccionRecepcion = new Transaccion();
        transaccionRecepcion.setTipoTransaccion(TipoTransaccion.TRANSFERENCIA_RECIBIDA);
        transaccionRecepcion.setMonto(monto);
        transaccionRecepcion.setDescripcion(descripcion != null ? descripcion
                : "Transferencia desde cuenta " + cuentaOrigen.getNumeroCuenta());
        transaccionRecepcion.setCuentaOrigen(cuentaDestino);
        transaccionRecepcion.setCuentaDestino(cuentaOrigen);
        transaccionRecepcion.setSaldoPosterior(cuentaDestino.getSaldo());

        // Guardar ambas transacciones
        Transaccion envioGuardada = transaccionRepository.save(transaccionEnvio);
        Transaccion recepcionGuardada = transaccionRepository.save(transaccionRecepcion);

        return List.of(envioGuardada, recepcionGuardada);
    }

    /**
     * Obtener el historial de transacciones de una cuenta
     */
    public List<Transaccion> obtenerHistorialCuenta(Long cuentaId) {
        // Validar que la cuenta existe
        if (!cuentaRepository.existsById(cuentaId)) {
            throw new IllegalArgumentException("Cuenta no encontrada con ID: " + cuentaId);
        }
        return transaccionRepository.findAllByCuentaId(cuentaId);
    }

    /**
     * Obtener todas las transacciones
     */
    public List<Transaccion> obtenerTodasLasTransacciones() {
        return transaccionRepository.findAll();
    }

    /**
     * Obtener una transacción por ID
     */
    public Transaccion obtenerTransaccionPorId(Long id) {
        return transaccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada con ID: " + id));
    }
}
