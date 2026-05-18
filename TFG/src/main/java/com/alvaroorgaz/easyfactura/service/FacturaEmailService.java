package com.alvaroorgaz.easyfactura.service;

import com.alvaroorgaz.easyfactura.model.Factura;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class FacturaEmailService {

    private static final Logger log = LoggerFactory.getLogger(FacturaEmailService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name:EasyFactura}")
    private String fromName;

    @Value("${app.brevo.api-key:}")
    private String brevoApiKey;

    @Value("${app.brevo.api-url:https://api.brevo.com/v3/smtp/email}")
    private String brevoApiUrl;

    public FacturaEmailService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void enviarFacturaFirmada(Factura factura, byte[] pdfFirmado) {
        try {
            validarConfiguracion();

            Set<String> destinatarios = new LinkedHashSet<>();
            agregarDestinatario(destinatarios, factura.getEmpresa().getEmail());
            agregarDestinatario(destinatarios, factura.getCliente().getEmail());

            if (destinatarios.isEmpty()) {
                throw new RuntimeException("La factura no tiene destinatarios de correo validos.");
            }

            String payload = objectMapper.writeValueAsString(construirPayload(factura, destinatarios, pdfFirmado));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(brevoApiUrl))
                    .timeout(Duration.ofSeconds(20))
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .header("api-key", brevoApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String detalle = extraerDetalleBrevo(response.body());
                log.error("Brevo respondio {} al enviar la factura {}. Cuerpo: {}", response.statusCode(), factura.getIdFactura(), response.body());
                throw new RuntimeException("Brevo devolvio " + response.statusCode() + ". " + detalle);
            }
        } catch (Exception e) {
            String detalle = obtenerDetalleError(e);
            log.error("Error al enviar la factura {} por la API de Brevo: {}", factura.getIdFactura(), detalle, e);
            throw new RuntimeException("No se pudo enviar la factura por email. Detalle: " + detalle, e);
        }
    }

    private void validarConfiguracion() {
        if (from == null || from.isBlank()) {
            throw new RuntimeException("Debes configurar MAIL_FROM con un remitente verificado en Brevo.");
        }

        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            throw new RuntimeException("Debes configurar BREVO_API_KEY en Render.");
        }
    }

    private Map<String, Object> construirPayload(Factura factura, Set<String> destinatarios, byte[] pdfFirmado) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sender", Map.of("name", valorSeguro(fromName, factura.getEmpresa().getNombre()), "email", from));
        payload.put("to", construirDestinatarios(destinatarios));
        payload.put("subject", "Factura " + factura.getIdFactura() + " - " + factura.getEmpresa().getNombre());
        payload.put("textContent", construirCuerpoTexto(factura));
        payload.put("htmlContent", construirCuerpoHtml(factura));

        if (factura.getEmpresa().getEmail() != null && !factura.getEmpresa().getEmail().isBlank()) {
            payload.put("replyTo", Map.of(
                    "email", factura.getEmpresa().getEmail().trim(),
                    "name", factura.getEmpresa().getNombre()
            ));
        }

        payload.put("attachment", List.of(Map.of(
                "name", "factura-" + factura.getIdFactura() + ".pdf",
                "content", Base64.getEncoder().encodeToString(pdfFirmado)
        )));

        return payload;
    }

    private List<Map<String, String>> construirDestinatarios(Set<String> destinatarios) {
        List<Map<String, String>> to = new ArrayList<>();
        for (String email : destinatarios) {
            to.add(Map.of("email", email));
        }
        return to;
    }

    private void agregarDestinatario(Set<String> destinatarios, String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        String emailNormalizado = email.trim();
        if (EMAIL_PATTERN.matcher(emailNormalizado).matches()) {
            destinatarios.add(emailNormalizado);
        }
    }

    private String construirCuerpoTexto(Factura factura) {
        return "Se adjunta la factura " + factura.getIdFactura()
                + " emitida por " + factura.getEmpresa().getNombre()
                + " para el cliente " + factura.getCliente().getNombre()
                + ". El documento PDF incluye la firma digital de la empresa emisora.";
    }

    private String construirCuerpoHtml(Factura factura) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #1f2937;">
                    <h2 style="margin-bottom: 8px;">Factura %s</h2>
                    <p>Se adjunta la factura <strong>%s</strong> emitida por <strong>%s</strong>.</p>
                    <p>Cliente asociado: <strong>%s</strong>.</p>
                    <p>El documento PDF incluye la firma digital de la empresa emisora.</p>
                    <p style="margin-top: 24px;">Equipo de EasyFactura</p>
                </body>
                </html>
                """.formatted(
                factura.getIdFactura(),
                factura.getIdFactura(),
                factura.getEmpresa().getNombre(),
                factura.getCliente().getNombre()
        );
    }

    private String extraerDetalleBrevo(String body) {
        if (body == null || body.isBlank()) {
            return "Respuesta vacia de la API de Brevo.";
        }

        try {
            Map<String, Object> respuesta = objectMapper.readValue(body, new TypeReference<>() {
            });

            if (respuesta.containsKey("message")) {
                return String.valueOf(respuesta.get("message"));
            }

            if (respuesta.containsKey("code")) {
                return String.valueOf(respuesta.get("code")) + ": " + respuesta.getOrDefault("message", body);
            }
        } catch (Exception ignored) {
        }

        return body;
    }

    private String obtenerDetalleError(Throwable throwable) {
        Throwable actual = throwable;
        while (actual.getCause() != null) {
            actual = actual.getCause();
        }

        String mensaje = actual.getMessage();
        if (mensaje == null || mensaje.isBlank()) {
            return actual.getClass().getSimpleName();
        }

        return mensaje;
    }

    private String valorSeguro(String valorPreferido, String valorAlternativo) {
        if (valorPreferido != null && !valorPreferido.isBlank()) {
            return valorPreferido;
        }
        return valorAlternativo;
    }
}
