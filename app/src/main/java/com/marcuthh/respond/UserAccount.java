package com.marcuthh.respond;

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
    private boolean isComplete = false;

    public UserAccount() {}

    public UserAccount(String phoneNumber, String emailAddress,
                       String firstName, String surname, String displayName,
                       String accountPhotoName, String accountPhotoNames,
                       boolean isComplete) {
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.surname = surname;
        this.displayName = displayName;
        this.accountPhotoName = accountPhotoName;
        this.accountPhotoNames = accountPhotoNames;
        this.isComplete = isComplete;

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

    public void setAccountPhotoName(String accountPhotoName) {
        this.accountPhotoName = accountPhotoName;
        addAccountPhotoName(new String[]{accountPhotoName});
    }

    public boolean isComplete() { return isComplete; }

    public void setComplete() { isComplete = true; }

    private void removeNulls() {
        if (phoneNumber == null) { phoneNumber = ""; }
        if (emailAddress == null) { emailAddress = ""; }
        if (firstName == null) { firstName = ""; }
        if (surname == null) { surname = ""; }
        if (displayName == null) { displayName = ""; }
        if (accountPhotoName == null) { accountPhotoName = ""; }
    }

    public String buildAccountPhotoNodeFilter(String u_Id, boolean appendFileName) {
        if (!u_Id.equals("")) {
            String filterString = "images/users/" + u_Id + "/account_photos";
            if (appendFileName) {
                filterString +=  accountPhotoName;
            }
            return filterString;
        }
        return "";
    }

    public boolean usesDefaultPhoto() {
        return accountPhotoName.equals("");
    }

    public String getAccountPhotoNames() { return accountPhotoNames; }

    private void addAccountPhotoName(String[] photoNames) {
        StringBuilder builder = new StringBuilder(accountPhotoNames);
        for (int i = 0; i < photoNames.length; i++)
        {
            if (!photoNames[i].equals("")) {
                if (!builder.toString().equals("")) {
                    builder.append(",");
                }
                builder.append(photoNames[i]);
            }
        }
    }
}