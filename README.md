# Course Search & Autocomplete (Java)

A command-line Java course-search demo that uses a **custom Trie** for fast prefix suggestions and a **HashMap** for course lookup. Search by course code or course title, filter by department, and load your own CSV data.

> **Data notice:** The included 40 entries are **illustrative demonstration data**, not an official UC San Diego catalog. Titles/descriptions and availability may differ from current university listings. This project is not affiliated with UC San Diego. Do not rely on the bundled CSV to make academic decisions.
>
> **Development disclosure:** This starter implementation was generated with AI assistance. Review, test, customize, and understand the code before presenting it as your project or a personal achievement. Do not claim sole authorship of AI-generated code.

## Features

- Custom prefix **Trie** indexes course codes, compact codes (`CSE12`), and course-title word boundaries
- **HashMap** lookup by normalized course code
- Case-insensitive search, exact-code prioritization, and ranked results
- Substring fallback for course titles
- Optional department filtering and a configurable result limit
- Interactive command-line mode, batch query mode, and full course listing
- CSV import with quoted-field support, input validation, and duplicate-code detection
- 20 automated tests, executable without external dependencies
- GitHub Actions workflow that runs tests on each push / pull request

## Requirements

- **Java JDK 17+** (validated on OpenJDK 21 with Java 17 target). No Maven, Gradle, or extra libraries required.
- On macOS / Linux: Terminal; on Windows: Command Prompt.

Check Java: `java -version` and `javac -version`.

## Quick start

### macOS / Linux

Run these commands from the project's root folder:

```bash
bash scripts/run.sh --query "CSE 1"
bash scripts/run.sh --query "data structures" --dept CSE
bash scripts/run.sh --query "cse12" --limit 3
bash scripts/run.sh --list --dept MATH
bash scripts/run.sh                # interactive mode; type quit to leave
bash scripts/test.sh               # run all 20 automated tests
```

### Windows (Command Prompt)

```bat
scripts\run.bat --query "CSE 1"
scripts\run.bat --query "data structures" --dept CSE
scripts\run.bat --list --dept MATH
scripts\test.bat
```

### Example output

```text
$ bash scripts/run.sh --query "cse12" --limit 1
CSE 12    | Basic Data Structures and Object-Oriented Design        | 4 units
            Lists trees recursion and fundamental data structures
1 result(s)
```

## How it works

1. `CsvCourseReader` validates the header and parses the CSV into immutable `Course` records.
2. `CourseCatalog` indexes course records in a `HashMap<String, Course>` by code.
3. `Trie` stores IDs of courses at every code or title-prefix node, including suffixes beginning at word boundaries.
4. `CourseCatalog.search()` obtains prefix candidates from the Trie and supplements them with title-substring matches.
5. Matches are filtered by department, ranked (exact code, code prefix, exact title, title prefix, word prefix, general substring), then sorted by course code.

**Complexity note:** Trie prefix lookup traverses the query in `O(m)` time, with `m` the query length, before reading candidate IDs. This implementation also scans all `n` course titles for substring matches, so **total search time is `O(n × L)` in the worst case**, where `L` is average title length. Do not claim end-to-end `O(m)` lookup for the current implementation.

## Project structure

```text
course-search/
├── .github/workflows/java-tests.yml
├── .gitignore
├── data/courses.csv
├── scripts/run.sh, test.sh, run.bat, test.bat
├── src/main/java/edu/portfolio/coursesearch/
│   ├── Main.java
│   ├── Course.java
│   ├── CourseCatalog.java
│   ├── Trie.java
│   ├── TextNormalizer.java
│   └── CsvCourseReader.java
└── src/test/java/edu/portfolio/coursesearch/CourseSearchTests.java
```

## Supply your own data

Create a UTF-8 CSV with this header exactly:

```csv
code,title,department,units,description
CS 101,Introduction to Computing,CS,4,"An example course, with a comma"
```

Then run:

```bash
bash scripts/run.sh --csv path/to/your-courses.csv --query "CS"
```

Fields: `code`, `title`, `department`, `units` (positive integer), `description`. All records must fit on a single CSV line (quoted multiline cells are not supported). Codes must be unique.

## Tests

Run `bash scripts/test.sh` or `scripts\test.bat`. The dependency-free test runner covers code/title matching, normalization, ranking, filtering, result limits, no-match cases, duplicate codes, quoted CSV fields, and invalid input. The process exits nonzero if a test fails.

## Ideas for your own improvements

- Replace the demo CSV with a properly sourced, up-to-date dataset (respect licensing / access rules)
- Add fuzzy matching for common typos
- Build a desktop or web interface
- Add favorites, course prerequisites, or local saved searches
- Benchmark a prefix-only method separately from the substring fallback

## GitHub upload

See [UPLOAD_TO_GITHUB.md](UPLOAD_TO_GITHUB.md) for browser and terminal instructions.

## Scope and integrity

The repository is a portfolio prototype and educational demo. It is not the university's enrollment system, does not use an official feed, and does not guarantee class availability or course descriptions.
