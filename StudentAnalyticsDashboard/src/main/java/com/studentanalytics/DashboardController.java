package com.studentanalytics;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controller and UI builder for the Student Analytics Dashboard.
 * Constructs the main layout (BorderPane), manages the navigation menu,
 * and handles events for Add Student, View Students, Search Student, and Academic Analytics.
 */
public class DashboardController {

    private final BorderPane root;
    private final StudentFileHandler fileHandler;
    private final ObservableList<Student> studentList;

    // Navigation Buttons
    private Button btnNavAdd;
    private Button btnNavView;
    private Button btnNavSearch;
    private Button btnNavAnalytics;

    // View Components
    private TableView<Student> studentTable;
    private Label tableStatusLabel;

    // Form Components
    private TextField txtId;
    private TextField txtName;
    private ComboBox<String> cmbDepartment;
    private TextField txtMarks;

    // Search Components
    private TextField txtSearchId;
    private VBox searchResultBox;

    // Analytics Labels
    private Label lblTotalStudents;
    private Label lblAverageMarks;
    private Label lblHighestMarks;
    private Label lblLowestMarks;
    private Label lblStudentsPassed;
    private Label lblStudentsFailed;
    private Label lblPassPercentage;
    private Label lblGradeDistribution;

    public DashboardController() {
        this.fileHandler = new StudentFileHandler();
        this.studentList = FXCollections.observableArrayList();
        this.root = new BorderPane();

        initializeUI();
        loadDataFromCsv();
        showViewStudentsSection(); // Default view
    }

    public BorderPane getRoot() {
        return root;
    }

    /**
     * Initializes the overall dashboard layout.
     */
    private void initializeUI() {
        root.setTop(createHeader());
        root.setLeft(createSidebar());
        root.getStyleClass().add("dashboard-root");
    }

    /**
     * Creates the top navigation header with dark navy styling.
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("top-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(16);
        header.setPadding(new Insets(16, 24, 16, 24));

        // Academic Icon Badge
        StackPane iconBadge = new StackPane();
        iconBadge.getStyleClass().add("header-icon-badge");
        Label iconLabel = new Label("🎓");
        iconLabel.setStyle("-fx-font-size: 24px;");
        iconBadge.getChildren().add(iconLabel);

        // Titles
        VBox titleBox = new VBox(2);
        Label lblAppTitle = new Label("Student Analytics Dashboard");
        lblAppTitle.getStyleClass().add("header-title");

        Label lblAppSubtitle = new Label("Faculty Academic Record & Performance Analytics System");
        lblAppSubtitle.getStyleClass().add("header-subtitle");

        titleBox.getChildren().addAll(lblAppTitle, lblAppSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Faculty Status Tag
        HBox userBadge = new HBox(8);
        userBadge.setAlignment(Pos.CENTER);
        userBadge.getStyleClass().add("header-user-badge");
        Label statusDot = new Label("●");
        statusDot.setStyle("-fx-text-fill: #10b981; -fx-font-size: 14px;");
        Label userLabel = new Label("Faculty Portal (Active Session)");
        userLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px; -fx-font-weight: 600;");
        userBadge.getChildren().addAll(statusDot, userLabel);

        header.getChildren().addAll(iconBadge, titleBox, spacer, userBadge);
        return header;
    }

    /**
     * Creates the left navigation sidebar with the 4 required buttons.
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(20, 14, 20, 14));
        sidebar.setSpacing(10);

        Label navSectionLabel = new Label("NAVIGATION");
        navSectionLabel.getStyleClass().add("sidebar-section-label");
        sidebar.getChildren().add(navSectionLabel);

        btnNavAdd = createNavButton("➕  Add Student", this::showAddStudentSection);
        btnNavView = createNavButton("📋  View Students", this::showViewStudentsSection);
        btnNavSearch = createNavButton("🔍  Search Student", this::showSearchStudentSection);
        btnNavAnalytics = createNavButton("📊  Academic Analytics", this::showAnalyticsSection);

        sidebar.getChildren().addAll(btnNavAdd, btnNavView, btnNavSearch, btnNavAnalytics);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // System info box in sidebar footer
        VBox sysBox = new VBox(4);
        sysBox.getStyleClass().add("sidebar-footer-box");
        Label sysTitle = new Label("Storage Engine");
        sysTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-size: 11px;");
        Label sysDetails = new Label("Local CSV (students.csv)\nJavaFX 21 • Java 21");
        sysDetails.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px;");
        sysBox.getChildren().addAll(sysTitle, sysDetails);

        sidebar.getChildren().add(sysBox);
        return sidebar;
    }

    private Button createNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> {
            clearNavActiveStyles();
            btn.getStyleClass().add("nav-button-active");
            action.run();
        });
        return btn;
    }

    private void clearNavActiveStyles() {
        btnNavAdd.getStyleClass().remove("nav-button-active");
        btnNavView.getStyleClass().remove("nav-button-active");
        btnNavSearch.getStyleClass().remove("nav-button-active");
        btnNavAnalytics.getStyleClass().remove("nav-button-active");
    }

    // ==========================================
    // DATA PERSISTENCE & SYNC
    // ==========================================

    private void loadDataFromCsv() {
        try {
            List<Student> loaded = fileHandler.loadStudents();
            studentList.setAll(loaded);
            if (tableStatusLabel != null) {
                updateTableStatusLabel();
            }
        } catch (IOException e) {
            showErrorAlert("File Load Error", "Failed to load student data from CSV: " + e.getMessage());
        }
    }

    private void updateTableStatusLabel() {
        if (tableStatusLabel != null) {
            tableStatusLabel.setText("Total Records: " + studentList.size() + " student(s) in database");
        }
    }

    // ==========================================
    // FEATURE 1: ADD STUDENT
    // ==========================================

    private void showAddStudentSection() {
        clearNavActiveStyles();
        btnNavAdd.getStyleClass().add("nav-button-active");

        VBox container = new VBox(20);
        container.getStyleClass().add("content-pane");
        container.setPadding(new Insets(24));

        // Section Title
        VBox titleBox = new VBox(4);
        Label title = new Label("Add New Student Record");
        title.getStyleClass().add("section-heading");
        Label desc = new Label("Enter student academic credentials below. All fields are mandatory.");
        desc.getStyleClass().add("section-subheading");
        titleBox.getChildren().addAll(title, desc);

        // Form Card
        VBox card = new VBox(20);
        card.getStyleClass().add("card-panel");
        card.setMaxWidth(680);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setPadding(new Insets(10));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(140);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        // 1. Student ID
        Label lblId = new Label("Student ID *");
        lblId.getStyleClass().add("form-label");
        txtId = new TextField();
        txtId.setPromptText("e.g., S104");
        txtId.getStyleClass().add("form-input");
        grid.add(lblId, 0, 0);
        grid.add(txtId, 1, 0);

        // 2. Student Name
        Label lblName = new Label("Student Name *");
        lblName.getStyleClass().add("form-label");
        txtName = new TextField();
        txtName.setPromptText("e.g., Ananya Sharma");
        txtName.getStyleClass().add("form-input");
        grid.add(lblName, 0, 1);
        grid.add(txtName, 1, 1);

        // 3. Department
        Label lblDept = new Label("Department *");
        lblDept.getStyleClass().add("form-label");
        cmbDepartment = new ComboBox<>();
        cmbDepartment.getItems().addAll("Computer Science & Engineering (CSE)", "Information Technology (IT)",
                "Electronics & Communication (ECE)", "Electrical & Electronics (EEE)", "Mechanical Engineering (MECH)",
                "Civil Engineering (CIVIL)", "Data Science & AI (AIDS)");
        cmbDepartment.setPromptText("Select or enter department");
        cmbDepartment.setEditable(true);
        cmbDepartment.getStyleClass().add("form-input");
        cmbDepartment.setMaxWidth(Double.MAX_VALUE);
        grid.add(lblDept, 0, 2);
        grid.add(cmbDepartment, 1, 2);

        // 4. Marks out of 100
        Label lblMarks = new Label("Marks (0 - 100) *");
        lblMarks.getStyleClass().add("form-label");
        txtMarks = new TextField();
        txtMarks.setPromptText("e.g., 87.5");
        txtMarks.getStyleClass().add("form-input");
        grid.add(lblMarks, 0, 3);
        grid.add(txtMarks, 1, 3);

        // Buttons
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button btnAdd = new Button("Add Student");
        btnAdd.getStyleClass().addAll("btn-primary", "btn-large");
        btnAdd.setOnAction(e -> handleAddStudentSubmit());

        Button btnClear = new Button("Clear Form");
        btnClear.getStyleClass().addAll("btn-secondary", "btn-large");
        btnClear.setOnAction(e -> clearAddForm());

        buttonBox.getChildren().addAll(btnAdd, btnClear);

        card.getChildren().addAll(grid, buttonBox);

        // Guidelines Card
        VBox guideCard = new VBox(6);
        guideCard.getStyleClass().add("info-callout");
        guideCard.setMaxWidth(680);
        Label guideTitle = new Label("📌 Grading System Reference");
        guideTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e3a8a; -fx-font-size: 13px;");
        Label guideText = new Label("• 90-100: Grade A+  |  • 80-89.99: Grade A  |  • 70-79.99: Grade B\n" +
                "• 60-69.99: Grade C  |  • 50-59.99: Grade D  |  • Below 50: Grade F (Fail)\n" +
                "Passing score threshold: 50.0 marks.");
        guideText.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 12px;");
        guideCard.getChildren().addAll(guideTitle, guideText);

        container.getChildren().addAll(titleBox, card, guideCard);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane-clean");
        root.setCenter(scroll);
    }

    private void handleAddStudentSubmit() {
        String id = txtId.getText() != null ? txtId.getText().trim() : "";
        String name = txtName.getText() != null ? txtName.getText().trim() : "";
        String dept = "";
        if (cmbDepartment.getValue() != null) {
            dept = cmbDepartment.getValue().trim();
            // Shorten to abbreviation if standard long name was chosen
            if (dept.contains("(") && dept.contains(")")) {
                int start = dept.indexOf('(') + 1;
                int end = dept.indexOf(')');
                dept = dept.substring(start, end);
            }
        }
        String marksStr = txtMarks.getText() != null ? txtMarks.getText().trim() : "";

        // 1. Mandatory check
        if (id.isEmpty() || name.isEmpty() || dept.isEmpty() || marksStr.isEmpty()) {
            showErrorAlert("Validation Error", "All fields are mandatory. Please fill in Student ID, Name, Department, and Marks.");
            return;
        }

        // 2. Whitespace check on name
        if (name.replaceAll("\\s+", "").isEmpty()) {
            showErrorAlert("Validation Error", "Student name cannot be empty or contain only whitespace.");
            return;
        }

        // 3. Numeric Marks validation (0 to 100)
        double marks;
        try {
            marks = Double.parseDouble(marksStr);
        } catch (NumberFormatException e) {
            showErrorAlert("Validation Error", "Marks must be a valid numeric value (e.g., 85 or 92.5).");
            return;
        }

        if (marks < 0.0 || marks > 100.0) {
            showErrorAlert("Validation Error", "Marks must be between 0.0 and 100.0. Entered value: " + marks);
            return;
        }

        // 4. Duplicate ID check (case-insensitive)
        for (Student existing : studentList) {
            if (existing.getId().equalsIgnoreCase(id)) {
                showErrorAlert("Duplicate Student ID", "A student with ID '" + id + "' already exists in the database. Student IDs must be unique.");
                return;
            }
        }

        // 5. Construct and Save
        Student newStudent = new Student(id, name, dept, marks);
        try {
            fileHandler.saveStudent(newStudent);
            studentList.add(newStudent);

            showSuccessAlert("Success", "Student added successfully!\n\nID: " + newStudent.getId() +
                    "\nName: " + newStudent.getName() +
                    "\nDepartment: " + newStudent.getDepartment() +
                    "\nMarks: " + newStudent.getFormattedMarks() +
                    "\nGrade: " + newStudent.getGrade());

            clearAddForm();
        } catch (IOException e) {
            showErrorAlert("File Error", "Failed to save student record to CSV: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showErrorAlert("Validation Error", e.getMessage());
        }
    }

    private void clearAddForm() {
        if (txtId != null) txtId.clear();
        if (txtName != null) txtName.clear();
        if (cmbDepartment != null) {
            cmbDepartment.getSelectionModel().clearSelection();
            cmbDepartment.setValue(null);
            if (cmbDepartment.getEditor() != null) cmbDepartment.getEditor().clear();
        }
        if (txtMarks != null) txtMarks.clear();
        if (txtId != null) txtId.requestFocus();
    }

    // ==========================================
    // FEATURE 2: VIEW STUDENTS
    // ==========================================

    private void showViewStudentsSection() {
        clearNavActiveStyles();
        btnNavView.getStyleClass().add("nav-button-active");

        VBox container = new VBox(16);
        container.getStyleClass().add("content-pane");
        container.setPadding(new Insets(24));

        // Heading with action buttons
        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Student Academic Records");
        title.getStyleClass().add("section-heading");
        tableStatusLabel = new Label("Total Records: " + studentList.size() + " student(s) in database");
        tableStatusLabel.getStyleClass().add("section-subheading");
        titleBox.getChildren().addAll(title, tableStatusLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.getStyleClass().add("btn-secondary");
        btnRefresh.setOnAction(e -> {
            loadDataFromCsv();
            showInformationAlert("Data Refreshed", "Student records successfully reloaded from CSV file.");
        });

        Button btnClearAll = new Button("🗑️ Clear All Records");
        btnClearAll.getStyleClass().add("btn-danger");
        btnClearAll.setOnAction(e -> handleClearAllRecords());

        headerRow.getChildren().addAll(titleBox, spacer, btnRefresh, btnClearAll);

        // TableView setup
        studentTable = new TableView<>();
        studentTable.getStyleClass().add("student-table");
        studentTable.setItems(studentList);
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(studentTable, Priority.ALWAYS);

        // Column 1: Student ID
        TableColumn<Student, String> colId = new TableColumn<>("Student ID");
        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        colId.setPrefWidth(120);
        colId.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");

        // Column 2: Student Name
        TableColumn<Student, String> colName = new TableColumn<>("Student Name");
        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colName.setPrefWidth(220);

        // Column 3: Department
        TableColumn<Student, String> colDept = new TableColumn<>("Department");
        colDept.setCellValueFactory(cellData -> cellData.getValue().departmentProperty());
        colDept.setPrefWidth(140);
        colDept.setStyle("-fx-alignment: CENTER;");

        // Column 4: Marks
        TableColumn<Student, Double> colMarks = new TableColumn<>("Marks (100)");
        colMarks.setCellValueFactory(cellData -> cellData.getValue().marksProperty().asObject());
        colMarks.setPrefWidth(120);
        colMarks.setStyle("-fx-alignment: CENTER;");
        colMarks.setCellFactory(col -> new TableCell<Student, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item == Math.floor(item)) {
                        setText(String.valueOf(item.longValue()));
                    } else {
                        setText(String.format("%.2f", item));
                    }
                }
            }
        });

        // Column 5: Grade
        TableColumn<Student, String> colGrade = new TableColumn<>("Grade");
        colGrade.setCellValueFactory(cellData -> cellData.getValue().gradeProperty());
        colGrade.setPrefWidth(100);
        colGrade.setStyle("-fx-alignment: CENTER;");
        colGrade.setCellFactory(col -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                } else {
                    setText(item);
                    if ("A+".equals(item) || "A".equals(item)) {
                        setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #059669;");
                    } else if ("B".equals(item) || "C".equals(item) || "D".equals(item)) {
                        setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #2563eb;");
                    } else {
                        setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
                    }
                }
            }
        });

        studentTable.getColumns().addAll(colId, colName, colDept, colMarks, colGrade);

        // Placeholder for empty table
        VBox emptyPlaceholder = new VBox(10);
        emptyPlaceholder.setAlignment(Pos.CENTER);
        Label lblNoData = new Label("No student records found.");
        lblNoData.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        Label lblPrompt = new Label("Click 'Add Student' in the navigation menu to record new students.");
        lblPrompt.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        emptyPlaceholder.getChildren().addAll(lblNoData, lblPrompt);
        studentTable.setPlaceholder(emptyPlaceholder);

        container.getChildren().addAll(headerRow, studentTable);
        root.setCenter(container);
    }

    private void handleClearAllRecords() {
        if (studentList.isEmpty()) {
            showInformationAlert("No Records", "There are currently no student records to clear.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Clear All Student Records?");
        confirmAlert.setContentText("Are you sure you want to delete all " + studentList.size() +
                " student record(s)?\n\nThis will permanently update students.csv and cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                fileHandler.clearAllRecords();
                studentList.clear();
                updateTableStatusLabel();
                showSuccessAlert("Records Cleared", "All student records have been successfully deleted from the CSV file.");
            } catch (IOException e) {
                showErrorAlert("File Error", "Failed to clear student records from CSV: " + e.getMessage());
            }
        }
    }

    // ==========================================
    // FEATURE 3: SEARCH STUDENT
    // ==========================================

    private void showSearchStudentSection() {
        clearNavActiveStyles();
        btnNavSearch.getStyleClass().add("nav-button-active");

        VBox container = new VBox(20);
        container.getStyleClass().add("content-pane");
        container.setPadding(new Insets(24));

        VBox titleBox = new VBox(4);
        Label title = new Label("Search Student Record");
        title.getStyleClass().add("section-heading");
        Label desc = new Label("Lookup an individual student's record and academic standing by Student ID.");
        desc.getStyleClass().add("section-subheading");
        titleBox.getChildren().addAll(title, desc);

        // Search Control Card
        VBox searchCard = new VBox(16);
        searchCard.getStyleClass().add("card-panel");
        searchCard.setMaxWidth(680);

        Label lblPrompt = new Label("Enter Student ID to Search:");
        lblPrompt.getStyleClass().add("form-label");

        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        txtSearchId = new TextField();
        txtSearchId.setPromptText("e.g., S101");
        txtSearchId.getStyleClass().add("form-input");
        HBox.setHgrow(txtSearchId, Priority.ALWAYS);

        // Trigger search on Enter key
        txtSearchId.setOnAction(e -> handleSearchSubmit());

        Button btnSearch = new Button("🔍 Search");
        btnSearch.getStyleClass().addAll("btn-primary", "btn-large");
        btnSearch.setOnAction(e -> handleSearchSubmit());

        Button btnClearSearch = new Button("Clear Search");
        btnClearSearch.getStyleClass().addAll("btn-secondary", "btn-large");
        btnClearSearch.setOnAction(e -> clearSearchSection());

        searchRow.getChildren().addAll(txtSearchId, btnSearch, btnClearSearch);
        searchCard.getChildren().addAll(lblPrompt, searchRow);

        // Result Container
        searchResultBox = new VBox(16);
        searchResultBox.setMaxWidth(680);

        // Default initial info message
        VBox initialGuide = new VBox(8);
        initialGuide.getStyleClass().add("info-callout");
        Label tipTitle = new Label("💡 Search Guidance");
        tipTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e3a8a; -fx-font-size: 13px;");
        Label tipDesc = new Label("Student ID search is case-insensitive (e.g. typing 's101' will match 'S101').\n" +
                "You can perform multiple searches sequentially without restarting the application.");
        tipDesc.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 12px;");
        initialGuide.getChildren().addAll(tipTitle, tipDesc);
        searchResultBox.getChildren().add(initialGuide);

        container.getChildren().addAll(titleBox, searchCard, searchResultBox);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane-clean");
        root.setCenter(scroll);
    }

    private void handleSearchSubmit() {
        String searchId = txtSearchId.getText() != null ? txtSearchId.getText().trim() : "";

        // 1. Validation for empty search
        if (searchId.isEmpty()) {
            showWarningAlert("Search Input Required", "Please enter a Student ID to search.");
            txtSearchId.requestFocus();
            return;
        }

        // 2. Case-insensitive lookup
        Student foundStudent = null;
        for (Student s : studentList) {
            if (s.getId().equalsIgnoreCase(searchId)) {
                foundStudent = s;
                break;
            }
        }

        searchResultBox.getChildren().clear();

        // 3. Not Found
        if (foundStudent == null) {
            showInformationAlert("No Result Found", "No student found with ID: '" + searchId + "'.\nPlease verify the ID and try again.");

            VBox notFoundBox = new VBox(8);
            notFoundBox.getStyleClass().add("card-panel");
            notFoundBox.setStyle("-fx-border-color: #fca5a5; -fx-background-color: #fef2f2;");
            Label lblNotFound = new Label("❌ Student Not Found");
            lblNotFound.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #b91c1c;");
            Label lblHint = new Label("No matching record was found for Student ID '" + searchId + "'.");
            lblHint.setStyle("-fx-text-fill: #7f1d1d; -fx-font-size: 13px;");
            notFoundBox.getChildren().addAll(lblNotFound, lblHint);
            searchResultBox.getChildren().add(notFoundBox);
            return;
        }

        // 4. Found - Display Comprehensive Result Card
        VBox resultCard = new VBox(16);
        resultCard.getStyleClass().add("card-panel");

        HBox cardHeader = new HBox(12);
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        Label idBadge = new Label(foundStudent.getId());
        idBadge.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12 6 12; -fx-background-radius: 6;");

        VBox studentTitleBox = new VBox(2);
        Label studentName = new Label(foundStudent.getName());
        studentName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label studentDept = new Label("Department: " + foundStudent.getDepartment());
        studentDept.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        studentTitleBox.getChildren().addAll(studentName, studentDept);

        Region cardSpacer = new Region();
        HBox.setHgrow(cardSpacer, Priority.ALWAYS);

        boolean passed = foundStudent.isPassed();
        Label statusBadge = new Label(passed ? "PASSED" : "FAILED");
        statusBadge.setStyle(passed ?
                "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-font-weight: bold; -fx-padding: 6 12 6 12; -fx-background-radius: 20; -fx-font-size: 12px;" :
                "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-padding: 6 12 6 12; -fx-background-radius: 20; -fx-font-size: 12px;");

        cardHeader.getChildren().addAll(idBadge, studentTitleBox, cardSpacer, statusBadge);

        // Details Grid
        GridPane details = new GridPane();
        details.setHgap(24);
        details.setVgap(12);
        details.setPadding(new Insets(12, 0, 8, 0));

        details.add(createDetailRowLabel("Student ID:"), 0, 0);
        details.add(createDetailValueLabel(foundStudent.getId()), 1, 0);

        details.add(createDetailRowLabel("Full Name:"), 0, 1);
        details.add(createDetailValueLabel(foundStudent.getName()), 1, 1);

        details.add(createDetailRowLabel("Department:"), 0, 2);
        details.add(createDetailValueLabel(foundStudent.getDepartment()), 1, 2);

        details.add(createDetailRowLabel("Marks Obtained:"), 0, 3);
        details.add(createDetailValueLabel(foundStudent.getFormattedMarks() + " / 100"), 1, 3);

        details.add(createDetailRowLabel("Grade Awarded:"), 0, 4);
        Label gradeVal = createDetailValueLabel(foundStudent.getGrade());
        gradeVal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " +
                ("F".equals(foundStudent.getGrade()) ? "#dc2626" : "#2563eb") + ";");
        details.add(gradeVal, 1, 4);

        details.add(createDetailRowLabel("Performance Status:"), 0, 5);
        details.add(createDetailValueLabel(passed ? "Meets or exceeds passing criteria (>= 50 marks)" : "Below academic passing threshold (< 50 marks)"), 1, 5);

        resultCard.getChildren().addAll(cardHeader, new Separator(), details);
        searchResultBox.getChildren().add(resultCard);
    }

    private Label createDetailRowLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 13px;");
        return lbl;
    }

    private Label createDetailValueLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 13px;");
        return lbl;
    }

    private void clearSearchSection() {
        if (txtSearchId != null) {
            txtSearchId.clear();
            txtSearchId.requestFocus();
        }
        if (searchResultBox != null) {
            searchResultBox.getChildren().clear();
        }
    }

    // ==========================================
    // FEATURE 4: ACADEMIC ANALYTICS
    // ==========================================

    private void showAnalyticsSection() {
        clearNavActiveStyles();
        btnNavAnalytics.getStyleClass().add("nav-button-active");

        VBox container = new VBox(20);
        container.getStyleClass().add("content-pane");
        container.setPadding(new Insets(24));

        // Header with Refresh button
        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Academic Performance Analytics");
        title.getStyleClass().add("section-heading");
        Label desc = new Label("Calculated dynamically from real student records stored in students.csv.");
        desc.getStyleClass().add("section-subheading");
        titleBox.getChildren().addAll(title, desc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefreshAnalytics = new Button("🔄 Refresh Analytics");
        btnRefreshAnalytics.getStyleClass().add("btn-primary");
        btnRefreshAnalytics.setOnAction(e -> {
            loadDataFromCsv();
            updateAnalyticsValues();
            showInformationAlert("Analytics Updated", "Academic statistics successfully recalculated.");
        });

        headerRow.getChildren().addAll(titleBox, spacer, btnRefreshAnalytics);

        // 6 Statistic Cards Grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(16);
        statsGrid.setVgap(16);
        statsGrid.setMaxWidth(960);

        // Responsive 3-column configuration
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            statsGrid.getColumnConstraints().add(col);
        }

        lblTotalStudents = new Label("0");
        VBox cardTotal = createStatCard("TOTAL STUDENTS", lblTotalStudents, "👥", "#2563eb", "Enrolled candidates");

        lblAverageMarks = new Label("0.00");
        VBox cardAverage = createStatCard("AVERAGE MARKS", lblAverageMarks, "📈", "#7c3aed", "Cohort mean score");

        lblHighestMarks = new Label("0.00");
        VBox cardHighest = createStatCard("HIGHEST MARKS", lblHighestMarks, "🏆", "#059669", "Top performing mark");

        lblLowestMarks = new Label("0.00");
        VBox cardLowest = createStatCard("LOWEST MARKS", lblLowestMarks, "📉", "#d97706", "Lowest recorded score");

        lblStudentsPassed = new Label("0");
        VBox cardPassed = createStatCard("STUDENTS PASSED", lblStudentsPassed, "✅", "#10b981", "Score ≥ 50.0");

        lblStudentsFailed = new Label("0");
        VBox cardFailed = createStatCard("STUDENTS FAILED", lblStudentsFailed, "❌", "#ef4444", "Score < 50.0");

        statsGrid.add(cardTotal, 0, 0);
        statsGrid.add(cardAverage, 1, 0);
        statsGrid.add(cardHighest, 2, 0);
        statsGrid.add(cardLowest, 0, 1);
        statsGrid.add(cardPassed, 1, 1);
        statsGrid.add(cardFailed, 2, 1);

        // Summary and Grade Distribution Card
        VBox summaryCard = new VBox(14);
        summaryCard.getStyleClass().add("card-panel");
        summaryCard.setMaxWidth(960);

        Label summaryTitle = new Label("Cohort Performance & Grade Breakdown");
        summaryTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        lblPassPercentage = new Label("Overall Pass Rate: 0.00%");
        lblPassPercentage.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        lblGradeDistribution = new Label("A+: 0  |  A: 0  |  B: 0  |  C: 0  |  D: 0  |  F: 0");
        lblGradeDistribution.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-font-family: 'monospace';");

        summaryCard.getChildren().addAll(summaryTitle, new Separator(), lblPassPercentage, lblGradeDistribution);

        container.getChildren().addAll(headerRow, statsGrid, summaryCard);

        updateAnalyticsValues();

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane-clean");
        root.setCenter(scroll);
    }

    private VBox createStatCard(String labelText, Label valueLabel, String icon, String accentColor, String footerText) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(16));

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label(labelText);
        lblTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-letter-spacing: 0.5px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblIcon = new Label(icon);
        lblIcon.setStyle("-fx-font-size: 16px;");

        topRow.getChildren().addAll(lblTitle, spacer, lblIcon);

        valueLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");

        Label lblFooter = new Label(footerText);
        lblFooter.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        card.getChildren().addAll(topRow, valueLabel, lblFooter);
        return card;
    }

    /**
     * Calculates statistics dynamically from student records.
     * Handles empty dataset gracefully without division by zero.
     */
    private void updateAnalyticsValues() {
        if (lblTotalStudents == null) return;

        int total = studentList.size();

        if (total == 0) {
            lblTotalStudents.setText("0");
            lblAverageMarks.setText("0.00");
            lblHighestMarks.setText("0.00");
            lblLowestMarks.setText("0.00");
            lblStudentsPassed.setText("0");
            lblStudentsFailed.setText("0");
            if (lblPassPercentage != null) {
                lblPassPercentage.setText("Overall Pass Rate: 0.00% (No data)");
            }
            if (lblGradeDistribution != null) {
                lblGradeDistribution.setText("A+: 0  |  A: 0  |  B: 0  |  C: 0  |  D: 0  |  F: 0");
            }
            return;
        }

        double sum = 0;
        double highest = -1.0;
        double lowest = 101.0;
        int passedCount = 0;
        int failedCount = 0;

        int countAPlus = 0;
        int countA = 0;
        int countB = 0;
        int countC = 0;
        int countD = 0;
        int countF = 0;

        for (Student s : studentList) {
            double m = s.getMarks();
            sum += m;

            if (m > highest) highest = m;
            if (m < lowest) lowest = m;

            if (s.isPassed()) {
                passedCount++;
            } else {
                failedCount++;
            }

            String g = s.getGrade();
            switch (g) {
                case "A+": countAPlus++; break;
                case "A":  countA++; break;
                case "B":  countB++; break;
                case "C":  countC++; break;
                case "D":  countD++; break;
                case "F":  countF++; break;
            }
        }

        double average = sum / total;
        double passRate = ((double) passedCount / total) * 100.0;

        lblTotalStudents.setText(String.valueOf(total));
        lblAverageMarks.setText(String.format("%.2f", average));
        lblHighestMarks.setText(formatScore(highest));
        lblLowestMarks.setText(formatScore(lowest));
        lblStudentsPassed.setText(String.valueOf(passedCount));
        lblStudentsFailed.setText(String.valueOf(failedCount));

        if (lblPassPercentage != null) {
            lblPassPercentage.setText(String.format("Overall Pass Rate: %.2f%%  (%d of %d students)", passRate, passedCount, total));
        }

        if (lblGradeDistribution != null) {
            lblGradeDistribution.setText(String.format(
                    "A+: %d  |  A: %d  |  B: %d  |  C: %d  |  D: %d  |  F: %d",
                    countAPlus, countA, countB, countC, countD, countF
            ));
        }
    }

    private String formatScore(double score) {
        if (score == Math.floor(score)) {
            return String.valueOf((long) score);
        } else {
            return String.format("%.2f", score);
        }
    }

    // ==========================================
    // ALERT DIALOG HELPERS
    // ==========================================

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Success Confirmation");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Action Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("Attention Required");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInformationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
