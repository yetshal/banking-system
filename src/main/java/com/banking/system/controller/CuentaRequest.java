package com.banking.system.controller;

import com.banking.system.entity.Cuenta.TipoCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO para recibir datos de creación de cuenta Evita problemas de serialización
 * JSON
 */
public class CuentaRequest {

    @NotNull(message = "El tipo de cuenta es obligatorio")
    private TipoCuenta tipoCuenta;

    @NotNull(message = "El saldo es obligatorio")
    @DecimalMin(value = "0.0", message = "El saldo no puede ser negativo")
    private BigDecimal saldo;

    @NotNull(message = "El campo exenta GMF es obligatorio")
    private Boolean exentaGMF;

    // Constructor vacío
    public CuentaRequest() {
    }

    // Constructor con parámetros
    public CuentaRequest(TipoCuenta tipoCuenta, BigDecimal saldo, Boolean exentaGMF) {
        this.tipoCuenta = tipoCuenta;
        this.saldo = saldo;
        this.exentaGMF = exentaGMF;
    }

    // Getters y Setters
    public TipoCuenta getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(TipoCuenta tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public Boolean getExentaGMF() {
        return exentaGMF;
    }

    public void setExentaGMF(Boolean exentaGMF) {
        this.exentaGMF = exentaGMF;
    }
}
