package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;
import java.sql.*;
import java.util.*;

public class DisasterReliefDatabaseTest {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/project";
    private static final String DB_USER = "oop";
    private static final String DB_PASSWORD = "ucalgary";
    private Connection conn;

    @BeforeClass
    public static void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS test_victims (id SERIAL PRIMARY KEY, first_name TEXT, entry_date TEXT)");
        } catch (SQLException e) {
            fail("Database setup failed: " + e.getMessage());
        }
    }

    @Before
    public void connectDatabase() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            fail("Could not connect to database: " + e.getMessage());
        }
    }

    @Test
    public void testInsertVictim() {
        try {
            String query = "INSERT INTO test_victims (first_name, entry_date) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, "John");
            pstmt.setString(2, "2025-03-13");
            int affectedRows = pstmt.executeUpdate();
            assertTrue("Victim insertion failed", affectedRows > 0);
            pstmt.close();
        } catch (SQLException e) {
            fail("Database insert failed: " + e.getMessage());
        }
    }

    @Test
    public void testRetrieveVictim() {
        String query = "SELECT first_name, entry_date FROM test_victims WHERE first_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "John");
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue("No victim found", rs.next());
                assertEquals("John", rs.getString("first_name"));
                assertEquals("2025-03-13", rs.getString("entry_date"));
            }
        } catch (SQLException e) {
            fail("Database retrieval failed: " + e.getMessage());
        }
    }


    @Test
    public void testUpdateVictim() {
        try {
            String query = "UPDATE test_victims SET entry_date = ? WHERE first_name = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "2025-04-01");
            pstmt.setString(2, "John");
            int affectedRows = pstmt.executeUpdate();
            assertTrue("Victim update failed", affectedRows > 0);
            pstmt.close();
        } catch (SQLException e) {
            fail("Database update failed: " + e.getMessage());
        }
    }

    @Test
    public void testDeleteVictim() {
        try {
            // Ensure the victim exists before attempting to delete
            String insertQuery = "INSERT INTO test_victims (first_name, entry_date) VALUES ('John', '2025-03-13')";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.executeUpdate();
            }

            // Now delete the victim
            String deleteQuery = "DELETE FROM test_victims WHERE first_name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
                pstmt.setString(1, "John");
                int affectedRows = pstmt.executeUpdate();
                assertTrue("Victim deletion failed", affectedRows > 0);
            }
        } catch (SQLException e) {
            fail("Database deletion failed: " + e.getMessage());
        }
    }


    @After
    public void closeDatabase() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            fail("Failed to close database connection: " + e.getMessage());
        }
    }

    @AfterClass
    public static void teardownDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS test_victims");
        } catch (SQLException e) {
            fail("Database teardown failed: " + e.getMessage());
        }
    }
}
