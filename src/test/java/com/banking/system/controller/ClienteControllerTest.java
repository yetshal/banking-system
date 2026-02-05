package com.banking.system.controller;

import com.banking.system.entity.Cliente;
import com.banking.system.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests unitarios para ClienteController
 */
@WebMvcTest(ClienteController.class)
@DisplayName("Tests de ClienteController")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    @Autowired
    private ObjectMapper objectMapper;

    private Cliente clienteValido;

    @BeforeEach
    void setUp() {
        clienteValido = new Cliente();
        clienteValido.setId(1L);
        clienteValido.setTipoIdentificacion("CC");
        clienteValido.setNumeroIdentificacion("1234567890");
        clienteValido.setNombres("Juan Carlos");
        clienteValido.setApellido("Pérez García");
        clienteValido.setCorreoElectronico("juan.perez@email.com");
        clienteValido.setFechaNacimiento(LocalDate.of(1990, 5, 15));
    }

    /**
     * Test: POST /api/clientes - Crear cliente exitosamente
     */
    @Test
    @DisplayName("Crear cliente válido - Debe retornar 201 Created")
    void testCrearCliente_Exitoso() throws Exception {
        when(clienteService.crearCliente(any(Cliente.class)))
                .thenReturn(clienteValido);

        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteValido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombres", is("Juan Carlos")))
                .andExpect(jsonPath("$.apellido", is("Pérez García")))
                .andExpect(jsonPath("$.correoElectronico", is("juan.perez@email.com")));

        verify(clienteService, times(1)).crearCliente(any(Cliente.class));
    }

    /**
     * Test: POST /api/clientes - Cliente menor de edad
     */
    @Test
    @DisplayName("Crear cliente menor de edad - Debe retornar 400 Bad Request")
    void testCrearCliente_MenorDeEdad() throws Exception {
        Cliente menorDeEdad = new Cliente();
        menorDeEdad.setTipoIdentificacion("TI");
        menorDeEdad.setNumeroIdentificacion("9876543210");
        menorDeEdad.setNombres("Pedro");
        menorDeEdad.setApellido("López");
        menorDeEdad.setCorreoElectronico("pedro@email.com");
        menorDeEdad.setFechaNacimiento(LocalDate.now().minusYears(15));

        when(clienteService.crearCliente(any(Cliente.class)))
                .thenThrow(new IllegalArgumentException("El cliente debe ser mayor de edad"));

        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(menorDeEdad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("El cliente debe ser mayor de edad")));

        verify(clienteService, times(1)).crearCliente(any(Cliente.class));
    }

    /**
     * Test: POST /api/clientes - Número de identificación duplicado
     */
    @Test
    @DisplayName("Crear cliente con número duplicado - Debe retornar 400")
    void testCrearCliente_NumeroIdentificacionDuplicado() throws Exception {
        when(clienteService.crearCliente(any(Cliente.class)))
                .thenThrow(new IllegalArgumentException("Ya existe un cliente con ese número de identificación"));

        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Ya existe un cliente")));
    }

    /**
     * Test: GET /api/clientes - Obtener todos los clientes
     */
    @Test
    @DisplayName("Obtener todos los clientes - Debe retornar lista")
    void testObtenerTodosLosClientes() throws Exception {
        Cliente cliente2 = new Cliente();
        cliente2.setId(2L);
        cliente2.setNombres("María");
        cliente2.setApellido("González");

        List<Cliente> clientes = Arrays.asList(clienteValido, cliente2);

        when(clienteService.obtenerTodosLosClientes()).thenReturn(clientes);

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombres", is("Juan Carlos")))
                .andExpect(jsonPath("$[1].nombres", is("María")));

        verify(clienteService, times(1)).obtenerTodosLosClientes();
    }

    /**
     * Test: GET /api/clientes - Lista vacía
     */
    @Test
    @DisplayName("Obtener todos los clientes - Lista vacía")
    void testObtenerTodosLosClientes_ListaVacia() throws Exception {
        when(clienteService.obtenerTodosLosClientes()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Test: GET /api/clientes/{id} - Cliente existente
     */
    @Test
    @DisplayName("Obtener cliente por ID - Cliente existe")
    void testObtenerClientePorId_Existente() throws Exception {
        when(clienteService.obtenerClientePorId(1L)).thenReturn(clienteValido);

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombres", is("Juan Carlos")));

        verify(clienteService, times(1)).obtenerClientePorId(1L);
    }

    /**
     * Test: GET /api/clientes/{id} - Cliente no existe
     */
    @Test
    @DisplayName("Obtener cliente por ID - Cliente no existe")
    void testObtenerClientePorId_NoExiste() throws Exception {
        when(clienteService.obtenerClientePorId(999L))
                .thenThrow(new IllegalArgumentException("Cliente con ID 999 no encontrado"));

        mockMvc.perform(get("/api/clientes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("no encontrado")));
    }

    /**
     * Test: PUT /api/clientes/{id} - Actualizar cliente exitosamente
     */
    @Test
    @DisplayName("Actualizar cliente - Debe retornar cliente actualizado")
    void testActualizarCliente_Exitoso() throws Exception {
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setId(1L);
        clienteActualizado.setNombres("Juan Carlos");
        clienteActualizado.setApellido("Pérez Martínez");
        clienteActualizado.setCorreoElectronico("juan.nuevo@email.com");
        clienteActualizado.setTipoIdentificacion("CC");
        clienteActualizado.setNumeroIdentificacion("1234567890");
        clienteActualizado.setFechaNacimiento(LocalDate.of(1990, 5, 15));

        when(clienteService.actualizarCliente(eq(1L), any(Cliente.class)))
                .thenReturn(clienteActualizado);

        mockMvc.perform(put("/api/clientes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apellido", is("Pérez Martínez")))
                .andExpect(jsonPath("$.correoElectronico", is("juan.nuevo@email.com")));

        verify(clienteService, times(1)).actualizarCliente(eq(1L), any(Cliente.class));
    }

    /**
     * Test: PUT /api/clientes/{id} - Cliente no existe
     */
    @Test
    @DisplayName("Actualizar cliente - Cliente no existe")
    void testActualizarCliente_NoExiste() throws Exception {
        when(clienteService.actualizarCliente(eq(999L), any(Cliente.class)))
                .thenThrow(new IllegalArgumentException("Cliente con ID 999 no encontrado"));

        mockMvc.perform(put("/api/clientes/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("no encontrado")));
    }

    /**
     * Test: DELETE /api/clientes/{id} - Eliminar exitosamente
     */
    @Test
    @DisplayName("Eliminar cliente sin cuentas - Debe retornar 200 OK")
    void testEliminarCliente_Exitoso() throws Exception {
        doNothing().when(clienteService).eliminarCliente(1L);

        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje", is("Cliente eliminado exitosamente")));

        verify(clienteService, times(1)).eliminarCliente(1L);
    }

    /**
     * Test: DELETE /api/clientes/{id} - Cliente con cuentas vinculadas
     */
    @Test
    @DisplayName("Eliminar cliente con cuentas - Debe retornar 400 Bad Request")
    void testEliminarCliente_ConCuentas() throws Exception {
        doThrow(new IllegalArgumentException("No se puede eliminar el cliente porque tiene cuentas vinculadas"))
                .when(clienteService).eliminarCliente(1L);

        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("cuentas vinculadas")));
    }

    /**
     * Test: DELETE /api/clientes/{id} - Cliente no existe
     */
    @Test
    @DisplayName("Eliminar cliente - Cliente no existe")
    void testEliminarCliente_NoExiste() throws Exception {
        doThrow(new IllegalArgumentException("Cliente con ID 999 no encontrado"))
                .when(clienteService).eliminarCliente(999L);

        mockMvc.perform(delete("/api/clientes/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("no encontrado")));
    }
}
