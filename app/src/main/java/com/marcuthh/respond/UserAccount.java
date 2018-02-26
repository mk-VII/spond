package com.marcuthh.respond;

import java.io.File;
import java.util.ArrayList;

public class UserAccount {

    private String phoneNumber;
    private String emailAddress;
    private String firstName;
    private String surname;
    private String displayName;
    private String accountPhotoName;
    private String accountPhotoNames;

    //flag to set up full account details after registration
    //always begins as false
    private boolean complete;

    public UserAccount() {}

    public UserAccount(String phoneNumber, String emailAddress,
                       String firstName, String surname, String displayName,
                       String accountPhotoName, String accountPhotoNames,
                       boolean complete) {
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.surname = surname;
        this.displayName = displayName;
        this.accountPhotoName = accountPhotoName;
        this.accountPhotoNames = accountPhotoNames;
        this.complete = complete;

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
    }

    public String buildAccountPhotoNodeFilter(String u_Id, boolean appendFileName) {
        if (!u_Id.equals("")) {
            String filterString = "images/users/" + u_Id + "/account_photos";
            if (appendFileName) {
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
}