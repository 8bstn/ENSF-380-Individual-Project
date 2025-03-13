package edu.ucalgary.oop;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MockDatabaseManager {
    private static MockDatabaseManager instance;
    private List<DisasterVictim> mockVictims;

    private MockDatabaseManager() {
        mockVictims = new ArrayList<>();
    }

    public static MockDatabaseManager getInstance() {
        if (instance == null) {
            instance = new MockDatabaseManager();
        }
        return instance;
    }

    public void addMockVictim(DisasterVictim victim) {
        mockVictims.add(victim);
    }

    public List<DisasterVictim> getMockVictims() {
        return mockVictims;
    }
}
