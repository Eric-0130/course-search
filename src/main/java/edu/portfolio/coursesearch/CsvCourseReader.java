package edu.portfolio.coursesearch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Simple RFC-4180-style reader for one-record-per-line CSV (including quoted commas). */
public final class CsvCourseReader {
    private CsvCourseReader() { }

    public static List<Course> read(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty()) throw new IllegalArgumentException("CSV is empty: " + path);
        List<String> header = parseLine(lines.get(0).replace("\ufeff", ""));
        if (!header.equals(List.of("code", "title", "department", "units", "description"))) {
            throw new IllegalArgumentException("CSV header must be: code,title,department,units,description");
        }

        List<Course> courses = new ArrayList<>();
        for (int lineNumber = 2; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);
            if (line.isBlank()) continue;
            try {
                List<String> fields = parseLine(line);
                if (fields.size() != 5) throw new IllegalArgumentException("expected 5 columns, got " + fields.size());
                courses.add(new Course(fields.get(0), fields.get(1), fields.get(2),
                        Integer.parseInt(fields.get(3).trim()), fields.get(4)));
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException("Invalid CSV line " + lineNumber + ": " + exception.getMessage(), exception);
            }
        }
        return List.copyOf(courses);
    }

    static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean closedQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                        closedQuote = true;
                    }
                } else {
                    field.append(ch);
                }
            } else if (ch == ',') {
                fields.add(field.toString());
                field.setLength(0);
                closedQuote = false;
            } else if (ch == '"') {
                if (field.length() > 0 || closedQuote) {
                    throw new IllegalArgumentException("unexpected quote");
                }
                inQuotes = true;
            } else {
                if (closedQuote && ch != ' ' && ch != '\t') {
                    throw new IllegalArgumentException("unexpected characters after quote");
                }
                if (!closedQuote) field.append(ch);
            }
        }
        if (inQuotes) throw new IllegalArgumentException("unclosed quote");
        fields.add(field.toString());
        return fields;
    }
}