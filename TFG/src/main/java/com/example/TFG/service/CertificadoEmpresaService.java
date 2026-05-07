package com.example.TFG.service;

import com.example.TFG.model.Empresa;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HexFormat;

@Service
public class CertificadoEmpresaService {

    @Value("${app.certificados.directorio}")
    private String directorioCertificados;

    @Value("${app.certificados.master-secret}")
    private String masterSecret;

    public CertificadoEmpresaService() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public void generarCertificadoSiNoExiste(Empresa empresa) {
        try {
            Path rutaCertificado = obtenerRutaCertificado(empresa.getCif());

            if (Files.exists(rutaCertificado)) {
                return;
            }

            Files.createDirectories(rutaCertificado.getParent());

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            X509Certificate certificado = crearCertificadoAutofirmado(empresa, keyPair);

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);

            String alias = obtenerAlias(empresa.getCif());
            char[] password = obtenerPasswordCertificado(empresa.getCif());

            keyStore.setKeyEntry(alias, keyPair.getPrivate(), password, new Certificate[]{certificado});

            try (OutputStream outputStream = Files.newOutputStream(rutaCertificado)) {
                keyStore.store(outputStream, password);
            }

        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el certificado de la empresa.", e);
        }
    }

    public KeyStore cargarKeyStorePorEmpresa(Empresa empresa) {
        generarCertificadoSiNoExiste(empresa);

        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream inputStream = Files.newInputStream(obtenerRutaCertificado(empresa.getCif()))) {
                keyStore.load(inputStream, obtenerPasswordCertificado(empresa.getCif()));
            }
            return keyStore;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar el certificado de la empresa.", e);
        }
    }

    public String obtenerAlias(String cif) {
        return normalizarCif(cif);
    }

    public char[] obtenerPasswordCertificado(String cif) {
        try {
            String base = masterSecret + ":" + normalizarCif(cif);
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(base.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 24).toCharArray();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener la password del certificado.", e);
        }
    }

    public Path obtenerRutaCertificado(String cif) {
        return Paths.get(directorioCertificados)
                .toAbsolutePath()
                .resolve(normalizarCif(cif) + ".p12");
    }

    private X509Certificate crearCertificadoAutofirmado(Empresa empresa, KeyPair keyPair) throws Exception {
        LocalDateTime ahora = LocalDateTime.now();
        Date fechaInicio = Date.from(ahora.atZone(ZoneId.systemDefault()).toInstant());
        Date fechaFin = Date.from(ahora.plusYears(2).atZone(ZoneId.systemDefault()).toInstant());

        String distinguishedName = "CN=" + limpiarValorDn(empresa.getNombre())
                + ", SERIALNUMBER=" + limpiarValorDn(empresa.getCif())
                + ", O=EasyFactura"
                + ", E=" + limpiarValorDn(empresa.getEmail())
                + ", C=ES";

        X500Name subject = new X500Name(distinguishedName);

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subject,
                BigInteger.valueOf(System.currentTimeMillis()),
                fechaInicio,
                fechaFin,
                subject,
                keyPair.getPublic()
        );

        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
        certBuilder.addExtension(
                Extension.keyUsage,
                true,
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.nonRepudiation)
        );
        certBuilder.addExtension(
                Extension.subjectKeyIdentifier,
                false,
                new JcaX509ExtensionUtils().createSubjectKeyIdentifier(keyPair.getPublic())
        );

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(keyPair.getPrivate());

        X509CertificateHolder holder = certBuilder.build(signer);

        return new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(holder);
    }

    private String normalizarCif(String cif) {
        return cif.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private String limpiarValorDn(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.replace(",", " ").replace("\"", " ").trim();
    }
}
