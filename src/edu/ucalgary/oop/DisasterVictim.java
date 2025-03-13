/*
Copyright Ann Barcomb and Khawla Shnaikat, 2024-2025
Licensed under GPL v3
See LICENSE.txt for more information.
*/

package edu.ucalgary.oop;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.time.LocalDate;


public class DisasterVictim {
    private static int counter = 0;

    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private final int ASSIGNED_SOCIAL_ID;
    private ArrayList<FamilyRelation> familyConnections = new ArrayList<>();
    private ArrayList<MedicalRecord> medicalRecords = new ArrayList<>();
    private Supply[] personalBelongings;
    private final String ENTRY_DATE;
    private String gender;
    private String comments;

    public DisasterVictim(String firstName, String ENTRY_DATE) throws IllegalArgumentException {
        this.firstName = firstName;
        if (!isValidDateFormat(ENTRY_DATE)) {
            throw new IllegalArgumentException("Invalid date format for entry date. Expected format: YYYY-MM-DD");
        }
        this.ENTRY_DATE = ENTRY_DATE;
        this.ASSIGNED_SOCIAL_ID = generateSocialID();
    }

    public DisasterVictim(String firstName, String ENTRY_DATE, String dateOfBirth) throws IllegalArgumentException {
        this.firstName = firstName;
        if (!isValidDateFormat(ENTRY_DATE)) {
            throw new IllegalArgumentException("Invalid date format for entry date. Expected format: YYYY-MM-DD");
        }
        this.ENTRY_DATE = ENTRY_DATE;
        this.ASSIGNED_SOCIAL_ID = generateSocialID();
        setDateOfBirth(dateOfBirth);
    }


    private static int generateSocialID() {
        counter++;
        return counter;
    }

    private static boolean isValidDateFormat(String date) {
        String dateFormatPattern = "^\\d{4}-\\d{2}-\\d{2}$";
        return date.matches(dateFormatPattern);
    }

    private static int convertDateStringToInt(String dateStr) {
        // Use regex to remove dashes from the date string
        String formattedDate = dateStr.replaceAll("-", "");
        
        // Convert the formatted string to an integer
        return Integer.parseInt(formattedDate);
    }

  
    // Getters and setters

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) throws IllegalArgumentException {
        if (!isValidDateFormat(dateOfBirth)) {
            throw new IllegalArgumentException("Invalid date format for date of birth. Expected format: YYYY-MM-DD");
        }

        // A person cannot be born after entering a centre
        int entryDate = convertDateStringToInt(ENTRY_DATE);
        int birthDate = convertDateStringToInt(dateOfBirth);
        if (birthDate > entryDate) {
            throw new IllegalArgumentException("Birthdate must be the same as or before entry date");
        }
        
        this.dateOfBirth = dateOfBirth;
    }

    public int getAssignedSocialID() {
        return ASSIGNED_SOCIAL_ID;
    }

  public FamilyRelation[] getFamilyConnections() {
        return familyConnections.toArray(new FamilyRelation[0]);
    }

    public MedicalRecord[] getMedicalRecords() {
        return medicalRecords.toArray(new MedicalRecord[0]);
    }

    public Supply[] getPersonalBelongings() {
        return this.personalBelongings;
    }

    // The add and remove methods remain correct.
    
    // Correct the setters to accept Lists instead of arrays
    public void setFamilyConnections(FamilyRelation[] connections) {
        this.familyConnections.clear();
        for (FamilyRelation newRecord : connections) {
            addFamilyConnection(newRecord);
        }
    }

    public void setMedicalRecords(MedicalRecord[] records) {
        this.medicalRecords.clear();
        for (MedicalRecord newRecord : records) {
            addMedicalRecord(newRecord);
        }
    }

    public void setPersonalBelongings(Supply[] belongings) {
        this.personalBelongings = belongings;
    }

    // Add a Supply to personalBelonging
    public void addPersonalBelonging(Supply supply) {

        if (this.personalBelongings == null) {
            Supply tmpSupply[] = { supply };
            this.setPersonalBelongings(tmpSupply);
            return;
        }

        // Create an array one larger than the previous array
        int newLength = this.personalBelongings.length + 1;
        Supply tmpPersonalBelongings[] = new Supply[newLength];

        // Copy all the items in the current array to the new array
        int i;
        for (i=0; i < personalBelongings.length; i++) {
            tmpPersonalBelongings[i] = this.personalBelongings[i];
        }

        // Add the new element at the end of the new array
        tmpPersonalBelongings[i] = supply;

        // Replace the original array with the new array
        this.personalBelongings = tmpPersonalBelongings;
    }

    // Remove a Supply from personalBelongings, we assume it only appears once
    public void removePersonalBelonging(Supply unwantedSupply) {
        Supply[] updatedBelongings = new Supply[personalBelongings.length-1];
        int index = 0;
        int newIndex = index;
        for (Supply supply : personalBelongings) {
            if (!supply.equals(unwantedSupply)) {
                updatedBelongings[newIndex] = supply;
                newIndex++;
            }
            index++;
        }
    }

    public void removeFamilyConnection(FamilyRelation exRelation) {
        familyConnections.remove(exRelation);
    }

    public void addFamilyConnection(FamilyRelation record) {
        familyConnections.add(record);
    }


    // Add a MedicalRecord to medicalRecords
    public void addMedicalRecord(MedicalRecord record) {
        medicalRecords.add(record);
    }

    public String getEntryDate() {
        return ENTRY_DATE;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments =  comments;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) throws IllegalArgumentException {
        if (!gender.matches("(?i)^(male|female|other)$")) {
            throw new IllegalArgumentException("Invalid gender. Acceptable values are male, female, or other.");
        }
        this.gender = gender.toLowerCase(); // Store in a consistent format
    }

   
}





