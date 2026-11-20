package edu.portfolio.coursesearch;

/** An immutable entry from the local, illustrative course catalog. */
public record Course(String code, String title, String department, int units, String description) {
    public Course {
        if (code == null || code.isBlank() || title == null || title.isBlank()
                || department == null || department.isBlank() || units <= 0) {
            throw new IllegalArgumentException("Course requires a code, title, department and positive units");
        }
        code = code.trim().toUpperCase(java.util.Locale.ROOT);
        title = title.trim();
        department = department.trim().toUpperCase(java.util.Locale.ROOT);
        description = description == null ? "" : description.trim();
    }
}
