package com.deange.githubstatus;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return hash;
    }

    public static String getVersionName(final Context context) {

        String versionName = null;

        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }

    public static int getVersionCode(final Context context) {

        int versionCode = 0;

        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionCode;
    }

}
