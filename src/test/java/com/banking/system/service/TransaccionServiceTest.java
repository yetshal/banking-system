package com.banking.system.service;

import com.banking.system.entity.Cliente;
import com.banking.system.entity.Cuenta;
import com.banking.system.entity.Cuenta.EstadoCuenta;
import com.banking.system.entity.Cuenta.TipoCuenta;
import com.banking.system.entity.Transaccion;
import com.banking.system.repository.CuentaRepository;
import com.banking.system.repository.TransaccionRepository;
import com.banking.system.service.TransaccionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TransaccionService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de TransaccionService")
class TransaccionServiceTest {
    
    @Mock
    private TransaccionRepository transaccionRepository;
    
    @Mock
    private CuentaRepository cuentaRepository;
    
    @InjectMocks
    private TransaccionService transaccionService;
    
    private Cliente cliente;
    private Cuenta cuentaOrigen;
    private Cuenta cuentaDestino;
    
    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombres("Juan");
        cliente.setApellido("Pérez");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        
        cuentaOrigen = new Cuenta();
        cuentaOrigen.setId(1L);
        cuentaOrigen.setTipoCuenta(TipoCuenta.CUENTA_AHORRO);
        cuentaOrigen.setNumeroCuenta("5300000001");
        cuentaOrigen.setEstado(EstadoCuenta.ACTIVA);
        cuentaOrigen.setSaldo(new BigDecimal("100000"));
        cuentaOrigen.setExentaGMF(false);
        cuentaOrigen.setCliente(cliente);
        
        cuentaDestino = new Cuenta();
        cuentaDestino.setId(2L);
        cuentaDestino.setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);
        cuentaDestino.setNumeroCuenta("3300000001");
        cuentaDestino.setEstado(EstadoCuenta.ACTIVA);
        cuentaDestino.setSaldo(new BigDecimal("50000"));
        cuentaDestino.setExentaGMF(true);
        cuentaDestino.setCliente(cliente);
    }
    
    @Test
    @DisplayName("Realizar consignación válida - Debe incrementar saldo")
    void testRealizarConsignacionValida() {
        // Arrange
        BigDecimal montoConsignacion = new BigDecimal("50000");
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaOrigen);
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(invocation -> {
            Transaccion t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        
        // Act
        Transaccion resultado = transaccionService.realizarConsignacion(1L, montoConsignacion, "Consignación test");
        
        // Assert
        assertNotNull(resultado);
        assertEquals(montoConsignacion, resultado.getMonto());
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
        verify(transaccionRepository, times(1)).save(any(Transaccion.class));
    }
    
    @Test
    @DisplayName("Realizar consignación con monto negativo - Debe fallar")
    void testRealizarConsignacionMontoNegativo() {
        // Arrange
        BigDecimal montoNegativo = new BigDecimal("-1000");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transaccionService.realizarConsignacion(1L, montoNegativo, "Test")
        );
        
        assertTrue(exception.getMessage().contains("mayor a cero"));
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }
    
    @Test
    @DisplayName("Realizar consignación en cuenta inactiva - Debe fallar")
    void testRealizarConsignacionCuentaInactiva() {
        // Arrange
        cuentaOrigen.setEstado(EstadoCuenta.INACTIVA);
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaOrigen));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transaccionService.realizarConsignacion(1L, new BigDecimal("10000"), "Test")
        );
        
        assertTrue(exception.getMessage().contains("activa"));
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }
    
    @Test
    @DisplayName("Realizar retiro válido - Debe disminuir saldo")
    void testRealizarRetiroValido() {
        // Arrange
        BigDecimal montoRetiro = new BigDecimal("20000");
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaOrigen);
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(invocation -> {
            Transaccion t = invocation.getArgument(0);
            t.setId(2L);
            return t;
        });
        
        // Act
        Transaccion resultado = transaccionService.realizarRetiro(1L, montoRetiro, "Retiro test");
        
        // Assert
        assertNotNull(resultado);
        assertEquals(montoRetiro, resultado.getMonto());
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
        verify(transaccionRepository, times(1)).save(any(Transaccion.class));
    }
    
    @Test
    @DisplayName("Realizar retiro con saldo insuficiente - Debe fallar")
    void testRealizarRetiroSaldoInsuficiente() {
        // Arrange
        BigDecimal montoExcesivo = new BigDecimal("200000");
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaOrigen));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transaccionService.realizarRetiro(1L, montoExcesivo, "Test")
        );
        
        assertTrue(exception.getMessage().contains("Saldo insuficiente"));
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }
    
    @Test
    @DisplayName("Realizar transferencia válida - Debe crear 2 transacciones")
    void testRealizarTransferenciaValida() {
        // Arrange
        BigDecimal montoTransferencia = new BigDecimal("30000");
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findById(2L)).thenReturn(Optional.of(cuentaDestino));
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(invocation -> {
            Transaccion t = invocation.getArgument(0);
            t.setId(System.currentTimeMillis());
            return t;
        });
        
        // Act
        List<Transaccion> resultado = transaccionService.realizarTransferencia(
            1L, 2L, montoTransferencia, "Transferencia test"
        );
        
        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(cuentaRepository, times(2)).save(any(Cuenta.class));
        verify(transaccionRepository, times(2)).save(any(Transaccion.class));
    }
    
    @Test
    @DisplayName("Realizar transferencia a misma cuenta - Debe fallar")
    void testRealizarTransferenciaMismaCuenta() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transaccionService.realizarTransferencia(1L, 1L, new BigDecimal("10000"), "Test")
        );
        
        assertTrue(exception.getMessage().contains("misma cuenta"));
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }
    
    @Test
    @DisplayName("Obtener transacción por ID - Debe retornar transacción")
    void testObtenerTransaccionPorId() {
        // Arrange
        Transaccion transaccion = new Transaccion();
        transaccion.setId(1L);
        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccion));
        
        // Act
        Transaccion resultado = transaccionService.obtenerTransaccionPorId(1L);
        
        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }
    
    @Test
    @DisplayName("Obtener transacción inexistente - Debe lanzar excepción")
    void testObtenerTransaccionInexistente() {
        // Arrange
        when(transaccionRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transaccionService.obtenerTransaccionPorId(999L)
        );
        
        assertTrue(exception.getMessage().contains("no encontrada"));
    }
}