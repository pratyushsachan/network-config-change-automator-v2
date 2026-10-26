package com.netchange.service;

/** Compares dotted OS versions such as "17.9.4" and "16.12". */
public final class VersionUtil {

    private VersionUtil() {
    }

    public static int compare(String a, String b) {
        int[] x = parse(a);
        int[] y = parse(b);
        int length = Math.max(x.length, y.length);
        for (int i = 0; i < length; i++) {
            int left = i < x.length ? x[i] : 0;
            int right = i < y.length ? y[i] : 0;
            if (left != right) {
                return Integer.compare(left, right);
            }
        }
        return 0;
    }

    private static int[] parse(String version) {
        if (version == null || version.isBlank()) {
            return new int[0];
        }
        String[] parts = version.trim().split("\\.");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String digits = parts[i].replaceAll("\\D", "");
            out[i] = digits.isEmpty() ? 0 : Integer.parseInt(digits);
        }
        return out;
    }
}
