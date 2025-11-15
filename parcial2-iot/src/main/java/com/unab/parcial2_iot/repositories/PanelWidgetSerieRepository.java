package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.PanelWidgetSerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PanelWidgetSerieRepository extends JpaRepository<PanelWidgetSerie, UUID> {

    interface SerieDTO { UUID getVariableId(); String getColor(); String getLabel(); }

    @Query("select s.variable.id as variableId, s.color as color, s.label as label from PanelWidgetSerie s where s.widget.id = :wid")
    List<SerieDTO> findSeriesDto(@Param("wid") UUID widgetId);
}

