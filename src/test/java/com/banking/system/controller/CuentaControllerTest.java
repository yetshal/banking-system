package com.banking.system.controller;

import com.banking.system.entity.Cliente;
import com.banking.system.entity.Cuenta;
import com.banking.system.entity.Cuenta.EstadoCuenta;
import com.banking.system.entity.Cuenta.TipoCuenta;
import com.banking.system.service.CuentaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CuentaController.class)
@DisplayName("Tests de CuentaController")
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CuentaService cuentaService;

    @Test
    @DisplayName("POST /api/cuentas?clienteId=X - Debe crear cuenta")
    void crearCuenta_ok() throws Exception {
        CuentaRequest request = new CuentaRequest();
        request.setTipoCuenta(TipoCuenta.CUENTA_AHORRO);
        request.setSaldo(new BigDecimal("1000.00"));
        request.setExentaGMF(false);

        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Cuenta response = new Cuenta();
        response.setId(10L);
        response.setTipoCuenta(TipoCuenta.CUENTA_AHORRO);
        response.setNumeroCuenta("5300000001");
        response.setEstado(EstadoCuenta.ACTIVA);
        response.setSaldo(new BigDecimal("1000.00"));
        response.setExentaGMF(false);
        response.setCliente(cliente);

        when(cuentaService.crearCuenta(any(Cuenta.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/cuentas")
                        .queryParam("clienteId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.numeroCuenta").value("5300000001"))
                .andExpect(jsonPath("$.tipoCuenta").value("CUENTA_AHORRO"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    @DisplayName("GET /api/cuentas/{id} - Debe retornar cuenta")
    void obtenerCuentaPorId_ok() throws Exception {
        Cuenta response = new Cuenta();
        response.setId(10L);
        response.setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);
        response.setNumeroCuenta("3300000001");
        response.setEstado(EstadoCuenta.ACTIVA);
        response.setSaldo(new BigDecimal("0.00"));
        response.setExentaGMF(true);

        when(cuentaService.obtenerCuentaPorId(10L)).thenReturn(response);

        mockMvc.perform(get("/api/cuentas/{id}", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.numeroCuenta").value("3300000001"));
    }

    @Test
    @DisplayName("PATCH /api/cuentas/{id}/estado?estado=INACTIVA - Debe actualizar estado")
    void actualizarEstado_ok() throws Exception {
        Cuenta response = new Cuenta();
        response.setId(10L);
        response.setTipoCuenta(TipoCuenta.CUENTA_AHORRO);
        response.setNumeroCuenta("5300000001");
        response.setEstado(EstadoCuenta.INACTIVA);
        response.setSaldo(new BigDecimal("1000.00"));
        response.setExentaGMF(false);

        when(cuentaService.actualizarEstadoCuenta(10L, EstadoCuenta.INACTIVA)).thenReturn(response);

        mockMvc.perform(patch("/api/cuentas/{id}/estado", 10)
                        .queryParam("estado", "INACTIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVA"));
    }

    @Test
    @DisplayName("POST /api/cuentas/{id}/cancelar - Error de negocio (400 con {error})")
    void cancelarCuenta_badRequest() throws Exception {
        when(cuentaService.cancelarCuenta(10L))
                .thenThrow(new IllegalArgumentException("No se puede cancelar la cuenta. El saldo debe ser $0."));

        mockMvc.perform(post("/api/cuentas/{id}/cancelar", 10))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No se puede cancelar la cuenta. El saldo debe ser $0."));
    }

    @Test
    @DisplayName("DELETE /api/cuentas/{id} - Debe eliminar cuenta (200)")
    void eliminarCuenta_ok() throws Exception {
        mockMvc.perform(delete("/api/cuentas/{id}", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Cuenta eliminada exitosamente"));
    }
}

