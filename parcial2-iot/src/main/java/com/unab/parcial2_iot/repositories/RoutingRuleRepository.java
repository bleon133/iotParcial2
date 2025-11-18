package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoutingRuleRepository extends JpaRepository<RoutingRule, UUID> {
    List<RoutingRule> findByEnabledTrue();
}

