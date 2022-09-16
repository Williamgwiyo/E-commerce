package com.tu.tuchati.Models;

public class FindFriends {
    public String profileImage,username,status;

    public FindFriends() {
    }

    public FindFriends(String profileImage, String username, String status) {
        this.profileImage = profileImage;
        this.username = username;
        this.status = status;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
