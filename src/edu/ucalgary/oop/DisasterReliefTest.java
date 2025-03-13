package edu.ucalgary.oop;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

public class DisasterReliefTest {

    @Test(expected = IllegalArgumentException.class)
    public void testDisasterVictimCreation_EmptyName() {
        new DisasterVictim("", "2025-03-13"); // Should throw an exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDisasterVictimCreation_InvalidCharacters() {
        new DisasterVictim("J@hn", "2025-03-13"); // Should throw an exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidEntryDate() {
        new DisasterVictim("Alice", "invalid-date");
    }

    @Test
    public void testSupplyAllocation() {
        Supply supply = new Supply("water", 10);
        assertEquals("water", supply.getType());
        assertEquals(10, supply.getQuantity());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidGenderAssignment() {
        DisasterVictim victim = new DisasterVictim("Sam", "2025-03-13");
        victim.setGender("INVALID_GENDER"); // Use setGender(String) instead of Gender.valueOf()
    }

    @Test
    public void testValidGenderAssignment() {
        DisasterVictim victim = new DisasterVictim("Emily", "2025-03-13");
        victim.setGender("NON_BINARY"); // Pass as a String
        assertEquals(DisasterVictim.Gender.NON_BINARY, victim.getGender());
    }

    @Test
    public void testFamilyGroupAssignment() {
        FamilyGroup family = new FamilyGroup(1, "Smith Family");
        DisasterVictim victim = new DisasterVictim("Lily", "2025-03-13");
        family.addMember(victim);
        assertEquals(1, family.getMembers().size());
    }

    @Test
    public void testLanguageFallback() {
        assertEquals("main_menu", DisasterReliefApp.translate("main_menu")); // Call directly
    }

}