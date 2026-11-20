package edu.portfolio.coursesearch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Prefix index: each node tracks course codes reachable from its prefix. */
final class Trie {
    private final Node root = new Node();

    private static final class Node {
        final Map<Character, Node> children = new HashMap<>();
        final Set<String> courseCodes = new HashSet<>();
    }

    void add(String key, String courseCode) {
        if (key == null || key.isEmpty()) return;
        Node current = root;
        for (char character : key.toCharArray()) {
            current = current.children.computeIfAbsent(character, ignored -> new Node());
            current.courseCodes.add(courseCode);
        }
    }

    Set<String> searchPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) return Set.of();
        Node current = root;
        for (char character : prefix.toCharArray()) {
            current = current.children.get(character);
            if (current == null) return Set.of();
        }
        return Set.copyOf(current.courseCodes);
    }
}
