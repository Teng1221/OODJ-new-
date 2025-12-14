package edu.apu.crs.service;

import edu.apu.crs.models.Course;
import edu.apu.crs.models.CourseRecoveryPlan;
import edu.apu.crs.models.Milestone;
import edu.apu.crs.models.Program;
import edu.apu.crs.models.Score;
import edu.apu.crs.models.Student;

import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CourseRecoveryService {

    private static final String DATA_FOLDER = "data/";
    private static final String COURSE_LIST_FILE = DATA_FOLDER + "courseList.txt";
    private static final String STUDENT_LIST_FILE = DATA_FOLDER + "stuList.txt";
    private static final String SCORE_FILE = DATA_FOLDER + "stuScore.txt";
    private static final String PROGRAM_LIST_FILE = DATA_FOLDER + "programList.txt";
    private static final String PROGRAM_FILE = DATA_FOLDER + "program.txt";
    private static final String MILESTONE_FILE = DATA_FOLDER + "milestoneList.txt";
    private static final String RECOVERY_PLAN_FILE = DATA_FOLDER + "courseRecoveryPlan.txt";
    private static final String CUSTOM_MILESTONE_FILE = DATA_FOLDER + "recoveryMilestoneCustom.txt";

    private Map<String, Course> courses = new HashMap<>();
    private Map<String, Student> students = new HashMap<>();
    private List<Score> scores = new ArrayList<>();
    private List<Milestone> milestones = new ArrayList<>();
    private List<CourseRecoveryPlan> recoveryPlans = new ArrayList<>();
    private Map<String, Program> programs = new HashMap<>();
    private Map<String, List<String>> programCourses = new HashMap<>();
    private List<String[]> customMilestones = new ArrayList<>();

    public CourseRecoveryService() {
        loadCourses();
        loadPrograms();
        loadProgramCourses();
        loadStudents();
        loadScores();
        loadMilestones();
        loadRecoveryPlans();
        loadCustomMilestones();
    }

    // 1. LOAD TXT FILES 

    private void loadCourses() {
        courses.clear();
        for (String[] parts : readCsv(COURSE_LIST_FILE)) {
            if (parts.length < 4)
                continue;

            String courseId = parts[0].trim();
            String courseName = parts[1].trim();
            int credits = parseIntSafe(parts[2]);
            int semester = parseIntSafe(parts[3]);

            Course course = new Course(courseId, courseName, credits, semester);
            courses.put(courseId, course);
        }
    }

    private void loadPrograms() {
        programs.clear();
        for (String[] parts : readCsv(PROGRAM_FILE)) {
            if (parts.length < 2)
                continue;

            String programId = parts[0].trim();
            String programName = parts[1].trim();

            Program program = new Program(programId, programName);
            programs.put(programId, program);
        }
    }

    private void loadProgramCourses() {
        programCourses.clear();
        for (String[] parts : readCsv(PROGRAM_LIST_FILE)) {
            if (parts.length < 2)
                continue;

            String programId = parts[0].trim();
            String courseId = parts[1].trim();

            List<String> list = programCourses.get(programId);
            if (list == null) {
                list = new ArrayList<>();
                programCourses.put(programId, list);
            }
            list.add(courseId);
        }
    }

    private void loadStudents() {
        students.clear();
        for (String[] parts : readCsv(STUDENT_LIST_FILE)) {
            if (parts.length < 6)
                continue;

            String studentId = parts[0].trim();
            String studentName = parts[1].trim();
            String email = parts[2].trim();
            String programId = parts[3].trim();
            int currentSemester = parseIntSafe(parts[5]);

            Student student = new Student(studentId, studentName, email, programId, currentSemester);
            students.put(studentId, student);
        }
    }

    private void loadScores() {
        scores.clear();
        for (String[] parts : readCsv(SCORE_FILE)) {
            if (parts.length < 9)
                continue;

            String studentId = parts[0].trim();
            String courseId = parts[1].trim();
            int attempt = parseIntSafe(parts[2]);
            int semester = parseIntSafe(parts[3]);
            int assignmentScore = parseIntSafe(parts[4]);
            int examScore = parseIntSafe(parts[5]);
            double gradePoint = parseDoubleSafe(parts[6]); 
            String grade = parts[7].trim(); 
            String status = parts[8].trim(); 

            Score score = new Score(
                    studentId,
                    courseId,
                    attempt,
                    semester,
                    assignmentScore,
                    examScore,
                    grade,
                    gradePoint,
                    status);

            scores.add(score);

            Student stu = students.get(studentId);
            if (stu != null) {
                stu.addScore(score);
            }
        }

        for (Student s : students.values()) {
            int failed = (int) s.getScores().stream()
                    .filter(sc -> "FAIL".equalsIgnoreCase(sc.getstatus()))
                    .count();
            s.setFailedCourseCount(failed);
        }
    }

    private void loadMilestones() {
        milestones.clear();
        for (String[] parts : readCsv(MILESTONE_FILE)) {
            if (parts.length < 4)
                continue;

            String templateId = parts[0].trim();
            String courseId = parts[1].trim();
            int week = parseIntSafe(parts[2]);
            String task = parts[3].trim();

            Milestone m = new Milestone(templateId, courseId, week, task);
            milestones.add(m);
        }
    }

    private void loadRecoveryPlans() {
        recoveryPlans.clear();

        for (String[] parts : readCsv(RECOVERY_PLAN_FILE)) { 
            if (parts.length < 5)
                continue;

            String planId = parts[0].trim();
            String studentId = parts[1].trim();
            String courseId = parts[2].trim();
            int week = parseIntSafe(parts[3]);
            String status = parts[4].trim();
            String rec = (parts.length > 5) ? parts[5].trim() : "NA";

            recoveryPlans.add(new CourseRecoveryPlan(planId, studentId, courseId, week, status, rec));
        }
    }

    // 2. QUERY METHODS 

    public List<Student> getStudentsWithFailedCourses() {
        List<Student> result = new ArrayList<>();
        for (Student s : students.values()) {
            if (s.getFailedCourseCount() > 0) {
                result.add(s);
            }
        }
        result.sort(Comparator.comparing(Student::getStudentId));
        return result;
    }

    public List<Course> getFailedCoursesForStudent(String studentId) {
        List<Course> result = new ArrayList<>();
        Student stu = students.get(studentId);
        if (stu == null)
            return result;

        Map<String, Score> latestByCourse = new HashMap<>();
        for (Score sc : stu.getScores()) {
            String cid = sc.getcourseId();
            Score old = latestByCourse.get(cid);
            if (old == null || sc.getattempt() > old.getattempt()) {
                latestByCourse.put(cid, sc);
            }
        }

        for (Map.Entry<String, Score> e : latestByCourse.entrySet()) {
            if ("FAIL".equalsIgnoreCase(e.getValue().getstatus())) {
                Course c = courses.get(e.getKey());
                if (c != null)
                    result.add(c);
            }
        }

        result.sort(Comparator.comparing(Course::getCourseId));
        return result;
    }

    public List<Milestone> getMilestonesForCourse(String courseId) {
        List<Milestone> result = new ArrayList<>();
        for (Milestone m : milestones) {
            if (courseId.equals(m.getCourseId())) {
                result.add(m);
            }
        }
        result.sort(Comparator.comparingInt(Milestone::getStudyWeek));
        return result;
    }

    public CourseRecoveryPlan getOrCreateRecoveryPlan(String studentId, String courseId) {
        for (CourseRecoveryPlan plan : recoveryPlans) {
            if (studentId.equals(plan.getStudentId())
                    && courseId.equals(plan.getCourseId())) {
                return plan;
            }
        }

        String newPlanId = generateNextPlanId();
        CourseRecoveryPlan newPlan = new CourseRecoveryPlan(
                newPlanId,
                studentId,
                courseId,
                1,
                "Not Started",
                "NA");
        recoveryPlans.add(newPlan);
        return newPlan;
    }

    public List<CourseRecoveryPlan> getRecoveryPlanEntries(String studentId, String courseId) {
        List<CourseRecoveryPlan> result = new ArrayList<>();
        for (CourseRecoveryPlan p : recoveryPlans) {
            if (studentId.equals(p.getStudentId()) && courseId.equals(p.getCourseId())) {
                result.add(p);
            }
        }
        result.sort(Comparator.comparingInt(CourseRecoveryPlan::getStudyWeek));
        return result;
    }

    public CourseRecoveryPlan getOrCreatePlanWeek(String planId, String studentId, String courseId, int week) {
        for (CourseRecoveryPlan p : recoveryPlans) {
            if (planId.equals(p.getPlanId())
                    && studentId.equals(p.getStudentId())
                    && courseId.equals(p.getCourseId())
                    && p.getStudyWeek() == week) {
                return p;
            }
        }
        CourseRecoveryPlan created = new CourseRecoveryPlan(
                planId, studentId, courseId, week, "Not Started", "NA");
        recoveryPlans.add(created);
        return created;
    }

    public boolean isStudentEnrolled(String studentId) {
        for (CourseRecoveryPlan p : recoveryPlans) {
            if (p.getStudentId().equals(studentId)) {
                return true;
            }
        }
        return false;
    }

    // 3. UPDATE METHODS 

    public void updateMilestoneStatus(String planId, String courseId, int week, String newStatus) {
        for (CourseRecoveryPlan plan : recoveryPlans) {
            if (planId.equals(plan.getPlanId())
                    && courseId.equals(plan.getCourseId())
                    && plan.getStudyWeek() == week) {
                plan.setStatus(newStatus);
                break;
            }
        }
    }

    public void addRecommendation(String planId, String text) {
        updateRecommendation(planId, text);
    }

    public void updateRecommendation(String planId, String newText) {
        for (CourseRecoveryPlan plan : recoveryPlans) {
            if (planId.equals(plan.getPlanId())) {
                plan.setRecommendation(newText);
                break;
            }
        }
    }

    public void updateRecommendation(String planId, String courseId, int week, String newText) {
        for (CourseRecoveryPlan plan : recoveryPlans) {
            if (planId.equals(plan.getPlanId())
                    && courseId.equals(plan.getCourseId())
                    && plan.getStudyWeek() == week) {
                plan.setRecommendation(newText);
                break;
            }
        }
    }

    public void removeRecommendation(String planId) {
        for (CourseRecoveryPlan plan : recoveryPlans) {
            if (planId.equals(plan.getPlanId())) {
                plan.setRecommendation("NA");
                break;
            }
        }
    }

    // 4. SAVE BACK TO TXT 

    public void saveRecoveryPlans() {
        List<String> lines = new ArrayList<>();
        for (CourseRecoveryPlan plan : recoveryPlans) {
            String line = plan.getPlanId() + "," +
                    plan.getStudentId() + "," +
                    plan.getCourseId() + "," +
                    plan.getStudyWeek() + "," +
                    (plan.getStatus() == null ? "" : plan.getStatus()) + "," +
                    (plan.getRecommendation() == null ? "NA" : plan.getRecommendation());
            lines.add(line);
        }
        writeLines(RECOVERY_PLAN_FILE, lines); 
    }

    public void updateScoreForRecovery(String studentId, String courseId, double newGradePoint) {
        for (Score s : scores) {
            if (studentId.equals(s.getstudentId())
                    && courseId.equals(s.getcourseId())) {
                s.setgradePoint(newGradePoint);
                if (newGradePoint >= 2.0) {
                    s.setstatus("PASS");
                } else {
                    s.setstatus("FAIL");
                }
            }
        }
        saveScores();

        Student stu = students.get(studentId);
        if (stu != null) {
            int failed = (int) stu.getScores().stream()
                    .filter(sc -> "FAIL".equalsIgnoreCase(sc.getstatus()))
                    .count();
            stu.setFailedCourseCount(failed);
        }
    }

    private void saveScores() {
        Map<String, Score> latestMap = new LinkedHashMap<>();
        for (Score s : scores) {
            String key = s.getstudentId() + "|" + s.getcourseId();
            Score old = latestMap.get(key);
            if (old == null || s.getattempt() > old.getattempt()) {
                latestMap.put(key, s);
            }
        }

        scores.clear();
        scores.addAll(latestMap.values());

        List<String> lines = new ArrayList<>();
        for (Score s : scores) {
            StringBuilder sb = new StringBuilder();
            sb.append(s.getstudentId()).append(",");
            sb.append(s.getcourseId()).append(",");
            sb.append(s.getattempt()).append(",");
            sb.append(s.getsemester()).append(",");
            sb.append(s.getassignmentScore()).append(",");
            sb.append(s.getexamScore()).append(",");
            sb.append(s.getgradePoint()).append(",");
            sb.append(s.getgrade()).append(",");
            sb.append(s.getstatus());
            lines.add(sb.toString());
        }
        writeLines(SCORE_FILE, lines);
    }


    private List<String[]> readCsv(String resourcePath) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = openReader(resourcePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#"))
                    continue;

                String[] parts = trimmed.split(",", -1);
                rows.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private static final String PROJECT_RESOURCE_ROOT = "src/main/resources/";

    private BufferedReader openReader(String resourcePath) throws IOException {
        File f = new File(PROJECT_RESOURCE_ROOT + resourcePath);
        if (f.exists()) {
            return new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        }

        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null)
            throw new FileNotFoundException("Resource not found: " + resourcePath);
        return new BufferedReader(new InputStreamReader(is, "UTF-8"));
    }

    private void writeLines(String resourcePath, List<String> lines) {
        try {
            File f = new File(PROJECT_RESOURCE_ROOT + resourcePath);
            File parent = f.getParentFile();
            if (parent != null)
                parent.mkdirs();

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(f, false), "UTF-8"))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String generateNextPlanId() {
        int max = 0;
        for (CourseRecoveryPlan plan : recoveryPlans) {
            String id = plan.getPlanId();
            if (id == null || id.length() < 2)
                continue;
            if (!id.startsWith("P"))
                continue;

            try {
                int n = Integer.parseInt(id.substring(1));
                if (n > max)
                    max = n;
            } catch (NumberFormatException ignored) {
            }
        }
        int next = max + 1;
        return String.format("P%03d", next);
    }

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>(courses.values());
        list.sort(Comparator.comparing(Course::getCourseId));
        return list;
    }

    private void loadCustomMilestones() {
        customMilestones.clear();
        customMilestones.addAll(readCsv(CUSTOM_MILESTONE_FILE));
    }

    public void saveCustomMilestones() {
        List<String> lines = new ArrayList<>();
        for (String[] p : customMilestones) {
            if (p.length < 4)
                continue;
            lines.add(p[0].trim() + "," + p[1].trim() + "," + p[2].trim() + "," + p[3].trim());
        }
        writeLines(CUSTOM_MILESTONE_FILE, lines);
    }

    public void upsertCustomMilestone(String planId, String courseId, int week, String task) {
        task = (task == null) ? "" : task.replace(",", "，").trim();
        String w = String.valueOf(week);

        for (int i = 0; i < customMilestones.size(); i++) {
            String[] p = customMilestones.get(i);
            if (p.length < 4)
                continue;
            if (planId.equals(p[0].trim()) && courseId.equals(p[1].trim()) && w.equals(p[2].trim())) {
                p[3] = task;
                return;
            }
        }
        customMilestones.add(new String[] { planId, courseId, w, task });
    }

    public void removeCustomMilestone(String planId, String courseId, int week) {
        String w = String.valueOf(week);
        customMilestones.removeIf(p -> p.length >= 4 &&
                planId.equals(p[0].trim()) &&
                courseId.equals(p[1].trim()) &&
                w.equals(p[2].trim()));
    }

    public List<Milestone> getMilestonesForPlan(String planId, String courseId) {
        List<Milestone> base = getMilestonesForCourse(courseId);

        Map<Integer, String> override = new HashMap<>();
        for (String[] p : customMilestones) {
            if (p.length < 4)
                continue;
            if (planId.equals(p[0].trim()) && courseId.equals(p[1].trim())) {
                int week = parseIntSafe(p[2]);
                override.put(week, p[3].trim());
            }
        }

        Map<Integer, Milestone> merged = new HashMap<>();
        for (Milestone m : base) {
            int w = m.getStudyWeek();
            String task = override.containsKey(w) ? override.get(w) : m.getTask();
            merged.put(w, new Milestone("TEMPLATE", courseId, w, task));
        }

        for (Map.Entry<Integer, String> e : override.entrySet()) {
            if (!merged.containsKey(e.getKey())) {
                merged.put(e.getKey(), new Milestone("CUSTOM", courseId, e.getKey(), e.getValue()));
            }
        }

        List<Milestone> result = new ArrayList<>(merged.values());
        result.sort(Comparator.comparingInt(Milestone::getStudyWeek));
        return result;
    }

    public void removePlanWeek(String planId, String studentId, String courseId, int week) {
        recoveryPlans.removeIf(p -> planId.equals(p.getPlanId()) &&
                studentId.equals(p.getStudentId()) &&
                courseId.equals(p.getCourseId()) &&
                p.getStudyWeek() == week);
    }

    public void recordRecoveryExamResult(String studentId, String courseId, double newGradePoint) {
        Score latest = null;
        for (Score s : scores) {
            if (studentId.equals(s.getstudentId()) && courseId.equals(s.getcourseId())) {
                if (latest == null || s.getattempt() > latest.getattempt())
                    latest = s;
            }
        }

        int nextAttempt = (latest == null) ? 1 : (latest.getattempt() + 1);

        String passFail = (newGradePoint >= 2.0) ? "PASS" : "FAIL";
        String grade = (newGradePoint >= 4.0) ? "A+"
                : (newGradePoint >= 3.7) ? "A"
                        : (newGradePoint >= 3.3) ? "B+"
                                : (newGradePoint >= 3.0) ? "B"
                                        : (newGradePoint >= 2.7) ? "C+"
                                                : (newGradePoint >= 2.3) ? "C"
                                                        : (newGradePoint >= 2.0) ? "D"
                                                                : "F";

        if (latest == null) {
            int semester = 1;
            Score created = new Score(studentId, courseId, nextAttempt, semester, 0, 0, grade, newGradePoint, passFail);
            scores.add(created);

            Student stu = students.get(studentId);
            if (stu != null)
                stu.addScore(created);

        } else {
            latest.setattempt(nextAttempt);
            latest.setgradePoint(newGradePoint);
            latest.setgrade(grade);
            latest.setstatus(passFail);
        }

        Student stu = students.get(studentId);
        if (stu != null) {
            int failed = (int) stu.getScores().stream()
                    .filter(sc -> "FAIL".equalsIgnoreCase(sc.getstatus()))
                    .count();
            stu.setFailedCourseCount(failed);
        }

        saveScores();
    }
}
