package com.threathunter.encrypt;

import com.threathunter.config.CommonDynamicConfig;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.security.*;

import static org.junit.Assert.assertNotNull;


/**
 * Created by yy on 17-9-13.
 */
public class SecurityFacadeTest {

    @Test
    public void testAsymmeticCrypto() throws Exception {
        AsymmetricCryptography ac = new AsymmetricCryptography();
        ClassLoader classLoader = getClass().getClassLoader();

//   File priv = new File(classLoader.getResource("rsa_key.priv").getFile());
//   File pub = new File(classLoader.getResource("rsa_key.pub").getFile());
//   System.out.println(file.getAbsolutePath());
//
        CommonDynamicConfig.getInstance().addConfigFile("test.conf");
        URL pathPrivate = classLoader.getResource("rsa_key.priv");
        PrivateKey privateKey = ac.getPrivate(pathPrivate.getFile());
        URL pathPublic = classLoader.getResource("rsa_key.pub");
        PublicKey publicKey = ac.getPublic(pathPublic.getPath());

//        String msg = "IW7xJPPmTy8lUVYIUlI5bnNjhmC+cDzTDpCcweKLHF6PLA9DCHKMWFAMbT3BpU6vReqAPd8yOoLDMu/m+SIcZ67j8E6dXHo+AVXHlThN6h+LaKehfuL6ZPXlH4hMmVmx013C9TjBgEnG3/s8Ba5VpFgKgn8atMU2kHDJcH6+6uyTHadgUl4/lsM2p6e2dXivD0JRiwyhfuPtmixoxbcn4rnhRex0mvRDQzhaflWa04OtBlnF6djdM/fHfE6qlLpS9sXa6ScwKnmNHtqbCoZahBJH1U+EdL32ZnZS9547HXypGFlUvzdpfvnqhRRCpICPV6ZegWdL91bzKOwlH6lIEw==";
        String msg = CommonDynamicConfig.getInstance().getString("redis_password");
        //use private key
//        String encrypted_msg = ac.encryptText(msg,privateKey);

        //use public key
//        String decrypted_msg = ac.decryptText(encrypted_msg, publicKey);
        String decrypted_msg=ac.decryptText(msg,privateKey);
//        System.out.println("\nEncrypted Message: " + encrypted_msg
//                + "\nDecrypted Message: " + decrypted_msg);
        System.out.println("\nDecrypted Message: " + decrypted_msg);
    }

    @Test
    public void testPrivateKey() throws IOException {

        PrivateKey privateKey = getPrivateKey();
//  PrivateKey privateKey=converter.getPrivateKey((PrivateKeyInfo)object);
        assertNotNull(privateKey);

    }

    @Test
    public void testPublicKey() throws IOException {

        PublicKey publicKey = getPublicKey();
        assertNotNull(publicKey);
//  publicKey=
    }

    public PrivateKey getPrivateKey() throws IOException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        ClassLoader classLoader = getClass().getClassLoader();
        File privateKeyFile = new File(classLoader.getResource("rsa_key.priv").getFile());
        PEMParser pemParser = new PEMParser(new FileReader(privateKeyFile));
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        Object object = pemParser.readObject();
        KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
        PrivateKey privateKey = kp.getPrivate();
        return privateKey;
    }

    public PublicKey getPublicKey() throws IOException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        ClassLoader classLoader = getClass().getClassLoader();
        File publicKeyFile = new File(classLoader.getResource("rsa_key.pub").getFile());
        PEMParser pemParser = new PEMParser(new FileReader(publicKeyFile));
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        Object object = pemParser.readObject();
        PublicKey publicKey = converter.getPublicKey((SubjectPublicKeyInfo) object);
        return publicKey;
    }

    @Test
    public void testGenerateKeys() {
        GenerateKeys gk;
        try {
            gk = new GenerateKeys(1024);
            gk.createKeys();
            PemFile publicKey = new PemFile(EncryptConstants.RSA_PUBLIC_KEY, gk.getPublicKey());
            publicKey.write("KeyPair/publicKey1");
            PemFile privateKey = new PemFile(EncryptConstants.RSA_PRIVATE_KEY, gk.getPrivateKey());
            privateKey.write("KeyPair/privateKey1");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
