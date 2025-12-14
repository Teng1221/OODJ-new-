package edu.apu.crs.courserecovery;

import edu.apu.crs.models.Student;
import edu.apu.crs.models.Course;
import edu.apu.crs.models.Milestone;
import edu.apu.crs.models.CourseRecoveryPlan;
import edu.apu.crs.service.MasterDataService;
import edu.apu.crs.service.CourseRecoveryService;
import edu.apu.crs.models.SystemUser;
import edu.apu.crs.notification.NotificationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import edu.apu.crs.models.Score;

public class CourseRecoveryDashboard extends JFrame {

    private final SystemUser currentUser;
    private final MasterDataService masterDataService;
    private final CourseRecoveryService courseRecoveryService;
    private final NotificationService notificationService;

    private CardLayout cardLayout;
    private JPanel mainContainer;

    // eligibility use
    private DefaultTableModel eligibilityModel;
    private JComboBox<String> eligibilityFilterCombo;
    private JTextField searchField;

    private JComboBox<Student> studentCombo;
    private JComboBox<Course> courseCombo;
    private JLabel planInfoLabel;
    private JTable milestoneTable;
    private DefaultTableModel milestoneTableModel;
    private String currentPlanId = null;

    public CourseRecoveryDashboard(SystemUser user) {
        this.currentUser = user;
        this.masterDataService = new MasterDataService();
        this.courseRecoveryService = new CourseRecoveryService();
        this.notificationService = new NotificationService();

        setTitle("CRS Dashboard - " + user.getRoleTitle());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // set layout
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(createMenuPanel(), "MENU");
        mainContainer.add(buildEligibilityPanel(), "ELIGIBILITY");
        mainContainer.add(buildRecoveryPanel(), "RECOVERY");
        mainContainer.add(buildReportPanel(), "REPORT");
        mainContainer.add(buildUserManagementPanel(), "USER_MANAGE");

        add(mainContainer);
        cardLayout.show(mainContainer, "MENU");
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        JLabel welcome = new JLabel(
                "Welcome, " + currentUser.getUsername() +
                        " (" + currentUser.getRoleTitle() + ")",
                SwingConstants.CENTER);
        welcome.setFont(new Font("Arial", Font.BOLD, 22));
        menuPanel.add(welcome);

        JLabel roleLabel = new JLabel("Role: " + currentUser.getRoleTitle(), SwingConstants.CENTER);
        roleLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        menuPanel.add(roleLabel);

        String role = currentUser.getRoleTitle();

        if (role.equalsIgnoreCase("Academic Officer")) {
            addButton(menuPanel, "Check Eligibility", "ELIGIBILITY");
            addButton(menuPanel, "Manage Recovery Plans", "RECOVERY");
            addButton(menuPanel, "Academic Reports", "REPORT");
        }

        if (role.equalsIgnoreCase("Course Administrator")
                || role.equalsIgnoreCase("Course Admin")) {
            addButton(menuPanel, "User Management", "USER_MANAGE");
        }

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(255, 200, 200));
        logoutBtn.addActionListener(e -> {
            dispose();
            new edu.apu.crs.usermanagement.LoginPage().setVisible(true);
        });
        menuPanel.add(logoutBtn);

        return menuPanel;
    }

    private void addButton(JPanel panel, String label, String cardName) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Arial", Font.PLAIN, 16));
        btn.addActionListener(e -> cardLayout.show(mainContainer, cardName));
        panel.add(btn);
    }

    //
    private JPanel createHeaderPanel(String title) {
        JPanel header = new JPanel(new BorderLayout());
        JButton backBtn = new JButton("<< Back");
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));

        header.add(backBtn, BorderLayout.WEST);
        header.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.CENTER);
        return header;
    }

    // 1. ELIGIBILITY PANEL
    private JPanel buildEligibilityPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.add(createHeaderPanel("Student Eligibility Check"), BorderLayout.NORTH);

        // Toolbar and Table
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        // FIX 1: Added missing semicolon below
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // filter and search ui
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        toolbar.add(new JLabel("Filter:"));
        eligibilityFilterCombo = new JComboBox<>(
                new String[] { "All Students", "Eligible Only", "Needs Recovery Only" });
        toolbar.add(eligibilityFilterCombo);

        toolbar.add(new JSeparator(SwingConstants.VERTICAL)); // Visual separator
        toolbar.add(new JLabel("Search ID:"));
        searchField = new JTextField(10);
        toolbar.add(searchField);

        JButton searchBtn = new JButton("Search");
        JButton resetBtn = new JButton("Reset");
        JButton enrollBtn = new JButton("Enroll to Recovery Plan"); // New Button

        toolbar.add(searchBtn);
        toolbar.add(resetBtn);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(enrollBtn);

        contentPanel.add(toolbar, BorderLayout.NORTH);
        //

        eligibilityModel = new DefaultTableModel(
                new String[] { "Student ID", "Name", "CGPA", "Failed Courses", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(eligibilityModel);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);
        contentPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Add Content to Main
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Logic & Listeners 
        filterAndLoadData(); // Initial load

        // Filter Action
        eligibilityFilterCombo.addActionListener(e -> filterAndLoadData());

        // Search Action
        searchBtn.addActionListener(e -> {
            String term = searchField.getText().trim();
            if (!term.isEmpty()) {
                searchStudentData(term);
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a Student ID to search.");
            }
        });

        // Reset Action
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            eligibilityFilterCombo.setSelectedIndex(0); // Reset filter to All
            filterAndLoadData(); // Reload full list
        });

        // Enroll Action
        enrollBtn.addActionListener(e -> {
            int[] selectedRows = table.getSelectedRows();
            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(this, "Please select at least one student to enroll.");
                return;
            }

            // Ensure no Eligible students are selected
            for (int row : selectedRows) {
                String status = (String) eligibilityModel.getValueAt(row, 4); // Column 4 is Status
                if ("Eligible".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(this, "Unable to select eligible student", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Enroll " + selectedRows.length + " selected student(s) to recovery plan?",
                    "Confirm Enrollment", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION)
                return;

            boolean emailsSent = false;

            for (int row : selectedRows) {
                String studentId = (String) eligibilityModel.getValueAt(row, 0);
                Student s = masterDataService.findStudentById(studentId);

                if (s != null) {
                    List<Course> failedCourses = courseRecoveryService.getFailedCoursesForStudent(studentId);

                    if (failedCourses.isEmpty()) {
                        // Skip if no failed courses, though they shouldn't be in logic if eligible
                        continue;
                    }

                    for (Course c : failedCourses) {
                        courseRecoveryService.getOrCreateRecoveryPlan(studentId, c.getCourseId());
                        // Email per course? Or one email per student? Request says "email hv sent"
                        // generally.
                        // I will send one email per enrollment to be safe, or just one summary.
                        // Impl plan said: "For each failed course... call sendFailedStudentEnrolled"

                        notificationService.sendFailedStudentEnrolled(s.getEmail(), c.getCourseName());
                    }
                    courseRecoveryService.saveRecoveryPlans();
                    emailsSent = true;
                }
            }

            if (emailsSent) {
                JOptionPane.showMessageDialog(this, "email hv sent to student !");
            }
        });

        return mainPanel;
    }

    // Filter 
    private void filterAndLoadData() {
        eligibilityModel.setRowCount(0);
        List<Student> allStudents = masterDataService.getAllProcessedStudents();
        String selectedFilter = (String) eligibilityFilterCombo.getSelectedItem();

        for (Student s : allStudents) {
            boolean isEligible = (s.getCurrentCGPA() >= 2.0 && s.getFailedCourseCount() <= 3);
            String statusText = isEligible ? "Eligible" : "Needs Recovery";
            boolean show = false;

            if (selectedFilter.equals("All Students"))
                show = true;
            else if (selectedFilter.equals("Eligible Only") && isEligible)
                show = true;
            else if (selectedFilter.equals("Needs Recovery Only") && !isEligible)
                show = true;

            if (show) {
                eligibilityModel.addRow(new Object[] {
                        s.getStudentId(), s.getStudentName(), String.format("%.2f", s.getCurrentCGPA()),
                        s.getFailedCourseCount(), statusText
                });
            }
        }
    }

    // Search 
    private void searchStudentData(String studentId) {
        // 1. Clear Table
        eligibilityModel.setRowCount(0);

        // 2. Find Student using MasterService
        Student s = masterDataService.findStudentById(studentId);

        if (s != null) {
            boolean isEligible = (s.getCurrentCGPA() >= 2.0 && s.getFailedCourseCount() <= 3);
            String statusText = isEligible ? "Eligible" : "Needs Recovery";

            eligibilityModel.addRow(new Object[] {
                    s.getStudentId(), s.getStudentName(), String.format("%.2f", s.getCurrentCGPA()),
                    s.getFailedCourseCount(), statusText
            });
        } else {
            JOptionPane.showMessageDialog(this, "Student ID '" + studentId + "' not found.", "Search Result",
                    JOptionPane.WARNING_MESSAGE);
            // Optionally reload data so table isn't empty
            filterAndLoadData();
        }
    } // Added missing closing brace here

    // 2. RECOVERY PANEL
    private JPanel buildRecoveryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.add(createHeaderPanel("Course Recovery Management"), BorderLayout.NORTH);

        studentCombo = new JComboBox<>();
        courseCombo = new JComboBox<>();

        JButton loadBtn = new JButton("Load Plan");
        JButton saveBtn = new JButton("Save Changes");
        JButton removeRecBtn = new JButton("Remove Recommendation");
        JButton addMilestoneBtn = new JButton("Add Milestone");
        JButton updateMilestoneBtn = new JButton("Update Milestone");
        JButton removeMilestoneBtn = new JButton("Remove Milestone");

        List<Student> processed = masterDataService.getAllProcessedStudents();
        List<Student> needRecovery = new ArrayList<>();

        for (Student s : processed) {
            boolean isEligible = (s.getCurrentCGPA() >= 2.0 && s.getFailedCourseCount() <= 3);
            if (!isEligible) {
                needRecovery.add(s);
            }
        }

        needRecovery.sort(Comparator.comparing(Student::getStudentId));

        studentCombo.removeAllItems();
        for (Student s : needRecovery) {
            studentCombo.addItem(s);
        }

        studentCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Student)
                    setText(((Student) value).getStudentId());
                return this;
            }
        });

        courseCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course)
                    setText(((Course) value).getCourseId());
                return this;
            }
        });

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row1.add(new JLabel("Student:"));
        row1.add(studentCombo);
        row1.add(new JLabel("Failed Course:"));
        row1.add(courseCombo);
        row1.add(loadBtn);
        row1.add(saveBtn);
        row1.add(removeRecBtn);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row2.add(addMilestoneBtn);
        row2.add(updateMilestoneBtn);
        row2.add(removeMilestoneBtn);

        controls.add(row1);
        controls.add(row2);

        northWrapper.add(controls, BorderLayout.SOUTH);
        panel.add(northWrapper, BorderLayout.NORTH);

        planInfoLabel = new JLabel("No plan selected yet");

        milestoneTableModel = new DefaultTableModel(
                new String[] { "Week", "Task", "Status", "Recommendation" }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 2 || col == 3;
            }
        };

        milestoneTable = new JTable(milestoneTableModel);
        milestoneTable.getTableHeader().setReorderingAllowed(false);

        JComboBox<String> statusEditor = new JComboBox<>(
                new String[] { "Not Started", "In Progress", "Completed", "PASS", "FAIL" });

        milestoneTable.getColumnModel().getColumn(2)
                .setCellEditor(new DefaultCellEditor(statusEditor));

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(planInfoLabel, BorderLayout.NORTH);
        center.add(new JScrollPane(milestoneTable), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        studentCombo.addActionListener(e -> reloadCoursesForSelectedStudent());
        loadBtn.addActionListener(e -> loadPlanForSelectedCourse());
        addMilestoneBtn.addActionListener(e -> addMilestoneAction());
        updateMilestoneBtn.addActionListener(e -> updateMilestoneAction());
        removeMilestoneBtn.addActionListener(e -> removeMilestoneAction());

        removeRecBtn.addActionListener(e -> {
            int row = milestoneTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a row first.");
                return;
            }
            int week = Integer.parseInt(milestoneTableModel.getValueAt(row, 0).toString());
            
            courseRecoveryService.updateRecommendation(currentPlanId, 
                ((Course)courseCombo.getSelectedItem()).getCourseId(), 
                week, "NA");
            courseRecoveryService.saveRecoveryPlans();

            loadPlanForSelectedCourse(); 

            // EMAIL NOTIFICATION
            Student s = (Student) studentCombo.getSelectedItem();
            Course c = (Course) courseCombo.getSelectedItem();
            if (s != null && c != null) {
                notificationService.sendRecoveryUpdate(
                    s.getEmail(), 
                    c.getCourseName(), 
                    "Recommendation Removed", 
                    "Recommendation for Week " + week + " has been removed."
                );
            }
        });

        saveBtn.addActionListener(e -> {
            if (milestoneTable.isEditing())
                milestoneTable.getCellEditor().stopCellEditing();
            saveRecoveryEdits();
        });

        if (studentCombo.getItemCount() > 0) {
            studentCombo.setSelectedIndex(0);
            reloadCoursesForSelectedStudent();
        }

        return panel;
    }

    // 3. REPORT PANEL
    private JPanel buildReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(createHeaderPanel("Academic Reports"), BorderLayout.NORTH);

        // Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Student Selection
        controls.add(new JLabel("Student:"));
        JComboBox<Student> reportStudentCombo = new JComboBox<>();
        List<Student> all = masterDataService.getAllProcessedStudents();
        all.sort(Comparator.comparing(Student::getStudentId));
        for (Student s : all)
            reportStudentCombo.addItem(s);

        reportStudentCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Student)
                    setText(((Student) value).getStudentId());
                return this;
            }
        });
        controls.add(reportStudentCombo);

        // Year/Sem Selection
        controls.add(new JLabel("Year:"));
        JComboBox<Integer> yearCombo = new JComboBox<>(new Integer[] { 1, 2 });
        controls.add(yearCombo);

        controls.add(new JLabel("Sem:"));
        JComboBox<String> semCombo = new JComboBox<>(new String[] { "1", "2", "All" });
        controls.add(semCombo);

        JButton previewBtn = new JButton("Preview");
        controls.add(previewBtn);

        panel.add(controls, BorderLayout.CENTER); // will move it to north

        // Table & Result
        DefaultTableModel reportModel = new DefaultTableModel(
                new String[] { "Sem", "Course Code", "Course Name", "Total Credit Hour", "Grade", "Point" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable reportTable = new JTable(reportModel);
        reportTable.getTableHeader().setReorderingAllowed(false);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        JLabel cgpaLabel = new JLabel("GPA/CGPA: -");
        cgpaLabel.setFont(new Font("Arial", Font.BOLD, 14));
        cgpaLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        centerPanel.add(cgpaLabel, BorderLayout.SOUTH);

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.add(controls, BorderLayout.NORTH);
        contentWrapper.add(centerPanel, BorderLayout.CENTER);

        // Footer Actions 
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton pdfBtn = new JButton("Export to PDF");
        JButton emailBtn = new JButton("Email to Student");
        footer.add(pdfBtn);
        footer.add(emailBtn);

        contentWrapper.add(footer, BorderLayout.SOUTH);
        panel.add(contentWrapper, BorderLayout.CENTER);

        // Local variables for listeners
        final java.util.List<Score> displayedScores = new ArrayList<>();
        final double[] displayedCgpa = { 0.0 };

        // Listeners
        previewBtn.addActionListener(e -> {
            reportModel.setRowCount(0);
            displayedScores.clear();
            displayedCgpa[0] = 0.0;
            cgpaLabel.setText("GPA/CGPA: -");

            Student s = (Student) reportStudentCombo.getSelectedItem();
            if (s == null)
                return;

            int year = (Integer) yearCombo.getSelectedItem();
            String semStr = (String) semCombo.getSelectedItem();

            // Map Year/Sem to absolute semester 
            // Y1S1=1, Y1S2=2, Y2S1=3, Y2S2=4
            List<Integer> targetSemesters = new ArrayList<>();
            if ("All".equals(semStr)) {
                if (year == 1) {
                    targetSemesters.add(1);
                    targetSemesters.add(2);
                } else {
                    targetSemesters.add(3);
                    targetSemesters.add(4);
                }
            } else {
                int sVal = Integer.parseInt(semStr);
                int absSem = (year - 1) * 2 + sVal;
                targetSemesters.add(absSem);
            }

            // Filter scores
            List<Score> scores = s.getScores();
            double totalPoints = 0;
            int totalCredits = 0;

            // Need Course Map for Titles/Credits
            Map<String, Course> courseMap = new HashMap<>();
            for (Course c : courseRecoveryService.getAllCourses())
                courseMap.put(c.getCourseId(), c);

            for (Score sc : scores) {
                if (targetSemesters.contains(sc.getsemester())) {
                    Course c = courseMap.get(sc.getcourseId());
                    String title = (c != null) ? c.getCourseName() : "Unknown";
                    int credits = (c != null) ? c.getCredits() : 0;

                    reportModel.addRow(new Object[] {
                            sc.getsemester(), sc.getcourseId(), title, credits, sc.getgrade(), sc.getgradePoint()
                    });

                    displayedScores.add(sc);
                    totalPoints += sc.getgradePoint() * credits;
                    totalCredits += credits;
                }
            }

            if (totalCredits > 0) {
                displayedCgpa[0] = totalPoints / totalCredits;
                cgpaLabel.setText(
                        String.format("Total Credit Hour: %d | GPA/CGPA: %.2f", totalCredits, displayedCgpa[0]));
            } else {
                cgpaLabel.setText("GPA/CGPA: 0.00");
            }
        });

        pdfBtn.addActionListener(e -> {
            if (displayedScores.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No data to export. Please preview first.");
                return;
            }
            Student s = (Student) reportStudentCombo.getSelectedItem();
            int year = (Integer) yearCombo.getSelectedItem();

            // Assuming semCombo is used, if All, just pass -1 or handle in service?
            // The user req says "Select a Semester(1 ,2 , all)".
            // I'll just pass 0 if All, or parse logic.
            int sem = "All".equals(semCombo.getSelectedItem()) ? 0
                    : Integer.parseInt((String) semCombo.getSelectedItem());

            edu.apu.crs.service.PdfService pdfService = new edu.apu.crs.service.PdfService();
            Map<String, Course> courseMap = new HashMap<>();
            for (Course c : courseRecoveryService.getAllCourses())
                courseMap.put(c.getCourseId(), c);

            String path = pdfService.generateStudentReport(s, year, sem, displayedScores, displayedCgpa[0], courseMap);
            if (path != null) {
                JOptionPane.showMessageDialog(this, "PDF Saved: " + path);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to generate PDF.");
            }
        });

        emailBtn.addActionListener(e -> {
            if (displayedScores.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No data to email. Please preview first.");
                return;
            }
            Student s = (Student) reportStudentCombo.getSelectedItem();

            StringBuilder body = new StringBuilder();
            body.append("Academic Report for ").append(s.getStudentName()).append("\n");
            body.append("Year: ").append(yearCombo.getSelectedItem()).append(", Sem: ")
                    .append(semCombo.getSelectedItem()).append("\n\n");
            body.append(String.format("%-4s %-10s %-30s %-8s %-5s %-5s\n", "Sem", "Code", "Name", "Total Credit Hour",
                    "Grade", "Point"));
            body.append("----------------------------------------------------------------------\n");

            Map<String, Course> courseMap = new HashMap<>();
            for (Course c : courseRecoveryService.getAllCourses())
                courseMap.put(c.getCourseId(), c);

            for (Score v : displayedScores) {
                Course c = courseMap.get(v.getcourseId());
                String title = (c != null) ? c.getCourseName() : "Unknown";
                int credits = (c != null) ? c.getCredits() : 0;

                // Truncate title if too long for text table
                if (title.length() > 28)
                    title = title.substring(0, 25) + "...";

                body.append(String.format("%-4d %-10s %-30s %-8d %-5s %-5.2f\n",
                        v.getsemester(), v.getcourseId(), title, credits, v.getgrade(), v.getgradePoint()));
            }
            // Calculate total credits for email
            int totalCreditsEmail = displayedScores.stream().mapToInt(sc -> {
                Course c = courseMap.get(sc.getcourseId());
                return (c != null) ? c.getCredits() : 0;
            }).sum();

            body.append("\nTotal Credit Hour: ").append(totalCreditsEmail).append("\n");
            body.append("GPA: ").append(String.format("%.2f", displayedCgpa[0]));

            notificationService.sendSemesterReport(s.getEmail(), s.getStudentName(), body.toString());
            JOptionPane.showMessageDialog(this, "Email sent to " + s.getEmail());
        });

        return panel;
    }

    // 4. USER MANAGEMENT PANEL
    private JPanel buildUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeaderPanel("User Management"), BorderLayout.NORTH);
        panel.add(new JLabel("Add/Edit System Users UI Placeholder",
                SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private void reloadCoursesForSelectedStudent() {
        if (courseCombo == null)
            return;

        courseCombo.removeAllItems();
        milestoneTableModel.setRowCount(0);
        planInfoLabel.setText("No plan selected yet");
        currentPlanId = null;

        Student selected = (Student) studentCombo.getSelectedItem();
        if (selected == null)
            return;

        List<Course> failedCourses = courseRecoveryService.getFailedCoursesForStudent(selected.getStudentId());

        for (Course c : failedCourses) {
            courseCombo.addItem(c);
        }
    }

    private void loadPlanForSelectedCourse() {
        Student s = (Student) studentCombo.getSelectedItem();
        Course c = (Course) courseCombo.getSelectedItem();

        if (s == null || c == null) {
            JOptionPane.showMessageDialog(this, "Please select both a student and a course.");
            return;
        }

        CourseRecoveryPlan any = courseRecoveryService.getOrCreateRecoveryPlan(s.getStudentId(), c.getCourseId());
        currentPlanId = any.getPlanId();

        planInfoLabel.setText("Plan " + currentPlanId
                + " | Student: " + s.getStudentId()
                + " | Course: " + c.getCourseId());

        List<CourseRecoveryPlan> entries = courseRecoveryService.getRecoveryPlanEntries(s.getStudentId(),
                c.getCourseId());
        java.util.Map<Integer, CourseRecoveryPlan> weekMap = new HashMap<>();
        for (CourseRecoveryPlan p : entries) {
            weekMap.put(p.getStudyWeek(), p);
        }

        List<Milestone> ms = courseRecoveryService.getMilestonesForPlan(currentPlanId, c.getCourseId());

        milestoneTableModel.setRowCount(0);

        for (Milestone m : ms) {
            CourseRecoveryPlan p = weekMap.get(m.getStudyWeek());

            String status = (p == null || p.getStatus() == null || p.getStatus().isBlank())
                    ? "Not Started"
                    : p.getStatus();

            String rec = (p == null || p.getRecommendation() == null)
                    ? "NA"
                    : p.getRecommendation();

            if ("NA".equalsIgnoreCase(rec))
                rec = "";

            milestoneTableModel.addRow(new Object[] {
                    m.getStudyWeek(),
                    m.getTask(),
                    status,
                    rec
            });
        }
    }

    private void saveRecoveryEdits() {
        Student s = (Student) studentCombo.getSelectedItem();
        Course c = (Course) courseCombo.getSelectedItem();

        if (s == null || c == null || currentPlanId == null) {
            JOptionPane.showMessageDialog(this, "Please load a plan first.");
            return;
        }

        if (milestoneTable.isEditing()) {
            milestoneTable.getCellEditor().stopCellEditing();
        }

        for (int r = 0; r < milestoneTableModel.getRowCount(); r++) {
            int week = Integer.parseInt(milestoneTableModel.getValueAt(r, 0).toString());

            String task = String.valueOf(milestoneTableModel.getValueAt(r, 1));
            boolean isExamRow = task != null && task.toLowerCase().contains("exam");

            String newStatus = String.valueOf(milestoneTableModel.getValueAt(r, 2));
            String newRec = String.valueOf(milestoneTableModel.getValueAt(r, 3));
            newRec = sanitizeForCsv(newRec);
            if (newRec.isBlank())
                newRec = "NA";

            CourseRecoveryPlan planWeek = courseRecoveryService.getOrCreatePlanWeek(currentPlanId, s.getStudentId(),
                    c.getCourseId(), week);

            String oldStatus = planWeek.getStatus();

            courseRecoveryService.updateMilestoneStatus(currentPlanId, c.getCourseId(), week, newStatus);
            courseRecoveryService.updateRecommendation(currentPlanId, c.getCourseId(), week, newRec);

            boolean isPassFail = "PASS".equalsIgnoreCase(newStatus) || "FAIL".equalsIgnoreCase(newStatus);

            if (isExamRow && isPassFail) {
                boolean passed = "Pass".equalsIgnoreCase(newStatus);
                double gradePoint = passed ? 2.0 : 0.0;

                courseRecoveryService.recordRecoveryExamResult(
                        s.getStudentId(),
                        c.getCourseId(),
                        gradePoint);
            }
        }

        courseRecoveryService.saveRecoveryPlans();
        JOptionPane.showMessageDialog(this, "Saved!");

        // EMAIL NOTIFICATION
        if (s != null && currentPlanId != null) {
            notificationService.sendRecoveryUpdate(
                s.getEmail(), 
                c.getCourseName(), 
                "Plan/Recommendation Update", 
                "Your recovery plan status or recommendations have been updated. Please check your dashboard."
            );
        }
    }

    private String sanitizeForCsv(String text) {
        if (text == null)
            return "";
        return text.replace(",", "，").trim();
    }

    private void addMilestoneAction() {
        if (currentPlanId == null) {
            JOptionPane.showMessageDialog(this, "Please load a plan first.");
            return;
        }
        Course c = (Course) courseCombo.getSelectedItem();
        if (c == null)
            return;

        String weekStr = JOptionPane.showInputDialog(this, "Enter Week (number):");
        if (weekStr == null)
            return;

        int week;
        try {
            week = Integer.parseInt(weekStr.trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid week number.");
            return;
        }

        String task = JOptionPane.showInputDialog(this, "Enter Milestone Task:");
        if (task == null || task.trim().isEmpty())
            return;

        courseRecoveryService.upsertCustomMilestone(currentPlanId, c.getCourseId(), week, task.trim());
        courseRecoveryService.saveCustomMilestones();

        Student s = (Student) studentCombo.getSelectedItem();
        if (s != null) {
            courseRecoveryService.getOrCreatePlanWeek(currentPlanId, s.getStudentId(), c.getCourseId(), week);
            courseRecoveryService.saveRecoveryPlans();
        }

        loadPlanForSelectedCourse(); // reload table

        if (s != null) {
            notificationService.sendRecoveryUpdate(
                s.getEmail(), 
                c.getCourseName(), 
                "Milestone Added", 
                "New Milestone for Week " + week + ": " + task.trim()
            );
        }
    }

    private void updateMilestoneAction() {
        if (currentPlanId == null) {
            JOptionPane.showMessageDialog(this, "Please load a plan first.");
            return;
        }
        int row = milestoneTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }

        Course c = (Course) courseCombo.getSelectedItem();
        if (c == null)
            return;

        int week = Integer.parseInt(milestoneTableModel.getValueAt(row, 0).toString());
        String oldTask = String.valueOf(milestoneTableModel.getValueAt(row, 1));

        String newTask = JOptionPane.showInputDialog(this, "Update Task:", oldTask);
        if (newTask == null || newTask.trim().isEmpty())
            return;

        courseRecoveryService.upsertCustomMilestone(currentPlanId, c.getCourseId(), week, newTask.trim());
        courseRecoveryService.saveCustomMilestones();

        loadPlanForSelectedCourse();

        Student s = (Student) studentCombo.getSelectedItem();
        if (s != null) {
            notificationService.sendRecoveryUpdate(
                s.getEmail(), 
                c.getCourseName(), 
                "Milestone Updated", 
                "Week " + week + " task updated to: " + newTask.trim()
            );
        }
    }

    private void removeMilestoneAction() {
        if (currentPlanId == null) {
            JOptionPane.showMessageDialog(this, "Please load a plan first.");
            return;
        }
        int row = milestoneTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }

        Course c = (Course) courseCombo.getSelectedItem();
        Student s = (Student) studentCombo.getSelectedItem();
        if (c == null || s == null)
            return;

        int week = Integer.parseInt(milestoneTableModel.getValueAt(row, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove milestone for Week " + week + "?\n(Will remove custom milestone and plan entry for that week)",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        courseRecoveryService.removeCustomMilestone(currentPlanId, c.getCourseId(), week);
        courseRecoveryService.saveCustomMilestones();

        courseRecoveryService.removePlanWeek(currentPlanId, s.getStudentId(), c.getCourseId(), week);
        courseRecoveryService.saveRecoveryPlans();

        loadPlanForSelectedCourse();

        if (s != null) {
            notificationService.sendRecoveryUpdate(
                s.getEmail(), 
                c.getCourseName(), 
                "Milestone Removed", 
                "Milestone for Week " + week + " has been removed."
            );
        }
    }
}
