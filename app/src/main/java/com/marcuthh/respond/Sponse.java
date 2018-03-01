package com.marcuthh.respond;

import java.util.Date;

public class Sponse {

    private String messageText;
    private String messageSender;
    private boolean messageViewed;
    private long messageTime;

    public Sponse(String msgText, String msgSender) {
        messageText = msgText;
        messageSender = msgSender;
        messageViewed = false;
        //initialise as system time
        messageTime = new Date().getTime();
    }

    public Sponse() {
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
}