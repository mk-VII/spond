package com.marcuthh.respond;

import java.util.Date;

public class Message {

    private String messageText;
    private String messageSender;
    private String messageChat;
    private long messageTimestamp;

    //additional
    private String eventKey;
    private String photoLoc;

    public Message(String msgText, String msgSender, String msgChat) {
        messageText = msgText;
        messageSender = msgSender;
        messageChat = msgChat;
        //initialise as system time
        messageTimestamp = new Date().getTime();
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

    public long getMessageTimestamp() {
        return messageTimestamp;
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

    public String getMessageChat() {
        return messageChat;
    }

    public void setMessageChat(String messageChat) {
        this.messageChat = messageChat;
    }
}