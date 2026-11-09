import java.io.*;
import java.util.*;
import java.util.logging.*;

// Custom exception - for duplicate student ID
class DuplicateStudentException extends RuntimeException {
    public DuplicateStudentException(String message) {
        super(message);
    }
}

public class MarksImporter {

    private static final Logger logger = Logger.getLogger(MarksImporter.class.getName());

    // throws IOException because FileReader / BufferedReader can fail - it's a checked exception
    static void importMarks(String filename) throws IOException {
        Set<String> seen = new HashSet<>();

        // try-with-resources: closes BufferedReader automatically even when exception happens
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                // We catch per-record so ONE bad record does NOT stop the whole import.
                // We skip the bad one and continue with the next.
                try {
                    String[] parts = line.split(",");

                    // TODO 1: check exactly 2 fields
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Wrong format, expected ID,mark : " + line);
                    }

                    String studentId = parts[0].trim();
                    String markText = parts[1].trim();

                    // TODO 2: parse mark and validate range 0..100
                    int mark;
                    try {
                        mark = Integer.parseInt(markText);
                    } catch (NumberFormatException e) {
                        // Friendly message + log technical details
                        System.out.println("Skipped " + studentId + ": mark '" + markText + "' is not a number.");
                        logger.log(Level.WARNING, "NumberFormatException for line: " + line, e);
                        continue;
                    }

                    if (mark < 0 || mark > 100) {
                        throw new IllegalArgumentException("Mark out of range 0-100: " + mark);
                    }

                    // TODO 3: detect duplicate
                    if (!seen.add(studentId)) {
                        throw new DuplicateStudentException("Duplicate student ID: " + studentId);
                    }

                    // TODO 4: display accepted record
                    System.out.println("Accepted: " + studentId + " -> " + mark);

                } catch (DuplicateStudentException | IllegalArgumentException e) {
                    System.out.println("Skipped record '" + line + "': " + e.getMessage());
                    logger.log(Level.WARNING, "Skipped record: " + line, e);
                    // continue to next line - do not stop import
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            importMarks("marks.txt");
        } catch (FileNotFoundException e) {
            // Must come BEFORE IOException because it is a child of IOException
            System.out.println("Marks file not found; check the file name.");
            logger.log(Level.SEVERE, "FileNotFoundException: marks.txt not found", e);
        } catch (IOException e) {
            System.out.println("Could not read marks file. Please try again.");
            logger.log(Level.SEVERE, "IOException while reading file", e);
        } finally {
            System.out.println("Import attempt finished");
        }
    }
}