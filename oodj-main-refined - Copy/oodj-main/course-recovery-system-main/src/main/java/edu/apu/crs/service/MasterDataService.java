package edu.apu.crs.service;

import edu.apu.crs.dataIO.CourseFileReader;
import edu.apu.crs.models.Course;
import edu.apu.crs.models.Student;
import java.util.List;

public class MasterDataService {

    private final StudentDataService studentDataService;
    private final EligibilityService eligibilityService;
    private final List<Student> processedStudents; 

    public MasterDataService() {
        System.out.println("Initializing Master Data Service...");

        // Load static Course data 
        List<Course> allCourses = CourseFileReader.readCourses();
        
        // Initialize Services
        this.eligibilityService = new EligibilityService(allCourses);
        this.studentDataService = new StudentDataService();
        
        // Load and Process Students
        List<Student> allStudents = studentDataService.getAllStudents();
        eligibilityService.processAllStudentEligibility(allStudents);
        
        this.processedStudents = allStudents;
    }

    public List<Student> getAllProcessedStudents() {
        return processedStudents;
    }

    public List<Student> getStudentsNeedingRecovery() {
        return eligibilityService.getStudentsNeedingRecovery(processedStudents);
    }

    public Student findStudentById(String studentId) {
        return studentDataService.findStudentById(studentId);
    }

}
