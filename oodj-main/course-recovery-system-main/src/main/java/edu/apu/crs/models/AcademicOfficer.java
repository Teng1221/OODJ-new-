package edu.apu.crs.models;

// import edu.apu.crs.models.SystemUser;

public class AcademicOfficer extends SystemUser {

    // Constructor chains up to the SystemUser constructor
    public AcademicOfficer(String userID, String username, String email, String password, String role,
            boolean isActive) {

        // Calls SystemUser constructor to initialize fields (username, password, role,
        // isActive)
        super(userID, username, email, password, role, isActive);
    }

    // Polymorphism: Must implement the abstract method from SystemUser
    @Override
    public String getRoleTitle() {
        return "Academic Officer";
    }

    // can add methods specific to an Academic Officer here (e.g., manageReports())

}
