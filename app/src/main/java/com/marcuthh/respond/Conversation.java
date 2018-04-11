package com.marcuthh.respond;

//Chat module
public class Conversation {

    private String name;
    private User[] users;
    private Message[] messages;
    private long lastResponded;

    public Conversation() {
    }

    public Conversation(String spName, User[] members, Message[] mess) {
        name = spName;
        users = members;
        messages = mess;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User[] getUsers() {
        return users;
    }

    public void setUsers(User[] users) {
        this.users = users;
    }

    public Message[] getMessages() {
        return messages;
    }

    public void setMessages(Message[] mess) {
        this.messages = mess;
    }
}