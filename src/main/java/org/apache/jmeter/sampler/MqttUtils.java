package org.apache.jmeter.sampler;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Slf4j
public class MqttUtils {

    public static SSLSocketFactory getSocketFactory(final String caCrtFile,
                                                    final String crtFile,
                                                    final String keyFile,
                                                    final char []password,
                                                    final String tlsVersion)
            throws Exception {
        log.info("TLS version {}", tlsVersion);
        Security.addProvider(new BouncyCastleProvider());
        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        // load CA certificate
        X509Certificate caCert = null;

        InputStream bis = new ByteArrayInputStream(caCrtFile.getBytes(StandardCharsets.UTF_8));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        int index = 0;
        while (bis.available() > 0) {
            caCert = (X509Certificate) cf.generateCertificate(bis);
            X500Name x500Name = new X500Name(caCert.getSubjectX500Principal().getName());
            caKs.setCertificateEntry(x500Name.getRDNs(BCStyle.CN)[0].getFirst().getValue().toASN1Primitive().toString(), caCert);
            log.info("Cargando {}", x500Name.getRDNs(BCStyle.CN)[0].getFirst().getValue().toASN1Primitive().toString());
        }

        // load client certificate
        bis = new ByteArrayInputStream(crtFile.getBytes(StandardCharsets.UTF_8));
        X509Certificate cert = null;
        while (bis.available() > 0) {
            cert = (X509Certificate) cf.generateCertificate(bis);
        }

        // load client private key
        PEMParser pemParser = new PEMParser(new StringReader(keyFile));
        Object object = pemParser.readObject();

        PrivateKey prvKey = null;
        if (object instanceof PEMEncryptedKeyPair) {
            log.info("PEMEncryptedKeyPair key - we will use provided password");
            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
                    .build(password);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
                    .setProvider("BC");
            prvKey = converter.getKeyPair(((PEMEncryptedKeyPair) object)
                    .decryptKeyPair(decProv)).getPrivate();
        } else if (object instanceof PEMKeyPair) {
            log.info("PEMKeyPair key - no password needed");
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
                    .setProvider("BC");
            prvKey = converter.getKeyPair((PEMKeyPair) object).getPrivate();
        } else if (object instanceof PrivateKeyInfo) {
            log.info("PrivateKeyInfo key - no password needed");
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            prvKey = converter.getPrivateKey((PrivateKeyInfo) object);
        } else {
            throw new UnsupportedOperationException("Private kay format doesn't supported");
        }
        pemParser.close();

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(caKs);


        // client key and certificates are sent to server so it can authenticate
        // us
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-key", prvKey, password, new java.security.cert.Certificate[]{cert});
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        kmf.init(ks, password);

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance(tlsVersion);
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return context.getSocketFactory();
    }
}
