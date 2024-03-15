package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void createPatient(String[] tokens) {
        // TODO: Part 1
        if (tokens.length !=3){
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        if(usernameExistsPatient(username)) {
            System.out.println("Username taken,try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);

        try{
            scheduler.model.Patient patient = new scheduler.model.Patient.PatientBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            patient.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }

    }

    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Patients WHERE Username_P = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            Caregiver caregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            caregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void loginPatient(String[] tokens) {
        // TODO: Part 1
        // login_patient <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (patient == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentPatient = patient;
        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        // TODO: Part 2
        ConnectionManager cm = new ConnectionManager();
        Connection conn = cm.createConnection();
        PreparedStatement preparedStat = null;
        ResultSet rs = null;
        //checks if any user is logged in
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
        }
        // checks if the user inputted both the task and date
        if (tokens.length != 2) {
            System.out.println("Please try again!");
        }
        try {
            String selectAvailabilitiesQuery = "SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username";
            String selectVacinneAvailQuery = "SELECT Name, Doses FROM Vaccines ORDER BY Name";
            PreparedStatement stmt = conn.prepareStatement(selectAvailabilitiesQuery);
            PreparedStatement stmtV = conn.prepareStatement(selectVacinneAvailQuery);
            Date parsedDate = Date.valueOf(tokens[1]);
            // Convert the java.util.Date object to a java.sql.Date object
            java.sql.Date dateToCheck = new java.sql.Date(parsedDate.getTime());
            stmt.setDate(1, dateToCheck);
            ResultSet resultSet = stmt.executeQuery();
            ResultSet resultSetV = stmtV.executeQuery();
            boolean availableCaregivers = false;
            boolean availableVaccines = false;

            System.out.print("Available caregivers on " + dateToCheck + ": ");
            while (resultSet.next()) {
                String username = resultSet.getString("Username");
                System.out.print(username + " ");
                availableCaregivers = true;
            }
            System.out.print("and Available Vaccines: ");
            while(resultSetV.next()) {
                String name = resultSetV.getString("Name");
                String doses = resultSetV.getString("Doses");
                System.out.print(name + " " + doses + " " );
                availableVaccines = true;
            }

            if (!availableCaregivers) {
                System.out.println("Please try again!");
            }
            if (!availableCaregivers){
                System.out.println("Please try again!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void reserve(String[] tokens) {
        // TODO: Part 2
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }
        if (currentPatient == null) {
            System.out.println("Please login as a patient!");
            return;
        }
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }

        ConnectionManager cm = new ConnectionManager();
        Connection conn = cm.createConnection();
        PreparedStatement preparedStat = null;
        ResultSet rs = null;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username");
            Date parsedDate = Date.valueOf(tokens[1]);
            // Convert the java.util.Date object to a java.sql.Date object
            java.sql.Date dateToCheck = new java.sql.Date(parsedDate.getTime());
            stmt.setDate(1, dateToCheck);
            ResultSet resultSet = stmt.executeQuery();
            boolean availableCG = false;
            while (resultSet.next()) {
                String username = resultSet.getString("Username");
                availableCG = true;
                String vaccineName = tokens[2];

                Vaccine vaccine = new Vaccine.VaccineGetter(vaccineName).get();

                if (vaccine == null) {
                    System.out.println("test");
                    return;
                }

                if (vaccine.getAvailableDoses() == 0) {
                    System.out.println("Not enough available doses!");
                    return;
                }

                vaccine.decreaseAvailableDoses(1);
                int appointmentID = 10000;

                String addAppointment = "INSERT INTO Appointments VALUES (?, ?, ?, ?, ?)";
                PreparedStatement addAppointmentStmt = conn.prepareStatement(addAppointment);
                addAppointmentStmt.setInt(1, appointmentID + 1);
                addAppointmentStmt.setDate(2, dateToCheck);
                addAppointmentStmt.setString(3, currentPatient.getUsername());
                addAppointmentStmt.setString(4, username);
                addAppointmentStmt.setString(5, vaccineName);
                addAppointmentStmt.executeUpdate();

                String dropAvailability = "DELETE FROM Availabilities WHERE Time = ? AND Username = ?";
                PreparedStatement dropAvailabilityStmt = conn.prepareStatement(dropAvailability);
                dropAvailabilityStmt.setDate(1, dateToCheck);
                dropAvailabilityStmt.setString(2, username);
                dropAvailabilityStmt.executeUpdate();

                conn.commit();

                System.out.println("Success! Below is information on your appointment:");
                // Logic to fetch and print appointment information
                System.out.println("Appointment ID: " + appointmentID + ", CaregiverID: " +  username);
                System.out.println("Your reservation is on " + dateToCheck + " and is with Caregiver: " + username
                + " and your Appointment ID: " + appointmentID);
                return;
            }
            System.out.println("No caregiver is available!");
        }catch (SQLException e) {
            System.out.println("Error trying to create appointment; try again");
            System.out.println("DBError: " + e.getMessage());
        } catch (NumberFormatException | NullPointerException e) {
            System.out.println("Invalid date format; try again");
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error occurred when creating an appointment; try again");
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
            e.printStackTrace();
        }
    }

    private static void cancel(String[] tokens) {
        // TODO: Extra credit
        if (currentPatient==null && currentCaregiver == null) {
            System.out.println("Please login first!");
            return;
        }

        if (tokens.length != 2) {
            System.out.println("Failed to cancel appointment; wrong arguments given");
            return;
        }

        try {
            ConnectionManager cm = new ConnectionManager();
            Connection conn = cm.createConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT AppointmentID, Time, Username_P, Username, Name FROM Appointments WHERE AppointmentID = ? AND (Username_P = ? OR Username = ?)");
            int cancelId = Integer.parseInt(tokens[1]);
            stmt.setInt(1, cancelId);
            stmt.setString(2, currentPatient != null ? currentPatient.getUsername() : "");
            stmt.setString(3, currentCaregiver != null ? currentCaregiver.getUsername() : "");
            ResultSet appointment = stmt.executeQuery();
            boolean validAppointment = false;

            if (appointment.next()) {
                String patientUsername = appointment.getString("Username_P");
                String caregiverUsername = appointment.getString("Username");

                if ((currentPatient != null && patientUsername.equals(currentPatient.getUsername())) ||
                        (currentCaregiver != null && caregiverUsername.equals(currentCaregiver.getUsername()))) {
                    validAppointment = true;
                } else {
                    System.out.println("Could not find appointment with id: " + cancelId);
                }
            } else {
                System.out.println("Could not find appointment with id: " + cancelId);
            }

            if (validAppointment) {
                PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM Appointments WHERE AppointmentID = ?");
                deleteStmt.setInt(1, cancelId);
                deleteStmt.executeUpdate();

                System.out.println("Appointment successfully cancelled.");

                if (currentPatient != null) {
                    // Add logic for availability update if a patient cancels an appointment
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to retrieve appointment information");
            System.out.println("DBError: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Could not find appointment with id: " + tokens[1]);
        }
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        // TODO: Part 2
        if (currentPatient != null && currentCaregiver != null){
            System.out.println("Please login first!");
            return;
        }
        if (tokens.length != 1){
            System.out.println("Please try again!");
            return;
        }

        ConnectionManager cm = new ConnectionManager();
        Connection conn = cm.createConnection();
        PreparedStatement preparedStat = null;
        ResultSet rs = null;
        try{
            List<String> appointments = new ArrayList<>();

            if (currentPatient != null) {
                String get_patient_appointments = "SELECT AppointmentID, Name, Time, Username FROM Appointments WHERE Username_P = ? ORDER BY AppointmentID";
                preparedStat = conn.prepareStatement(get_patient_appointments);
                preparedStat.setString(1, currentPatient.getUsername());
                rs = preparedStat.executeQuery();
                while (rs.next()) {
                    appointments.add(String.format("%10s\t%12s\t%13s\t%10s\n",
                            rs.getInt("AppointmentID"),
                            rs.getString("Name"),
                            rs.getDate("Time").toString(),
                            rs.getString("Username")));
                }
            } else if (currentCaregiver != null) {
                String get_caregiver_appointments = "SELECT AppointmentID, Name, Time, Username_P FROM Appointments WHERE Username = ? ORDER BY AppointmentID";
                preparedStat = conn.prepareStatement(get_caregiver_appointments);
                preparedStat.setString(1, currentCaregiver.getUsername());
                rs = preparedStat.executeQuery();
                while (rs.next()) {
                    appointments.add(String.format("%10s\t%12s\t%13s\t%10s\n",
                            rs.getInt("AppointmentID"),
                            rs.getString("Name"),
                            rs.getDate("Time").toString(),
                            rs.getString("Username_P")));
                }
            }

            if (appointments.isEmpty()) {
                System.out.println("There are no appointments scheduled");
                return;
            }

            System.out.println("-".repeat(appointments.size() * 20));
            System.out.printf("%10s\t%10s\t%10s\t%17s\n", "Appointment ID", "Vaccine", "Date", currentPatient != null ? "Caregiver" : "Patient");

            for (String appointment : appointments) {
                System.out.print(appointment);
            }
        } catch (SQLException e) {
            System.out.println("Error in retrieving appointments");
            System.out.println("DBError: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error in showing appointments");
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (preparedStat != null) preparedStat.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error");
            }
        }

    }

    private static void logout(String[] tokens) {
        // TODO: Part 2
        if (currentPatient != null || currentCaregiver != null) {
            currentPatient = null;
            currentCaregiver = null;
            System.out.println("Successfully logged out!");
        } else {
            System.out.println("Please login first!");
        }
        System.out.println("Please try again!");
        return;
    }
}