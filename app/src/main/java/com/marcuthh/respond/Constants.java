package com.marcuthh.respond;

public class Constants {

    public enum DatabaseChild {
        CHILD_USERS("users"),
        CHILD_CHATS("chats"),
        CHILD_SPONDERS("sponders"),
        CHILD_EVENTS("events"),
        CHILD("invites");

        private String value;
        DatabaseChild(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;        }
    }
}
