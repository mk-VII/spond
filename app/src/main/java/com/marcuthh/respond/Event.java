package com.marcuthh.respond;

import java.util.ArrayList;

public class Event {

    private String eventTitle;
    private String eventAdmin;
    private String eventDesc;
    private String eventLocation;
    private String eventImage;
    private long eventCreated;
    private long eventTimestamp;
    private ArrayList<EventInvite> eventInvites;

    public Event() {
    }

    public Event(String eventTitle, String eventAdmin, String eventDesc,
                 String eventLocation, String eventImage,
                 long eventCreated, long eventTimestamp, ArrayList<EventInvite> eventInvites) {
        this.eventTitle = eventTitle;
        this.eventAdmin = eventAdmin;
        this.eventDesc = eventDesc;
        this.eventLocation = eventLocation;
        this.eventImage = eventImage;
        this.eventCreated = eventCreated;
        this.eventTimestamp = eventTimestamp;
        this.eventInvites = eventInvites;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDesc() {
        return eventDesc;
    }

    public void setEventDesc(String eventDesc) {
        this.eventDesc = eventDesc;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(long eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public ArrayList<EventInvite> getAllInvites() {
        return eventInvites;
    }

    public String[] getAllNotResponded() {
        return getInvitesByStatus(EventInvite.NO_RESPONSE);
    }

    public String[] getAllNotAttending() {
        return getInvitesByStatus(EventInvite.NOT_ATTENDING);
    }

    public String[] getAllAttending() {
        return getInvitesByStatus(EventInvite.ATTENDING);
    }

    private String[] getInvitesByStatus(int respStatus) {
        ArrayList<String> inviteSet = new ArrayList<String>();
        for (EventInvite invite : eventInvites) {
            if (invite.getStatus() == respStatus) {
                //inviteSet.add(invite.getUserKey());
            }
        }

        return inviteSet.toArray(new String[inviteSet.size()]);
    }

    public String getEventImage() {
        return eventImage;
    }

    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }

    public String getEventAdmin() {
        return eventAdmin;
    }

    public void setEventAdmin(String eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    public long getEventCreated() {
        return eventCreated;
    }

    public void setEventCreated(long eventCreated) {
        this.eventCreated = eventCreated;
    }
}
