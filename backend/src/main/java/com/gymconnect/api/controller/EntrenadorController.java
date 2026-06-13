package com.gymconnect.api.controller;

import com.gymconnect.api.dto.EntrenadorDto;
import com.gymconnect.api.service.EntrenadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entrenadores")
@RequiredArgsConstructor
public class EntrenadorController {

    private final EntrenadorService service;

    @GetMapping
    public ResponseEntity<List<EntrenadorDto>> buscar(
            @RequestParam(required = false) String especialidad,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) Double ratingMin) {
        return ResponseEntity.ok(service.buscar(especialidad, precioMin, precioMax, ratingMin)
                .stream().map(EntrenadorDto::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(EntrenadorDto.from(service.getById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
