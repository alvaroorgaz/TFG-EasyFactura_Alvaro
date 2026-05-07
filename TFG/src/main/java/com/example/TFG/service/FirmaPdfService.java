package com.example.TFG.service;

import com.example.TFG.model.Empresa;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;

@Service
public class FirmaPdfService {

    private final CertificadoEmpresaService certificadoEmpresaService;

    public FirmaPdfService(CertificadoEmpresaService certificadoEmpresaService) {
        this.certificadoEmpresaService = certificadoEmpresaService;

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public byte[] firmarPdf(byte[] pdfOriginal, Empresa empresa) {
        try {
            KeyStore keyStore = certificadoEmpresaService.cargarKeyStorePorEmpresa(empresa);
            String alias = certificadoEmpresaService.obtenerAlias(empresa.getCif());
            char[] password = certificadoEmpresaService.obtenerPasswordCertificado(empresa.getCif());

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password);
            Certificate[] chain = keyStore.getCertificateChain(alias);

            try (PDDocument document = Loader.loadPDF(pdfOriginal);
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                PDSignature signature = new PDSignature();
                signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
                signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
                signature.setName(empresa.getNombre());
                signature.setLocation("EasyFactura");
                signature.setReason("Firma digital de factura");
                signature.setSignDate(Calendar.getInstance());

                document.addSignature(signature);

                ExternalSigningSupport signingSupport = document.saveIncrementalForExternalSigning(outputStream);
                byte[] contenido = IOUtils.toByteArray(signingSupport.getContent());
                byte[] firmaCms = crearFirmaCms(contenido, privateKey, chain);
                signingSupport.setSignature(firmaCms);

                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo firmar el PDF de la factura.", e);
        }
    }

    private byte[] crearFirmaCms(byte[] contenido, PrivateKey privateKey, Certificate[] chain)
            throws Exception {
        X509Certificate certificado = (X509Certificate) chain[0];
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(privateKey);

        generator.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build())
                        .build(signer, certificado)
        );

        generator.addCertificates(new JcaCertStore(Arrays.asList(chain)));

        CMSTypedData cmsData = new CMSProcessableByteArray(contenido);
        CMSSignedData signedData = generator.generate(cmsData, false);
        return signedData.getEncoded();
    }
}
