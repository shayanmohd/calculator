package com.minimalist.calculator;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

final class Crypto {
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "calculator_vault_media_key_v1";
    private static final byte[] MAGIC = new byte[]{'M', 'C', 'V', '1'};

    private Crypto() {
    }

    static byte[] encryptBytes(Context context, byte[] plain) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(plain);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeHeader(out, iv);
        out.write(encrypted);
        return out.toByteArray();
    }

    static byte[] decryptBytes(Context context, byte[] blob) throws Exception {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(blob));
        byte[] iv = readHeader(in);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(128, iv));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(new CipherInputStream(in, cipher), out);
        return out.toByteArray();
    }

    static void encryptStream(Context context, InputStream input, File outputFile) throws Exception {
        File parent = outputFile.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        byte[] iv = cipher.getIV();
        try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
            writeHeader(fileOut, iv);
            try (CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher)) {
                copy(input, cipherOut);
            }
        }
    }

    static void decryptFile(Context context, File inputFile, File outputFile) throws Exception {
        File parent = outputFile.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (DataInputStream fileIn = new DataInputStream(new FileInputStream(inputFile))) {
            byte[] iv = readHeader(fileIn);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(128, iv));
            try (CipherInputStream cipherIn = new CipherInputStream(fileIn, cipher);
                 FileOutputStream out = new FileOutputStream(outputFile)) {
                copy(cipherIn, out);
            }
        }
    }

    private static SecretKey getKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE);
        keyStore.load(null);
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE);
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build();
            generator.init(spec);
            generator.generateKey();
        }
        return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
    }

    private static void writeHeader(OutputStream out, byte[] iv) throws Exception {
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.write(MAGIC);
        dataOut.writeByte(iv.length);
        dataOut.write(iv);
    }

    private static byte[] readHeader(DataInputStream in) throws Exception {
        byte[] magic = new byte[MAGIC.length];
        in.readFully(magic);
        for (int i = 0; i < MAGIC.length; i++) {
            if (magic[i] != MAGIC[i]) {
                throw new SecurityException("Invalid vault file");
            }
        }
        int ivLength = in.readUnsignedByte();
        if (ivLength < 12 || ivLength > 16) {
            throw new SecurityException("Invalid vault file");
        }
        byte[] iv = new byte[ivLength];
        in.readFully(iv);
        return iv;
    }

    private static void copy(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[64 * 1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
