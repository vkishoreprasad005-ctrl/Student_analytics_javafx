package com.studentanalytics;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

/**
 * Model class representing a Student with academic records.
 * Provides JavaFX observable properties for direct TableView binding
 * and implements grading rules specified by the faculty system.
 */
public class Student {

    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty department;
    private final DoubleProperty marks;
    private final StringProperty grade;

    /**
     * Constructs a new Student.
     *
     * @param id         Unique student identifier (e.g. S101)
     * @param name       Student full name
     * @param department Academic department (e.g. CSE, IT, ECE)
     * @param marks      Marks scored out of 100 (0 to 100)
     */
    public Student(String id, String name, String department, double marks) {
        this.id = new SimpleStringProperty(id != null ? id.trim() : "");
        this.name = new SimpleStringProperty(name != null ? name.trim() : "");
        this.department = new SimpleStringProperty(department != null ? department.trim() : "");
        this.marks = new SimpleDoubleProperty(marks);
        this.grade = new SimpleStringProperty(calculateGrade(marks));

        // Recompute grade if marks change
        this.marks.addListener((obs, oldVal, newVal) -> {
            this.grade.set(calculateGrade(newVal.doubleValue()));
        });
    }

    /**
     * Calculates the letter grade based on faculty rules:
     * - 90 to 100: A+
     * - 80 to 89.99: A
     * - 70 to 79.99: B
     * - 60 to 69.99: C
     * - 50 to 59.99: D
     * - Below 50: F
     *
     * @param marks marks scored (0 - 100)
     * @return Grade string ("A+", "A", "B", "C", "D", "F")
     */
    public static String calculateGrade(double marks) {
        if (marks >= 90.0) {
            return "A+";
        } else if (marks >= 80.0) {
            return "A";
        } else if (marks >= 70.0) {
            return "B";
        } else if (marks >= 60.0) {
            return "C";
        } else if (marks >= 50.0) {
            return "D";
        } else {
            return "F";
        }
    }

    /**
     * Checks if the student passed (marks >= 50).
     *
     * @return true if passed, false otherwise
     */
    public boolean isPassed() {
        return getMarks() >= 50.0;
    }

    // Property getters for JavaFX TableView data binding

    public StringProperty idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty departmentProperty() {
        return department;
    }

    public DoubleProperty marksProperty() {
        return marks;
    }

    public StringProperty gradeProperty() {
        return grade;
    }

    // Standard getters and setters

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id != null ? id.trim() : "");
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name != null ? name.trim() : "");
    }

    public String getDepartment() {
        return department.get();
    }

    public void setDepartment(String department) {
        this.department.set(department != null ? department.trim() : "");
    }

    public double getMarks() {
        return marks.get();
    }

    public void setMarks(double marks) {
        this.marks.set(marks);
    }

    public String getGrade() {
        return grade.get();
    }

    /**
     * Formats marks for display, removing unnecessary decimals if integer.
     */
    public String getFormattedMarks() {
        double m = getMarks();
        if (m == Math.floor(m)) {
            return String.valueOf((long) m);
        } else {
            return String.format("%.2f", m);
        }
    }

    /**
     * Converts student data into a CSV line.
     * Escapes commas and quotes according to standard CSV RFC 4180 rules.
     */
    public String toCsvRow() {
        return escapeCsv(getId()) + "," +
               escapeCsv(getName()) + "," +
               escapeCsv(getDepartment()) + "," +
               getFormattedMarks();
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(getId().toUpperCase(), student.getId().toUpperCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId().toUpperCase());
    }

    @Override
    public String toString() {
        return "Student{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", department='" + getDepartment() + '\'' +
                ", marks=" + getMarks() +
                ", grade='" + getGrade() + '\'' +
                '}';
    }
}
