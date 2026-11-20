package edu.portfolio.coursesearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** In-memory search catalog backed by a HashMap and a custom prefix Trie. */
public final class CourseCatalog {
    private final Map<String, Course> byCode;
    private final Trie prefixIndex;

    public CourseCatalog(List<Course> courses) {
        byCode = new HashMap<>();
        prefixIndex = new Trie();
        for (Course course : courses) {
            if (byCode.putIfAbsent(course.code(), course) != null) {
                throw new IllegalArgumentException("Duplicate course code: " + course.code());
            }
            String code = TextNormalizer.normalize(course.code());
            String title = TextNormalizer.normalize(course.title());
            prefixIndex.add(code, course.code());
            prefixIndex.add(TextNormalizer.compact(code), course.code());
            prefixIndex.add(title, course.code());
            // Allow a prefix match at any word boundary within a course title.
            String[] words = title.split(" ");
            for (int i = 1; i < words.length; i++) {
                prefixIndex.add(String.join(" ", java.util.Arrays.copyOfRange(words, i, words.length)), course.code());
            }
        }
    }

    public int size() { return byCode.size(); }

    public Set<String> departments() {
        Set<String> result = new TreeSet<>();
        for (Course course : byCode.values()) result.add(course.department());
        return Set.copyOf(result);
    }

    public List<Course> all(String department) {
        String normalizedDepartment = normalizeDepartment(department);
        return byCode.values().stream()
                .filter(course -> normalizedDepartment.isEmpty() || course.department().equals(normalizedDepartment))
                .sorted(Comparator.comparing(Course::code)).toList();
    }

    public List<Course> search(String query, String department, int limit) {
        if (limit < 1) throw new IllegalArgumentException("limit must be positive");
        String q = TextNormalizer.normalize(query);
        if (q.isEmpty()) return List.of();
        String compact = TextNormalizer.compact(q);
        String dept = normalizeDepartment(department);

        // The Trie finds prefix matches; a small fallback scans titles for non-prefix substrings.
        Set<String> candidateCodes = new HashSet<>(prefixIndex.searchPrefix(q));
        candidateCodes.addAll(prefixIndex.searchPrefix(compact));
        for (Course course : byCode.values()) {
            if (TextNormalizer.normalize(course.title()).contains(q)) candidateCodes.add(course.code());
        }

        List<Course> result = new ArrayList<>();
        for (String code : candidateCodes) {
            Course course = byCode.get(code);
            if (dept.isEmpty() || course.department().equals(dept)) result.add(course);
        }
        result.sort(Comparator.comparingInt((Course course) -> rank(course, q, compact))
                .thenComparing(Course::code));
        return List.copyOf(result.subList(0, Math.min(limit, result.size())));
    }

    private static String normalizeDepartment(String department) {
        return department == null ? "" : department.trim().toUpperCase(Locale.ROOT);
    }

    private static int rank(Course course, String query, String compact) {
        String code = TextNormalizer.normalize(course.code());
        String codeCompact = TextNormalizer.compact(code);
        String title = TextNormalizer.normalize(course.title());
        if (code.equals(query) || codeCompact.equals(compact)) return 0;
        if (code.startsWith(query) || codeCompact.startsWith(compact)) return 1;
        if (title.equals(query)) return 2;
        if (title.startsWith(query)) return 3;
        // Word-prefix results outrank general substring matches.
        if (title.contains(" " + query)) return 4;
        return 5;
    }
}