package com.marcuthh.respond;

import java.util.Date;

public class Message {

    private String messageText;
    private String messageSender;
    private boolean messageViewed;
    private long messageTime;

    //additional
    private String eventKey;
    private String photoLoc;

    public Message(String msgText, String msgSender) {
        messageText = msgText;
        messageSender = msgSender;
        messageViewed = false;
        //initialise as system time
        messageTime = new Date().getTime();
    }

    public Message() {
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String mText) {
        messageText = mText;
    }

    public String getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }

    public boolean isMessageViewed() {
        return messageViewed;
    }

    public void setMessageViewed(boolean messageViewed) {
        this.messageViewed = messageViewed;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public boolean hasEvent() {
        return (eventKey != null && !eventKey.equals(""));
    }

    public String getPhotoLoc() {
        return photoLoc;
    }

    public void setPhotoLoc(String photoLoc) {
        this.photoLoc = photoLoc;
    }

    public boolean hasPhoto() {
        return (photoLoc != null && !photoLoc.equals(""));
    }
}