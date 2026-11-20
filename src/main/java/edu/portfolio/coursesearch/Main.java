package edu.portfolio.coursesearch;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/** Command-line entry point. No external dependencies required. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        try {
            Options options = Options.parse(args);
            if (options.help) { printHelp(); return; }
            CourseCatalog catalog = new CourseCatalog(CsvCourseReader.read(Path.of(options.csvPath)));
            if (options.listAll) {
                printCourses(catalog.all(options.department));
            } else if (options.query != null) {
                printCourses(catalog.search(options.query, options.department, options.limit));
            } else {
                interactive(catalog);
            }
        } catch (IllegalArgumentException | IOException exception) {
            System.err.println("Error: " + exception.getMessage());
            System.err.println("Run with --help for usage.");
            System.exit(1);
        }
    }

    private static void interactive(CourseCatalog catalog) {
        System.out.println("Course Search & Autocomplete — illustrative demo catalog");
        System.out.printf("Loaded %d courses. Departments: %s%n", catalog.size(),
                String.join(", ", new java.util.TreeSet<>(catalog.departments())));
        System.out.println("Type a course code/title, 'all' to list courses, or 'quit' to exit.");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("search> ");
            if (!scanner.hasNextLine()) { System.out.println(); return; }
            String query = scanner.nextLine().trim();
            if (query.equalsIgnoreCase("quit") || query.equalsIgnoreCase("exit")) return;
            if (query.isBlank()) continue;
            if (query.equalsIgnoreCase("all")) printCourses(catalog.all(""));
            else printCourses(catalog.search(query, "", 10));
        }
    }

    private static void printCourses(List<Course> courses) {
        if (courses.isEmpty()) { System.out.println("No matching courses found."); return; }
        for (Course course : courses) {
            System.out.printf("%-9s | %-54s | %d units%n", course.code(), course.title(), course.units());
            if (!course.description().isBlank()) System.out.println("            " + course.description());
        }
        System.out.printf("%d result(s)%n", courses.size());
    }

    private static void printHelp() {
        System.out.println("Course Search & Autocomplete (Java 17+)");
        System.out.println("  --query TEXT   Search course code or title, case-insensitive");
        System.out.println("  --dept CODE    Restrict search to a department (e.g. CSE)");
        System.out.println("  --limit N      Maximum number of search results (default 10)");
        System.out.println("  --list         List all courses (optionally filtered by --dept)");
        System.out.println("  --csv PATH     Load a different CSV (default data/courses.csv)");
        System.out.println("  --help         Show this message");
        System.out.println("  No arguments  Start an interactive search session");
    }

    private static final class Options {
        String csvPath = "data/courses.csv";
        String query;
        String department = "";
        int limit = 10;
        boolean listAll;
        boolean help;

        static Options parse(String[] args) {
            Options options = new Options();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                switch (arg) {
                    case "--help", "-h" -> options.help = true;
                    case "--list" -> options.listAll = true;
                    case "--query", "--dept", "--limit", "--csv" -> {
                        if (i + 1 >= args.length) throw new IllegalArgumentException("Missing value for " + arg);
                        String value = args[++i];
                        switch (arg) {
                            case "--query" -> options.query = value;
                            case "--dept" -> options.department = value;
                            case "--csv" -> options.csvPath = value;
                            case "--limit" -> {
                                try { options.limit = Integer.parseInt(value); }
                                catch (NumberFormatException ex) { throw new IllegalArgumentException("--limit must be a number"); }
                                if (options.limit < 1) throw new IllegalArgumentException("--limit must be positive");
                            }
                            default -> throw new IllegalStateException();
                        }
                    }
                    default -> throw new IllegalArgumentException("Unknown option: " + arg);
                }
            }
            if (options.listAll && options.query != null) throw new IllegalArgumentException("Use --list or --query, not both");
            return options;
        }
    }
}