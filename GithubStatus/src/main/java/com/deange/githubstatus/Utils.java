package com.deange.githubstatus;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public final class Utils {

    private Utils() {
        // Uninstantiable
    }

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
            hash = String.valueOf(Arrays.hashCode(e.getStackTrace()));

        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            hash = String.valueOf(Arrays.hashCode(e.getStackTrace()));
        }

        return hash;
    }

    public static String streamToString(final InputStream in) throws IOException {

        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        final StringBuilder sb = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        reader.close();

        return sb.toString();
    }


    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    public static String buildAction(final String action) {
        return BuildConfig.PACKAGE_NAME + "." + action;
    }
}
