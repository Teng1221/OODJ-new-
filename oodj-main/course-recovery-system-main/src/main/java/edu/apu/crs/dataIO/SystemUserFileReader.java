package edu.apu.crs.dataIO;

import edu.apu.crs.models.AcademicOfficer;
import edu.apu.crs.models.CourseAdministrator;
import edu.apu.crs.models.SystemUser; // Assuming SystemUser is the class name

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SystemUserFileReader extends BaseDataReader<SystemUser> {

    private static final String FILE_NAME = "systemUserList.txt";

    public static List<SystemUser> readAllUsers() {
        List<SystemUser> users = new ArrayList<>();

        try (BufferedReader br = new SystemUserFileReader().getReader(FILE_NAME)) {
            if (br == null)
                return users; // File not found error handling

            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;

                String[] parts = line.split(",");

                if (parts.length < 5) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }

                String userID = parts[0].trim();
                String username = parts[1].trim();
                String email = parts[2].trim();
                String password = parts[3].trim();
                String role = parts[4].trim();

                SystemUser user = null;

                // Instantiation based on Role (Polymorphism)
                switch (role) {

                    case "Academic Officer":
                        user = new AcademicOfficer(userID, username, email, password, role, true);
                        break;

                    case "Course Admin":
                        user = new CourseAdministrator(userID, username, email, password, role, true);
                        break;

                    default:
                        System.err.println("Skipping user with unknown role: " + role);
                        continue;
                }

                if (user != null) {
                    users.add(user);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading SystemUser data file: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }
}
