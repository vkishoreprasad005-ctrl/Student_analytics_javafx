package com.studentanalytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying Student model properties, grade calculation logic,
 * pass/fail boundaries, CSV serialization, and file handler operations.
 */
public class StudentTest {

    @TempDir
    Path tempDir;

    private StudentFileHandler fileHandler;

    @BeforeEach
    void setUp() {
        Path tempCsv = tempDir.resolve("test_students.csv");
        fileHandler = new StudentFileHandler(tempCsv);
    }

    @Test
    @DisplayName("Verify expected grades for Arun (85 -> A), Priya (92 -> A+), Rahul (45 -> F)")
    void testExpectedGrades() {
        Student arun = new Student("S101", "Arun", "CSE", 85);
        Student priya = new Student("S102", "Priya", "IT", 92);
        Student rahul = new Student("S103", "Rahul", "CSE", 45);

        assertEquals("A", arun.getGrade(), "Arun with 85 marks must have Grade A");
        assertTrue(arun.isPassed(), "Arun must have passed (marks >= 50)");

        assertEquals("A+", priya.getGrade(), "Priya with 92 marks must have Grade A+");
        assertTrue(priya.isPassed(), "Priya must have passed (marks >= 50)");

        assertEquals("F", rahul.getGrade(), "Rahul with 45 marks must have Grade F");
        assertFalse(rahul.isPassed(), "Rahul must have failed (marks < 50)");
    }

    @Test
    @DisplayName("Verify exact boundary conditions for all grade tiers")
    void testGradeBoundaries() {
        // A+ : 90 to 100
        assertEquals("A+", Student.calculateGrade(100.0));
        assertEquals("A+", Student.calculateGrade(90.0));
        assertEquals("A+", Student.calculateGrade(90.01));

        // A : 80 to 89.99
        assertEquals("A", Student.calculateGrade(89.99));
        assertEquals("A", Student.calculateGrade(85.0));
        assertEquals("A", Student.calculateGrade(80.0));

        // B : 70 to 79.99
        assertEquals("B", Student.calculateGrade(79.99));
        assertEquals("B", Student.calculateGrade(75.5));
        assertEquals("B", Student.calculateGrade(70.0));

        // C : 60 to 69.99
        assertEquals("C", Student.calculateGrade(69.99));
        assertEquals("C", Student.calculateGrade(65.0));
        assertEquals("C", Student.calculateGrade(60.0));

        // D : 50 to 59.99
        assertEquals("D", Student.calculateGrade(59.99));
        assertEquals("D", Student.calculateGrade(55.0));
        assertEquals("D", Student.calculateGrade(50.0));

        // F : Below 50
        assertEquals("F", Student.calculateGrade(49.99));
        assertEquals("F", Student.calculateGrade(25.0));
        assertEquals("F", Student.calculateGrade(0.0));
    }

    @Test
    @DisplayName("Verify CSV line parsing with quotes and standard tokens")
    void testCsvParsing() {
        Student s = StudentFileHandler.parseCsvLine("S105,\"Doe, Jane\",CSE,88.5");
        assertNotNull(s);
        assertEquals("S105", s.getId());
        assertEquals("Doe, Jane", s.getName());
        assertEquals("CSE", s.getDepartment());
        assertEquals(88.5, s.getMarks());
        assertEquals("A", s.getGrade());
    }

    @Test
    @DisplayName("Verify duplicate student ID rejection in file handler")
    void testDuplicateIdPrevention() throws IOException {
        Student s1 = new Student("S101", "Arun", "CSE", 85);
        fileHandler.saveStudent(s1);

        // Attempt duplicate with same case
        assertThrows(IllegalArgumentException.class, () -> {
            fileHandler.saveStudent(new Student("S101", "Arun Duplicate", "CSE", 90));
        });

        // Attempt duplicate with lower case
        assertThrows(IllegalArgumentException.class, () -> {
            fileHandler.saveStudent(new Student("s101", "Arun Lower", "CSE", 90));
        });
    }

    @Test
    @DisplayName("Verify persistence and reload across session simulation")
    void testPersistenceAndReload() throws IOException {
        fileHandler.saveStudent(new Student("S101", "Arun", "CSE", 85));
        fileHandler.saveStudent(new Student("S102", "Priya", "IT", 92));
        fileHandler.saveStudent(new Student("S103", "Rahul", "CSE", 45));

        // Create new handler instance pointing to same file
        StudentFileHandler reloadedHandler = new StudentFileHandler(fileHandler.getFilePath());
        List<Student> students = reloadedHandler.loadStudents();

        assertEquals(3, students.size());
        assertEquals("S101", students.get(0).getId());
        assertEquals("S102", students.get(1).getId());
        assertEquals("S103", students.get(2).getId());

        // Test clear all
        reloadedHandler.clearAllRecords();
        List<Student> emptyList = reloadedHandler.loadStudents();
        assertEquals(0, emptyList.size(), "After clearing all records, list must be empty");
    }
}
