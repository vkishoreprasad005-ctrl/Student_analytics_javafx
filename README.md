# Student Analytics Dashboard for Faculty

> **Faculty Academic Record & Performance Analytics System**  
> Built with **Java 21**, **JavaFX 21**, **Maven**, and local **CSV Data Persistence**.

---

## 📖 Project Overview

**Student Analytics Dashboard** is a desktop application designed for university and college faculty to manage, audit, and analyze student academic performance. The system provides local file persistence using `students.csv`, an interactive JavaFX UI styled with a modern dark navy and slate theme, real-time statistical analytics, and search capabilities.

### Key Highlights
- **100% Native JavaFX 21 Desktop GUI** using `Stage`, `Scene`, `BorderPane`, `TableView`, `GridPane`, and responsive styling via `styles.css`.
- **Zero External Backend / Database Requirement**: Operates with zero configuration, storing data directly in an RFC 4180-compliant `students.csv` file.
- **Immediate Reactive Sync**: Adding, updating, or clearing records reflects instantaneously across the table and statistical analytics panels.
- **Tested with JUnit 5**: Automated unit test suite verifying grading boundary conditions, CSV persistence, duplicate protection, and analytics calculations.

---

## 🛠️ Technology Stack

| Component | Technology | Version |
| :--- | :--- | :--- |
| **Language** | Java (OpenJDK) | 21 (LTS) |
| **GUI Framework** | OpenJFX (JavaFX Controls, Graphics, Base) | 21.0.2 |
| **Build Tool** | Apache Maven | 3.8+ / 3.9+ |
| **Testing Framework** | JUnit Jupiter (JUnit 5) | 5.10.2 |
| **Styling** | JavaFX CSS | Custom Navy/Slate Theme |
| **Persistence** | Local CSV File (`students.csv`) | UTF-8 / RFC 4180 |

---

## 📂 Project Architecture

```
StudentAnalyticsDashboard/
├── pom.xml                               # Maven project configuration & JavaFX plugins
├── students.csv                          # Local persistent CSV database
├── README.md                             # Comprehensive technical documentation
├── .vscode/                              # VS Code editor configuration
│   ├── launch.json                       # One-click debug & run configuration
│   └── settings.json                     # Java classpath and compiler settings
└── src/
    ├── main/q
    │   ├── java/
    │   │   └── com/
    │   │       └── studentanalytics/
    │   │           ├── Main.java                # JavaFX Application launch entry point
    │   │           ├── Launcher.java            # Standalone Uber-JAR entry point
    │   │           ├── Student.java             # Student model & grade computation
    │   │           ├── StudentFileHandler.java  # CSV parser, writer & persistence engine
    │   │           └── DashboardController.java # UI builder, event handlers & analytics
    │   └── resources/
    │       └── styles.css                       # JavaFX stylesheet (Navy header, cards, buttons)
    └── test/
        └── java/
            └── com/
                └── studentanalytics/
                    ├── StudentTest.java         # Tests for grading, boundaries & CSV I/O
                    └── AnalyticsTest.java       # Tests for benchmark dataset calculations
```

---

## 🚀 Quick Start & How to Run

### 1. Prerequisites
Ensure you have the following installed on your machine:
- **Java Development Kit (JDK) 21** or later:
  ```bash
  java -version
  ```
- **Apache Maven 3.8+**:
  ```bash
  mvn -version
  ```

### 2. Running with Maven (Standard Command)
Open a terminal in the project directory and execute:
```bash
mvn clean javafx:run
```
This compiles all sources, loads JavaFX modules, and launches the native desktop window.

### 3. Running Automated Tests
To run the automated verification suite:
```bash
mvn clean test
```
All 7 unit tests covering grade rules, boundaries, duplicate ID rejection, CSV persistence, and mathematical analytics will run and verify the system.

### 4. Building an Executable Fat JAR
To package the entire application and its dependencies into a standalone executable JAR:
```bash
mvn clean package -DskipTests
java -jar target/student-analytics-dashboard-1.0.0.jar
```

---

## 💻 Running in Visual Studio Code (VS Code)

1. Open **VS Code**.
2. Click **File -> Open Folder...** and select the `StudentAnalyticsDashboard` folder.
3. Install the recommended extension:
   - **Extension Pack for Java** (by Microsoft)
4. VS Code will automatically detect `pom.xml` and configure the project classpath using the included `.vscode/settings.json`.
5. Press **F5** or go to the **Run & Debug** panel (Ctrl+Shift+D / Cmd+Shift+D) and choose:
   - **Run JavaFX Dashboard (Launcher - No VM Args)** or **Run JavaFX Dashboard (Main)**.
6. The native JavaFX desktop window will appear.

---

## 🎯 Features Implemented

### 1. Add Student (`Feature 1`)
- **Fields**:
  - **Student ID**: Unique identifier (e.g., `S104`).
  - **Student Name**: Non-empty text, validated against whitespace-only submissions.
  - **Department**: Dropdown with editable option (`CSE`, `IT`, `ECE`, `EEE`, `MECH`, `CIVIL`, `AIDS`).
  - **Marks**: Validated decimal or integer in the range `[0.0, 100.0]`.
- **Validation**:
  - All fields are strictly mandatory.
  - Case-insensitive duplicate check rejects duplicate Student IDs with an error dialog.
  - Marks strictly validated (non-numeric, negative, or > 100 trigger an alert).
- **Behavior**:
  - Displays a confirmation alert: *"Student added successfully!"*
  - Automatically clears the input form.
  - Immediately appends record to `students.csv`.
  - Refreshes the TableView and recalculates analytics.

### 2. View Students (`Feature 2`)
- **TableView Columns**:
  1. `Student ID` (Bold, Left-aligned)
  2. `Student Name`
  3. `Department` (Centered)
  4. `Marks (100)` (Formatted numeric, Centered)
  5. `Grade` (Color-coded: Green for A+/A, Blue for B/C/D, Red for F)
- **Controls**:
  - **🔄 Refresh**: Reloads data directly from `students.csv`.
  - **🗑️ Clear All Records**: Prompts user with a JavaFX `ConfirmationDialog`. Only when confirmed does it clear `students.csv` and reset the table and analytics.
- **Empty State**: Displays an informative placeholder message when no records exist.

### 3. Search Student (`Feature 3`)
- **Controls**:
  - Search `TextField` for entering Student ID.
  - `Search` button (also triggers on Enter key).
  - `Clear Search` button.
- **Behavior**:
  - Case-insensitive lookup (e.g., searching `s101` matches `S101`).
  - Displays a dedicated Student Profile card with ID badge, Name, Department, Marks, Grade, and Pass/Fail status.
  - Displays a warning alert if the search field is blank.
  - Displays an information alert if no matching student is found.
  - Allows repeated searches without application restart.

### 4. Academic Analytics (`Feature 4`)
- Dynamic calculations derived from actual records:
  - **Total Students**: Count of enrolled candidates.
  - **Average Marks**: Computed cohort mean, formatted to **2 decimal places** (e.g., `74.00`).
  - **Highest Marks**: Maximum score achieved.
  - **Lowest Marks**: Minimum score recorded.
  - **Students Passed**: Count of students scoring $\ge 50.0$.
  - **Students Failed**: Count of students scoring $< 50.0$.
  - **Pass Percentage**: Overall success percentage formatted to 2 decimal places.
  - **Grade Distribution**: Breakdown of grades ($A+, A, B, C, D, F$).
- **Zero Division Safety**: When records are empty, handles statistics gracefully (`Average = 0.00`, `Highest = 0.00`, `Lowest = 0.00`, `Pass Rate = 0.00%`) without runtime errors or `NaN`.
- **🔄 Refresh Analytics** button for manual recalculation.

---

## 📊 Grading Rules

The application implements the faculty grading scale:

| Marks Range | Letter Grade | Academic Status |
| :---: | :---: | :---: |
| **90.00 – 100.00** | **A+** | Passed |
| **80.00 – 89.99** | **A** | Passed |
| **70.00 – 79.99** | **B** | Passed |
| **60.00 – 69.99** | **C** | Passed |
| **50.00 – 59.99** | **D** | Passed |
| **Below 50.00** | **F** | Failed |

---

## 🧪 Benchmark Verification Test Case

Tested with the benchmark records:
```csv
Student ID,Student Name,Department,Marks
S101,Arun,CSE,85
S102,Priya,IT,92
S103,Rahul,CSE,45
```

### Results Computed:
- **Total Students**: `3`
- **Average Marks**: `74.00`
- **Highest Marks**: `92`
- **Lowest Marks**: `45`
- **Students Passed**: `2`
- **Students Failed**: `1`
- **Individual Grades**:
  - Arun: `A` (85 marks)
  - Priya: `A+` (92 marks)
  - Rahul: `F` (45 marks)

---

## 🎬 Lab Demonstration Script (5-Minute Walkthrough)

Follow this step-by-step sequence during an evaluation or lab demonstration:

1. **Launch the Application**:
   ```bash
   mvn clean javafx:run
   ```
2. **Demonstrate Data Loading (View Students)**:
   - Notice the initial records loaded from `students.csv`: `S101 (Arun)`, `S102 (Priya)`, `S103 (Rahul)`.
   - Point out the grade colors: Green for `A+` and `A`, Red for `F`.
3. **Demonstrate Academic Analytics**:
   - Click **Academic Analytics** in the left menu.
   - Show that **Total Students = 3**, **Average Marks = 74.00**, **Highest = 92**, **Lowest = 45**, **Passed = 2**, **Failed = 1**.
   - Show the grade distribution breakdown: `A+: 1 | A: 1 | B: 0 | C: 0 | D: 0 | F: 1`.
4. **Demonstrate Validation & Adding a Student**:
   - Click **Add Student**.
   - Try submitting with empty fields -> **Validation Error Alert** triggers.
   - Try entering ID `S101` (already exists) -> **Duplicate Student ID Alert** triggers.
   - Try entering marks `105` or `-10` -> **Marks Out of Range Alert** triggers.
   - Enter valid data:
     - ID: `S104`
     - Name: `Kavya Patel`
     - Department: `ECE`
     - Marks: `78`
   - Click **Add Student** -> Observe success dialog: *"Student added successfully!"*. Form clears automatically.
5. **Demonstrate Live Sync**:
   - Switch to **View Students** -> `S104` appears with Grade `B`. Total count increments to 4.
   - Switch to **Analytics** -> Total students is now 4, average updates to `75.00`, passed count is 3.
6. **Demonstrate Search**:
   - Click **Search Student**.
   - Search `s104` (lowercase) -> Record found! Displays `Kavya Patel`, `ECE`, `78 marks`, Grade `B`, and Green `PASSED` badge.
   - Search non-existent ID `S999` -> Information alert: *"No student found with ID: 'S999'"*.
7. **Demonstrate Persistence**:
   - Close the desktop window.
   - Inspect `students.csv` in VS Code -> Notice `S104` is saved.
   - Relaunch with `mvn javafx:run` -> All 4 records are retained.

---

## 🔒 Limitations & Future Enhancements

- **Single Concurrent Writer**: Local CSV file storage is optimized for single-faculty workstation usage. Multi-user concurrent write operations should migrate to an ACID RDBMS if concurrent networked multi-device writes are needed.
- **Export Formats**: Currently persists natively in CSV format. PDF report export can be added in future iterations.
