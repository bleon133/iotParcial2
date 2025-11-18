package com.unab.parcial2_iot.services;

public interface RetentionService {
    void rollupHourly();
    void rollupDaily();
    void purgeOld();
}

