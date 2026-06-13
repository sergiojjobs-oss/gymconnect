package com.gymconnect.api.service;

import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.repository.EntrenadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EntrenadorService {

    private final EntrenadorRepository repo;

    public List<Entrenador> buscar(String especialidad, Double precioMin, Double precioMax, Double ratingMin) {
        return repo.buscarConFiltros(especialidad, precioMin, precioMax, ratingMin);
    }

    public Entrenador getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado: " + id));
    }
}
