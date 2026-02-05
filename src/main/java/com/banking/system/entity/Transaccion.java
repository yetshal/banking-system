package com.banking.system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Transaccion Representa un movimiento financiero en una cuenta
 */
@Entity
@Table(name = "transacciones")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El tipo de transacción es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transaccion", nullable = false, length = 20)
    private TipoTransaccion tipoTransaccion;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(length = 200)
    private String descripcion;

    @CreationTimestamp
    @Column(name = "fecha_transaccion", nullable = false, updatable = false)
    private LocalDateTime fechaTransaccion;

    // Cuenta origen (para todas las transacciones)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_origen_id", nullable = false)
    @JsonIgnoreProperties({"cliente", "hibernateLazyInitializer", "handler"})
    private Cuenta cuentaOrigen;

    // Cuenta destino (solo para transferencias)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_destino_id")
    @JsonIgnoreProperties({"cliente", "hibernateLazyInitializer", "handler"})
    private Cuenta cuentaDestino;

    // Saldo después de la transacción
    @Column(name = "saldo_posterior", precision = 15, scale = 2)
    private BigDecimal saldoPosterior;

    // ============================================
    // ENUMS
    // ============================================
    public enum TipoTransaccion {
        CONSIGNACION,
        RETIRO,
        TRANSFERENCIA_ENVIADA,
        TRANSFERENCIA_RECIBIDA
    }

    // ============================================
    // CONSTRUCTORES
    // ============================================
    public Transaccion() {
    }

    public Transaccion(TipoTransaccion tipoTransaccion, BigDecimal monto,
            String descripcion, Cuenta cuentaOrigen) {
        this.tipoTransaccion = tipoTransaccion;
        this.monto = monto;
        this.descripcion = descripcion;
        this.cuentaOrigen = cuentaOrigen;
    }

    // ============================================
    // GETTERS Y SETTERS
    // ============================================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoTransaccion getTipoTransaccion() {
        return tipoTransaccion;
    }

    public void setTipoTransaccion(TipoTransaccion tipoTransaccion) {
        this.tipoTransaccion = tipoTransaccion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaTransaccion() {
        return fechaTransaccion;
    }

    public void setFechaTransaccion(LocalDateTime fechaTransaccion) {
        this.fechaTransaccion = fechaTransaccion;
    }

    public Cuenta getCuentaOrigen() {
        return cuentaOrigen;
    }

    public void setCuentaOrigen(Cuenta cuentaOrigen) {
        this.cuentaOrigen = cuentaOrigen;
    }

    public Cuenta getCuentaDestino() {
        return cuentaDestino;
    }

    public void setCuentaDestino(Cuenta cuentaDestino) {
        this.cuentaDestino = cuentaDestino;
    }

    public BigDecimal getSaldoPosterior() {
        return saldoPosterior;
    }

    public void setSaldoPosterior(BigDecimal saldoPosterior) {
        this.saldoPosterior = saldoPosterior;
    }

    // ============================================
    // EQUALS, HASHCODE Y TOSTRING
    // ============================================
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Transaccion that = (Transaccion) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Transaccion{"
                + "id=" + id
                + ", tipoTransaccion=" + tipoTransaccion
                + ", monto=" + monto
                + ", descripcion='" + descripcion + '\''
                + ", fechaTransaccion=" + fechaTransaccion
                + ", saldoPosterior=" + saldoPosterior
                + '}';
    }
}
