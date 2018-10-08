package com.creaginetech.expresshoes.Model;

//import android.app.Notification;

public class Sender {
    public String to;
    public Notification notification;

    public Sender(String to, Notification notification) {
        this.to = to;
        this.notification = notification;
    }
}
