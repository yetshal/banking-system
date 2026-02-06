package com.banking.system.controller;

import com.banking.system.entity.Cliente;
import com.banking.system.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
@DisplayName("Tests de ClienteController")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    @Test
    @DisplayName("POST /api/clientes - Debe crear un cliente")
    void crearCliente_ok() throws Exception {
        Cliente request = new Cliente();
        request.setTipoIdentificacion("CC");
        request.setNumeroIdentificacion("123456789");
        request.setNombres("Juan");
        request.setApellido("Pérez");
        request.setCorreoElectronico("juan.perez@example.com");
        request.setFechaNacimiento(LocalDate.of(1990, 1, 15));

        Cliente response = new Cliente();
        response.setId(1L);
        response.setTipoIdentificacion(request.getTipoIdentificacion());
        response.setNumeroIdentificacion(request.getNumeroIdentificacion());
        response.setNombres(request.getNombres());
        response.setApellido(request.getApellido());
        response.setCorreoElectronico(request.getCorreoElectronico());
        response.setFechaNacimiento(request.getFechaNacimiento());

        when(clienteService.crearCliente(any(Cliente.class))).thenReturn(response);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.correoElectronico").value("juan.perez@example.com"));
    }

    @Test
    @DisplayName("POST /api/clientes - Regla de negocio inválida (400 con {error})")
    void crearCliente_badRequest() throws Exception {
        Cliente request = new Cliente();
        request.setTipoIdentificacion("CC");
        request.setNumeroIdentificacion("123456789");
        request.setNombres("Juan");
        request.setApellido("Pérez");
        request.setCorreoElectronico("juan.perez@example.com");
        request.setFechaNacimiento(LocalDate.of(1990, 1, 15));

        when(clienteService.crearCliente(any(Cliente.class)))
                .thenThrow(new IllegalArgumentException("Ya existe un cliente con el correo electrónico"));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ya existe un cliente con el correo electrónico"));
    }

    @Test
    @DisplayName("GET /api/clientes/{id} - Cliente inexistente (404 con {error})")
    void obtenerClientePorId_notFound() throws Exception {
        when(clienteService.obtenerClientePorId(999L))
                .thenThrow(new IllegalArgumentException("Cliente no encontrado con ID: 999"));

        mockMvc.perform(get("/api/clientes/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Cliente no encontrado con ID: 999"));
    }

    @Test
    @DisplayName("PUT /api/clientes/{id} - Debe actualizar un cliente")
    void actualizarCliente_ok() throws Exception {
        Cliente request = new Cliente();
        request.setTipoIdentificacion("CC");
        request.setNumeroIdentificacion("123456789");
        request.setNombres("Juan");
        request.setApellido("Pérez Martínez");
        request.setCorreoElectronico("juan.perez@example.com");
        request.setFechaNacimiento(LocalDate.of(1990, 1, 15));

        Cliente response = new Cliente();
        response.setId(1L);
        response.setTipoIdentificacion(request.getTipoIdentificacion());
        response.setNumeroIdentificacion(request.getNumeroIdentificacion());
        response.setNombres(request.getNombres());
        response.setApellido(request.getApellido());
        response.setCorreoElectronico(request.getCorreoElectronico());
        response.setFechaNacimiento(request.getFechaNacimiento());

        when(clienteService.actualizarCliente(org.mockito.ArgumentMatchers.eq(1L), any(Cliente.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/clientes/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.apellido").value("Pérez Martínez"));
    }

    @Test
    @DisplayName("DELETE /api/clientes/{id} - Debe eliminar un cliente (200)")
    void eliminarCliente_ok() throws Exception {
        mockMvc.perform(delete("/api/clientes/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Cliente eliminado exitosamente"));
    }
}

