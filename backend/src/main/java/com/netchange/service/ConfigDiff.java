package com.netchange.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Line-based config merge and diff.
 * <ul>
 *   <li>{@code "ntp server 10.0.0.2"} adds the line if it is not already present.</li>
 *   <li>{@code "no snmp-server community public RO"} removes the matching line if present.</li>
 *   <li>Blank lines and lines starting with {@code !} (comments) are ignored.</li>
 * </ul>
 */
public final class ConfigDiff {

    public record Result(List<String> added, List<String> removed) {
        public boolean isEmpty() {
            return added.isEmpty() && removed.isEmpty();
        }
    }

    private ConfigDiff() {
    }

    public static List<String> lines(String config) {
        List<String> out = new ArrayList<>();
        if (config == null) {
            return out;
        }
        for (String raw : config.split("\\R")) {
            String line = raw.trim();
            if (!line.isEmpty() && !line.startsWith("!")) {
                out.add(line);
            }
        }
        return out;
    }

    public static Result diff(String running, String change) {
        List<String> current = lines(running);
        List<String> added = new ArrayList<>();
        List<String> removed = new ArrayList<>();
        for (String line : lines(change)) {
            if (line.startsWith("no ")) {
                String target = line.substring(3).trim();
                if (current.remove(target)) {
                    removed.add(target);
                }
            } else if (!current.contains(line)) {
                current.add(line);
                added.add(line);
            }
        }
        return new Result(added, removed);
    }

    public static String apply(String running, String change) {
        List<String> current = lines(running);
        for (String line : lines(change)) {
            if (line.startsWith("no ")) {
                current.remove(line.substring(3).trim());
            } else if (!current.contains(line)) {
                current.add(line);
            }
        }
        return String.join("\n", current);
    }
}
