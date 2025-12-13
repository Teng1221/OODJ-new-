package edu.apu.crs.models;

//import edu.apu.crs.models.SystemUser;

public class CourseAdministrator extends SystemUser {

    public CourseAdministrator(String userID, String username, String email, String password, String role,
            boolean isActive) {
        super(userID, username, email, password, role, isActive);
    }

    // Polymorphism: Implements the abstract method
    @Override
    public String getRoleTitle() {
        return "Course Administrator";
    }

    // can add methods specific to a Course Administrator here (e.g.,
    // manageRecoveryPlans())
}
