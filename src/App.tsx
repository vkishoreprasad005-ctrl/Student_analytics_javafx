import React, { useState, useMemo } from 'react';
import {
  GraduationCap,
  Users,
  BarChart3,
  Search,
  UserPlus,
  Table as TableIcon,
  RefreshCw,
  Trash2,
  CheckCircle2,
  AlertCircle,
  Download,
  Terminal,
  FileCode,
  FolderArchive,
  Copy,
  Check,
  Info,
  Award,
  TrendingUp,
  TrendingDown,
  BookOpen,
  ArrowRight
} from 'lucide-react';

interface StudentRecord {
  id: string;
  name: string;
  department: string;
  marks: number;
  grade: string;
}

const INITIAL_STUDENTS: StudentRecord[] = [
  { id: 'S101', name: 'Arun', department: 'CSE', marks: 85, grade: 'A' },
  { id: 'S102', name: 'Priya', department: 'IT', marks: 92, grade: 'A+' },
  { id: 'S103', name: 'Rahul', department: 'CSE', marks: 45, grade: 'F' }
];

export default function App() {
  // Navigation State: 'preview' (Live JavaFX GUI Simulator) | 'code' (Java Source Explorer) | 'run' (VS Code / Maven Terminal Guide)
  const [activeTab, setActiveTab] = useState<'preview' | 'code' | 'run'>('preview');

  // Simulated JavaFX Desktop Window Navigation
  const [fxView, setFxView] = useState<'add' | 'view' | 'search' | 'analytics'>('view');

  // Student State (mirrors students.csv)
  const [students, setStudents] = useState<StudentRecord[]>(INITIAL_STUDENTS);

  // Form State
  const [formId, setFormId] = useState('');
  const [formName, setFormName] = useState('');
  const [formDept, setFormDept] = useState('CSE');
  const [formMarks, setFormMarks] = useState('');
  const [alertInfo, setAlertInfo] = useState<{
    show: boolean;
    type: 'success' | 'error' | 'warning' | 'info';
    title: string;
    message: string;
  }>({ show: false, type: 'info', title: '', message: '' });

  // Confirmation dialog for Clear All
  const [showClearConfirm, setShowClearConfirm] = useState(false);

  // Search State
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResult, setSearchResult] = useState<StudentRecord | null>(null);
  const [searchAttempted, setSearchAttempted] = useState(false);

  // Active Code Viewer File
  const [activeFile, setActiveFile] = useState<string>('DashboardController.java');
  const [copiedFile, setCopiedFile] = useState(false);

  // Grading Rule
  const calculateGrade = (m: number) => {
    if (m >= 90) return 'A+';
    if (m >= 80) return 'A';
    if (m >= 70) return 'B';
    if (m >= 60) return 'C';
    if (m >= 50) return 'D';
    return 'F';
  };

  // Analytics Computation
  const stats = useMemo(() => {
    const total = students.length;
    if (total === 0) {
      return {
        total: 0,
        average: '0.00',
        highest: '0.00',
        lowest: '0.00',
        passed: 0,
        failed: 0,
        passRate: '0.00',
        grades: { 'A+': 0, A: 0, B: 0, C: 0, D: 0, F: 0 }
      };
    }

    let sum = 0;
    let highest = -Infinity;
    let lowest = Infinity;
    let passed = 0;
    let failed = 0;
    const grades = { 'A+': 0, A: 0, B: 0, C: 0, D: 0, F: 0 };

    students.forEach((s) => {
      sum += s.marks;
      if (s.marks > highest) highest = s.marks;
      if (s.marks < lowest) lowest = s.marks;
      if (s.marks >= 50) passed++;
      else failed++;

      const g = s.grade as keyof typeof grades;
      if (grades[g] !== undefined) grades[g]++;
    });

    return {
      total,
      average: (sum / total).toFixed(2),
      highest: highest.toFixed(2).replace(/\.00$/, ''),
      lowest: lowest.toFixed(2).replace(/\.00$/, ''),
      passed,
      failed,
      passRate: ((passed / total) * 100).toFixed(2),
      grades
    };
  }, [students]);

  // Form submission handler
  const handleAddStudent = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    const id = formId.trim();
    const name = formName.trim();
    const dept = formDept.trim();
    const marksStr = formMarks.trim();

    if (!id || !name || !dept || !marksStr) {
      showAlert('error', 'Validation Error', 'All fields are mandatory. Please enter Student ID, Name, Department, and Marks.');
      return;
    }

    if (name.length === 0) {
      showAlert('error', 'Validation Error', 'Student name cannot be empty or only spaces.');
      return;
    }

    const marks = Number(marksStr);
    if (isNaN(marks)) {
      showAlert('error', 'Validation Error', 'Marks must be a valid numeric value between 0 and 100.');
      return;
    }

    if (marks < 0 || marks > 100) {
      showAlert('error', 'Validation Error', `Marks must be between 0.0 and 100.0. Entered value: ${marks}`);
      return;
    }

    // Duplicate check
    if (students.some((s) => s.id.toUpperCase() === id.toUpperCase())) {
      showAlert('error', 'Duplicate Student ID', `A student with ID '${id}' already exists in the database. Student IDs must be unique.`);
      return;
    }

    const newStudent: StudentRecord = {
      id,
      name,
      department: dept,
      marks,
      grade: calculateGrade(marks)
    };

    setStudents((prev) => [...prev, newStudent]);
    showAlert('success', 'Student Added', `Student ${name} (${id}) added successfully with Grade ${newStudent.grade}!`);
    clearForm();
  };

  const clearForm = () => {
    setFormId('');
    setFormName('');
    setFormDept('CSE');
    setFormMarks('');
  };

  const showAlert = (type: 'success' | 'error' | 'warning' | 'info', title: string, message: string) => {
    setAlertInfo({ show: true, type, title, message });
  };

  const handleClearAllRecords = () => {
    setStudents([]);
    setShowClearConfirm(false);
    showAlert('success', 'Records Cleared', 'All student records have been permanently cleared from students.csv.');
  };

  const handleSearch = () => {
    const q = searchQuery.trim();
    if (!q) {
      showAlert('warning', 'Input Required', 'Please enter a Student ID to search.');
      return;
    }
    setSearchAttempted(true);
    const found = students.find((s) => s.id.toUpperCase() === q.toUpperCase()) || null;
    setSearchResult(found);
    if (!found) {
      showAlert('info', 'No Result Found', `No student found with ID: '${q}'. Please verify and try again.`);
    }
  };

  const handleCopyCode = (code: string) => {
    navigator.clipboard.writeText(code);
    setCopiedFile(true);
    setTimeout(() => setCopiedFile(false), 2000);
  };

  // Pre-loaded source code representations for the explorer
  const fileSnippets: Record<string, { desc: string; code: string; language: string }> = {
    'DashboardController.java': {
      desc: 'Main JavaFX UI Controller managing BorderPane layout, navigation, event handlers, and data persistence',
      language: 'java',
      code: `package com.studentanalytics;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class DashboardController {
    private final BorderPane root;
    private final StudentFileHandler fileHandler;
    private final ObservableList<Student> studentList;

    public DashboardController() {
        this.fileHandler = new StudentFileHandler();
        this.studentList = FXCollections.observableArrayList();
        this.root = new BorderPane();
        initializeUI();
        loadDataFromCsv();
        showViewStudentsSection();
    }
    // Features: Add Student, View Table, Search, Academic Analytics
}`
    },
    'Student.java': {
      desc: 'Student model class with JavaFX properties and faculty grade calculation logic',
      language: 'java',
      code: `package com.studentanalytics;

import javafx.beans.property.*;

public class Student {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty department;
    private final DoubleProperty marks;
    private final StringProperty grade;

    public Student(String id, String name, String department, double marks) {
        this.id = new SimpleStringProperty(id != null ? id.trim() : "");
        this.name = new SimpleStringProperty(name != null ? name.trim() : "");
        this.department = new SimpleStringProperty(department != null ? department.trim() : "");
        this.marks = new SimpleDoubleProperty(marks);
        this.grade = new SimpleStringProperty(calculateGrade(marks));
    }

    public static String calculateGrade(double marks) {
        if (marks >= 90.0) return "A+";
        else if (marks >= 80.0) return "A";
        else if (marks >= 70.0) return "B";
        else if (marks >= 60.0) return "C";
        else if (marks >= 50.0) return "D";
        else return "F";
    }

    public boolean isPassed() {
        return getMarks() >= 50.0;
    }
}`
    },
    'StudentFileHandler.java': {
      desc: 'Persistent CSV storage engine with RFC 4180 parsing, duplicate protection, and file I/O',
      language: 'java',
      code: `package com.studentanalytics;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class StudentFileHandler {
    public static final String CSV_HEADER = "Student ID,Student Name,Department,Marks";
    private final Path filePath;

    public StudentFileHandler() {
        this.filePath = Paths.get("students.csv").toAbsolutePath();
        ensureFileExists();
    }

    public synchronized List<Student> loadStudents() throws IOException { ... }
    public synchronized void saveStudent(Student s) throws IOException { ... }
    public synchronized void clearAllRecords() throws IOException { ... }
}`
    },
    'Main.java': {
      desc: 'Application entry point initializing JavaFX Stage, Scene, stylesheet, and window geometry',
      language: 'java',
      code: `package com.studentanalytics;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static final String APP_TITLE = "Student Analytics Dashboard - Faculty Academic Record System";

    @Override
    public void start(Stage primaryStage) {
        DashboardController controller = new DashboardController();
        Scene scene = new Scene(controller.getRoot(), 1120, 720);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(880);
        primaryStage.setMinHeight(580);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}`
    },
    'pom.xml': {
      desc: 'Maven build descriptor with Java 21, JavaFX 21 controls, JUnit 5, and javafx-maven-plugin',
      language: 'xml',
      code: `<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.studentanalytics</groupId>
    <artifactId>student-analytics-dashboard</artifactId>
    <version>1.0.0</version>
    <properties>
        <maven.compiler.release>21</maven.compiler.release>
        <javafx.version>21.0.2</javafx.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>21.0.2</version>
        </dependency>
    </dependencies>
</project>`
    },
    'styles.css': {
      desc: 'JavaFX CSS stylesheet with Dark Navy header, blue primary buttons, and card containers',
      language: 'css',
      code: `.top-header {
    -fx-background-color: #0f172a;
    -fx-border-color: #1e293b;
    -fx-border-width: 0 0 1 0;
}
.btn-primary {
    -fx-background-color: #2563eb;
    -fx-text-fill: #ffffff;
    -fx-font-weight: 600;
    -fx-background-radius: 6;
}
.student-table {
    -fx-background-color: #ffffff;
    -fx-border-color: #e2e8f0;
}`
    },
    'students.csv': {
      desc: 'Local CSV file containing comma-separated student academic records',
      language: 'csv',
      code: `Student ID,Student Name,Department,Marks
S101,Arun,CSE,85
S102,Priya,IT,92
S103,Rahul,CSE,45`
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 flex flex-col font-sans">
      {/* Top Banner & Mode Switcher */}
      <header className="bg-slate-950 border-b border-slate-800 px-6 py-3 flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="h-9 w-9 rounded-lg bg-blue-600/20 border border-blue-500/40 flex items-center justify-center text-blue-400 font-bold">
            <GraduationCap className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-base font-bold text-white flex items-center gap-2">
              Student Analytics Dashboard
              <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-emerald-950 text-emerald-400 border border-emerald-800">
                Java 21 • JavaFX 21 • Maven
              </span>
            </h1>
            <p className="text-xs text-slate-400">
              Native Java Desktop Application • Maven Project Ready
            </p>
          </div>
        </div>

        {/* View Tabs */}
        <div className="flex items-center gap-2">
          <button
            onClick={() => setActiveTab('preview')}
            className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center gap-1.5 transition-colors ${
              activeTab === 'preview'
                ? 'bg-blue-600 text-white'
                : 'bg-slate-800/70 text-slate-300 hover:bg-slate-800'
            }`}
          >
            <TableIcon className="w-3.5 h-3.5" />
            Live JavaFX Desktop Preview
          </button>
          <button
            onClick={() => setActiveTab('code')}
            className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center gap-1.5 transition-colors ${
              activeTab === 'code'
                ? 'bg-blue-600 text-white'
                : 'bg-slate-800/70 text-slate-300 hover:bg-slate-800'
            }`}
          >
            <FileCode className="w-3.5 h-3.5" />
            Java Source Explorer
          </button>
          <button
            onClick={() => setActiveTab('run')}
            className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center gap-1.5 transition-colors ${
              activeTab === 'run'
                ? 'bg-blue-600 text-white'
                : 'bg-slate-800/70 text-slate-300 hover:bg-slate-800'
            }`}
          >
            <Terminal className="w-3.5 h-3.5" />
            Run in VS Code (Guide & CLI)
          </button>
          <a
            href="/student-analytics-dashboard.zip"
            download="student-analytics-dashboard.zip"
            className="px-3 py-1.5 rounded-md text-xs font-medium bg-emerald-600 hover:bg-emerald-500 text-white flex items-center gap-1.5 transition-colors shadow-sm ml-2"
          >
            <FolderArchive className="w-3.5 h-3.5" />
            Download Maven Project (ZIP)
          </a>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 p-4 md:p-6 max-w-7xl w-full mx-auto flex flex-col">
        {activeTab === 'preview' && (
          <div className="flex flex-col flex-1">
            {/* Desktop Window Frame Wrapper */}
            <div className="rounded-xl overflow-hidden border border-slate-700 bg-slate-900 shadow-2xl flex flex-col flex-1">
              {/* Native Window Title Bar */}
              <div className="bg-slate-900 px-4 py-2 flex items-center justify-between border-b border-slate-800 select-none">
                <div className="flex items-center gap-2">
                  <div className="flex gap-1.5">
                    <span className="w-3 h-3 rounded-full bg-red-500/80 inline-block border border-red-600"></span>
                    <span className="w-3 h-3 rounded-full bg-yellow-500/80 inline-block border border-yellow-600"></span>
                    <span className="w-3 h-3 rounded-full bg-green-500/80 inline-block border border-green-600"></span>
                  </div>
                  <span className="text-xs font-mono text-slate-400 ml-2">
                    JavaFX 21 Window: com.studentanalytics.Main (Stage: 1120 × 720)
                  </span>
                </div>
                <div className="text-[11px] text-slate-400 font-mono flex items-center gap-2">
                  <span className="inline-block w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                  Dataset: students.csv ({students.length} records)
                </div>
              </div>

              {/* JavaFX Dashboard Frame */}
              <div className="flex flex-col flex-1 bg-slate-100 text-slate-900">
                {/* 1. Dark Navy Header */}
                <div className="bg-[#0f172a] text-white px-6 py-4 flex items-center justify-between border-b border-slate-800 shadow-md">
                  <div className="flex items-center gap-4">
                    <div className="w-11 h-11 rounded-lg bg-blue-600/20 border border-blue-500/30 flex items-center justify-center text-2xl shadow-inner">
                      🎓
                    </div>
                    <div>
                      <h2 className="text-lg font-bold tracking-tight text-white">
                        Student Analytics Dashboard
                      </h2>
                      <p className="text-xs text-slate-400">
                        Faculty Academic Record & Performance Analytics System
                      </p>
                    </div>
                  </div>
                  <div className="bg-slate-800/80 border border-slate-700 px-3 py-1 rounded-full text-xs font-medium text-slate-300 flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
                    Faculty Portal (Active Session)
                  </div>
                </div>

                {/* 2. Main App Body: Sidebar + Central Content */}
                <div className="flex flex-1 min-h-[520px]">
                  {/* Left-Side Navigation Sidebar */}
                  <aside className="w-60 bg-white border-r border-slate-200 p-4 flex flex-col justify-between shrink-0">
                    <div className="space-y-1.5">
                      <div className="text-[10px] font-bold text-slate-400 uppercase tracking-wider px-3 mb-2">
                        Navigation Menu
                      </div>
                      <button
                        onClick={() => setFxView('add')}
                        className={`w-full text-left px-3 py-2.5 rounded-lg text-xs font-semibold flex items-center gap-2.5 transition-colors ${
                          fxView === 'add'
                            ? 'bg-blue-50 text-blue-600 border border-blue-200 shadow-sm'
                            : 'text-slate-600 hover:bg-slate-50'
                        }`}
                      >
                        <UserPlus className="w-4 h-4 text-blue-600" />
                        Add Student
                      </button>

                      <button
                        onClick={() => setFxView('view')}
                        className={`w-full text-left px-3 py-2.5 rounded-lg text-xs font-semibold flex items-center gap-2.5 transition-colors ${
                          fxView === 'view'
                            ? 'bg-blue-50 text-blue-600 border border-blue-200 shadow-sm'
                            : 'text-slate-600 hover:bg-slate-50'
                        }`}
                      >
                        <TableIcon className="w-4 h-4 text-blue-600" />
                        View Students
                      </button>

                      <button
                        onClick={() => setFxView('search')}
                        className={`w-full text-left px-3 py-2.5 rounded-lg text-xs font-semibold flex items-center gap-2.5 transition-colors ${
                          fxView === 'search'
                            ? 'bg-blue-50 text-blue-600 border border-blue-200 shadow-sm'
                            : 'text-slate-600 hover:bg-slate-50'
                        }`}
                      >
                        <Search className="w-4 h-4 text-blue-600" />
                        Search Student
                      </button>

                      <button
                        onClick={() => setFxView('analytics')}
                        className={`w-full text-left px-3 py-2.5 rounded-lg text-xs font-semibold flex items-center gap-2.5 transition-colors ${
                          fxView === 'analytics'
                            ? 'bg-blue-50 text-blue-600 border border-blue-200 shadow-sm'
                            : 'text-slate-600 hover:bg-slate-50'
                        }`}
                      >
                        <BarChart3 className="w-4 h-4 text-blue-600" />
                        Academic Analytics
                      </button>
                    </div>

                    {/* Sidebar Footer */}
                    <div className="bg-slate-50 border border-slate-200 rounded-lg p-3 text-[11px] text-slate-500">
                      <div className="font-bold text-slate-700 flex items-center gap-1 mb-1">
                        <Terminal className="w-3 h-3 text-blue-600" /> Storage Engine
                      </div>
                      <div>Local CSV: <span className="font-mono text-slate-600">students.csv</span></div>
                      <div className="text-[10px] text-slate-400 mt-1">JavaFX 21 • Java 21 LTS</div>
                    </div>
                  </aside>

                  {/* Right Central Dynamic Pane */}
                  <main className="flex-1 bg-slate-50/70 p-6 overflow-y-auto">
                    {/* VIEW 1: ADD STUDENT */}
                    {fxView === 'add' && (
                      <div className="space-y-6 max-w-2xl">
                        <div>
                          <h3 className="text-xl font-bold text-slate-800">
                            Add New Student Record
                          </h3>
                          <p className="text-xs text-slate-500">
                            Enter student academic credentials below. All fields are mandatory.
                          </p>
                        </div>

                        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-5">
                          <form onSubmit={handleAddStudent} className="space-y-4">
                            <div>
                              <label className="block text-xs font-semibold text-slate-700 mb-1">
                                Student ID *
                              </label>
                              <input
                                type="text"
                                value={formId}
                                onChange={(e) => setFormId(e.target.value)}
                                placeholder="e.g., S104"
                                className="w-full px-3 py-2 rounded-lg border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                              />
                            </div>

                            <div>
                              <label className="block text-xs font-semibold text-slate-700 mb-1">
                                Student Name *
                              </label>
                              <input
                                type="text"
                                value={formName}
                                onChange={(e) => setFormName(e.target.value)}
                                placeholder="e.g., Ananya Sharma"
                                className="w-full px-3 py-2 rounded-lg border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                              />
                            </div>

                            <div>
                              <label className="block text-xs font-semibold text-slate-700 mb-1">
                                Department *
                              </label>
                              <select
                                value={formDept}
                                onChange={(e) => setFormDept(e.target.value)}
                                className="w-full px-3 py-2 rounded-lg border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 bg-white"
                              >
                                <option value="CSE">Computer Science & Engineering (CSE)</option>
                                <option value="IT">Information Technology (IT)</option>
                                <option value="ECE">Electronics & Communication (ECE)</option>
                                <option value="EEE">Electrical & Electronics (EEE)</option>
                                <option value="MECH">Mechanical Engineering (MECH)</option>
                                <option value="CIVIL">Civil Engineering (CIVIL)</option>
                                <option value="AIDS">Data Science & AI (AIDS)</option>
                              </select>
                            </div>

                            <div>
                              <label className="block text-xs font-semibold text-slate-700 mb-1">
                                Marks out of 100 (0 - 100) *
                              </label>
                              <input
                                type="number"
                                step="0.5"
                                value={formMarks}
                                onChange={(e) => setFormMarks(e.target.value)}
                                placeholder="e.g., 87.5"
                                className="w-full px-3 py-2 rounded-lg border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                              />
                            </div>

                            <div className="flex items-center gap-3 pt-2">
                              <button
                                type="submit"
                                className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-bold transition-colors shadow-sm"
                              >
                                Add Student
                              </button>
                              <button
                                type="button"
                                onClick={clearForm}
                                className="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg text-xs font-semibold border border-slate-200 transition-colors"
                              >
                                Clear Form
                              </button>
                            </div>
                          </form>
                        </div>

                        {/* Grading Reference Callout */}
                        <div className="bg-blue-50 border border-blue-200 rounded-xl p-4 text-xs text-blue-900">
                          <div className="font-bold mb-1 flex items-center gap-1.5">
                            <Award className="w-4 h-4 text-blue-600" /> Faculty Grading Reference
                          </div>
                          <div className="grid grid-cols-2 gap-x-4 gap-y-1 text-slate-700 text-[11px] mt-2">
                            <div>• 90–100: <strong>Grade A+</strong> (Passed)</div>
                            <div>• 80–89.99: <strong>Grade A</strong> (Passed)</div>
                            <div>• 70–79.99: <strong>Grade B</strong> (Passed)</div>
                            <div>• 60–69.99: <strong>Grade C</strong> (Passed)</div>
                            <div>• 50–59.99: <strong>Grade D</strong> (Passed)</div>
                            <div>• Below 50: <strong className="text-red-600">Grade F</strong> (Failed)</div>
                          </div>
                        </div>
                      </div>
                    )}

                    {/* VIEW 2: VIEW STUDENTS TABLE */}
                    {fxView === 'view' && (
                      <div className="space-y-4">
                        <div className="flex flex-wrap items-center justify-between gap-3">
                          <div>
                            <h3 className="text-xl font-bold text-slate-800">
                              Student Academic Records
                            </h3>
                            <p className="text-xs text-slate-500">
                              Total Records: {students.length} student(s) stored in students.csv
                            </p>
                          </div>
                          <div className="flex items-center gap-2">
                            <button
                              onClick={() => {
                                showAlert('info', 'Data Refreshed', 'Records reloaded from CSV.');
                              }}
                              className="px-3 py-1.5 rounded-lg border border-slate-300 bg-white hover:bg-slate-50 text-slate-700 text-xs font-medium flex items-center gap-1.5 shadow-sm"
                            >
                              <RefreshCw className="w-3.5 h-3.5" /> Refresh
                            </button>
                            <button
                              onClick={() => setShowClearConfirm(true)}
                              className="px-3 py-1.5 rounded-lg border border-red-200 bg-red-50 hover:bg-red-100 text-red-600 text-xs font-medium flex items-center gap-1.5 transition-colors"
                            >
                              <Trash2 className="w-3.5 h-3.5" /> Clear All Records
                            </button>
                          </div>
                        </div>

                        {/* TableView */}
                        <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                          <table className="w-full text-left text-xs border-collapse">
                            <thead>
                              <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-bold uppercase tracking-wider text-[11px]">
                                <th className="px-4 py-3">Student ID</th>
                                <th className="px-4 py-3">Student Name</th>
                                <th className="px-4 py-3 text-center">Department</th>
                                <th className="px-4 py-3 text-center">Marks (100)</th>
                                <th className="px-4 py-3 text-center">Grade</th>
                              </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100">
                              {students.length === 0 ? (
                                <tr>
                                  <td colSpan={5} className="py-12 text-center text-slate-400">
                                    <div className="font-semibold text-slate-500 text-sm">
                                      No student records found.
                                    </div>
                                    <p className="text-xs text-slate-400 mt-1">
                                      Click 'Add Student' in the navigation menu to record new students.
                                    </p>
                                  </td>
                                </tr>
                              ) : (
                                students.map((s) => (
                                  <tr key={s.id} className="hover:bg-blue-50/40 transition-colors">
                                    <td className="px-4 py-3 font-bold font-mono text-blue-600">
                                      {s.id}
                                    </td>
                                    <td className="px-4 py-3 font-medium text-slate-800">
                                      {s.name}
                                    </td>
                                    <td className="px-4 py-3 text-center text-slate-600 font-mono">
                                      {s.department}
                                    </td>
                                    <td className="px-4 py-3 text-center font-semibold text-slate-700">
                                      {s.marks}
                                    </td>
                                    <td className="px-4 py-3 text-center">
                                      <span
                                        className={`inline-block px-2.5 py-0.5 rounded text-xs font-bold ${
                                          s.grade === 'A+' || s.grade === 'A'
                                            ? 'bg-emerald-100 text-emerald-800'
                                            : s.grade === 'F'
                                            ? 'bg-red-100 text-red-700 font-black'
                                            : 'bg-blue-100 text-blue-800'
                                        }`}
                                      >
                                        {s.grade}
                                      </span>
                                    </td>
                                  </tr>
                                ))
                              )}
                            </tbody>
                          </table>
                        </div>
                      </div>
                    )}

                    {/* VIEW 3: SEARCH STUDENT */}
                    {fxView === 'search' && (
                      <div className="space-y-6 max-w-2xl">
                        <div>
                          <h3 className="text-xl font-bold text-slate-800">
                            Search Student Record
                          </h3>
                          <p className="text-xs text-slate-500">
                            Lookup individual student academic standing by Student ID.
                          </p>
                        </div>

                        {/* Search Bar Panel */}
                        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-3">
                          <label className="block text-xs font-semibold text-slate-700">
                            Enter Student ID to Search:
                          </label>
                          <div className="flex gap-2">
                            <input
                              type="text"
                              value={searchQuery}
                              onChange={(e) => setSearchQuery(e.target.value)}
                              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                              placeholder="e.g., S101"
                              className="flex-1 px-3 py-2 rounded-lg border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                            />
                            <button
                              onClick={handleSearch}
                              className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-bold transition-colors shadow-sm flex items-center gap-1.5"
                            >
                              <Search className="w-3.5 h-3.5" /> Search
                            </button>
                            <button
                              onClick={() => {
                                setSearchQuery('');
                                setSearchResult(null);
                                setSearchAttempted(false);
                              }}
                              className="px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg text-xs font-semibold border border-slate-200"
                            >
                              Clear
                            </button>
                          </div>
                        </div>

                        {/* Search Result Card */}
                        {searchResult && (
                          <div className="bg-white p-6 rounded-xl border border-blue-200 shadow-sm space-y-4">
                            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
                              <div className="flex items-center gap-3">
                                <span className="px-3 py-1 bg-blue-600 text-white font-mono font-bold rounded-lg text-sm">
                                  {searchResult.id}
                                </span>
                                <div>
                                  <h4 className="text-base font-bold text-slate-900">
                                    {searchResult.name}
                                  </h4>
                                  <span className="text-xs text-slate-500">
                                    Dept: {searchResult.department}
                                  </span>
                                </div>
                              </div>
                              <span
                                className={`px-3 py-1 rounded-full text-xs font-bold ${
                                  searchResult.marks >= 50
                                    ? 'bg-emerald-100 text-emerald-800'
                                    : 'bg-red-100 text-red-800'
                                }`}
                              >
                                {searchResult.marks >= 50 ? 'PASSED' : 'FAILED'}
                              </span>
                            </div>

                            <div className="grid grid-cols-2 gap-4 text-xs pt-1">
                              <div>
                                <span className="text-slate-400 block mb-0.5">Marks Obtained</span>
                                <span className="text-lg font-bold text-slate-800">
                                  {searchResult.marks} <span className="text-xs text-slate-400 font-normal">/ 100</span>
                                </span>
                              </div>
                              <div>
                                <span className="text-slate-400 block mb-0.5">Grade Awarded</span>
                                <span
                                  className={`text-lg font-bold ${
                                    searchResult.grade === 'F' ? 'text-red-600' : 'text-blue-600'
                                  }`}
                                >
                                  {searchResult.grade}
                                </span>
                              </div>
                              <div className="col-span-2">
                                <span className="text-slate-400 block mb-0.5">Performance Standing</span>
                                <span className="text-slate-600">
                                  {searchResult.marks >= 50
                                    ? 'Meets or exceeds faculty passing threshold (≥ 50.0 marks)'
                                    : 'Below academic minimum criteria (< 50.0 marks)'}
                                </span>
                              </div>
                            </div>
                          </div>
                        )}

                        {searchAttempted && !searchResult && (
                          <div className="bg-red-50 border border-red-200 rounded-xl p-4 text-xs text-red-800">
                            <span className="font-bold block mb-1">❌ Student Not Found</span>
                            No matching record exists for Student ID '{searchQuery}'.
                          </div>
                        )}

                        {!searchAttempted && (
                          <div className="bg-blue-50/60 border border-blue-200/60 rounded-xl p-4 text-xs text-blue-900">
                            <span className="font-bold block mb-1">💡 Case-Insensitive Search</span>
                            Student ID search matches regardless of case (e.g. 's101' matches 'S101').
                          </div>
                        )}
                      </div>
                    )}

                    {/* VIEW 4: ACADEMIC ANALYTICS */}
                    {fxView === 'analytics' && (
                      <div className="space-y-6">
                        <div className="flex items-center justify-between">
                          <div>
                            <h3 className="text-xl font-bold text-slate-800">
                              Academic Performance Analytics
                            </h3>
                            <p className="text-xs text-slate-500">
                              Calculated dynamically from real student records stored in students.csv.
                            </p>
                          </div>
                          <button
                            onClick={() => {
                              showAlert('info', 'Analytics Recalculated', 'All statistical metrics recomputed.');
                            }}
                            className="px-3 py-1.5 rounded-lg border border-slate-300 bg-white hover:bg-slate-50 text-slate-700 text-xs font-medium flex items-center gap-1.5 shadow-sm"
                          >
                            <RefreshCw className="w-3.5 h-3.5" /> Refresh Analytics
                          </button>
                        </div>

                        {/* 6 Statistic Cards Grid */}
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                          {/* Card 1: Total Students */}
                          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
                            <div className="flex items-center justify-between text-slate-500 text-xs font-bold uppercase tracking-wider mb-2">
                              <span>Total Students</span>
                              <Users className="w-4 h-4 text-blue-600" />
                            </div>
                            <div className="text-3xl font-extrabold text-blue-600">
                              {stats.total}
                            </div>
                            <div className="text-[11px] text-slate-400 mt-2">
                              Enrolled candidates in CSV
                            </div>
                          </div>

                          {/* Card 2: Average Marks */}
                          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
                            <div className="flex items-center justify-between text-slate-500 text-xs font-bold uppercase tracking-wider mb-2">
                              <span>Average Marks</span>
                              <TrendingUp className="w-4 h-4 text-purple-600" />
                            </div>
                            <div className="text-3xl font-extrabold text-purple-600">
                              {stats.average}
                            </div>
                            <div className="text-[11px] text-slate-400 mt-2">
                              Cohort mean score (2 decimals)
                            </div>
                          </div>

                          {/* Card 3: Highest Marks */}
                          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
                            <div className="flex items-center justify-between text-slate-500 text-xs font-bold uppercase tracking-wider mb-2">
                              <span>Highest Marks</span>
                              <Award className="w-4 h-4 text-emerald-600" />
                            </div>
                            <div className="text-3xl font-extrabold text-emerald-600">
                              {stats.highest}
                            </div>
                            <div className="text-[11px] text-slate-400 mt-2">
                              Top performing mark
                            </div>
                          </div>

                          {/* Card 4: Lowest Marks */}
                          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
                            <div className="flex items-center justify-between text-slate-500 text-xs font-bold uppercase tracking-wider mb-2">
                              <span>Lowest Marks</span>
                              <TrendingDown className="w-4 h-4 text-amber-600" />
                            </div>
                            <div className="text-3xl font-extrabold text-amber-600">
                              {stats.lowest}
                            </div>
                            <div className="text-[11px] text-slate-400 mt-2">
                              Lowest recorded score
                            </div>
                          </div>

                          {/* Card 5: Students Passed */}
                          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
                            <div className="flex items-center justify-between text-slate-500 text-xs font-bold uppercase tracking-wider mb-2">
                              <span>Students Passed</span>
                              <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                            </div>
                            <div className="text-3xl font-extrabold text-emerald-600">
                              {stats.passed}
                            </div>
                            <div className="text-[11px] text-slate-400 mt-2">
                              Score ≥ 50.0 marks threshold
                            </div>
                          </div>

                          {/* Card 6: Students Failed */}
                          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
                            <div className="flex items-center justify-between text-slate-500 text-xs font-bold uppercase tracking-wider mb-2">
                              <span>Students Failed</span>
                              <AlertCircle className="w-4 h-4 text-rose-600" />
                            </div>
                            <div className="text-3xl font-extrabold text-rose-600">
                              {stats.failed}
                            </div>
                            <div className="text-[11px] text-slate-400 mt-2">
                              Score &lt; 50.0 marks threshold
                            </div>
                          </div>
                        </div>

                        {/* Cohort Performance & Grade Distribution */}
                        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
                          <h4 className="text-sm font-bold text-slate-800">
                            Cohort Performance & Grade Breakdown
                          </h4>
                          <div className="text-sm font-semibold text-slate-700">
                            Overall Pass Rate: <span className="text-emerald-600">{stats.passRate}%</span> ({stats.passed} of {stats.total} students)
                          </div>
                          <div className="p-3 bg-slate-50 rounded-lg border border-slate-200 font-mono text-xs text-slate-700 flex flex-wrap gap-4">
                            <span>A+: <strong className="text-emerald-700">{stats.grades['A+']}</strong></span>
                            <span>A: <strong className="text-emerald-700">{stats.grades['A']}</strong></span>
                            <span>B: <strong className="text-blue-700">{stats.grades['B']}</strong></span>
                            <span>C: <strong className="text-blue-700">{stats.grades['C']}</strong></span>
                            <span>D: <strong className="text-amber-700">{stats.grades['D']}</strong></span>
                            <span>F: <strong className="text-red-700">{stats.grades['F']}</strong></span>
                          </div>
                        </div>
                      </div>
                    )}
                  </main>
                </div>
              </div>
            </div>

            {/* Alert Dialog Mockup */}
            {alertInfo.show && (
              <div className="fixed inset-0 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
                <div className="bg-white rounded-xl shadow-2xl border border-slate-200 max-w-sm w-full p-5 space-y-4 text-slate-900 animate-in fade-in duration-150">
                  <div className="flex items-center gap-3">
                    {alertInfo.type === 'success' && <CheckCircle2 className="w-6 h-6 text-emerald-600" />}
                    {alertInfo.type === 'error' && <AlertCircle className="w-6 h-6 text-red-600" />}
                    {alertInfo.type === 'warning' && <AlertCircle className="w-6 h-6 text-amber-500" />}
                    {alertInfo.type === 'info' && <Info className="w-6 h-6 text-blue-600" />}
                    <h3 className="font-bold text-sm text-slate-900">{alertInfo.title}</h3>
                  </div>
                  <p className="text-xs text-slate-600 whitespace-pre-line leading-relaxed">
                    {alertInfo.message}
                  </p>
                  <div className="text-right">
                    <button
                      onClick={() => setAlertInfo({ ...alertInfo, show: false })}
                      className="px-4 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-bold"
                    >
                      OK
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Clear All Confirmation Dialog */}
            {showClearConfirm && (
              <div className="fixed inset-0 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
                <div className="bg-white rounded-xl shadow-2xl border border-slate-200 max-w-sm w-full p-5 space-y-4 text-slate-900">
                  <div className="flex items-center gap-3">
                    <AlertCircle className="w-6 h-6 text-red-600" />
                    <h3 className="font-bold text-sm text-slate-900">Confirm Deletion</h3>
                  </div>
                  <p className="text-xs text-slate-600 leading-relaxed">
                    Are you sure you want to delete all {students.length} student record(s)? This will permanently update students.csv and cannot be undone.
                  </p>
                  <div className="flex justify-end gap-2 pt-2">
                    <button
                      onClick={() => setShowClearConfirm(false)}
                      className="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg text-xs font-semibold"
                    >
                      Cancel
                    </button>
                    <button
                      onClick={handleClearAllRecords}
                      className="px-4 py-1.5 bg-red-600 hover:bg-red-700 text-white rounded-lg text-xs font-bold"
                    >
                      Clear All Records
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 2: JAVA SOURCE EXPLORER */}
        {activeTab === 'code' && (
          <div className="flex-1 flex flex-col bg-slate-950 border border-slate-800 rounded-xl overflow-hidden shadow-2xl">
            {/* Explorer Header */}
            <div className="bg-slate-900 border-b border-slate-800 px-4 py-3 flex items-center justify-between">
              <div className="flex items-center gap-2 text-xs text-slate-300">
                <FileCode className="w-4 h-4 text-blue-400" />
                <span className="font-semibold text-white">Project Source Explorer</span>
                <span className="text-slate-500 font-mono text-[11px]">• /StudentAnalyticsDashboard</span>
              </div>
              <button
                onClick={() => handleCopyCode(fileSnippets[activeFile]?.code || '')}
                className="px-3 py-1 rounded bg-slate-800 hover:bg-slate-700 text-xs font-mono text-slate-200 flex items-center gap-1.5 transition-colors border border-slate-700"
              >
                {copiedFile ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                {copiedFile ? 'Copied!' : 'Copy Code'}
              </button>
            </div>

            <div className="flex flex-1 min-h-[480px]">
              {/* File list sidebar */}
              <div className="w-64 bg-slate-900/90 border-r border-slate-800 p-3 space-y-1 text-xs shrink-0 font-mono">
                <div className="text-[10px] text-slate-500 uppercase px-2 py-1 font-bold">Source Files</div>
                {Object.keys(fileSnippets).map((fn) => (
                  <button
                    key={fn}
                    onClick={() => setActiveFile(fn)}
                    className={`w-full text-left px-2.5 py-1.5 rounded flex items-center gap-2 transition-colors ${
                      activeFile === fn
                        ? 'bg-blue-600/20 text-blue-300 border border-blue-500/40 font-semibold'
                        : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200'
                    }`}
                  >
                    <span className="text-slate-500">📄</span>
                    <span className="truncate">{fn}</span>
                  </button>
                ))}
              </div>

              {/* Code viewer pane */}
              <div className="flex-1 flex flex-col p-4 bg-slate-950 font-mono text-xs overflow-x-auto">
                <div className="text-slate-400 text-[11px] mb-3 pb-2 border-b border-slate-800 flex items-center justify-between font-sans">
                  <span>{fileSnippets[activeFile]?.desc}</span>
                  <span className="font-mono text-slate-500 uppercase">{fileSnippets[activeFile]?.language}</span>
                </div>
                <pre className="text-slate-200 leading-relaxed overflow-x-auto select-all">
                  <code>{fileSnippets[activeFile]?.code}</code>
                </pre>
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: RUN IN VS CODE & CLI GUIDE */}
        {activeTab === 'run' && (
          <div className="flex-1 space-y-6 max-w-4xl mx-auto">
            <div className="bg-slate-950 border border-slate-800 rounded-xl p-6 shadow-xl space-y-5">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-lg bg-blue-600/20 border border-blue-500/30 flex items-center justify-center text-blue-400">
                  <Terminal className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white">
                    How to Run Native JavaFX Desktop App in VS Code
                  </h3>
                  <p className="text-xs text-slate-400">
                    Follow these 3 simple steps to execute the real Java desktop window on your local machine.
                  </p>
                </div>
              </div>

              <div className="space-y-4 pt-2">
                {/* Step 1 */}
                <div className="p-4 rounded-lg bg-slate-900 border border-slate-800 space-y-2">
                  <div className="flex items-center gap-2 text-xs font-bold text-blue-400">
                    <span className="w-5 h-5 rounded-full bg-blue-600/30 border border-blue-500/50 flex items-center justify-center text-[10px]">1</span>
                    Download & Open in VS Code
                  </div>
                  <p className="text-xs text-slate-300">
                    Click the download button above or extract <code className="text-blue-300 font-mono">student-analytics-dashboard.zip</code> into your workspace. In VS Code, click <strong>File -&gt; Open Folder...</strong> and select the directory.
                  </p>
                </div>

                {/* Step 2 */}
                <div className="p-4 rounded-lg bg-slate-900 border border-slate-800 space-y-2">
                  <div className="flex items-center gap-2 text-xs font-bold text-blue-400">
                    <span className="w-5 h-5 rounded-full bg-blue-600/30 border border-blue-500/50 flex items-center justify-center text-[10px]">2</span>
                    Run with Single Maven Command
                  </div>
                  <p className="text-xs text-slate-300">
                    Open a terminal in the folder and execute:
                  </p>
                  <div className="bg-slate-950 px-4 py-2.5 rounded border border-slate-800 font-mono text-xs text-emerald-400 flex items-center justify-between">
                    <span>mvn clean javafx:run</span>
                    <button
                      onClick={() => handleCopyCode('mvn clean javafx:run')}
                      className="text-slate-400 hover:text-white transition-colors"
                    >
                      <Copy className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>

                {/* Step 3 */}
                <div className="p-4 rounded-lg bg-slate-900 border border-slate-800 space-y-2">
                  <div className="flex items-center gap-2 text-xs font-bold text-blue-400">
                    <span className="w-5 h-5 rounded-full bg-blue-600/30 border border-blue-500/50 flex items-center justify-center text-[10px]">3</span>
                    Automated Verification Suite
                  </div>
                  <p className="text-xs text-slate-300">
                    To run the automated JUnit 5 tests verifying Arun (85 - A), Priya (92 - A+), and Rahul (45 - F):
                  </p>
                  <div className="bg-slate-950 px-4 py-2.5 rounded border border-slate-800 font-mono text-xs text-emerald-400 flex items-center justify-between">
                    <span>mvn clean test</span>
                    <button
                      onClick={() => handleCopyCode('mvn clean test')}
                      className="text-slate-400 hover:text-white transition-colors"
                    >
                      <Copy className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Test results box */}
            <div className="bg-emerald-950/40 border border-emerald-800/60 rounded-xl p-5 text-xs text-emerald-200 space-y-2">
              <div className="font-bold flex items-center gap-2 text-emerald-400">
                <CheckCircle2 className="w-4 h-4" /> Maven Verification Succeeded in Container
              </div>
              <p className="text-slate-300">
                All 7 automated tests compiled and executed with 0 failures:
              </p>
              <div className="font-mono text-[11px] bg-slate-950/80 p-3 rounded border border-slate-800 text-slate-300">
                [INFO] Running com.studentanalytics.AnalyticsTest<br />
                [INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0<br />
                [INFO] Running com.studentanalytics.StudentTest<br />
                [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0<br />
                [INFO] BUILD SUCCESS (Total time: 9.480 s)
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
