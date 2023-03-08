package com.example.ihuntwithjavalins.Player;

import com.example.ihuntwithjavalins.QRCode.QRCode;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Player who competes to scan and own more valuable QRCodes than other
 * Players in the application.
 * @version 1.0
 */
public class Player {
    /**
     * Holds the unique username of the Player
     */
    private String username;
    /**
     * Holds the email of the Player
     */
    private String email;
    /**
     * Holds the phone number of the Player
     */
    private String phoneNumber;
    /**
     * Holds the region the Player competes/lives in
     */
    private String region;    // in login activity, there should be limits so user does not enter invalid region
    /**
     * Holds the QRCodes the Player has scanned
     */
    private List<QRCode> codes = new ArrayList<>();    // add QRCode class later

    /**
     * Constructor for new instance of Player object representing a Player in the application
     * competing against each other.
     * @param username The username of the Player
     * @param email The email of the Player
     * @param phoneNumber The phone number of the Player
     * @param region The region the Player is in
     */
    public Player(String username, String email, String phoneNumber, String region) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.region = region;
    }

    /**
     * Gets the username of the Player
     * @return The String representing the username of the Player
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the Player
     * @param username The String representing the username of the Player
     */
    public void setUsername(String username) {     // This setter may be deleted if we decide a Player cannot change their username(although I think the functionality is good if an administrator wishes to get rid of someone's inappropriate name)
        this.username = username;
    }

    /**
     * Gets the email of the Player
     * @return The String representing the email of the Player
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email of the Player
     * @param email The String representing the email of the Player
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the phone number of the Player
     * @return The String representing the phone number of the Player
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone number of the Player
     * @param phoneNumber The String representing the phone number of the Player
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the region the Player is playing in
     * @return The String representing the region the Player is playing in
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the region the Player is playing in
     * @param region The String representing the region the Player is playing in
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Gets the list of QRCode objects the Player has scanned
     * @return The list containing QRCode objects the Player has scanned
     */
    public List<QRCode> getCodes() {
        return codes;
    }

    // I won't write a unit test for Player methods yet, I think it makes more sense for there to be a unit test for a controller using said model class

    /**
     * Adds a QRCode to the list of codes the Player has scanned
     * @param code The QRCode the Player scanned to be added to list of codes
     */
    public void addCode(QRCode code) {
        codes.add(code);
    }

    /**
     * Deletes a QRCode from the list of codes the Player has scanned
     * @param code The QRCode to be removed from list of codes Player has scanned
     */
    public void delCode(QRCode code) {
        codes.remove(code);    // May want to have an exception thrown if trying to remove a QRCode that does not exist
    }

    /**
     * Checks if the QRCode given is a QRCode the Player has scanned
     * @param code The QRCode to be searched for in the Player's list of codes
     * @return True if the QRCode is in the Player list of codes, false otherwise
     */
    public boolean hasCode(QRCode code) {
        if (codes.contains(code)) {
            return true;
        } else {
            return false;
        }
    }
}