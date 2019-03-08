package com.threathunter.basictools.licensecreator;

import com.threathunter.config.CommonDynamicConfig;
import de.schlichtherle.license.*;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;

/**
 * This is a tool for creating license for java program.
 * You must provide some basic files so that it will create license correctly.
 *
 * License verification is by license generate by this tool with cipher. Either lack of license or cipher,
 * the verify will failed.
 * So cipher is for decode the license file and content of license file will verify if match with the public key in
 * public key store.
 * This license protocol is useless if some evils totally change all the store file.
 *
 * @author daisy
 */
public class LicenseCreator {
    private static final X500Principal ISSUER = new X500Principal("CN=threathunter.com, L=Shanghai, OU=threathunter Software, " +
            "O=threathunter, C=China");
    private static final long DAY_TIME_MILLIS = 24 * 3600 * 1000;

    private String username;
    private String city;
    private String country;
    private Date notAfter;
    private String userLicenseInfo;
    private String licenseDir;

    private String subject;
    private String keystoreFile;
    private String alias;
    private String keystorePassword;
    private String keyPassword;

    private KeyStoreParam privateKeyStoreParam;
    private CipherParam cipherParam;

    public static void main(String[] args) {
        LicenseCreator licenseCreator = new LicenseCreator();
        if (licenseCreator.createLicense()) {
            System.out.println("User license create successfully!");
        } else {
            System.out.println("User license create Failed.");
        }
    }

    public LicenseCreator() {
        this("private.info", "user.dat");
    }

    public LicenseCreator(String privateInfoFile, String userDatFile) {
        initial(privateInfoFile, userDatFile);
    }

    private void initial(String privateInfoFile, String userDatFile) {
        CommonDynamicConfig.getInstance().addConfigFile(privateInfoFile);
        ensureConfigProperty("application");
        ensureConfigProperty("keystore_file");
        ensureConfigProperty("alias");
        ensureConfigProperty("keystore_password");
        ensureConfigProperty("key_password");

        CommonDynamicConfig.getInstance().addConfigFile(userDatFile);
        ensureConfigProperty("license_info");
        ensureConfigProperty("username");
        ensureConfigProperty("city");
        ensureConfigProperty("country");
        ensureConfigProperty("cipher_param_password");
        try {
            this.loadFromPrivateInfoFile();
            this.loadFromDatFile();
        } catch (Exception e) {
            System.out.println("error when creating license: ");
            e.printStackTrace();
        }
    }

    private void loadFromDatFile() throws ParseException {
        this.userLicenseInfo = CommonDynamicConfig.getInstance().getString("license_info");
        this.username = CommonDynamicConfig.getInstance().getString("username");
        this.city = CommonDynamicConfig.getInstance().getString("city");
        this.country = CommonDynamicConfig.getInstance().getString("country");
        this.cipherParam = getCipherParam(CommonDynamicConfig.getInstance().getString("cipher_param_password"));
        this.notAfter = getExpireDate();
        this.licenseDir = getLicenseOutputDir();
    }

    private static void ensureConfigProperty(String propertyName) {
        if (CommonDynamicConfig.getInstance().getString(propertyName) == null) {
            throw new RuntimeException(String.format("property does not exist, require property in config: %s", propertyName));
        }
    }

    private Date getExpireDate() throws ParseException {
        if (CommonDynamicConfig.getInstance().getString("expire_date") != null) {
            return new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").parse(CommonDynamicConfig.getInstance().getString("expire_date"));
        } else {
            long currentMillis = System.currentTimeMillis();
            long aligned = currentMillis / DAY_TIME_MILLIS * DAY_TIME_MILLIS;
            long expireTimestamp = aligned + CommonDynamicConfig.getInstance().getInt("expire_period_days", 2) * DAY_TIME_MILLIS;
            return new Date(expireTimestamp);
        }
    }

    private String getLicenseOutputDir() {
        String outputPath = CommonDynamicConfig.getInstance().getString("output_dir");
        if (outputPath == null) {
            outputPath = System.getProperty("user.dir");
        }
        return outputPath;
    }

    private CipherParam getCipherParam(String cipher_param_password) {
        return () -> cipher_param_password;
    }

    private void loadFromPrivateInfoFile() {
        this.subject = CommonDynamicConfig.getInstance().getString("application");
        this.keystoreFile = CommonDynamicConfig.getInstance().getString("keystore_file");
        this.alias = CommonDynamicConfig.getInstance().getString("alias");
        this.keystorePassword = CommonDynamicConfig.getInstance().getString("keystore_password");
        this.keyPassword = CommonDynamicConfig.getInstance().getString("key_password");

        this.privateKeyStoreParam = getPrivateKeyStoreParam();
    }

    private KeyStoreParam getPrivateKeyStoreParam() {
        return new KeyStoreParam() {
            public InputStream getStream() {
                final String currentKeystoreFile = keystoreFile;
                InputStream in;
                try {
                    in = new FileInputStream(currentKeystoreFile);
                } catch (Exception e) {
                    in = Thread.currentThread().getContextClassLoader().getResourceAsStream(currentKeystoreFile);
                }
                if (in == null) {
                    throw new RuntimeException("Could not load keystore file: " + currentKeystoreFile);
                }
                return in;
            }

            public String getAlias() {
                return alias;
            }

            public String getStorePwd() {
                return keystorePassword;
            }

            public String getKeyPwd() {
                return keyPassword;
            }
        };
    }


    public boolean createLicense() {
        LicenseParam licenseParam = new LicenseParam() {
            public String getSubject() {
                return subject;
            }

            public Preferences getPreferences() {
                return Preferences.userNodeForPackage(LicenseCreator.class);
            }

            public KeyStoreParam getKeyStoreParam() {
                return privateKeyStoreParam;
            }

            public CipherParam getCipherParam() {
                return cipherParam;
            }
        };

        LicenseManager licenseManager = new LicenseManager(licenseParam);
        try {
            File dir = new File(licenseDir);
            String file = String.format("%s/%s.lic", licenseDir, username);
            if (!dir.exists()) {
                file = String.format("%s/%s/%s.lic", System.getProperties().get("user.dir").toString(), licenseDir, username);
            }
            licenseManager.store(createLicenseContent(), new File(file));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private LicenseContent createLicenseContent() {
        LicenseContent licenseContent = new LicenseContent();
        X500Principal holder = new X500Principal(String.format("CN=%s, L=%s, C=%s", username, city, country));
        licenseContent.setHolder(holder);
        licenseContent.setIssuer(ISSUER);
        licenseContent.setConsumerAmount(1);
        licenseContent.setConsumerType("User");
        licenseContent.setInfo(userLicenseInfo);
        Date now = new Date();
        licenseContent.setIssued(now);
        licenseContent.setNotAfter(notAfter);
        licenseContent.setSubject(subject);
        return  licenseContent;
    }
}