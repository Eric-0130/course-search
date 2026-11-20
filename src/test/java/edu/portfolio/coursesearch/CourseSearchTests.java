package edu.portfolio.coursesearch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Dependency-free automated test runner: exit code 1 if any test fails. */
public final class CourseSearchTests {
    private static int passed = 0;

    public static void main(String[] args) throws Exception {
        run("load real CSV data", CourseSearchTests::loadData);
        run("prefix search for course code", CourseSearchTests::codePrefix);
        run("compact code search", CourseSearchTests::compactCode);
        run("exact code ranks first", CourseSearchTests::exactCodeFirst);
        run("case insensitive title search", CourseSearchTests::titleSearch);
        run("search within title words", CourseSearchTests::middleWordSearch);
        run("search middle-of-word", CourseSearchTests::substringSearch);
        run("department filter", CourseSearchTests::departmentFilter);
        run("limit is enforced", CourseSearchTests::limit);
        run("empty query returns no results", CourseSearchTests::emptyQuery);
        run("nonmatching query returns no results", CourseSearchTests::noMatch);
        run("missing department returns no results", CourseSearchTests::nonexistentDepartment);
        run("negative or zero limit is rejected", CourseSearchTests::invalidLimit);
        run("duplicate codes are rejected", CourseSearchTests::duplicate);
        run("CSV supports quoted commas", CourseSearchTests::quotedCsv);
        run("CSV supports escaped quotes", CourseSearchTests::escapedQuoteCsv);
        run("CSV rejects malformed line", CourseSearchTests::invalidCsv);
        run("CSV rejects wrong header", CourseSearchTests::invalidHeader);
        run("all courses are sorted", CourseSearchTests::listAll);
        run("department listing available", CourseSearchTests::departments);
        System.out.println("\nAll " + passed + " tests passed.");
    }

    private static CourseCatalog catalog() throws IOException {
        return new CourseCatalog(CsvCourseReader.read(Path.of("data/courses.csv")));
    }

    private static void run(String name, Test body) throws Exception {
        try { body.execute(); passed++; System.out.println("[PASS] " + name); }
        catch (Throwable error) { System.err.println("[FAIL] " + name + ": " + error); System.exit(1); }
    }

    private static void check(boolean expression, String reason) {
        if (!expression) throw new AssertionError(reason);
    }

    private static void loadData() throws Exception {
        check(catalog().size() == 40, "expected 40 demo courses");
    }
    private static void codePrefix() throws Exception {
        check(catalog().search("CSE 1", "", 20).size() >= 7, "expected several CSE 1xx courses");
    }
    private static void compactCode() throws Exception {
        check(catalog().search("cse12", "", 10).get(0).code().equals("CSE 12"), "compact code");
    }
    private static void exactCodeFirst() throws Exception {
        check(catalog().search("CSE 11", "", 10).get(0).code().equals("CSE 11"), "exact code should rank first");
    }
    private static void titleSearch() throws Exception {
        check(catalog().search("DATA STRUCTURES", "", 20).stream().anyMatch(course -> course.code().equals("CSE 12")), "title match");
    }
    private static void middleWordSearch() throws Exception {
        check(catalog().search("object-oriented", "", 20).stream().anyMatch(course -> course.code().equals("CSE 11")), "word match");
    }
    private static void substringSearch() throws Exception {
        check(catalog().search("gorithm", "", 20).stream().anyMatch(course -> course.code().equals("CSE 101")), "substring match");
    }
    private static void departmentFilter() throws Exception {
        List<Course> courses = catalog().search("data", "DSC", 20);
        check(!courses.isEmpty() && courses.stream().allMatch(course -> course.department().equals("DSC")), "all in DSC");
    }
    private static void limit() throws Exception {
        check(catalog().search("introduction", "", 3).size() == 3, "limit=3");
    }
    private static void emptyQuery() throws Exception {
        check(catalog().search("  ", "", 10).isEmpty(), "blank query");
    }
    private static void noMatch() throws Exception {
        check(catalog().search("zxqv908", "", 10).isEmpty(), "no match");
    }
    private static void nonexistentDepartment() throws Exception {
        check(catalog().search("data", "BIO", 10).isEmpty(), "nonexistent department");
    }
    private static void invalidLimit() throws Exception {
        try { catalog().search("cse", "", 0); throw new AssertionError("should throw"); }
        catch (IllegalArgumentException expected) { /* expected */ }
    }
    private static void duplicate() {
        Course example = new Course("CSE 12", "Course", "CSE", 4, "");
        try { new CourseCatalog(List.of(example, example)); throw new AssertionError("should throw"); }
        catch (IllegalArgumentException expected) { /* expected */ }
    }
    private static void quotedCsv() throws Exception {
        Path temp = tempCsv("code,title,department,units,description\nCSE 99,\"A, B\",CSE,4,\"Course, Description\"\n");
        try {
            Course entry = CsvCourseReader.read(temp).get(0);
            check(entry.title().equals("A, B") && entry.description().equals("Course, Description"), "quoted comma");
        } finally { Files.deleteIfExists(temp); }
    }
    private static void escapedQuoteCsv() throws Exception {
        Path temp = tempCsv("code,title,department,units,description\nCSE 99,\"The \"\"Design\"\" Course\",CSE,4,Test\n");
        try { check(CsvCourseReader.read(temp).get(0).title().equals("The \"Design\" Course"), "quotes"); }
        finally { Files.deleteIfExists(temp); }
    }
    private static void invalidCsv() throws Exception {
        Path temp = tempCsv("code,title,department,units,description\nCSE 11,\"Missing quote,CSE,4,Desc\n");
        try {
            try { CsvCourseReader.read(temp); throw new AssertionError("should throw"); }
            catch (IllegalArgumentException expected) { check(expected.getMessage().contains("line 2"), "error line"); }
        } finally { Files.deleteIfExists(temp); }
    }
    private static void invalidHeader() throws Exception {
        Path temp = tempCsv("name,department\nCSE 11,CSE\n");
        try {
            try { CsvCourseReader.read(temp); throw new AssertionError("should throw"); }
            catch (IllegalArgumentException expected) { check(expected.getMessage().contains("header"), "header error"); }
        } finally { Files.deleteIfExists(temp); }
    }
    private static void listAll() throws Exception {
        List<Course> entries = catalog().all("");
        check(entries.size() == 40, "all items");
        check(entries.get(0).code().compareTo(entries.get(entries.size() - 1).code()) < 0, "sorted");
    }
    private static void departments() throws Exception {
        check(catalog().departments().containsAll(List.of("CSE", "MATH", "DSC", "ECE", "COGS")), "all departments");
    }
    private static Path tempCsv(String content) throws IOException {
        Path path = Files.createTempFile("course-search-test-", ".csv");
        Files.writeString(path, content, StandardCharsets.UTF_8);
        return path;
    }
    @FunctionalInterface private interface Test { void execute() throws Exception; }
}
