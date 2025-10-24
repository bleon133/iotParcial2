package com.unab.parcial2_iot.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Histórico largo: una fila por (dispositivo, variable, timestamp).
 * El trigger en BD valida que SOLO uno de los cuatro campos valor_* sea NO nulo
 * y que coincida con el tipo_dato de VariablePlantilla.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "telemetria", schema = "iot",
        indexes = {
                @Index(name = "idx_tel_dev_var_ts", columnList = "dispositivo_id, variable_plantilla_id, ts DESC")
        })
public class Telemetria {

    /** BIGSERIAL */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Dispositivo que reporta */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;

    /** Variable reportada */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variable_plantilla_id", nullable = false)
    private VariablePlantilla variable;

    /** Momento de la medición */
    @Column(name = "ts", nullable = false)
    private OffsetDateTime ts;

    /** Valores posibles (uno y solo uno NO nulo) */
    @Column(name = "valor_numero")
    private Double valorNumero;

    @Column(name = "valor_booleano")
    private Boolean valorBooleano;

    @Column(name = "valor_texto")
    private String valorTexto;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valor_json")
    private Map<String, Object> valorJson;

    /** Tags puntuales del evento (opcional) */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> etiquetas;

    /* ------------ Ayudas para setear el valor correcto ------------ */

    /** Limpia todos los campos valor_* para reutilizar la entidad con seguridad */
    public void clearValores() {
        this.valorNumero = null;
        this.valorBooleano = null;
        this.valorTexto = null;
        this.valorJson = null;
    }

    public void setValorNumeroSeguro(Double v) { clearValores(); this.valorNumero = v; }
    public void setValorBooleanoSeguro(Boolean v) { clearValores(); this.valorBooleano = v; }
    public void setValorTextoSeguro(String v) { clearValores(); this.valorTexto = v; }
    public void setValorJsonSeguro(Map<String,Object> v) { clearValores(); this.valorJson = v; }
}