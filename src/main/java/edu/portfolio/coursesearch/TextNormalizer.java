package edu.portfolio.coursesearch;

import java.util.Locale;

final class TextNormalizer {
    private TextNormalizer() { }

    static String normalize(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    static String compact(String input) {
        return normalize(input).replace(" ", "");
    }
}
