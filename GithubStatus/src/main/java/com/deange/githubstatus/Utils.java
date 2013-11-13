package com.deange.githubstatus;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Utils {

    public static String hash(final String toHash) {

        String hash = null;

        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = toHash.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();
            final StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X", b));
            }
            hash = sb.toString();

        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();

        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return hash;
    }

    private Utils() {
        // Uninstantiable
    }

}
