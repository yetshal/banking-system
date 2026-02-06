package com.banking.system.controller;

import com.banking.system.entity.Cuenta;
import com.banking.system.entity.Transaccion;
import com.banking.system.entity.Transaccion.TipoTransaccion;
import com.banking.system.service.TransaccionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransaccionController.class)
@DisplayName("Tests de TransaccionController")
class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransaccionService transaccionService;

    @Test
    @DisplayName("POST /api/transacciones/consignacion - Debe crear transacción")
    void consignacion_ok() throws Exception {
        ConsignacionRequest request = new ConsignacionRequest();
        request.setCuentaId(1L);
        request.setMonto(new BigDecimal("250.00"));
        request.setDescripcion("Depósito inicial");

        Cuenta cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setNumeroCuenta("5300000001");
        cuenta.setTipoCuenta(Cuenta.TipoCuenta.CUENTA_AHORRO);
        cuenta.setEstado(Cuenta.EstadoCuenta.ACTIVA);
        cuenta.setSaldo(new BigDecimal("1250.00"));
        cuenta.setExentaGMF(false);

        Transaccion response = new Transaccion();
        response.setId(100L);
        response.setTipoTransaccion(TipoTransaccion.CONSIGNACION);
        response.setMonto(new BigDecimal("250.00"));
        response.setDescripcion("Depósito inicial");
        response.setCuentaOrigen(cuenta);
        response.setSaldoPosterior(new BigDecimal("1250.00"));

        when(transaccionService.realizarConsignacion(any(Long.class), any(BigDecimal.class), any(String.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/transacciones/consignacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.tipoTransaccion").value("CONSIGNACION"))
                .andExpect(jsonPath("$.monto").value(250.00));
    }

    @Test
    @DisplayName("POST /api/transacciones/transferencia - Debe retornar 2 transacciones")
    void transferencia_ok() throws Exception {
        TransferenciaRequest request = new TransferenciaRequest();
        request.setCuentaOrigenId(1L);
        request.setCuentaDestinoId(2L);
        request.setMonto(new BigDecimal("50.00"));
        request.setDescripcion("Pago");

        Transaccion envio = new Transaccion();
        envio.setId(201L);
        envio.setTipoTransaccion(TipoTransaccion.TRANSFERENCIA_ENVIADA);
        envio.setMonto(new BigDecimal("50.00"));
        envio.setDescripcion("Pago");

        Transaccion recepcion = new Transaccion();
        recepcion.setId(202L);
        recepcion.setTipoTransaccion(TipoTransaccion.TRANSFERENCIA_RECIBIDA);
        recepcion.setMonto(new BigDecimal("50.00"));
        recepcion.setDescripcion("Pago");

        when(transaccionService.realizarTransferencia(any(Long.class), any(Long.class), any(BigDecimal.class), any(String.class)))
                .thenReturn(List.of(envio, recepcion));

        mockMvc.perform(post("/api/transacciones/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipoTransaccion").value("TRANSFERENCIA_ENVIADA"))
                .andExpect(jsonPath("$[1].tipoTransaccion").value("TRANSFERENCIA_RECIBIDA"));
    }

    @Test
    @DisplayName("GET /api/transacciones/cuenta/{cuentaId} - Error de negocio (400 con {error})")
    void historialCuenta_badRequest() throws Exception {
        when(transaccionService.obtenerHistorialCuenta(999L))
                .thenThrow(new IllegalArgumentException("Cuenta no encontrada con ID: 999"));

        mockMvc.perform(get("/api/transacciones/cuenta/{cuentaId}", 999))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cuenta no encontrada con ID: 999"));
    }
}

