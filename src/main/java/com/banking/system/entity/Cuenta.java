package com.banking.system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Cuenta Representa una cuenta bancaria (corriente o ahorro)
 */
@Entity
@Table(name = "cuentas")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    private TipoCuenta tipoCuenta;

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 10)
    private String numeroCuenta;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCuenta estado;

    @NotNull(message = "El saldo es obligatorio")
    @DecimalMin(value = "0.0", message = "El saldo no puede ser negativo")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @NotNull(message = "El campo exenta GMF es obligatorio")
    @Column(name = "exenta_gmf", nullable = false)
    private Boolean exentaGMF;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Relación con Cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"cuentas", "hibernateLazyInitializer", "handler"})
    private Cliente cliente;

    // ============================================
    // ENUMS
    // ============================================
    public enum TipoCuenta {
        CUENTA_CORRIENTE,
        CUENTA_AHORRO
    }

    public enum EstadoCuenta {
        ACTIVA,
        INACTIVA,
        CANCELADA
    }

    // ============================================
    // CONSTRUCTORES
    // ============================================
    public Cuenta() {
    }

    public Cuenta(TipoCuenta tipoCuenta, String numeroCuenta, EstadoCuenta estado,
            BigDecimal saldo, Boolean exentaGMF, Cliente cliente) {
        this.tipoCuenta = tipoCuenta;
        this.numeroCuenta = numeroCuenta;
        this.estado = estado;
        this.saldo = saldo;
        this.exentaGMF = exentaGMF;
        this.cliente = cliente;
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

    public TipoCuenta getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(TipoCuenta tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuenta estado) {
        this.estado = estado;
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    // ============================================
    // MÉTODOS DE NEGOCIO
    // ============================================
    /**
     * Verifica si la cuenta puede ser cancelada (saldo = 0)
     */
    public boolean puedeSerCancelada() {
        return saldo.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Verifica si es una cuenta de ahorro
     */
    public boolean esCuentaAhorro() {
        return tipoCuenta == TipoCuenta.CUENTA_AHORRO;
    }

    /**
     * Verifica si es una cuenta corriente
     */
    public boolean esCuentaCorriente() {
        return tipoCuenta == TipoCuenta.CUENTA_CORRIENTE;
    }

    /**
     * Aumenta el saldo de la cuenta
     */
    public void aumentarSaldo(BigDecimal monto) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        this.saldo = this.saldo.add(monto);
    }

    /**
     * Disminuye el saldo de la cuenta
     */
    public void disminuirSaldo(BigDecimal monto) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }

        BigDecimal nuevoSaldo = this.saldo.subtract(monto);

        // Validar que cuentas de ahorro no queden en negativo
        if (esCuentaAhorro() && nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Las cuentas de ahorro no pueden tener saldo negativo");
        }

        this.saldo = nuevoSaldo;
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
        Cuenta cuenta = (Cuenta) o;
        return id != null && id.equals(cuenta.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Cuenta{"
                + "id=" + id
                + ", tipoCuenta=" + tipoCuenta
                + ", numeroCuenta='" + numeroCuenta + '\''
                + ", estado=" + estado
                + ", saldo=" + saldo
                + ", exentaGMF=" + exentaGMF
                + '}';
    }
}
