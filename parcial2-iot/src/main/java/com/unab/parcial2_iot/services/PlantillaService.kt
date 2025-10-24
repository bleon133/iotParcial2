package com.unab.parcial2_iot.services

import com.unab.parcial2_iot.models.Plantilla
import java.util.*


interface PlantillaService {
    fun listar(): MutableList<Plantilla?>?
    fun crear(nueva: Plantilla?): Plantilla?
    fun buscarPorId(id: UUID?): Optional<Plantilla?>?
    fun actualizar(id: UUID?, cambios: Plantilla?): Plantilla?
    fun eliminar(id: UUID?)
}