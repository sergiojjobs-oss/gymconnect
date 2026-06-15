package com.gymconnect.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
public class PaypalService {

    public PaypalService(RestTemplate rest) {
        this.rest = rest;
    }

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.base-url}")
    private String baseUrl;

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate rest;

    // Obtener token de acceso de PayPal
    private String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String credentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        headers.set("Authorization", "Basic " + credentials);

        HttpEntity<String> entity = new HttpEntity<>("grant_type=client_credentials", headers);
        ResponseEntity<JsonNode> resp = rest.exchange(
                baseUrl + "/v1/oauth2/token", HttpMethod.POST, entity, JsonNode.class);

        return resp.getBody().get("access_token").asText();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(getAccessToken());
        return h;
    }

    // Crear orden de pago con payee opcional (para pagos directos al entrenador)
    public JsonNode crearOrdenConPayee(String descripcion, String importe, String moneda, String payeeEmail) throws Exception {
        Map<String, Object> purchaseUnit = payeeEmail != null && !payeeEmail.isBlank()
                ? Map.of(
                    "description", descripcion,
                    "amount", Map.of("currency_code", moneda, "value", importe),
                    "payee", Map.of("email_address", payeeEmail))
                : Map.of(
                    "description", descripcion,
                    "amount", Map.of("currency_code", moneda, "value", importe));

        Map<String, Object> body = Map.of(
                "intent", "CAPTURE",
                "purchase_units", new Object[]{purchaseUnit},
                "application_context", Map.of(
                        "return_url", "http://localhost:3000/paypal-ok.html",
                        "cancel_url", "http://localhost:3000/paypal-cancel.html",
                        "brand_name", "MomentFit",
                        "user_action", "PAY_NOW"
                )
        );

        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(body), authHeaders());
        ResponseEntity<JsonNode> resp = rest.exchange(
                baseUrl + "/v2/checkout/orders", HttpMethod.POST, entity, JsonNode.class);
        return resp.getBody();
    }

    // Crear orden de pago
    public JsonNode crearOrden(String descripcion, String importe, String moneda) throws Exception {
        Map<String, Object> body = Map.of(
                "intent", "CAPTURE",
                "purchase_units", new Object[]{Map.of(
                        "description", descripcion,
                        "amount", Map.of("currency_code", moneda, "value", importe)
                )},
                "application_context", Map.of(
                        "return_url", "http://localhost:3000/paypal-ok.html",
                        "cancel_url", "http://localhost:3000/paypal-cancel.html",
                        "brand_name", "MomentFit",
                        "user_action", "PAY_NOW"
                )
        );

        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(body), authHeaders());
        ResponseEntity<JsonNode> resp = rest.exchange(
                baseUrl + "/v2/checkout/orders", HttpMethod.POST, entity, JsonNode.class);

        return resp.getBody();
    }

    // Capturar pago tras aprobación del usuario
    public JsonNode capturarOrden(String orderId) {
        HttpEntity<String> entity = new HttpEntity<>("{}", authHeaders());
        ResponseEntity<JsonNode> resp = rest.exchange(
                baseUrl + "/v2/checkout/orders/" + orderId + "/capture",
                HttpMethod.POST, entity, JsonNode.class);
        return resp.getBody();
    }
}
