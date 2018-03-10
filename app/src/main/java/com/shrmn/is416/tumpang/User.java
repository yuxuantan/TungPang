package com.shrmn.is416.tumpang;

import android.os.Build;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class User {

    private String identifier;
    private String telegramUsername;
    private boolean isAdmin;
    private String name;
    private long createdAt;
    private String deviceModel;
    private String deviceManufacturer;

    @ServerTimestamp
    private Date serverTimestamp;

    // An empty constructor is required for Firestore's document.toObject(User.class) method
    public User() {}

    public User(String identifier, String telegramUsername, boolean isAdmin, String name, long createdAt) {
        this.identifier = identifier;
        this.telegramUsername = telegramUsername;
        this.isAdmin = isAdmin;
        this.name = name;
        this.createdAt = createdAt;

        deviceModel = Build.MODEL;
        deviceManufacturer = Build.MANUFACTURER;
    }

    public User(String identifier, String telegramUsername, boolean isAdmin, String name) {
        this(identifier, telegramUsername, isAdmin, name, System.currentTimeMillis());
    }

    public User(String identifier) {
        this(identifier, null, false, null);
    }

    public User(String identifier, boolean isAdmin) {
        this(identifier, null, isAdmin, null);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTelegramUsername() {
        return telegramUsername;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public String getName() {
        return name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public Date getServerTimestamp() {
        return serverTimestamp;
    }

    public String telegramLink() {
        return telegramUsername == null ? null : "https://t.me/" + telegramUsername;
    }

    public void setTelegramUsername(String telegramUsername) {
        if(telegramUsername != null && !telegramUsername.isEmpty()) {
            // Strip whitespace
            telegramUsername = telegramUsername.trim();
            // Remove @ from first character
            if(telegramUsername.charAt(0) == '@') {
                telegramUsername = telegramUsername.substring(1);
            }
            this.telegramUsername = telegramUsername;
        }

    }

    public void setName(String name) {
        // Trim whitespace off name
        if(name != null && !name.isEmpty()) {
            this.name = name.trim();
        }
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return identifier.equals(user.identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "identifier='" + identifier + '\'' +
                ", telegramUsername='" + telegramUsername + '\'' +
                ", isAdmin=" + isAdmin +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", deviceModel='" + deviceModel + '\'' +
                ", deviceManufacturer='" + deviceManufacturer + '\'' +
                ", serverTimestamp=" + serverTimestamp +
                '}';
    }
}
