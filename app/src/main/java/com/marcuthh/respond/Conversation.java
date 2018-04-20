package com.marcuthh.respond;

//Chat module
public class Conversation {

    private String[] userKeys;
    private Message[] messages;
    private long lastMessaged;

    public Conversation() {
    }

    public Conversation(String[] keys, Message[] mess) {
        userKeys = keys;
        messages = mess;
    }

    public String[] getUsers() {
        return userKeys;
    }

    public void setUsers(String[] keys) {
        this.userKeys = keys;
    }

    public Message[] getMessages() {
        return messages;
    }

    public void setMessages(Message[] mess) {
        this.messages = mess;
    }
}