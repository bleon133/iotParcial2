package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.PanelWidget;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PanelWidgetRepository extends JpaRepository<PanelWidget, UUID> {
    @EntityGraph(attributePaths = {"dispositivo","variable"})
    List<PanelWidget> findByPanel_IdOrderByPosAsc(UUID panelId);
}

