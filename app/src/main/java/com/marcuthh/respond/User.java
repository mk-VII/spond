package com.marcuthh.respond;

import java.io.File;
import java.util.ArrayList;

public class User {

    private String phoneNumber;
    private String emailAddress;
    private String firstName;
    private String surname;
    private String displayName;
    private String accountPhotoName;
    private String accountPhotoNames;
    private String status;
    private long lastOnline;

    //flag to set up full account details after registration
    //always begins as false
    private boolean complete;
    //token set on initial registration
    private String token;

    public User() {}

    public User(String phoneNumber, String emailAddress,
                String firstName, String surname, String displayName,
                String accountPhotoName, String accountPhotoNames,
                String status, long lastOnline,
                boolean complete, String token) {
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.surname = surname;
        this.displayName = displayName;
        this.accountPhotoName = accountPhotoName;
        this.accountPhotoNames = accountPhotoNames;
        this.status = status;
        this.lastOnline = lastOnline;

        this.complete = complete;
        this.token = token;

        //set any null values to equal ""
        removeNulls();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAccountPhotoName() { return accountPhotoName; }

    public void setAccountPhotoName(String accountPhotoName, boolean appendTolist) {
        this.accountPhotoName = accountPhotoName;
        if (appendTolist) {
            addAccountPhotoName(new String[]{accountPhotoName});
        }
    }

    public boolean isComplete() { return complete; }

    public void setComplete() { complete = true; }

    private void removeNulls() {
        if (phoneNumber == null) { phoneNumber = ""; }
        if (emailAddress == null) { emailAddress = ""; }
        if (firstName == null) { firstName = ""; }
        if (surname == null) { surname = ""; }
        if (displayName == null) { displayName = ""; }
        if (accountPhotoName == null) { accountPhotoName = ""; }
        if (accountPhotoNames == null) { accountPhotoNames = ""; }
        if (status == null) { status = ""; }
        if (token == null) { token = ""; }
    }

    public String buildAccountPhotoNodeFilter(String u_Id, boolean appendFileName) {
        if (!u_Id.equals("")) {
            //path to folder of account photos
            String filterString = "images/users/" + u_Id + "/account_photos";
            if (appendFileName) {
                //path to individual file within folder
                filterString += File.separator + accountPhotoName;
            }
            return filterString;
        }
        return "";
    }

    public boolean usesDefaultPhoto() { return accountPhotoName.equals(""); }

    public String getAccountPhotoNames() { return accountPhotoNames; }

    private void addAccountPhotoName(String[] photoNames) {
        if (accountPhotoNames == null) { accountPhotoNames = ""; }
        StringBuilder builder = new StringBuilder(accountPhotoNames);
        for (String photoName : photoNames) {
            if (!photoName.equals("")) {
                if (!builder.toString().equals("")) {
                    builder.append(",");
                }
                builder.append(photoName);
            }
        }
        //re-assign updated comma-separated list to string
        accountPhotoNames = builder.toString();
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}