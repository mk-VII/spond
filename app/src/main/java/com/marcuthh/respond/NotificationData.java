package com.marcuthh.respond;

public class NotificationData {

    public static final String TEXT = "TEXT";

    private String imageName;
    private int id; //notification identifier
    private String messageTitle;
    private String messageText;
    private String sound;

    public NotificationData() {
        //required empty constructor
    }

    public NotificationData(String imgName, int _id,
                            String msgTitle, String txtMsg, String snd) {
        imageName = imgName;
        id = _id;
        messageTitle = msgTitle;
        messageText = txtMsg;
        sound = snd;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }
}
