package com.banking.system.controller;

import com.banking.system.entity.Cuenta;
import com.banking.system.entity.Cuenta.EstadoCuenta;
import com.banking.system.service.CuentaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para Cuentas Define los endpoints de la API de cuentas
 * bancarias
 */
@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    @Autowired
    private CuentaService cuentaService;

    /**
     * POST /api/cuentas?clienteId=X Crear una nueva cuenta para un cliente
     */
    @PostMapping
    public ResponseEntity<?> crearCuenta(
            @Valid @RequestBody CuentaRequest request,
            @RequestParam Long clienteId) {
        try {
            // Convertir el DTO a entidad
            Cuenta cuenta = new Cuenta();
            cuenta.setTipoCuenta(request.getTipoCuenta());
            cuenta.setSaldo(request.getSaldo());
            cuenta.setExentaGMF(request.getExentaGMF());

            Cuenta nuevaCuenta = cuentaService.crearCuenta(cuenta, clienteId);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCuenta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    /**
     * GET /api/cuentas Obtener todas las cuentas
     */
    @GetMapping
    public ResponseEntity<List<Cuenta>> obtenerTodasLasCuentas() {
        List<Cuenta> cuentas = cuentaService.obtenerTodasLasCuentas();
        return ResponseEntity.ok(cuentas);
    }

    /**
     * GET /api/cuentas/{id} Obtener una cuenta por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCuentaPorId(@PathVariable Long id) {
        try {
            Cuenta cuenta = cuentaService.obtenerCuentaPorId(id);
            return ResponseEntity.ok(cuenta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError(e.getMessage()));
        }
    }

    /**
     * GET /api/cuentas/numero/{numeroCuenta} Obtener una cuenta por su número
     */
    @GetMapping("/numero/{numeroCuenta}")
    public ResponseEntity<?> obtenerCuentaPorNumero(@PathVariable String numeroCuenta) {
        try {
            Cuenta cuenta = cuentaService.obtenerCuentaPorNumero(numeroCuenta);
            return ResponseEntity.ok(cuenta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError(e.getMessage()));
        }
    }

    /**
     * GET /api/cuentas/cliente/{clienteId} Obtener todas las cuentas de un
     * cliente
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> obtenerCuentasPorCliente(@PathVariable Long clienteId) {
        try {
            List<Cuenta> cuentas = cuentaService.obtenerCuentasPorCliente(clienteId);
            return ResponseEntity.ok(cuentas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    /**
     * PATCH /api/cuentas/{id}/estado Actualizar el estado de una cuenta
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoCuenta(
            @PathVariable Long id,
            @RequestParam EstadoCuenta estado) {
        try {
            Cuenta cuenta = cuentaService.actualizarEstadoCuenta(id, estado);
            return ResponseEntity.ok(cuenta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    /**
     * POST /api/cuentas/{id}/cancelar Cancelar una cuenta (solo si saldo = 0)
     */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCuenta(@PathVariable Long id) {
        try {
            Cuenta cuenta = cuentaService.cancelarCuenta(id);
            return ResponseEntity.ok(cuenta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    /**
     * DELETE /api/cuentas/{id} Eliminar una cuenta
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCuenta(@PathVariable Long id) {
        try {
            cuentaService.eliminarCuenta(id);
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Cuenta eliminada exitosamente");
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    /**
     * Método auxiliar para crear respuestas de error consistentes
     */
    private Map<String, String> crearRespuestaError(String mensaje) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return error;
    }
}
