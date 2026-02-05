package com.banking.system.service;

import com.banking.system.entity.Cliente;
import com.banking.system.entity.Cuenta;
import com.banking.system.entity.Cuenta.EstadoCuenta;
import com.banking.system.entity.Cuenta.TipoCuenta;
import com.banking.system.repository.ClienteRepository;
import com.banking.system.repository.CuentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de Cuentas Contiene la lógica de negocio para gestionar cuentas
 * bancarias
 */
@Service
@Transactional
public class CuentaService {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Crear una nueva cuenta
     */
    public Cuenta crearCuenta(Cuenta cuenta, Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + clienteId));

        if (cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo no puede ser negativo");
        }

        String numeroCuenta = generarNumeroCuenta(cuenta.getTipoCuenta());
        cuenta.setNumeroCuenta(numeroCuenta);
        cuenta.setCliente(cliente);

        if (cuenta.getTipoCuenta() == TipoCuenta.CUENTA_AHORRO && cuenta.getEstado() == null) {
            cuenta.setEstado(EstadoCuenta.ACTIVA);
        }

        if (cuenta.getEstado() == null) {
            cuenta.setEstado(EstadoCuenta.ACTIVA);
        }

        return cuentaRepository.save(cuenta);
    }

    private String generarNumeroCuenta(TipoCuenta tipoCuenta) {
        String prefijo = tipoCuenta == TipoCuenta.CUENTA_CORRIENTE ? "33" : "53";
        List<String> ultimosCodigos = cuentaRepository.findUltimoNumeroCuentaPorTipo(tipoCuenta);

        long siguienteNumero = 1;

        if (!ultimosCodigos.isEmpty()) {
            String ultimoCodigo = ultimosCodigos.get(0);
            String numeroSinPrefijo = ultimoCodigo.substring(2);
            siguienteNumero = Long.parseLong(numeroSinPrefijo) + 1;
        }

        String numeroFormateado = String.format("%08d", siguienteNumero);
        return prefijo + numeroFormateado;
    }

    public List<Cuenta> obtenerTodasLasCuentas() {
        return cuentaRepository.findAll();
    }

    public Cuenta obtenerCuentaPorId(Long id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada con ID: " + id));
    }

    public Cuenta obtenerCuentaPorNumero(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada con número: " + numeroCuenta));
    }

    public List<Cuenta> obtenerCuentasPorCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new IllegalArgumentException("Cliente no encontrado con ID: " + clienteId);
        }
        return cuentaRepository.findByClienteId(clienteId);
    }

    public Cuenta actualizarEstadoCuenta(Long id, EstadoCuenta nuevoEstado) {
        Cuenta cuenta = obtenerCuentaPorId(id);
        cuenta.setEstado(nuevoEstado);
        return cuentaRepository.save(cuenta);
    }

    public Cuenta cancelarCuenta(Long id) {
        Cuenta cuenta = obtenerCuentaPorId(id);

        if (!cuenta.puedeSerCancelada()) {
            throw new IllegalArgumentException(
                    "No se puede cancelar la cuenta. El saldo debe ser $0. Saldo actual: $" + cuenta.getSaldo()
            );
        }

        cuenta.setEstado(EstadoCuenta.CANCELADA);
        return cuentaRepository.save(cuenta);
    }

    public void eliminarCuenta(Long id) {
        Cuenta cuenta = obtenerCuentaPorId(id);

        if (cuenta.getEstado() != EstadoCuenta.CANCELADA) {
            throw new IllegalArgumentException("Solo se pueden eliminar cuentas canceladas");
        }

        if (!cuenta.puedeSerCancelada()) {
            throw new IllegalArgumentException("La cuenta debe tener saldo $0 para ser eliminada");
        }

        cuentaRepository.delete(cuenta);
    }
}
