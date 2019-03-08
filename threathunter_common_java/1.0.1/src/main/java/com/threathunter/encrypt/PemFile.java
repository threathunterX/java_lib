package com.threathunter.encrypt;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.*;
import java.security.Key;

public class PemFile {

    private PemObject pemObject;

    public PemFile(String description, Key key) {
        this.pemObject = new PemObject(description, key.getEncoded());
    }

    public void write(String filename) throws FileNotFoundException, IOException {
        File f = new File(filename);
        f.getParentFile().mkdirs();
        PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(f)));
        try {
            pemWriter.writeObject(this.pemObject);
        } finally {
            pemWriter.close();
        }
    }
}