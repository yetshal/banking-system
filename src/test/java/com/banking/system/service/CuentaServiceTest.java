package com.banking.system.service;

import com.banking.system.entity.Cliente;
import com.banking.system.entity.Cuenta;
import com.banking.system.entity.Cuenta.EstadoCuenta;
import com.banking.system.entity.Cuenta.TipoCuenta;
import com.banking.system.repository.ClienteRepository;
import com.banking.system.repository.CuentaRepository;
import com.banking.system.service.CuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CuentaService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de CuentaService")
class CuentaServiceTest {
    
    @Mock
    private CuentaRepository cuentaRepository;
    
    @Mock
    private ClienteRepository clienteRepository;
    
    @InjectMocks
    private CuentaService cuentaService;
    
    private Cliente cliente;
    private Cuenta cuentaAhorro;
    private Cuenta cuentaCorriente;
    
    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombres("Juan");
        cliente.setApellido("Pérez");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        
        cuentaAhorro = new Cuenta();
        cuentaAhorro.setId(1L);
        cuentaAhorro.setTipoCuenta(TipoCuenta.CUENTA_AHORRO);
        cuentaAhorro.setSaldo(new BigDecimal("100000"));
        cuentaAhorro.setExentaGMF(false);
        cuentaAhorro.setCliente(cliente);
        
        cuentaCorriente = new Cuenta();
        cuentaCorriente.setId(2L);
        cuentaCorriente.setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);
        cuentaCorriente.setSaldo(new BigDecimal("50000"));
        cuentaCorriente.setExentaGMF(true);
        cuentaCorriente.setCliente(cliente);
    }
    
    @Test
    @DisplayName("Crear cuenta de ahorro - Debe generar número correcto")
    void testCrearCuentaAhorro() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findUltimoNumeroCuentaPorTipo(TipoCuenta.CUENTA_AHORRO))
            .thenReturn(new ArrayList<>());
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(invocation -> {
            Cuenta cuenta = invocation.getArgument(0);
            cuenta.setId(1L);
            cuenta.setNumeroCuenta("5300000001");
            return cuenta;
        });
        
        // Act
        Cuenta resultado = cuentaService.crearCuenta(cuentaAhorro, 1L);
        
        // Assert
        assertNotNull(resultado);
        assertEquals(TipoCuenta.CUENTA_AHORRO, resultado.getTipoCuenta());
        assertTrue(resultado.getNumeroCuenta().startsWith("53"));
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
    }
    
    @Test
    @DisplayName("Crear cuenta corriente - Debe generar número correcto")
    void testCrearCuentaCorriente() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findUltimoNumeroCuentaPorTipo(TipoCuenta.CUENTA_CORRIENTE))
            .thenReturn(new ArrayList<>());
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(invocation -> {
            Cuenta cuenta = invocation.getArgument(0);
            cuenta.setId(2L);
            cuenta.setNumeroCuenta("3300000001");
            return cuenta;
        });
        
        // Act
        Cuenta resultado = cuentaService.crearCuenta(cuentaCorriente, 1L);
        
        // Assert
        assertNotNull(resultado);
        assertEquals(TipoCuenta.CUENTA_CORRIENTE, resultado.getTipoCuenta());
        assertTrue(resultado.getNumeroCuenta().startsWith("33"));
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
    }
    
    @Test
    @DisplayName("Crear cuenta con cliente inexistente - Debe fallar")
    void testCrearCuentaClienteInexistente() {
        // Arrange
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cuentaService.crearCuenta(cuentaAhorro, 999L)
        );
        
        assertTrue(exception.getMessage().contains("Cliente no encontrado"));
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }
    
    @Test
    @DisplayName("Crear cuenta con saldo negativo - Debe fallar")
    void testCrearCuentaSaldoNegativo() {
        // Arrange
        cuentaAhorro.setSaldo(new BigDecimal("-1000"));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cuentaService.crearCuenta(cuentaAhorro, 1L)
        );
        
        assertTrue(exception.getMessage().contains("no puede ser negativo"));
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }
    
    @Test
    @DisplayName("Cancelar cuenta con saldo cero - Debe ser exitoso")
    void testCancelarCuentaSaldoCero() {
        // Arrange
        cuentaAhorro.setSaldo(BigDecimal.ZERO);
        cuentaAhorro.setEstado(EstadoCuenta.ACTIVA);
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaAhorro));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaAhorro);
        
        // Act
        Cuenta resultado = cuentaService.cancelarCuenta(1L);
        
        // Assert
        assertNotNull(resultado);
        assertEquals(EstadoCuenta.CANCELADA, resultado.getEstado());
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
    }
    
    @Test
    @DisplayName("Cancelar cuenta con saldo - Debe fallar")
    void testCancelarCuentaConSaldo() {
        // Arrange
        cuentaAhorro.setSaldo(new BigDecimal("100000"));
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaAhorro));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cuentaService.cancelarCuenta(1L)
        );
        
        assertTrue(exception.getMessage().contains("saldo debe ser $0"));
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }
    
    @Test
    @DisplayName("Obtener cuenta por ID - Debe retornar cuenta")
    void testObtenerCuentaPorId() {
        // Arrange
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaAhorro));
        
        // Act
        Cuenta resultado = cuentaService.obtenerCuentaPorId(1L);
        
        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }
    
    @Test
    @DisplayName("Obtener cuenta inexistente - Debe lanzar excepción")
    void testObtenerCuentaInexistente() {
        // Arrange
        when(cuentaRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cuentaService.obtenerCuentaPorId(999L)
        );
        
        assertTrue(exception.getMessage().contains("no encontrada"));
    }
    
    @Test
    @DisplayName("Actualizar estado de cuenta - Debe ser exitoso")
    void testActualizarEstadoCuenta() {
        // Arrange
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaAhorro));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaAhorro);
        
        // Act
        Cuenta resultado = cuentaService.actualizarEstadoCuenta(1L, EstadoCuenta.INACTIVA);
        
        // Assert
        assertNotNull(resultado);
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
    }
}