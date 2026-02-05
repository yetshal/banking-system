package com.banking.system.controller;

import com.banking.system.entity.Transaccion;
import com.banking.system.service.TransaccionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;

/**
 * Tests unitarios para TransaccionController
 */
@WebMvcTest(TransaccionController.class)
@DisplayName("Tests de TransaccionController")
class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransaccionService transaccionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Transaccion consignacion;
    private Transaccion retiro;
    private ConsignacionRequest consignacionRequest;
    private RetiroRequest retiroRequest;
    private TransferenciaRequest transferenciaRequest;

    @BeforeEach
    void setUp() {
        consignacion = new Transaccion();
        consignacion.setId(1L);
        consignacion.setMonto(new BigDecimal("50000"));
        consignacion.setDescripcion("Consignación test");
        consignacion.setFechaTransaccion(LocalDateTime.now());

        retiro = new Transaccion();
        retiro.setId(2L);
        retiro.setMonto(new BigDecimal("20000"));
        retiro.setDescripcion("Retiro test");
        retiro.setFechaTransaccion(LocalDateTime.now());

        consignacionRequest = new ConsignacionRequest(1L, new BigDecimal("50000"), "Consignación test");
        retiroRequest = new RetiroRequest(1L, new BigDecimal("20000"), "Retiro test");
        transferenciaRequest = new TransferenciaRequest(1L, 2L, new BigDecimal("30000"), "Transferencia test");
    }

    @Test
    @DisplayName("Realizar consignación - Debe retornar 201 Created")
    void testRealizarConsignacion_Exitosa() throws Exception {
        when(transaccionService.realizarConsignacion(
                eq(1L),
                any(BigDecimal.class),
                anyString()
        )).thenReturn(consignacion);

        mockMvc.perform(post("/api/transacciones/consignacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consignacionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.descripcion", is("Consignación test")));

        verify(transaccionService, times(1)).realizarConsignacion(
                eq(1L),
                eq(new BigDecimal("50000")),
                eq("Consignación test")
        );
    }

    @Test
    @DisplayName("Consignación con monto negativo - Debe retornar 400")
    void testRealizarConsignacion_MontoNegativo() throws Exception {
        when(transaccionService.realizarConsignacion(anyLong(), any(BigDecimal.class), anyString()))
                .thenThrow(new IllegalArgumentException("El monto debe ser mayor a cero"));

        ConsignacionRequest requestInvalida = new ConsignacionRequest(1L, new BigDecimal("-1000"), "Test");

        mockMvc.perform(post("/api/transacciones/consignacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("mayor a cero")));
    }

    @Test
    @DisplayName("Consignación en cuenta inactiva - Debe retornar 400")
    void testRealizarConsignacion_CuentaInactiva() throws Exception {
        when(transaccionService.realizarConsignacion(anyLong(), any(BigDecimal.class), anyString()))
                .thenThrow(new IllegalArgumentException("La cuenta debe estar activa"));

        mockMvc.perform(post("/api/transacciones/consignacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consignacionRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("La cuenta debe estar activa")));
    }

    @Test
    @DisplayName("Consignación en cuenta inexistente - Debe retornar 400")
    void testRealizarConsignacion_CuentaNoExiste() throws Exception {
        when(transaccionService.realizarConsignacion(anyLong(), any(BigDecimal.class), anyString()))
                .thenThrow(new IllegalArgumentException("Cuenta no encontrada con ID: 999"));

        ConsignacionRequest requestInvalida = new ConsignacionRequest(999L, new BigDecimal("10000"), "Test");

        mockMvc.perform(post("/api/transacciones/consignacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Cuenta no encontrada")));
    }

    @Test
    @DisplayName("Realizar retiro - Debe retornar 201 Created")
    void testRealizarRetiro_Exitoso() throws Exception {
        when(transaccionService.realizarRetiro(
                eq(1L),
                any(BigDecimal.class),
                anyString()
        )).thenReturn(retiro);

        mockMvc.perform(post("/api/transacciones/retiro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(retiroRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.descripcion", is("Retiro test")));

        verify(transaccionService, times(1)).realizarRetiro(
                eq(1L),
                eq(new BigDecimal("20000")),
                eq("Retiro test")
        );
    }

    @Test
    @DisplayName("Retiro con saldo insuficiente - Debe retornar 400")
    void testRealizarRetiro_SaldoInsuficiente() throws Exception {
        when(transaccionService.realizarRetiro(anyLong(), any(BigDecimal.class), anyString()))
                .thenThrow(new IllegalArgumentException("Saldo insuficiente"));

        RetiroRequest requestExcesiva = new RetiroRequest(1L, new BigDecimal("1000000"), "Test");

        mockMvc.perform(post("/api/transacciones/retiro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestExcesiva)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Saldo insuficiente")));
    }

    @Test
    @DisplayName("Retiro en cuenta inactiva - Debe retornar 400")
    void testRealizarRetiro_CuentaInactiva() throws Exception {
        when(transaccionService.realizarRetiro(anyLong(), any(BigDecimal.class), anyString()))
                .thenThrow(new IllegalArgumentException("La cuenta debe estar activa"));

        mockMvc.perform(post("/api/transacciones/retiro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(retiroRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("La cuenta debe estar activa")));
    }

    @Test
    @DisplayName("Realizar transferencia - Debe retornar 201 Created")
    void testRealizarTransferencia_Exitosa() throws Exception {
        Transaccion transferenciaDebito = new Transaccion();
        transferenciaDebito.setId(3L);
        transferenciaDebito.setMonto(new BigDecimal("30000"));
        transferenciaDebito.setDescripcion("Transferencia test - Débito");

        Transaccion transferenciaCredito = new Transaccion();
        transferenciaCredito.setId(4L);
        transferenciaCredito.setMonto(new BigDecimal("30000"));
        transferenciaCredito.setDescripcion("Transferencia test - Crédito");

        List<Transaccion> transacciones = Arrays.asList(transferenciaDebito, transferenciaCredito);

        when(transaccionService.realizarTransferencia(
                eq(1L),
                eq(2L),
                any(BigDecimal.class),
                anyString()
        )).thenReturn(transacciones);

        mockMvc.perform(post("/api/transacciones/transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferenciaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(3)))
                .andExpect(jsonPath("$[1].id", is(4)));

        verify(transaccionService, times(1)).realizarTransferencia(
                eq(1L),
                eq(2L),
                eq(new BigDecimal("30000")),
                eq("Transferencia test")
        );
    }

    @Test
    @DisplayName("Transferencia a misma cuenta - Debe retornar 400")
    void testRealizarTransferencia_MismaCuenta() throws Exception {
        when(transaccionService.realizarTransferencia(anyLong(), anyLong(), any(BigDecimal.class), anyString()))
                .thenThrow(new IllegalArgumentException("No se puede transferir a la misma cuenta"));

        TransferenciaRequest requestInvalida = new TransferenciaRequest(1L, 1L, new BigDecimal("10000"), "Test");

        mockMvc.perform(post("/api/transacciones/transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("misma cuenta")));
    }

    @Test
    @DisplayName("Transferencia a cuenta inexistente - Debe retornar 400")
    void testRealizarTransferencia_CuentaDestinoNoExiste() throws Exception {
        when(transaccionService.realizarTransferencia(anyLong(), anyLong(), any(BigDecimal.class), anyString()))
                .thenThrow(new IllegalArgumentException("Cuenta destino no encontrada con ID: 999"));

        TransferenciaRequest requestInvalida = new TransferenciaRequest(1L, 999L, new BigDecimal("10000"), "Test");

        mockMvc.perform(post("/api/transacciones/transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Cuenta destino no encontrada")));
    }

    @Test
    @DisplayName("Transferencia con saldo insuficiente - Debe retornar 400")
    void testRealizarTransferencia_SaldoInsuficiente() throws Exception {
        when(transaccionService.realizarTransferencia(anyLong(), anyLong(), any(BigDecimal.class), anyString()))
                .thenThrow(new IllegalArgumentException("Saldo insuficiente"));

        TransferenciaRequest requestExcesiva = new TransferenciaRequest(1L, 2L, new BigDecimal("1000000"), "Test");

        mockMvc.perform(post("/api/transacciones/transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestExcesiva)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Saldo insuficiente")));
    }

    @Test
    @DisplayName("Obtener todas las transacciones - Debe retornar lista")
    void testObtenerTodasLasTransacciones() throws Exception {
        List<Transaccion> transacciones = Arrays.asList(consignacion, retiro);

        when(transaccionService.obtenerTodasLasTransacciones()).thenReturn(transacciones);

        mockMvc.perform(get("/api/transacciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(transaccionService, times(1)).obtenerTodasLasTransacciones();
    }

    @Test
    @DisplayName("Obtener transacción por ID - Transacción existe")
    void testObtenerTransaccionPorId_Existente() throws Exception {
        when(transaccionService.obtenerTransaccionPorId(1L)).thenReturn(consignacion);

        mockMvc.perform(get("/api/transacciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(transaccionService, times(1)).obtenerTransaccionPorId(1L);
    }

    @Test
    @DisplayName("Obtener transacción por ID - Transacción no existe")
    void testObtenerTransaccionPorId_NoExiste() throws Exception {
        when(transaccionService.obtenerTransaccionPorId(999L))
                .thenThrow(new IllegalArgumentException("Transacción no encontrada con ID: 999"));

        mockMvc.perform(get("/api/transacciones/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Transacción no encontrada")));
    }

    @Test
    @DisplayName("Obtener historial de cuenta - Debe retornar lista")
    void testObtenerHistorialCuenta() throws Exception {
        List<Transaccion> transacciones = Arrays.asList(consignacion, retiro);

        when(transaccionService.obtenerHistorialCuenta(1L)).thenReturn(transacciones);

        mockMvc.perform(get("/api/transacciones/cuenta/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(transaccionService, times(1)).obtenerHistorialCuenta(1L);
    }

    @Test
    @DisplayName("Obtener historial de cuenta vacía - Debe retornar lista vacía")
    void testObtenerHistorialCuenta_SinTransacciones() throws Exception {
        when(transaccionService.obtenerHistorialCuenta(1L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/transacciones/cuenta/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Obtener historial de cuenta inexistente - Debe retornar 400")
    void testObtenerHistorialCuenta_CuentaNoExiste() throws Exception {
        when(transaccionService.obtenerHistorialCuenta(999L))
                .thenThrow(new IllegalArgumentException("Cuenta no encontrada con ID: 999"));

        mockMvc.perform(get("/api/transacciones/cuenta/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Cuenta no encontrada")));
    }
}
