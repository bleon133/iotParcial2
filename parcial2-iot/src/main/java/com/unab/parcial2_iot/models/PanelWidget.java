package com.unab.parcial2_iot.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "panel_widget", schema = "iot")
public class PanelWidget {
    @Id @GeneratedValue @org.hibernate.annotations.UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "panel_id", nullable = false)
    private Panel panel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variable_plantilla_id", nullable = false)
    private VariablePlantilla variable;

    private String titulo;
    @Column(name = "chart_type")
    private String chartType; // line | bar
    private String rango;     // 6h | 24h | 7d
    private String color;
    private Integer pos;
}

