package com.minimalist.calculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;

final class PasscodeManager {
    static final int MIN_TAPS = 6;
    private static final String PREFS = "calculator_lock";
    private static final String HASH = "hash";
    private static final String SALT = "salt";
    private static final String LENGTH = "length";
    private final SharedPreferences prefs;
    private byte[] cachedSalt;
    private byte[] cachedHash;
    private int cachedLength;

    PasscodeManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        loadCache();
    }

    boolean hasPasscode() {
        return cachedHash != null && cachedSalt != null && cachedLength >= MIN_TAPS;
    }

    int length() {
        return cachedLength;
    }

    void save(List<String> taps) throws Exception {
        if (taps.size() < MIN_TAPS) {
            throw new IllegalArgumentException("Use at least " + MIN_TAPS + " taps");
        }
        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt);
        byte[] hash = hash(taps, salt);
        prefs.edit()
                .putString(SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                .putString(HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
                .putInt(LENGTH, taps.size())
                .apply();
        cachedSalt = salt;
        cachedHash = hash;
        cachedLength = taps.size();
    }

    boolean matches(List<String> taps) {
        if (!hasPasscode() || taps.size() != cachedLength) {
            return false;
        }
        try {
            return MessageDigest.isEqual(cachedHash, hash(taps, cachedSalt));
        } catch (Exception ignored) {
            return false;
        }
    }

    static boolean same(List<String> first, List<String> second) {
        if (first.size() != second.size()) {
            return false;
        }
        for (int i = 0; i < first.size(); i++) {
            if (!first.get(i).equals(second.get(i))) {
                return false;
            }
        }
        return true;
    }

    private void loadCache() {
        cachedLength = prefs.getInt(LENGTH, 0);
        String salt = prefs.getString(SALT, null);
        String hash = prefs.getString(HASH, null);
        if (salt == null || hash == null) {
            return;
        }
        try {
            cachedSalt = Base64.decode(salt, Base64.NO_WRAP);
            cachedHash = Base64.decode(hash, Base64.NO_WRAP);
        } catch (Exception ignored) {
            cachedSalt = null;
            cachedHash = null;
            cachedLength = 0;
        }
    }

    private static byte[] hash(List<String> taps, byte[] salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        digest.update(sequence(taps).getBytes(StandardCharsets.UTF_8));
        return digest.digest();
    }

    private static String sequence(List<String> taps) {
        StringBuilder builder = new StringBuilder();
        for (String tap : taps) {
            builder.append(tap).append('\u001f');
        }
        return builder.toString();
    }
}
