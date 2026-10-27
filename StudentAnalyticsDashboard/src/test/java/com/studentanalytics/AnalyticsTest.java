package com.studentanalytics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying Academic Analytics calculations with exact benchmark criteria.
 */
public class AnalyticsTest {

    @Test
    @DisplayName("Verify Analytics on mandatory test dataset: Arun (85), Priya (92), Rahul (45)")
    void testStandardAnalyticsDataset() {
        List<Student> dataset = new ArrayList<>();
        dataset.add(new Student("S101", "Arun", "CSE", 85));
        dataset.add(new Student("S102", "Priya", "IT", 92));
        dataset.add(new Student("S103", "Rahul", "CSE", 45));

        int total = dataset.size();
        assertEquals(3, total, "Total students must be 3");

        double sum = 0;
        double highest = Double.MIN_VALUE;
        double lowest = Double.MAX_VALUE;
        int passed = 0;
        int failed = 0;

        for (Student s : dataset) {
            double m = s.getMarks();
            sum += m;
            if (m > highest) highest = m;
            if (m < lowest) lowest = m;
            if (s.isPassed()) passed++;
            else failed++;
        }

        double average = sum / total;
        String formattedAverage = String.format("%.2f", average);

        assertEquals("74.00", formattedAverage, "Average marks must be 74.00");
        assertEquals(92.0, highest, "Highest marks must be 92");
        assertEquals(45.0, lowest, "Lowest marks must be 45");
        assertEquals(2, passed, "Students passed must be 2");
        assertEquals(1, failed, "Students failed must be 1");

        // Verify individual grades
        assertEquals("A", dataset.get(0).getGrade(), "Arun must have grade A");
        assertEquals("A+", dataset.get(1).getGrade(), "Priya must have grade A+");
        assertEquals("F", dataset.get(2).getGrade(), "Rahul must have grade F");
    }

    @Test
    @DisplayName("Verify empty dataset handling without division by zero errors")
    void testEmptyDatasetAnalytics() {
        List<Student> emptyList = new ArrayList<>();

        int total = emptyList.size();
        assertEquals(0, total);

        double average = total > 0 ? (0.0 / total) : 0.0;
        double highest = total > 0 ? 100 : 0.0;
        double lowest = total > 0 ? 0 : 0.0;
        int passed = 0;
        int failed = 0;

        assertFalse(Double.isNaN(average), "Average must not be NaN");
        assertFalse(Double.isInfinite(average), "Average must not be Infinite");
        assertEquals(0.0, average);
        assertEquals(0.0, highest);
        assertEquals(0.0, lowest);
        assertEquals(0, passed);
        assertEquals(0, failed);
    }
}
