package com.alvaroorgaz.easyfactura.service;

import com.alvaroorgaz.easyfactura.model.Factura;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class FacturaEmailService {

    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from}")
    private String from;

    public FacturaEmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void enviarFacturaFirmada(Factura factura, byte[] pdfFirmado) {
        try {
            if (from == null || from.isBlank()) {
                throw new RuntimeException("Debes configurar MAIL_FROM con un remitente verificado en Brevo.");
            }

            Set<String> destinatarios = new LinkedHashSet<>();
            agregarDestinatario(destinatarios, factura.getEmpresa().getEmail());
            agregarDestinatario(destinatarios, factura.getCliente().getEmail());

            if (destinatarios.isEmpty()) {
                throw new RuntimeException("La factura no tiene destinatarios de correo validos.");
            }

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(from);
            if (factura.getEmpresa().getEmail() != null && !factura.getEmpresa().getEmail().isBlank()) {
                helper.setReplyTo(factura.getEmpresa().getEmail().trim());
            }
            helper.setTo(destinatarios.toArray(String[]::new));
            helper.setSubject("Factura " + factura.getIdFactura() + " - " + factura.getEmpresa().getNombre());
            helper.setText(construirCuerpoTexto(factura), construirCuerpoHtml(factura));
            helper.addAttachment(
                    "factura-" + factura.getIdFactura() + ".pdf",
                    new ByteArrayResource(pdfFirmado),
                    "application/pdf"
            );

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar la factura por email.", e);
        }
    }

    private void agregarDestinatario(Set<String> destinatarios, String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        try {
            InternetAddress address = new InternetAddress(email.trim());
            address.validate();
            destinatarios.add(address.getAddress());
        } catch (Exception ignored) {
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
}
