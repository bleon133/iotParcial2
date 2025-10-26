package com.unab.parcial2_iot.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unab.parcial2_iot.dto.DispositivoForm;
import com.unab.parcial2_iot.models.*;
import com.unab.parcial2_iot.repositories.PlantillaRepository;
import com.unab.parcial2_iot.services.DispositivoService;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dispositivos")
public class DispositivoController {

    private final DispositivoService dispositivoService;
    private final PlantillaRepository plantillaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public String vista(Model model,
                        @RequestParam(value = "ok", required = false) String ok,
                        @RequestParam(value = "error", required = false) String error) {
        DispositivoForm form = new DispositivoForm();
        form.setEstado("habilitado"); // por defecto

        model.addAttribute("form", form);
        model.addAttribute("plantillas", plantillaRepository.findAll());
        model.addAttribute("tipos", TipoDispositivo.values());
        model.addAttribute("protocolos", Protocolo.values());
        model.addAttribute("dispositivos", dispositivoService.listar());
        model.addAttribute("ok", ok);
        model.addAttribute("error", error);
        return "dispositivos/crear"; // templates/dispositivos/crear.html
    }

    @PostMapping
    public String crear(@ModelAttribute("form") DispositivoForm form,
                        BindingResult binding,
                        Model model) {
        try {
            // Validación simple
            if (form.getPlantillaId() == null) {
                binding.rejectValue("plantillaId", "plantilla.obligatoria", "Selecciona una plantilla");
            }
            if (isBlank(form.getIdExterno())) {
                binding.rejectValue("idExterno", "idExt.obligatorio", "id_externo es obligatorio");
            }
            if (isBlank(form.getNombre())) {
                binding.rejectValue("nombre", "nombre.obligatorio", "El nombre es obligatorio");
            }
            if (form.getTipo() == null) {
                binding.rejectValue("tipo", "tipo.obligatorio", "Selecciona el tipo de dispositivo");
            }

            // Si hay errores, recargar combos y lista
            if (binding.hasErrors()) {
                model.addAttribute("plantillas", plantillaRepository.findAll());
                model.addAttribute("tipos", TipoDispositivo.values());
                model.addAttribute("protocolos", Protocolo.values());
                model.addAttribute("dispositivos", dispositivoService.listar());
                return "dispositivos/crear";
            }

            // Cargar plantilla
            Plantilla plantilla = plantillaRepository.findById(form.getPlantillaId())
                    .orElseThrow(() -> new IllegalArgumentException("Plantilla no encontrada"));

            // Parseo de etiquetas JSON (opcional)
            Map<String, Object> etiquetas = null;
            if (!isBlank(form.getEtiquetasJson())) {
                try {
                    etiquetas = objectMapper.readValue(
                            form.getEtiquetasJson().trim(),
                            new TypeReference<Map<String, Object>>() {});
                } catch (Exception je) {
                    binding.rejectValue("etiquetasJson", "json.invalido", "JSON inválido en etiquetas");
                }
            }
            if (binding.hasErrors()) {
                model.addAttribute("plantillas", plantillaRepository.findAll());
                model.addAttribute("tipos", TipoDispositivo.values());
                model.addAttribute("protocolos", Protocolo.values());
                model.addAttribute("dispositivos", dispositivoService.listar());
                return "dispositivos/crear";
            }

            // Construir entidad
            Dispositivo d = Dispositivo.builder()
                    .plantilla(plantilla)
                    .idExterno(form.getIdExterno())
                    .nombre(form.getNombre())
                    .tipo(form.getTipo())
                    .estado(isBlank(form.getEstado()) ? "habilitado" : form.getEstado())
                    .protocolo(form.getProtocolo()) // puede venir null → el service pondrá el de la plantilla
                    .topicoMqttTelemetria(form.getTopicoMqttTelemetria())
                    .topicoMqttComando(form.getTopicoMqttComando())
                    .latitud(form.getLatitud())
                    .longitud(form.getLongitud())
                    .etiquetas(etiquetas)
                    .build();

            dispositivoService.crear(d);
            return "redirect:/dispositivos?ok=Dispositivo%20creado";
        } catch (DataIntegrityViolationException dive) {
            binding.reject("db.integridad", "Violación de integridad: " + dive.getMostSpecificCause().getMessage());
        } catch (IllegalStateException dup) {
            binding.rejectValue("idExterno", "idExt.duplicado", dup.getMessage());
        } catch (IllegalArgumentException iae) {
            binding.reject("form.invalido", iae.getMessage());
        } catch (Exception ex) {
            binding.reject("form.error", "Error inesperado: " + ex.getMessage());
        }

        model.addAttribute("plantillas", plantillaRepository.findAll());
        model.addAttribute("tipos", TipoDispositivo.values());
        model.addAttribute("protocolos", Protocolo.values());
        model.addAttribute("dispositivos", dispositivoService.listar());
        return "dispositivos/crear";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") java.util.UUID id) {
        dispositivoService.eliminar(id);
        return "redirect:/dispositivos?ok=Dispositivo%20eliminado";
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}