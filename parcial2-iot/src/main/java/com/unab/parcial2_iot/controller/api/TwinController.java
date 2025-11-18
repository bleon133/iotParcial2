package com.unab.parcial2_iot.controller.api;

import com.unab.parcial2_iot.dto.TwinOut;
import com.unab.parcial2_iot.services.TwinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/twin")
public class TwinController {

    private final TwinService twinService;

    @GetMapping("/{dispositivoId}")
    public TwinOut getTwin(@PathVariable UUID dispositivoId) {
        return twinService.getTwin(dispositivoId);
    }

    @PatchMapping("/{dispositivoId}/desired")
    public TwinOut patchDesired(@PathVariable UUID dispositivoId,
                                @RequestBody Map<String, Object> desiredPatch) {
        return twinService.patchDesired(dispositivoId, desiredPatch);
    }

    @PostMapping("/{dispositivoId}/reported")
    public TwinOut updateReported(@PathVariable UUID dispositivoId,
                                  @RequestBody Map<String, Object> reportedPatch) {
        return twinService.updateReported(dispositivoId, reportedPatch);
    }
}

