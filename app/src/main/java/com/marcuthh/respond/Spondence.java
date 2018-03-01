package com.marcuthh.respond;

import java.util.Date;

//Chat module
public class Spondence {

    private String name;
    private Sponder[] sponders;
    private Sponse[] sponses;
    private long lastSponded;

    public Spondence() {
    }

    public Spondence(String spName, Sponder[] members, Sponse[] messages) {
        name = spName;
        sponders = members;
        sponses = messages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Sponder[] getSponders() {
        return sponders;
    }

    public void setSponders(Sponder[] sponders) {
        this.sponders = sponders;
    }

    public Sponse[] getSponses() {
        return sponses;
    }

    public void setSponses(Sponse[] sponses) {
        this.sponses = sponses;
    }
}