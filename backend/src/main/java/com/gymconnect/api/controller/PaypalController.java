package com.gymconnect.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.model.Relacion;
import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.EntrenadorRepository;
import com.gymconnect.api.repository.RelacionRepository;
import com.gymconnect.api.repository.UsuarioRepository;
import com.gymconnect.api.service.PaypalService;
import com.gymconnect.api.service.SuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/paypal")
@RequiredArgsConstructor
public class PaypalController {

    private final PaypalService paypalService;
    private final SuscripcionService suscripcionService;
    private final UsuarioRepository usuarioRepo;
    private final EntrenadorRepository entrenadorRepo;
    private final RelacionRepository relacionRepo;

    // ── Suscripción del entrenador ──────────────────────────────
    @PostMapping("/suscripcion/crear")
    public ResponseEntity<?> crearOrdenSuscripcion(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails ud) {
        try {
            String plan = body.get("plan"); // PRO o ELITE
            String importe = plan.equalsIgnoreCase("ELITE") ? "25.00" : "10.00";
            String descripcion = "GymConnect Plan " + plan + " — mensual";

            JsonNode orden = paypalService.crearOrden(descripcion, importe, "EUR");
            String orderId = orden.get("id").asText();

            // Buscar URL de aprobación
            String approveUrl = null;
            for (JsonNode link : orden.get("links")) {
                if ("approve".equals(link.get("rel").asText())) {
                    approveUrl = link.get("href").asText();
                    break;
                }
            }

            return ResponseEntity.ok(Map.of("orderId", orderId, "approveUrl", approveUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/suscripcion/capturar")
    public ResponseEntity<?> capturarSuscripcion(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails ud) {
        try {
            String orderId = body.get("orderId");
            String plan    = body.get("plan");

            JsonNode resultado = paypalService.capturarOrden(orderId);
            String estado = resultado.get("status").asText();

            if ("COMPLETED".equals(estado)) {
                Usuario u = usuarioRepo.findByEmail(ud.getUsername()).orElseThrow();
                suscripcionService.actualizarPlan(u.getId(), Usuario.PlanSuscripcion.valueOf(plan.toUpperCase()));
                return ResponseEntity.ok(Map.of("ok", true, "plan", plan));
            }

            return ResponseEntity.badRequest().body(Map.of("error", "Pago no completado: " + estado));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── Contratación del cliente ────────────────────────────────
    @PostMapping("/contratacion/crear")
    public ResponseEntity<?> crearOrdenContratacion(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails ud) {
        try {
            String entrenadorNombre = body.get("entrenadorNombre");
            String entrenadorId     = body.get("entrenadorId");
            String importe          = body.get("importe");
            String descripcion      = "GymConnect — Entrenador: " + entrenadorNombre;

            // Buscar PayPal del entrenador para que el dinero vaya a su cuenta
            String payeeEmail = null;
            if (entrenadorId != null) {
                payeeEmail = entrenadorRepo.findById(Long.parseLong(entrenadorId))
                        .map(e -> e.getPaypalEmail())
                        .orElse(null);
            }

            JsonNode orden = paypalService.crearOrdenConPayee(descripcion, importe, "EUR", payeeEmail);
            String orderId = orden.get("id").asText();

            String approveUrl = null;
            for (JsonNode link : orden.get("links")) {
                if ("approve".equals(link.get("rel").asText())) {
                    approveUrl = link.get("href").asText();
                    break;
                }
            }

            return ResponseEntity.ok(Map.of("orderId", orderId, "approveUrl", approveUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/contratacion/capturar")
    public ResponseEntity<?> capturarContratacion(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails ud) {
        try {
            String orderId      = body.get("orderId");
            String entrenadorId = body.get("entrenadorId");

            JsonNode resultado = paypalService.capturarOrden(orderId);
            String estado = resultado.get("status").asText();

            if ("COMPLETED".equals(estado)) {
                Usuario cliente = usuarioRepo.findByEmail(ud.getUsername()).orElseThrow();
                Entrenador entrenador = entrenadorRepo.findById(Long.parseLong(entrenadorId))
                        .orElseThrow(() -> new RuntimeException("Entrenador no encontrado"));

                // Solo crear relación si no existe ya una activa
                boolean yaExiste = relacionRepo.existsByClienteIdAndEntrenadorIdAndEstado(
                        cliente.getId(), entrenador.getId(), Relacion.Estado.ACTIVA);

                if (!yaExiste) {
                    Relacion relacion = new Relacion();
                    relacion.setCliente(cliente);
                    relacion.setEntrenador(entrenador);
                    relacion.setEstado(Relacion.Estado.ACTIVA);
                    relacionRepo.save(relacion);
                }

                return ResponseEntity.ok(Map.of("ok", true, "entrenadorId", entrenador.getId()));
            }

            return ResponseEntity.badRequest().body(Map.of("error", "Pago no completado: " + estado));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
