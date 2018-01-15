package com.marcuthh.respond;

import java.util.Date;

public class ChatMessage {

    private String messageText;
    private String messageUser;
    private long messageTime;

    public ChatMessage(String mText, String mUser) {
        messageText = mText;
        messageUser = mUser;

        //initialise as system time
        messageTime = new Date().getTime();
    }

    public ChatMessage() {}

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String mText) {
        messageText = mText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String mUser) {
        messageUser = mUser;
    }

    public long getMessageTime() {
        return  messageTime;
    }
}
