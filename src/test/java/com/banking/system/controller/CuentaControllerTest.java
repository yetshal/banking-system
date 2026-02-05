package com.banking.system.controller;

import com.banking.system.entity.Cliente;
import com.banking.system.entity.Cuenta;
import com.banking.system.entity.Cuenta.EstadoCuenta;
import com.banking.system.entity.Cuenta.TipoCuenta;
import com.banking.system.service.CuentaService;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;

/**
 * Tests unitarios para CuentaController
 */
@WebMvcTest(CuentaController.class)
@DisplayName("Tests de CuentaController")
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CuentaService cuentaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Cliente cliente;
    private Cuenta cuentaAhorro;
    private Cuenta cuentaCorriente;
    private CuentaRequest cuentaAhorroRequest;
    private CuentaRequest cuentaCorrienteRequest;

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
        cuentaAhorro.setNumeroCuenta("5300000001");
        cuentaAhorro.setEstado(EstadoCuenta.ACTIVA);
        cuentaAhorro.setSaldo(new BigDecimal("100000"));
        cuentaAhorro.setExentaGMF(false);
        cuentaAhorro.setCliente(cliente);

        cuentaCorriente = new Cuenta();
        cuentaCorriente.setId(2L);
        cuentaCorriente.setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);
        cuentaCorriente.setNumeroCuenta("3300000001");
        cuentaCorriente.setEstado(EstadoCuenta.ACTIVA);
        cuentaCorriente.setSaldo(new BigDecimal("50000"));
        cuentaCorriente.setExentaGMF(true);
        cuentaCorriente.setCliente(cliente);

        cuentaAhorroRequest = new CuentaRequest(
                TipoCuenta.CUENTA_AHORRO,
                new BigDecimal("100000"),
                false
        );

        cuentaCorrienteRequest = new CuentaRequest(
                TipoCuenta.CUENTA_CORRIENTE,
                new BigDecimal("50000"),
                true
        );
    }

    @Test
    @DisplayName("Crear cuenta de ahorro - Debe retornar 201 Created")
    void testCrearCuenta_AhorroExitoso() throws Exception {
        when(cuentaService.crearCuenta(any(Cuenta.class), eq(1L)))
                .thenReturn(cuentaAhorro);

        mockMvc.perform(post("/api/cuentas")
                .param("clienteId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cuentaAhorroRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.tipoCuenta", is("CUENTA_AHORRO")))
                .andExpect(jsonPath("$.numeroCuenta", is("5300000001")))
                .andExpect(jsonPath("$.estado", is("ACTIVA")));

        verify(cuentaService, times(1)).crearCuenta(any(Cuenta.class), eq(1L));
    }

    @Test
    @DisplayName("Crear cuenta corriente - Debe retornar 201 Created")
    void testCrearCuenta_CorrienteExitoso() throws Exception {
        when(cuentaService.crearCuenta(any(Cuenta.class), eq(1L)))
                .thenReturn(cuentaCorriente);

        mockMvc.perform(post("/api/cuentas")
                .param("clienteId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cuentaCorrienteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoCuenta", is("CUENTA_CORRIENTE")))
                .andExpect(jsonPath("$.numeroCuenta", startsWith("33")));

        verify(cuentaService, times(1)).crearCuenta(any(Cuenta.class), eq(1L));
    }

    @Test
    @DisplayName("Crear cuenta - Cliente no existe")
    void testCrearCuenta_ClienteNoExiste() throws Exception {
        when(cuentaService.crearCuenta(any(Cuenta.class), eq(999L)))
                .thenThrow(new IllegalArgumentException("Cliente no encontrado con ID: 999"));

        mockMvc.perform(post("/api/cuentas")
                .param("clienteId", "999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cuentaAhorroRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Cliente no encontrado")));
    }

    @Test
    @DisplayName("Crear cuenta con saldo negativo - Debe retornar 400")
    void testCrearCuenta_SaldoNegativo() throws Exception {
        when(cuentaService.crearCuenta(any(Cuenta.class), anyLong()))
                .thenThrow(new IllegalArgumentException("El saldo no puede ser negativo"));

        CuentaRequest requestInvalida = new CuentaRequest(
                TipoCuenta.CUENTA_AHORRO,
                new BigDecimal("-1000"),
                false
        );

        mockMvc.perform(post("/api/cuentas")
                .param("clienteId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("El saldo no puede ser negativo")));
    }

    @Test
    @DisplayName("Obtener todas las cuentas - Debe retornar lista")
    void testObtenerTodasLasCuentas() throws Exception {
        List<Cuenta> cuentas = Arrays.asList(cuentaAhorro, cuentaCorriente);

        when(cuentaService.obtenerTodasLasCuentas()).thenReturn(cuentas);

        mockMvc.perform(get("/api/cuentas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipoCuenta", is("CUENTA_AHORRO")))
                .andExpect(jsonPath("$[1].tipoCuenta", is("CUENTA_CORRIENTE")));

        verify(cuentaService, times(1)).obtenerTodasLasCuentas();
    }

    @Test
    @DisplayName("Obtener todas las cuentas - Lista vacía")
    void testObtenerTodasLasCuentas_ListaVacia() throws Exception {
        when(cuentaService.obtenerTodasLasCuentas()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/cuentas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Obtener cuenta por ID - Cuenta existe")
    void testObtenerCuentaPorId_Existente() throws Exception {
        when(cuentaService.obtenerCuentaPorId(1L)).thenReturn(cuentaAhorro);

        mockMvc.perform(get("/api/cuentas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.numeroCuenta", is("5300000001")));

        verify(cuentaService, times(1)).obtenerCuentaPorId(1L);
    }

    @Test
    @DisplayName("Obtener cuenta por ID - Cuenta no existe")
    void testObtenerCuentaPorId_NoExiste() throws Exception {
        when(cuentaService.obtenerCuentaPorId(999L))
                .thenThrow(new IllegalArgumentException("Cuenta no encontrada con ID: 999"));

        mockMvc.perform(get("/api/cuentas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Cuenta no encontrada")));
    }

    @Test
    @DisplayName("Obtener cuenta por número - Cuenta existe")
    void testObtenerCuentaPorNumero_Existente() throws Exception {
        when(cuentaService.obtenerCuentaPorNumero("5300000001")).thenReturn(cuentaAhorro);

        mockMvc.perform(get("/api/cuentas/numero/5300000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta", is("5300000001")))
                .andExpect(jsonPath("$.tipoCuenta", is("CUENTA_AHORRO")));

        verify(cuentaService, times(1)).obtenerCuentaPorNumero("5300000001");
    }

    @Test
    @DisplayName("Obtener cuenta por número - Cuenta no existe")
    void testObtenerCuentaPorNumero_NoExiste() throws Exception {
        when(cuentaService.obtenerCuentaPorNumero("9999999999"))
                .thenThrow(new IllegalArgumentException("Cuenta no encontrada con número: 9999999999"));

        mockMvc.perform(get("/api/cuentas/numero/9999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Cuenta no encontrada")));
    }

    @Test
    @DisplayName("Obtener cuentas por cliente - Debe retornar lista")
    void testObtenerCuentasPorCliente() throws Exception {
        List<Cuenta> cuentasCliente = Arrays.asList(cuentaAhorro, cuentaCorriente);

        when(cuentaService.obtenerCuentasPorCliente(1L)).thenReturn(cuentasCliente);

        mockMvc.perform(get("/api/cuentas/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(cuentaService, times(1)).obtenerCuentasPorCliente(1L);
    }

    @Test
    @DisplayName("Obtener cuentas por cliente - Cliente sin cuentas")
    void testObtenerCuentasPorCliente_SinCuentas() throws Exception {
        when(cuentaService.obtenerCuentasPorCliente(1L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/cuentas/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Obtener cuentas por cliente - Cliente no existe")
    void testObtenerCuentasPorCliente_ClienteNoExiste() throws Exception {
        when(cuentaService.obtenerCuentasPorCliente(999L))
                .thenThrow(new IllegalArgumentException("Cliente no encontrado con ID: 999"));

        mockMvc.perform(get("/api/cuentas/cliente/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Cliente no encontrado")));
    }

    @Test
    @DisplayName("Actualizar estado de cuenta a INACTIVA")
    void testActualizarEstadoCuenta_Inactivar() throws Exception {
        Cuenta cuentaInactiva = new Cuenta();
        cuentaInactiva.setId(1L);
        cuentaInactiva.setEstado(EstadoCuenta.INACTIVA);

        when(cuentaService.actualizarEstadoCuenta(1L, EstadoCuenta.INACTIVA))
                .thenReturn(cuentaInactiva);

        mockMvc.perform(patch("/api/cuentas/1/estado")
                .param("estado", "INACTIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("INACTIVA")));

        verify(cuentaService, times(1)).actualizarEstadoCuenta(1L, EstadoCuenta.INACTIVA);
    }

    @Test
    @DisplayName("Actualizar estado de cuenta a ACTIVA")
    void testActualizarEstadoCuenta_Activar() throws Exception {
        when(cuentaService.actualizarEstadoCuenta(1L, EstadoCuenta.ACTIVA))
                .thenReturn(cuentaAhorro);

        mockMvc.perform(patch("/api/cuentas/1/estado")
                .param("estado", "ACTIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("ACTIVA")));
    }

    @Test
    @DisplayName("Actualizar estado - Cuenta no existe")
    void testActualizarEstadoCuenta_NoExiste() throws Exception {
        when(cuentaService.actualizarEstadoCuenta(999L, EstadoCuenta.ACTIVA))
                .thenThrow(new IllegalArgumentException("Cuenta no encontrada con ID: 999"));

        mockMvc.perform(patch("/api/cuentas/999/estado")
                .param("estado", "ACTIVA"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cancelar cuenta con saldo cero")
    void testCancelarCuenta_SaldoCero() throws Exception {
        Cuenta cuentaCancelada = new Cuenta();
        cuentaCancelada.setId(1L);
        cuentaCancelada.setEstado(EstadoCuenta.CANCELADA);
        cuentaCancelada.setSaldo(BigDecimal.ZERO);

        when(cuentaService.cancelarCuenta(1L)).thenReturn(cuentaCancelada);

        mockMvc.perform(post("/api/cuentas/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADA")));

        verify(cuentaService, times(1)).cancelarCuenta(1L);
    }

    @Test
    @DisplayName("Cancelar cuenta con saldo - Debe retornar 400")
    void testCancelarCuenta_ConSaldo() throws Exception {
        when(cuentaService.cancelarCuenta(1L))
                .thenThrow(new IllegalArgumentException("No se puede cancelar la cuenta. El saldo debe ser $0. Saldo actual: $100000"));

        mockMvc.perform(post("/api/cuentas/1/cancelar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("saldo debe ser $0")));
    }

    @Test
    @DisplayName("Cancelar cuenta - Cuenta no existe")
    void testCancelarCuenta_NoExiste() throws Exception {
        when(cuentaService.cancelarCuenta(999L))
                .thenThrow(new IllegalArgumentException("Cuenta no encontrada con ID: 999"));

        mockMvc.perform(post("/api/cuentas/999/cancelar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Cuenta no encontrada")));
    }

    @Test
    @DisplayName("Eliminar cuenta cancelada")
    void testEliminarCuenta_Exitoso() throws Exception {
        doNothing().when(cuentaService).eliminarCuenta(1L);

        mockMvc.perform(delete("/api/cuentas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje", is("Cuenta eliminada exitosamente")));

        verify(cuentaService, times(1)).eliminarCuenta(1L);
    }

    @Test
    @DisplayName("Eliminar cuenta activa - Debe retornar 400")
    void testEliminarCuenta_CuentaActiva() throws Exception {
        doThrow(new IllegalArgumentException("Solo se pueden eliminar cuentas canceladas"))
                .when(cuentaService).eliminarCuenta(1L);

        mockMvc.perform(delete("/api/cuentas/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Solo se pueden eliminar cuentas canceladas")));
    }

    @Test
    @DisplayName("Eliminar cuenta - Cuenta no existe")
    void testEliminarCuenta_NoExiste() throws Exception {
        doThrow(new IllegalArgumentException("Cuenta no encontrada con ID: 999"))
                .when(cuentaService).eliminarCuenta(999L);

        mockMvc.perform(delete("/api/cuentas/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Cuenta no encontrada")));
    }
}
