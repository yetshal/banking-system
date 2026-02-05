package com.banking.system.service;

import com.banking.system.entity.Cliente;
import com.banking.system.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ClienteService
 * Usa Mockito para simular el repositorio
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ClienteService")
class ClienteServiceTest {
    
    @Mock
    private ClienteRepository clienteRepository;
    
    @InjectMocks
    private ClienteService clienteService;
    
    private Cliente clienteValido;
    private Cliente clienteMenor;
    
    @BeforeEach
    void setUp() {
        // Cliente válido (mayor de edad)
        clienteValido = new Cliente();
        clienteValido.setId(1L);
        clienteValido.setTipoIdentificacion("CC");
        clienteValido.setNumeroIdentificacion("1234567890");
        clienteValido.setNombres("Juan Carlos");
        clienteValido.setApellido("Pérez García");
        clienteValido.setCorreoElectronico("juan@example.com");
        clienteValido.setFechaNacimiento(LocalDate.of(1995, 5, 15));
        
        // Cliente menor de edad
        clienteMenor = new Cliente();
        clienteMenor.setTipoIdentificacion("TI");
        clienteMenor.setNumeroIdentificacion("9876543210");
        clienteMenor.setNombres("Pedro");
        clienteMenor.setApellido("López");
        clienteMenor.setCorreoElectronico("pedro@example.com");
        clienteMenor.setFechaNacimiento(LocalDate.of(2010, 1, 1));
    }
    
    @Test
    @DisplayName("Crear cliente válido - Debe ser exitoso")
    void testCrearClienteValido() {
        // Arrange
        when(clienteRepository.existsByNumeroIdentificacion(anyString())).thenReturn(false);
        when(clienteRepository.findByCorreoElectronico(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteValido);
        
        // Act
        Cliente resultado = clienteService.crearCliente(clienteValido);
        
        // Assert
        assertNotNull(resultado);
        assertEquals("Juan Carlos", resultado.getNombres());
        assertEquals("juan@example.com", resultado.getCorreoElectronico());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }
    
    @Test
    @DisplayName("Crear cliente menor de edad - Debe fallar")
    void testCrearClienteMenorDeEdad() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> clienteService.crearCliente(clienteMenor)
        );
        
        assertTrue(exception.getMessage().contains("mayor de edad"));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }
    
    @Test
    @DisplayName("Crear cliente con número de identificación duplicado - Debe fallar")
    void testCrearClienteNumeroIdentificacionDuplicado() {
        // Arrange
        when(clienteRepository.existsByNumeroIdentificacion("1234567890")).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> clienteService.crearCliente(clienteValido)
        );
        
        assertTrue(exception.getMessage().contains("Ya existe un cliente"));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }
    
    @Test
    @DisplayName("Crear cliente con correo duplicado - Debe fallar")
    void testCrearClienteCorreoDuplicado() {
        // Arrange
        when(clienteRepository.existsByNumeroIdentificacion(anyString())).thenReturn(false);
        when(clienteRepository.findByCorreoElectronico("juan@example.com"))
            .thenReturn(Optional.of(clienteValido));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> clienteService.crearCliente(clienteValido)
        );
        
        assertTrue(exception.getMessage().contains("correo electrónico"));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }
    
    @Test
    @DisplayName("Obtener cliente por ID existente - Debe retornar cliente")
    void testObtenerClientePorIdExistente() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteValido));
        
        // Act
        Cliente resultado = clienteService.obtenerClientePorId(1L);
        
        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan Carlos", resultado.getNombres());
    }
    
    @Test
    @DisplayName("Obtener cliente por ID inexistente - Debe lanzar excepción")
    void testObtenerClientePorIdInexistente() {
        // Arrange
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> clienteService.obtenerClientePorId(999L)
        );
        
        assertTrue(exception.getMessage().contains("no encontrado"));
    }
    
    @Test
    @DisplayName("Obtener todos los clientes - Debe retornar lista")
    void testObtenerTodosLosClientes() {
        // Arrange
        List<Cliente> clientes = Arrays.asList(clienteValido);
        when(clienteRepository.findAll()).thenReturn(clientes);
        
        // Act
        List<Cliente> resultado = clienteService.obtenerTodosLosClientes();
        
        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Juan Carlos", resultado.get(0).getNombres());
    }
    
    @Test
    @DisplayName("Actualizar cliente válido - Debe ser exitoso")
    void testActualizarClienteValido() {
        // Arrange
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setTipoIdentificacion("CC");
        clienteActualizado.setNumeroIdentificacion("1234567890");
        clienteActualizado.setNombres("Juan Carlos");
        clienteActualizado.setApellido("Pérez Martínez");
        clienteActualizado.setCorreoElectronico("juan@example.com");
        clienteActualizado.setFechaNacimiento(LocalDate.of(1995, 5, 15));
        
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteValido));
        when(clienteRepository.existsByCorreoElectronicoAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(clienteRepository.existsByNumeroIdentificacionAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);
        
        // Act
        Cliente resultado = clienteService.actualizarCliente(1L, clienteActualizado);
        
        // Assert
        assertNotNull(resultado);
        assertEquals("Pérez Martínez", resultado.getApellido());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }
    
    @Test
    @DisplayName("Eliminar cliente sin cuentas - Debe ser exitoso")
    void testEliminarClienteSinCuentas() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteValido));
        doNothing().when(clienteRepository).delete(any(Cliente.class));
        
        // Act
        assertDoesNotThrow(() -> clienteService.eliminarCliente(1L));
        
        // Assert
        verify(clienteRepository, times(1)).delete(clienteValido);
    }
    
    @Test
    @DisplayName("Eliminar cliente inexistente - Debe lanzar excepción")
    void testEliminarClienteInexistente() {
        // Arrange
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> clienteService.eliminarCliente(999L)
        );
        
        assertTrue(exception.getMessage().contains("no encontrado"));
        verify(clienteRepository, never()).delete(any(Cliente.class));
    }
}