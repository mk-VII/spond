package com.marcuthh.respond;

public class EventInvite {

    private int status;
    private long sentTimestamp;
    private long responseTimestamp;
    public final static int NO_RESPONSE = 0,
            NOT_ATTENDING = -1,
            ATTENDING = 1;

    public EventInvite() {
    }

    public EventInvite(int status, long sentTimestamp, long responseTimestamp) {
        this.status = status;
        this.sentTimestamp = sentTimestamp;
        this.responseTimestamp = responseTimestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getSentTimestamp() {
        return sentTimestamp;
    }

    public void setSentTimestamp(long sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }

    public long getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(long responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }
}

