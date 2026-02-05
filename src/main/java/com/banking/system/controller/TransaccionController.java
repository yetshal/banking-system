package com.banking.system.controller;

import com.banking.system.entity.Transaccion;
import com.banking.system.service.TransaccionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para Transacciones Define los endpoints de la API de
 * transacciones bancarias
 */
@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    @Autowired
    private TransaccionService transaccionService;

    /**
     * POST /api/transacciones/consignacion Realizar una consignación
     */
    @PostMapping("/consignacion")
    public ResponseEntity<?> realizarConsignacion(@Valid @RequestBody ConsignacionRequest request) {
        try {
            Transaccion transaccion = transaccionService.realizarConsignacion(
                    request.getCuentaId(),
                    request.getMonto(),
                    request.getDescripcion()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(transaccion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    /**
     * POST /api/transacciones/retiro Realizar un retiro
     */
    @PostMapping("/retiro")
    public ResponseEntity<?> realizarRetiro(@Valid @RequestBody RetiroRequest request) {
        try {
            Transaccion transaccion = transaccionService.realizarRetiro(
                    request.getCuentaId(),
                    request.getMonto(),
                    request.getDescripcion()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(transaccion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    /**
     * POST /api/transacciones/transferencia Realizar una transferencia entre
     * cuentas
     */
    @PostMapping("/transferencia")
    public ResponseEntity<?> realizarTransferencia(@Valid @RequestBody TransferenciaRequest request) {
        try {
            List<Transaccion> transacciones = transaccionService.realizarTransferencia(
                    request.getCuentaOrigenId(),
                    request.getCuentaDestinoId(),
                    request.getMonto(),
                    request.getDescripcion()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(transacciones);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    /**
     * GET /api/transacciones Obtener todas las transacciones
     */
    @GetMapping
    public ResponseEntity<List<Transaccion>> obtenerTodasLasTransacciones() {
        List<Transaccion> transacciones = transaccionService.obtenerTodasLasTransacciones();
        return ResponseEntity.ok(transacciones);
    }

    /**
     * GET /api/transacciones/{id} Obtener una transacción por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerTransaccionPorId(@PathVariable Long id) {
        try {
            Transaccion transaccion = transaccionService.obtenerTransaccionPorId(id);
            return ResponseEntity.ok(transaccion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError(e.getMessage()));
        }
    }

    /**
     * GET /api/transacciones/cuenta/{cuentaId} Obtener el historial de
     * transacciones de una cuenta
     */
    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<?> obtenerHistorialCuenta(@PathVariable Long cuentaId) {
        try {
            List<Transaccion> transacciones = transaccionService.obtenerHistorialCuenta(cuentaId);
            return ResponseEntity.ok(transacciones);
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
