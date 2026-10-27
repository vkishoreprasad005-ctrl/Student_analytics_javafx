package com.studentanalytics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles CSV reading, writing, and data persistence for Student records.
 * Uses consistent file path resolution and robust CSV escaping/parsing.
 */
public class StudentFileHandler {

    public static final String CSV_HEADER = "Student ID,Student Name,Department,Marks";
    private static final String DEFAULT_FILE_NAME = "students.csv";

    private final Path filePath;

    /**
     * Default constructor resolving students.csv in the application root directory.
     */
    public StudentFileHandler() {
        this(DEFAULT_FILE_NAME);
    }

    /**
     * Constructor allowing custom file path (useful for testing).
     *
     * @param fileName Path or filename to use
     */
    public StudentFileHandler(String fileName) {
        Path p = Paths.get(fileName);
        if (!p.isAbsolute()) {
            // Resolve relative to user working directory
            this.filePath = Paths.get(System.getProperty("user.dir", ".")).resolve(fileName).toAbsolutePath().normalize();
        } else {
            this.filePath = p.normalize();
        }
        ensureFileExists();
    }

    /**
     * Constructor with explicit Path.
     */
    public StudentFileHandler(Path path) {
        this.filePath = path.toAbsolutePath().normalize();
        ensureFileExists();
    }

    public Path getFilePath() {
        return filePath;
    }

    /**
     * Ensures that the CSV file exists with the standard header.
     */
    public synchronized void ensureFileExists() {
        try {
            File file = filePath.toFile();
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists() || file.length() == 0) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                    writer.write(CSV_HEADER);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error initializing CSV storage file: " + e.getMessage());
        }
    }

    /**
     * Loads all students from the CSV file.
     * Skips the header row, ignores corrupt lines, and filters out duplicates.
     *
     * @return List of valid Student records
     * @throws IOException on read errors
     */
    public synchronized List<Student> loadStudents() throws IOException {
        ensureFileExists();
        List<Student> students = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                // Check and skip header
                if (isFirstLine) {
                    isFirstLine = false;
                    if (isHeaderLine(line)) {
                        continue;
                    }
                }

                Student student = parseCsvLine(line);
                if (student != null) {
                    String normId = student.getId().toUpperCase();
                    if (!seenIds.contains(normId)) {
                        seenIds.add(normId);
                        students.add(student);
                    }
                }
            }
        }

        return students;
    }

    /**
     * Checks if a line matches the CSV header.
     */
    private boolean isHeaderLine(String line) {
        String lower = line.toLowerCase();
        return lower.contains("student id") || lower.contains("department") || lower.contains("marks");
    }

    /**
     * Appends a new student to the CSV file.
     *
     * @param student The student to save
     * @throws IllegalArgumentException if ID already exists or student is invalid
     * @throws IOException              on file write failure
     */
    public synchronized void saveStudent(Student student) throws IOException {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null");
        }

        List<Student> current = loadStudents();
        String targetId = student.getId().toUpperCase();
        for (Student existing : current) {
            if (existing.getId().equalsIgnoreCase(targetId)) {
                throw new IllegalArgumentException("Student ID already exists: " + student.getId());
            }
        }

        // Append to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), StandardCharsets.UTF_8, true))) {
            writer.write(student.toCsvRow());
            writer.newLine();
        }
    }

    /**
     * Overwrites the CSV file with the given list of students.
     *
     * @param students List of students to persist
     * @throws IOException on file write failure
     */
    public synchronized void saveAllStudents(List<Student> students) throws IOException {
        ensureFileExists();
        Path tempFile = filePath.resolveSibling(filePath.getFileName().toString() + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile.toFile(), StandardCharsets.UTF_8))) {
            writer.write(CSV_HEADER);
            writer.newLine();

            if (students != null) {
                Set<String> seenIds = new HashSet<>();
                for (Student s : students) {
                    if (s != null && !seenIds.contains(s.getId().toUpperCase())) {
                        seenIds.add(s.getId().toUpperCase());
                        writer.write(s.toCsvRow());
                        writer.newLine();
                    }
                }
            }
        }

        // Atomically replace file or rename
        try {
            Files.move(tempFile, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(tempFile, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Clears all student records and resets the CSV to only the header.
     *
     * @throws IOException on file write failure
     */
    public synchronized void clearAllRecords() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), StandardCharsets.UTF_8))) {
            writer.write(CSV_HEADER);
            writer.newLine();
        }
    }

    /**
     * Checks if a Student ID already exists in the file (case-insensitive).
     *
     * @param id Student ID to check
     * @return true if ID exists, false otherwise
     */
    public synchronized boolean isIdExists(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        try {
            List<Student> students = loadStudents();
            for (Student s : students) {
                if (s.getId().equalsIgnoreCase(id.trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV for duplicate check: " + e.getMessage());
        }
        return false;
    }

    /**
     * Robust CSV line parser compliant with RFC 4180.
     * Handles quoted values, internal commas, and double quotes.
     *
     * @param line Raw CSV line
     * @return Student object or null if parsing fails
     */
    public static Student parseCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    // Escaped quote
                    sb.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());

        if (tokens.size() < 4) {
            return null;
        }

        String id = tokens.get(0);
        String name = tokens.get(1);
        String department = tokens.get(2);
        String marksStr = tokens.get(3);

        if (id.isEmpty() || name.isEmpty()) {
            return null;
        }

        try {
            double marks = Double.parseDouble(marksStr);
            if (marks < 0 || marks > 100) {
                return null;
            }
            return new Student(id, name, department, marks);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
