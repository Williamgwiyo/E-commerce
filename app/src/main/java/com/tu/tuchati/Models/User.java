package com.tu.tuchati.Models;

public class User {
    private String uid,username,status,phoneNumber,city,profileImage,onlineStatus,chatingTo,email,country;
    private Long latitude,longitude;
    boolean isBlocked = false;

    public User() {
    }

    public User(String uid, String username, String status, String phoneNumber, String city, String profileImage, String onlineStatus, String chatingTo, String email, String country, Long latitude, Long longitude, boolean isBlocked) {
        this.uid = uid;
        this.username = username;
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.profileImage = profileImage;
        this.onlineStatus = onlineStatus;
        this.chatingTo = chatingTo;
        this.email = email;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isBlocked = isBlocked;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getChatingTo() {
        return chatingTo;
    }

    public void setChatingTo(String chatingTo) {
        this.chatingTo = chatingTo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getLatitude() {
        return latitude;
    }

    public void setLatitude(Long latitude) {
        this.latitude = latitude;
    }

    public Long getLongitude() {
        return longitude;
    }

    public void setLongitude(Long longitude) {
        this.longitude = longitude;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
