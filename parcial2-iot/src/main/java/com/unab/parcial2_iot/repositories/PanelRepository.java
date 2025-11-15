package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.Panel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PanelRepository extends JpaRepository<Panel, UUID> { }

