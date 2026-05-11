package com.alvaroorgaz.easyfactura.service;

import com.alvaroorgaz.easyfactura.model.Factura;
import com.alvaroorgaz.easyfactura.model.FacturaDetalle;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class FacturaPdfService {

    private static final float MARGIN = 50f;
    private static final float PAGE_TOP = 780f;
    private static final float LINE_HEIGHT = 16f;
    private static final float TABLE_ROW_HEIGHT = 18f;

    public byte[] generarPdf(Factura factura,
                             List<FacturaDetalle> detalles,
                             BigDecimal baseImponible,
                             BigDecimal totalIva,
                             BigDecimal totalFactura) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            PdfPageState pageState = crearPagina(document);

            try {
                PDPageContentStream contentStream = pageState.contentStream;
                float y = pageState.y;

                y = escribirLinea(contentStream, fontBold, 18, MARGIN, y, "EasyFactura - Factura");
                y -= 8;
                y = escribirLinea(contentStream, fontRegular, 10, MARGIN, y, "Numero de factura: " + factura.getIdFactura());
                y = escribirLinea(contentStream, fontRegular, 10, MARGIN, y, "Fecha: " + factura.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                y -= 10;

                y = escribirLinea(contentStream, fontBold, 12, MARGIN, y, "Empresa emisora");
                y = escribirLinea(contentStream, fontRegular, 10, MARGIN, y, factura.getEmpresa().getNombre());
                y = escribirLinea(contentStream, fontRegular, 10, MARGIN, y, "CIF: " + factura.getEmpresa().getCif());
                y = escribirLinea(contentStream, fontRegular, 10, MARGIN, y, "Email: " + factura.getEmpresa().getEmail());
                y = escribirLinea(contentStream, fontRegular, 10, MARGIN, y, "Telefono: " + factura.getEmpresa().getTelefono());
                if (factura.getEmpresa().getDireccion() != null && !factura.getEmpresa().getDireccion().isBlank()) {
                    y = escribirLinea(contentStream, fontRegular, 10, MARGIN, y, "Direccion: " + factura.getEmpresa().getDireccion());
                }
                y -= 10;

                y = escribirLinea(contentStream, fontBold, 12, MARGIN, y, "Cliente");
                y = escribirLinea(contentStream, fontRegular, 10, MARGIN, y, factura.getCliente().getNombre());
                y = escribirLinea(contentStream, fontRegular, 10, MARGIN, y, "NIF: " + factura.getCliente().getNif());
                y = escribirLinea(contentStream, fontRegular, 10, MARGIN, y, "Email: " + factura.getCliente().getEmail());
                y = escribirLinea(contentStream, fontRegular, 10, MARGIN, y, "Telefono: " + factura.getCliente().getTelefono());
                if (factura.getCliente().getDireccion() != null && !factura.getCliente().getDireccion().isBlank()) {
                    y = escribirLinea(contentStream, fontRegular, 10, MARGIN, y, "Direccion: " + factura.getCliente().getDireccion());
                }
                y -= 14;

                y = escribirLinea(contentStream, fontBold, 12, MARGIN, y, "Lineas de factura");
                y -= 6;

                dibujarCabeceraTabla(contentStream, fontBold, y);
                y -= TABLE_ROW_HEIGHT;

                for (FacturaDetalle detalle : detalles) {
                    if (y < 120) {
                        pageState = nuevaPagina(document, pageState);
                        contentStream = pageState.contentStream;
                        y = pageState.y;
                        y = escribirLinea(contentStream, fontBold, 12, MARGIN, y, "Lineas de factura (continuacion)");
                        y -= 6;
                        dibujarCabeceraTabla(contentStream, fontBold, y);
                        y -= TABLE_ROW_HEIGHT;
                    }

                    BigDecimal baseLinea = detalle.getPrecioUnitario()
                            .multiply(BigDecimal.valueOf(detalle.getCantidad()))
                            .setScale(2, RoundingMode.HALF_UP);

                    escribirTexto(contentStream, fontRegular, 9, 55, y, recortar(detalle.getProducto().getNombre(), 25));
                    escribirTexto(contentStream, fontRegular, 9, 245, y, String.valueOf(detalle.getCantidad()));
                    escribirTexto(contentStream, fontRegular, 9, 300, y, formatearImporte(detalle.getPrecioUnitario()));
                    escribirTexto(contentStream, fontRegular, 9, 385, y, detalle.getIva() + "%");
                    escribirTexto(contentStream, fontRegular, 9, 445, y, formatearImporte(baseLinea));
                    escribirTexto(contentStream, fontRegular, 9, 515, y, formatearImporte(detalle.getTotal()));
                    y -= TABLE_ROW_HEIGHT;
                }

                if (y < 120) {
                    pageState = nuevaPagina(document, pageState);
                    contentStream = pageState.contentStream;
                    y = pageState.y;
                }

                y -= 10;
                y = escribirLinea(contentStream, fontBold, 12, 360, y, "Resumen");
                y = escribirLinea(contentStream, fontRegular, 10, 360, y, "Base imponible: " + formatearImporte(baseImponible));
                y = escribirLinea(contentStream, fontRegular, 10, 360, y, "Total IVA: " + formatearImporte(totalIva));
                y = escribirLinea(contentStream, fontBold, 11, 360, y, "Total factura: " + formatearImporte(totalFactura));
            } finally {
                pageState.contentStream.close();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el PDF de la factura.", e);
        }
    }

    private float escribirLinea(PDPageContentStream contentStream,
                                PDType1Font font,
                                float fontSize,
                                float x,
                                float y,
                                String texto) throws IOException {
        escribirTexto(contentStream, font, fontSize, x, y, texto);
        return y - LINE_HEIGHT;
    }

    private void escribirTexto(PDPageContentStream contentStream,
                               PDType1Font font,
                               float fontSize,
                               float x,
                               float y,
                               String texto) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(texto);
        contentStream.endText();
    }

    private void dibujarCabeceraTabla(PDPageContentStream contentStream,
                                      PDType1Font font,
                                      float y) throws IOException {
        escribirTexto(contentStream, font, 9, 55, y, "Producto");
        escribirTexto(contentStream, font, 9, 245, y, "Ud.");
        escribirTexto(contentStream, font, 9, 300, y, "P. unit.");
        escribirTexto(contentStream, font, 9, 385, y, "IVA");
        escribirTexto(contentStream, font, 9, 445, y, "Base");
        escribirTexto(contentStream, font, 9, 515, y, "Total");
    }

    private String recortar(String texto, int maximo) {
        if (texto == null) {
            return "";
        }

        if (texto.length() <= maximo) {
            return texto;
        }

        return texto.substring(0, maximo - 3) + "...";
    }

    private String formatearImporte(BigDecimal valor) {
        return String.format(Locale.US, "%.2f EUR", valor);
    }

    private PdfPageState crearPagina(PDDocument document) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        return new PdfPageState(contentStream, PAGE_TOP);
    }

    private PdfPageState nuevaPagina(PDDocument document, PdfPageState estadoActual) throws IOException {
        estadoActual.contentStream.close();
        return crearPagina(document);
    }

    private static class PdfPageState {
        private final PDPageContentStream contentStream;
        private final float y;

        private PdfPageState(PDPageContentStream contentStream, float y) {
            this.contentStream = contentStream;
            this.y = y;
        }
    }
}
