package com.unab.parcial2_iot.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NumericSeries {
    private List<String> labels;
    private List<Double> data;
}

