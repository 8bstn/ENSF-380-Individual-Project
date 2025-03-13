package edu.ucalgary.oop;

import java.sql.*;
import java.util.*;
import java.time.*;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class DisasterReliefApp {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/project";
    private static final String DB_USER = "oop";
    private static final String DB_PASSWORD = "ucalgary";
    private static final Properties languageProperties = new Properties();
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<DisasterVictim> victims = new ArrayList<>();
    private static final List<FamilyGroup> familyGroups = new ArrayList<>();
    private static final List<Supply> inventory = new ArrayList<>();
    private static String languageFile = "data/en-CA.xml";


    // Starts application
    public static void main(String[] args) {
        if (args.length > 0) {
            setLanguage(args[0]);
        }
        loadLanguage();
        loadData(); // Load database data at startup
        runCLI();
    }

    // Sets the language file
    private static void setLanguage(String langCode) {
        File dataDir = new File("data");
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            System.out.println("No language directory found. Defaulting to en-CA.");
            languageFile = "data/en-CA.xml";
            return;
        }

        File[] languageFiles = dataDir.listFiles((dir, name) -> name.matches("[a-z]{2}-[A-Z]{2}\\.xml"));
        if (languageFiles == null || languageFiles.length == 0) {
            System.out.println("No language files found. Defaulting to en-CA.");
            languageFile = "data/en-CA.xml";
            return;
        }

        String filePath = "data/" + langCode + ".xml";
        File file = new File(filePath);
        if (file.exists()) {
            languageFile = filePath;
        } else {
            System.out.println("Language file not found. Defaulting to en-CA.");
            languageFile = "data/en-CA.xml";
        }
    }


    // Loads translations from an XML file
    private static void loadLanguage() {
        try {
            File file = new File(languageFile);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();

            NodeList list = document.getElementsByTagName("translation");
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String key = element.getElementsByTagName("key").item(0).getTextContent();
                    String value = element.getElementsByTagName("value").item(0).getTextContent();
                    languageProperties.setProperty(key, value);
                }
            }
            System.out.println("Language loaded successfully.");
        } catch (Exception e) {
            logError("Error loading language file: " + e.getMessage());
            System.out.println("Error loading language file. Defaulting to English.");
        }
    }

    // Retrieves a translated string
    public static String translate(String key) {
        return languageProperties.getProperty(key, key);
    }

    // Runs the command-line interface
    private static void runCLI() {
        while (true) {
            System.out.println("\n" + translate("main_menu"));
            System.out.println("1. " + translate("add_victim"));
            System.out.println("2. " + translate("log_inquiry"));
            System.out.println("3. " + translate("allocate_supplies"));
            System.out.println("4. Create Family Group");
            System.out.println("5. " + translate("modify_data"));
            System.out.println("6. Display Disaster Victims");
            System.out.println("7. Display Family Groups");
            System.out.println("8. Display Inventory");
            System.out.println("9. Assign Person to Family Group"); // New option
            System.out.println("10. " + translate("exit_program")); // Updated option number
            System.out.print(translate("select_option"));

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    System.out.print("Enter first name: ");
                    String firstName = scanner.nextLine();
                    System.out.print("Enter entry date (YYYY-MM-DD): ");
                    String entryDate = scanner.nextLine();
                    addDisasterVictim(firstName, entryDate, false);
                    break;
                case "2": logInquiry(); break;
                case "3": allocateSupplies(); break;
                case "4": createFamilyGroup(); break;
                case "5": modifyData(); break;
                case "6": displayVictims(); break;
                case "7": displayFamilyGroups(); break;
                case "8": displayInventory(); break;
                case "9": assignPersonToFamilyGroup(); break; // Call the method
                case "10": System.out.println(translate("exiting")); return; // Updated option number
                default: System.out.println(translate("invalid_option"));
            }
        }
    }

    // Display the list of victims
    private static void displayVictims() {
        if (victims.isEmpty()) {
            System.out.println("No disaster victims recorded.");
            return;
        }
        System.out.println("List of Disaster Victims:");
        for (DisasterVictim victim : victims) {
            System.out.println("- " + victim.getFirstName() + " (Entry Date: " + victim.getEntryDate() + ")");
        }
    }

    // Creates a new family group
    private static void createFamilyGroup() {
        System.out.print("Enter family group ID: ");
        int familyGroupId = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter head of family name: ");
        String headName = scanner.nextLine();

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            String query = "INSERT INTO family_groups (group_id, head_name) VALUES (?, ?)";
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, familyGroupId);
            pstmt.setString(2, headName);
            pstmt.executeUpdate();

            FamilyGroup newFamily = new FamilyGroup(familyGroupId, headName);
            familyGroups.add(newFamily);
            System.out.println("Family group created successfully.");
        } catch (SQLException e) {
            logError("Database error: " + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { logError("Error closing PreparedStatement: " + e.getMessage()); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { logError("Error closing Connection: " + e.getMessage()); }
        }
    }

    // Display family groups
    private static void displayFamilyGroups() {
        if (familyGroups.isEmpty()) {
            System.out.println("No family groups recorded.");
            return;
        }
        System.out.println("List of Family Groups:");
        for (FamilyGroup group : familyGroups) {
            System.out.println("- Group ID: " + group.getGroupId() + " | Head: " + group.getHeadName());
        }
    }

    // Assigns a person to a family group
    private static void assignPersonToFamilyGroup() {
        System.out.print("Enter person's name: ");
        String personName = scanner.nextLine();
        System.out.print("Enter family group ID: ");
        int familyGroupId = Integer.parseInt(scanner.nextLine());

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Check if the person is already in a family group
            String checkQuery = "SELECT family_group_id FROM disaster_victims WHERE first_name = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, personName);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt("family_group_id") != 0) {
                    System.out.println("Person is already in a family group.");
                    return;
                }
            }

            // Assign the person to the family group
            String updateQuery = "UPDATE disaster_victims SET family_group_id = ? WHERE first_name = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, familyGroupId);
                updateStmt.setString(2, personName);
                int updatedRows = updateStmt.executeUpdate();

                if (updatedRows > 0) {
                    System.out.println("Person assigned to family group successfully.");
                } else {
                    System.out.println("Person not found in the database.");
                }
            }
        } catch (SQLException e) {
            logError("Database error: " + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Manages supply allocation
    private static void allocateSupplies() {
        System.out.print("Enter supply type (personal belonging, blanket, cot, water): ");
        String type = scanner.nextLine().toLowerCase();
        if (!List.of("personal belonging", "blanket", "cot", "water").contains(type)) {
            System.out.println("Invalid supply type.");
            return;
        }

        System.out.print("Enter quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine());
        System.out.print("Allocate to (location/person): ");
        String allocationType = scanner.nextLine().toLowerCase();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query;
            if (allocationType.equals("location")) {
                System.out.print("Enter location name: ");
                String locationName = scanner.nextLine();
                query = "INSERT INTO supplies (type, quantity, location) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, type);
                    pstmt.setInt(2, quantity);
                    pstmt.setString(3, locationName);
                    pstmt.executeUpdate();
                }
            } else if (allocationType.equals("person")) {
                System.out.print("Enter disaster victim's name: ");
                String victimName = scanner.nextLine();
                query = "INSERT INTO supplies (type, quantity, person) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, type);
                    pstmt.setInt(2, quantity);
                    pstmt.setString(3, victimName);
                    pstmt.executeUpdate();
                }
            } else {
                System.out.println("Invalid allocation type.");
                return;
            }

            // Handle water expiry
            if (type.equals("water")) {
                String expireWaterQuery = "UPDATE supplies SET expired = true WHERE type = 'water' AND allocated_date < NOW() - INTERVAL '1 day'";
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(expireWaterQuery);
                }
            }

            System.out.println("Supplies allocated successfully.");
        } catch (SQLException e) {
            logError("Database error: " + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Display the inventory
    private static void displayInventory() {
        if (inventory.isEmpty()) {
            System.out.println("No inventory items recorded.");
            return;
        }
        System.out.println("List of Inventory Items:");
        for (Supply item : inventory) {
            System.out.println("- Type: " + item.getType() + " | Quantity: " + item.getQuantity());
        }
    }

    // Loads disaster victim data from the database
    private static void loadData() {
        System.out.println("Loading data from database...");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Delete expired water allocations
            String deleteExpiredWaterQuery = "DELETE FROM supplies WHERE type = 'water' AND allocated_date < NOW() - INTERVAL '1 day'";
            stmt.executeUpdate(deleteExpiredWaterQuery);

            // Load disaster victims
            ResultSet rs = stmt.executeQuery("SELECT * FROM disaster_victims");
            while (rs.next()) {
                DisasterVictim victim = new DisasterVictim(
                        rs.getString("first_name"),
                        rs.getString("entry_date")
                );
                victims.add(victim);
            }
            System.out.println("Data loaded successfully.");
        } catch (SQLException e) {
            logError("Database connection error: " + e.getMessage());
        }
    }

    // Allow users to update disaster victim details
    public static void modifyData() {
        System.out.print("Enter the first name of the victim to modify: ");
        String firstName = scanner.nextLine();

        System.out.println("Select what to modify:");
        System.out.println("1. First Name");
        System.out.println("2. Entry Date");
        System.out.print("Choice: ");
        String choice = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query; // Declare the variable without initializing it

            if ("1".equals(choice)) {
                System.out.print("Enter new first name: ");
                String newFirstName = scanner.nextLine();
                query = "UPDATE disaster_victims SET first_name = ? WHERE first_name = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, newFirstName);
                    pstmt.setString(2, firstName);
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        System.out.println("Data updated successfully.");
                    } else {
                        System.out.println("No matching record found.");
                    }
                }
            } else if ("2".equals(choice)) {
                System.out.print("Enter new entry date (YYYY-MM-DD): ");
                String newEntryDate = scanner.nextLine();

                if (!isValidDate(newEntryDate)) {
                    System.out.println("Invalid date format. Use YYYY-MM-DD.");
                    return;
                }

                query = "UPDATE disaster_victims SET entry_date = ? WHERE first_name = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, newEntryDate);
                    pstmt.setString(2, firstName);
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        System.out.println("Data updated successfully.");
                    } else {
                        System.out.println("No matching record found.");
                    }
                }
            } else {
                System.out.println("Invalid choice.");
                return;
            }
        } catch (SQLException e) {
            logError("Database error: " + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Adds a new disaster victim to the database
    public static void addDisasterVictim(String firstName, String entryDate, boolean useMock) {
        if (useMock) {
            // Use MockDatabaseManager instead of a real database connection
            MockDatabaseManager.getInstance().addMockVictim(new DisasterVictim(firstName, entryDate));
            System.out.println("Mock disaster victim added successfully.");
            return;
        }

        // Real database interaction
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO disaster_victims (first_name, entry_date) VALUES (?, ?)")) {

            pstmt.setString(1, firstName);
            pstmt.setString(2, entryDate);
            pstmt.executeUpdate();

            System.out.println("Disaster victim added successfully.");
        } catch (SQLException e) {
            logError("Database error: " + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Helper method to validate date format
    private static boolean isValidDate(String date) {
        return date.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    // Logs an inquiry in the database
    public static void logInquiry() {
        System.out.print("Are you a disaster victim? (yes/no): ");
        String isVictim = scanner.nextLine().toLowerCase();

        String inquirerName;
        if (isVictim.equals("yes")) {
            System.out.print("Enter your name: ");
            inquirerName = scanner.nextLine();

            // Check if victim exists in the database
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement checkStmt = conn.prepareStatement(
                         "SELECT COUNT(*) FROM disaster_victims WHERE first_name = ?")) {

                checkStmt.setString(1, inquirerName);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("No disaster victim found with that name.");
                    return;
                }
            } catch (SQLException e) {
                logError("Database error: " + e.getMessage());
                System.out.println("Error: " + e.getMessage());
                return;
            }
        } else {
            System.out.print("Enter inquirer name: ");
            inquirerName = scanner.nextLine();
        }

        System.out.print("Enter missing person name: ");
        String missingPerson = scanner.nextLine();
        System.out.print("Enter date of inquiry (YYYY-MM-DD): ");
        String inquiryDate = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO inquiries (inquirer_name, missing_person, date_of_inquiry) VALUES (?, ?, ?)")) {

            pstmt.setString(1, inquirerName);
            pstmt.setString(2, missingPerson);
            pstmt.setString(3, inquiryDate);
            pstmt.executeUpdate();

            System.out.println("Inquiry logged successfully.");
        } catch (SQLException e) {
            logError(e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }


    // Logs errors to errorlog.txt
    private static void logError(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("errorlog.txt", true))) {
            writer.println(LocalDateTime.now() + " - " + message);
        } catch (IOException e) {
            System.out.println("Could not write to error log.");
        }
    }
}