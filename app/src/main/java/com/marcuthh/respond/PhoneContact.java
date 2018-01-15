package com.marcuthh.respond;

public class PhoneContact {
    private String contactName = "";
    private String[] contactNo;
    private String[] email;
    private boolean isUser = false;

    public PhoneContact() {
    }

    public PhoneContact(String contactName, String[] contactNo, String[] email) {
        this.contactName = contactName;
        this.contactNo = contactNo;
        this.email = email;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String[] getContactNumbers() { return contactNo; }

    public void setContactNo(String[] contactNo) {
        this.contactNo = contactNo;
    }

    public String[] getContactEmails() {
        return email;
    }

    public void setEmail(String[] email) {
        this.email = email;
    }

    public boolean hasApp() {
        return isUser;
    }

    public void setUser(boolean isUser) {
        this.isUser = isUser;
    }

    public boolean hasContactNo() { return contactNo.length > 0; }

    public boolean hasContactEmail() { return email.length > 0; }
}
